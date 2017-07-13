package io.pivotal.atlas.groovy.script

import io.pivotal.atlas.groovy.Tag

class Timer(q: String): TimeSeriesExpr(q) {
    constructor(name: String, tags: Collection<Tag>): this(build(name, tags))

    fun distAvg(): TimeSeriesExpr {
        return Timer("$query,:dist-avg")
    }

    fun distMax(): TimeSeriesExpr {
        return Timer("$query,:dist-max")
    }

    fun latency(): TimeSeriesExpr {
        return distAvg()
    }

    fun throughput(): TimeSeriesExpr {
        return Timer("$query,statistic,count,:eq,:and")
    }
}
