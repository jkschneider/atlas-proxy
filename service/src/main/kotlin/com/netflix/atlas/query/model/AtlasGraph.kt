package com.netflix.atlas.query.model

data class AtlasGraph(val source: String, val query: String, val explodableTags: Collection<TagValues>)