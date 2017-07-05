package com.netflix.atlas.query.script

import com.netflix.atlas.query.Tag

class Counter(q: String): TimeSeriesExpr(q) {
    constructor(name: String, tags: Collection<Tag>): this(TimeSeriesExpr.build(name, tags))
}
