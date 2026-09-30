package com.brunoshiroma.vibememory.model

import com.brunoshiroma.vibememory.json.JsonParseException
import com.brunoshiroma.vibememory.json.JsonValue
import com.brunoshiroma.vibememory.json.MiniJson

/**
 * One row of a benchmark result, mirroring `memory_cache_bench::Measurement`
 * on the Rust side (https://github.com/brunoshiroma/rust-vibe-memory-test).
 */
data class Measurement(
    val operation: String,
    val sizeBytes: Long,
    val iterations: Int,
    val elapsedSeconds: Double,
    val bytesPerSecond: Double,
    val nanosecondsPerElement: Double,
) {
    val gigabytesPerSecond: Double
        get() = bytesPerSecond / 1_000_000_000.0

    companion object {
        /** Parses the JSON array emitted by the native `runBenchmark` JNI bridge. */
        fun parseList(json: String): List<Measurement> {
            val array = MiniJson.parse(json) as? JsonValue.JsonArray
                ?: throw JsonParseException("Expected a JSON array of measurements")
            return array.items.map(::parseOne)
        }

        private fun parseOne(value: JsonValue): Measurement {
            val obj = value as? JsonValue.JsonObject
                ?: throw JsonParseException("Expected a JSON object for a measurement")
            return Measurement(
                operation = obj.string("operation"),
                sizeBytes = obj.number("sizeBytes").toLong(),
                iterations = obj.number("iterations").toInt(),
                elapsedSeconds = obj.number("elapsedSeconds"),
                bytesPerSecond = obj.number("bytesPerSecond"),
                nanosecondsPerElement = obj.number("nanosecondsPerElement"),
            )
        }

        private fun JsonValue.JsonObject.string(key: String): String =
            (fields[key] as? JsonValue.JsonString)?.value
                ?: throw JsonParseException("Missing or invalid string field '$key'")

        private fun JsonValue.JsonObject.number(key: String): Double =
            (fields[key] as? JsonValue.JsonNumber)?.value
                ?: throw JsonParseException("Missing or invalid number field '$key'")
    }
}

/** Formats a byte count using the largest whole binary unit it evenly divides into. */
fun formatSizeBytes(bytes: Long): String = when {
    bytes != 0L && bytes % (1_024L * 1_024L * 1_024L) == 0L -> "${bytes / (1_024L * 1_024L * 1_024L)} GiB"
    bytes != 0L && bytes % (1_024L * 1_024L) == 0L -> "${bytes / (1_024L * 1_024L)} MiB"
    bytes != 0L && bytes % 1_024L == 0L -> "${bytes / 1_024L} KiB"
    else -> "$bytes B"
}
