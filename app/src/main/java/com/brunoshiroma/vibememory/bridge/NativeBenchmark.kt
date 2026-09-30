package com.brunoshiroma.vibememory.bridge

/**
 * Thin wrapper around the native `memory_bench_jni` library, which links the
 * upstream `memory-cache-bench` Rust crate from
 * https://github.com/brunoshiroma/rust-vibe-memory-test.
 */
object NativeBenchmark {
    init {
        System.loadLibrary("memory_bench_jni")
    }

    /**
     * Runs the benchmark suite for the requested working-set [sizesBytes]
     * (in bytes) and [iterations], returning a JSON array of measurements.
     *
     * @throws IllegalArgumentException if the configuration is invalid (e.g.
     * an empty size list, zero iterations, or a size that isn't a positive
     * multiple of 8 bytes).
     */
    @JvmStatic
    external fun runBenchmark(sizesBytes: LongArray, iterations: Int): String
}
