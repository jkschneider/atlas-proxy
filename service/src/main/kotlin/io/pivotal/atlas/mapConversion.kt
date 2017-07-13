package io.pivotal.atlas

import scala.collection.JavaConversions

fun scala.collection.Map<*, *>.toMap(): Map<Any, Any> {
    fun mapOrDoNothing(v: Any) = when(v) {
        is scala.collection.Map<*, *> -> v.toMap()
        else -> v
    }

    return JavaConversions.mapAsJavaMap(this).entries
            .map { (k, v) -> k to mapOrDoNothing(v) }
            .toMap()
}