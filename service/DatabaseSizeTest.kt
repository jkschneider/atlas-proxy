package io.pivotal.atlas

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.atlas.config.ConfigManager
import com.netflix.atlas.core.db.BlockStats
import com.netflix.atlas.core.db.MemoryDatabase
import com.netflix.atlas.core.model.Datapoint
import com.netflix.atlas.core.util.SmallHashMap
import com.typesafe.config.ConfigFactory
import org.junit.Test
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

// how can we improve with https://github.com/Netflix/atlas/pull/525?
class DatabaseSizeTest {
    enum class BlockType(val nextValue: () -> Double) {
        Constant({ 1.0 }), // best case scenario (uncommon?)

        // FIXME the number of distinct values varies with the block size, these work well for 2w of 1m step intervals
        Sparse({ ((Math.random() * 1000).toInt() % 10).toDouble() }),
        HybridSparseArray({ ((Math.random() * 1000).toInt() % 40).toDouble() }),

        Array(Math::random) // worst case scenario (most common?)
    }

    val blockType = BlockType.Array
    val minStepIntervalInSeconds = 60
    val hoursOfData = 6

    @Test
    fun howManyMetricsCanWeStuffInAnInstance() {
        val c = ConfigFactory.parseString("""
            atlas {
              core {
                model {
                  step = ${minStepIntervalInSeconds}s
                }

                db {
                  class = "com.netflix.atlas.core.db.MemoryDatabase"

                  // How often to rebuild the index for the memory database
                  rebuild-frequency = 8s

                  block-size = ${3600/minStepIntervalInSeconds}

                  num-blocks = $hoursOfData

                  // Don't start a thread to automatically rebuild the index
                  test-mode = true
                }
              }
            }
        """)
        ConfigManager.update(c)

        val db: MemoryDatabase = MemoryDatabase.apply(ConfigManager.current())
        val mapper = ObjectMapper()

        val start = System.nanoTime()

        val tagsBuilder = SmallHashMap.Builder<String, String>(2)
        tagsBuilder.add("name", "timer")
        val tags = tagsBuilder.result()

        /**
         * Flood the database with 2 weeks worth of data
         */
        val timeRange = (0..(TimeUnit.SECONDS.convert(14, TimeUnit.DAYS) / minStepIntervalInSeconds).toInt())
        timeRange.forEach { n ->
            if(n == 1 || n % 5000 == 1) {
                println("$n:".padEnd(8) + mapper.writeValueAsString(BlockStats.statsMap().toMap()))
                db.rebuild()
            }

            val ts = start + n * TimeUnit.MILLISECONDS.convert(minStepIntervalInSeconds.toLong(), TimeUnit.SECONDS)
            db.update(Datapoint(tags, ts, blockType.nextValue()))
        }

        db.rebuild()

        /**
         * How many metrics would we get for various RAM sizes
         */
        println("\nTotal number of metrics that fit in various RAM sizes:")
        (1..6).forEach { n ->
            val ram = Math.pow(2.0, n.toDouble()).toInt()
            println("${ram}G:".padEnd(5) + NumberFormat.getInstance().format(metricsPerGb() * ram))
        }
    }

    private fun metricsPerGb() = 1073741824 / blockSizeInBytes()

    @Suppress("UNCHECKED_CAST")
    private fun blockSizeInBytes() = (BlockStats.statsMap().toMap()["bytes"] as Map<String, *>)["total"] as Long
}