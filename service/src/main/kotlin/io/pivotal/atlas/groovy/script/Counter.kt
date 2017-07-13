package io.pivotal.atlas.groovy.script

import io.pivotal.atlas.groovy.Tag

class Counter(q: String): TimeSeriesExpr(q) {
    constructor(name: String, tags: Collection<Tag>): this(build(name, tags))
}
