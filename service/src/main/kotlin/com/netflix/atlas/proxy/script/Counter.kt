package com.netflix.atlas.proxy.script

import com.netflix.atlas.proxy.Tag

class Counter(q: String): TimeSeriesExpr(q) {
    constructor(name: String, tags: Collection<Tag>): this(TimeSeriesExpr.build(name, tags))
}
