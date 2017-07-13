package io.pivotal.atlas

import com.netflix.atlas.core.db.BlockStats
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import scala.collection.JavaConversions


@RestController
class DatabaseController {
    @GetMapping("/api/db/blockStats")
    fun blockStats() = BlockStats.statsMap().toMap()
}