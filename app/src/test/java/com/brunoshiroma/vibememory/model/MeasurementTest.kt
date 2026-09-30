package com.brunoshiroma.vibememory.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MeasurementTest {

    private val sampleJson = """
        [
          {"operation":"read","sizeBytes":64,"iterations":2,"elapsedSeconds":0.001,"bytesPerSecond":64000.0,"nanosecondsPerElement":12.5},
          {"operation":"write","sizeBytes":64,"iterations":2,"elapsedSeconds":0.002,"bytesPerSecond":32000.0,"nanosecondsPerElement":25.0}
        ]
    """.trimIndent()

    @Test
    fun `parses a JSON array of measurements`() {
        val measurements = Measurement.parseList(sampleJson)

        assertEquals(2, measurements.size)
        assertEquals("read", measurements[0].operation)
        assertEquals(64L, measurements[0].sizeBytes)
        assertEquals(2, measurements[0].iterations)
        assertEquals(12.5, measurements[0].nanosecondsPerElement, 0.0001)
    }

    @Test
    fun `computes gigabytes per second from bytes per second`() {
        val measurements = Measurement.parseList(sampleJson)
        assertEquals(64000.0 / 1_000_000_000.0, measurements[0].gigabytesPerSecond, 1e-12)
    }

    @Test(expected = com.brunoshiroma.vibememory.json.JsonParseException::class)
    fun `rejects a non-array payload`() {
        Measurement.parseList("""{"operation":"read"}""")
    }

    @Test(expected = com.brunoshiroma.vibememory.json.JsonParseException::class)
    fun `rejects a measurement missing a required field`() {
        Measurement.parseList("""[{"operation":"read"}]""")
    }

    @Test
    fun `formats sizes using the largest exact binary unit`() {
        assertEquals("4 KiB", formatSizeBytes(4L * 1024))
        assertEquals("2 MiB", formatSizeBytes(2L * 1024 * 1024))
        assertEquals("1 GiB", formatSizeBytes(1024L * 1024 * 1024))
        assertEquals("7 B", formatSizeBytes(7L))
        assertEquals("0 B", formatSizeBytes(0L))
    }

    @Test
    fun `size format round trips are consistent for every default size`() {
        SizeSelection.DEFAULTS.forEach { selection ->
            assertTrue(selection.isValid)
            assertEquals(selection.label, formatSizeBytes(selection.bytes))
        }
    }
}
