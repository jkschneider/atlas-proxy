package com.netflix.atlas.proxy.model

data class AtlasGraph(val source: String, val query: String, val explodableTags: Collection<TagValues>)