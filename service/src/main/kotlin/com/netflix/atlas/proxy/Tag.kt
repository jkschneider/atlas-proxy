package com.netflix.atlas.proxy

data class Tag(val key: String, val value: String) {
    companion object {
        fun zip(vararg keyValues: String): List<Tag> {
            if (keyValues.size % 2 == 1) {
                throw IllegalArgumentException ("size must be even, it is a set of key=value pairs")
            }
            return (0 until keyValues.size step 2).map { Tag(keyValues[it], keyValues[it + 1]) }
        }
    }
}
