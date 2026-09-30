package com.brunoshiroma.vibememory.model

/**
 * Binary size units the user can pick when choosing a working-set size to
 * benchmark, matching the units accepted by the upstream CLI's `--sizes`
 * option (KiB, MiB, GiB).
 */
enum class SizeUnit(val label: String, val bytesPerUnit: Long) {
    KIB("KiB", 1_024L),
    MIB("MiB", 1_024L * 1_024L),
    GIB("GiB", 1_024L * 1_024L * 1_024L),
}
