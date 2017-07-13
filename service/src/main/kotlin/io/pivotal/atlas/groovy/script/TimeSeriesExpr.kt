package io.pivotal.atlas.groovy.script

import io.pivotal.atlas.groovy.Tag

@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
open class TimeSeriesExpr (q: String): StyleExpr(q) {
    companion object {
        fun build(name: String, tags: Collection<Tag>): String {
            var atlasQuery = "name,$name,:eq"
            tags.forEach { tag -> atlasQuery += ",${tag.key},${tag.value},:eq,:and" }
            return atlasQuery
        }
    }

    /**
     * Apply a common group by to all aggregation functions in the expression.
     */
    fun by(group: List<String>): TimeSeriesExpr {
        return TimeSeriesExpr("$query,(,${group.joinToString(",")},),:by")
    }

    /**
     * Apply a common group by to all aggregation functions in the expression.
     */
    fun add(expr: TimeSeriesExpr): TimeSeriesExpr {
        return TimeSeriesExpr("$query,${expr.query},:add")
    }
}
