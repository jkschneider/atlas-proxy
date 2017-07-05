package com.netflix.atlas.query.script;

class Graph {
    private val atlasStack = arrayListOf<String>()
    private val graphOptions = hashMapOf<String, String>()

    fun line(expr: Expr): Graph {
        atlasStack.add(expr.query)
        return this
    }

    fun timeZone(timeZone: String): Graph {
        graphOptions.put("tz", timeZone)
        return this
    }

    fun title(title: String): Graph {
        graphOptions.put("title", title.replace(' ', '+'))
        return this
    }

    fun axisLabel(n: Int, label: String): Graph {
        graphOptions.put("ylabel.$n", label.replace(' ', '+'))
        return this
    }

    /**
     * Example {@code s=e-15m}
     */
    fun s(duration: String): Graph {
        graphOptions.put("s", duration)
        return this
    }

    fun buildQuery(width: Int): String {
        return graphOptions
            .entries
            .map({ opt -> opt.key + "=" + opt.value })
            .plus("w=$width")
            .plus("layout=iw") // see com.netflix.atlas.chart.model.Layout
            .plus("q=${atlasStack.joinToString(",")}")
            .joinToString("&")
    }
}
