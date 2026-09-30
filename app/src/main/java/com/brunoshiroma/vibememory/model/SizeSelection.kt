package com.brunoshiroma.vibememory.model

/**
 * A single working-set size chosen by the user, expressed as a [magnitude] in
 * a given [unit] (KiB, MiB, or GiB). Mirrors one entry of the `--sizes`
 * option of the rust-vibe-memory-test CLI, e.g. "16KiB" or "2MiB".
 */
data class SizeSelection(val magnitude: Long, val unit: SizeUnit) {

    /** Size in bytes, the unit expected by the native benchmark bridge. */
    val bytes: Long
        get() = magnitude * unit.bytesPerUnit

    /** Human readable label, e.g. "16 KiB". */
    val label: String
        get() = "$magnitude ${unit.label}"

    /**
     * Whether this selection is accepted by the benchmark: it must be
     * strictly positive and a multiple of 8 bytes (the size of the `u64`
     * elements the benchmark reads/writes/copies).
     */
    val isValid: Boolean
        get() = bytes >= 8 && bytes % 8 == 0L

    companion object {
        /** Matches [memory_cache_bench::BenchmarkConfig]'s default sizes. */
        val DEFAULTS: List<SizeSelection> = listOf(
            SizeSelection(4, SizeUnit.KIB),
            SizeSelection(32, SizeUnit.KIB),
            SizeSelection(256, SizeUnit.KIB),
            SizeSelection(2, SizeUnit.MIB),
            SizeSelection(16, SizeUnit.MIB),
        )
    }
}
