package io.pivotal.atlas.groovy

import com.fasterxml.jackson.databind.ObjectMapper
import io.pivotal.atlas.groovy.model.AtlasGraph
import io.pivotal.atlas.groovy.model.TagValues
import io.pivotal.atlas.groovy.script.Graph
import io.pivotal.atlas.groovy.script.InvalidTimeSeriesException
import io.pivotal.atlas.groovy.script.Select
import groovy.lang.Binding
import groovy.lang.GroovyShell
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import javax.servlet.http.HttpServletResponse

@RestController
class GraphController(val mapper: ObjectMapper,
                      val restTemplate: RestTemplate) {
    private val logger = LoggerFactory.getLogger(GraphController::class.java)

    @PostMapping("/api/graph")
    fun query(@RequestBody querySource: String,
              @RequestParam(required = false, defaultValue = "http://localhost:7101") atlasUri: String,
              @RequestParam(required = false, defaultValue = "1000") width: Int,
              @RequestParam(required = false, defaultValue = "false") redirect: Boolean,
              response: HttpServletResponse) {
        val normalizedAtlasUri = atlasUri.trimEnd('/')

        val graph = Graph()

        val binding = Binding()
        binding.setProperty("__graph", graph)
        binding.setProperty("__select", Select(atlasUri, restTemplate))

        // see http://groovy-lang.org/integrating.html for more about GroovyShell and alternatives
        val shell = GroovyShell(javaClass.classLoader, binding, CompilerConfiguration()
                .addCompilationCustomizers(ASTTransformationCustomizer(CompileStatic::class.java)))

        // Add some preamble code that binds our script to local Graph and Select instances while
        // still allowing the user-supplied script to be statically compiled.
        val script =
                "import io.pivotal.atlas.groovy.script.*;" +
                "import groovy.transform.CompileStatic;" +
                "import groovy.transform.TypeCheckingMode;" +
                "@CompileStatic(TypeCheckingMode.SKIP) Graph __graph() { return __graph };" +
                "Graph graph = __graph();" +
                "@CompileStatic(TypeCheckingMode.SKIP) Select __select() { return __select };" +
                "Select select = __select();" +
                querySource

        try {
            val queryScript = shell.parse(script, "query")
            queryScript.run()

            val q = graph.buildQuery(width)
            logger.info("{}/api/v1/graph?{}", normalizedAtlasUri, q)

            if (redirect) {
                response.sendRedirect("$normalizedAtlasUri/api/v1/graph?$q")
            } else {
                response.status = 200
                response.outputStream.writer().use {
                    it.write(mapper.writeValueAsString(
                            AtlasGraph("$normalizedAtlasUri/api/v1/graph?$q", q, commonTags(q, normalizedAtlasUri))))
                    it.flush()
                }
            }
        } catch(e: CompilationFailedException) {
            response.status = 400
            response.outputStream.writer().use {
                it.write(e.message)
                it.flush()
            }
            logger.debug("Compilation failed", e)
        } catch(e: InvalidTimeSeriesException) {
            response.status = 400
            response.outputStream.writer().use {
                it.write(e.message)
                it.flush()
            }
            logger.debug("Invalid time series", e)
        }
    }

    private fun metricsInQuery(q: String): List<String> {
        return "name,([^,]+),:eq".toRegex().findAll(q).map { it.groupValues[1] }.toList()
    }

    /**
     * Tags which all metrics in the graph have in common.
     */
    private fun commonTags(q: String, atlasUri: String): Collection<TagValues> {
        val metrics = metricsInQuery(q)

        try {
            // tag names that occur in every metric
            val commonTags = metrics
                    .map { metricName ->
                        restTemplate
                                .getForObject("$atlasUri/api/v1/tags?q=name,{metric},:eq", Array<String>::class.java, metricName)
                                .toSet()
                                .minus(arrayOf("name", "statistic"))
                    }
                    .reduce { acc, names -> acc.intersect(names) }

            return commonTags.map { tag ->
                TagValues(tag, metrics.flatMap { metricName ->
                    restTemplate
                            .getForObject("$atlasUri/api/v1/tags/{tag}?q=name,{metric},:eq", Array<String>::class.java, tag, metricName)
                            .toList()
                }.toSet().sorted())
            }
        } catch(e: RestClientException) {
            logger.debug("Unable to retrieve a list of common tags because the Atlas server is unreachable")
            return emptyList()
        }
    }
}

