package io.pivotal.atlas.groovy.model

data class AtlasGraph(val source: String, val query: String, val explodableTags: Collection<TagValues>)