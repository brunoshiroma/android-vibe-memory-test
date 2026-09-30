# android-vibe-memory-test

Android app for the [rust-vibe-memory-test](https://github.com/brunoshiroma/rust-vibe-memory-test)
memory and cache benchmark. The app lets you pick one or more working-set
sizes (magnitude + unit: **KiB**, **MiB**, or **GiB**) and runs the sequential
read/write/copy and pointer-chase benchmarks natively through a JNI bridge to
the `memory-cache-bench` Rust crate.

## Project layout

- `app/` — Android application (Kotlin + Jetpack Compose).
  - `ui/BenchmarkScreen.kt` / `ui/BenchmarkViewModel.kt` — size selection UI
    (add/remove sizes, choose magnitude and KiB/MiB/GiB unit) and the
    benchmark run state.
  - `model/` — `SizeUnit`, `SizeSelection` and `Measurement` (plain Kotlin,
    no Android dependency, unit-tested under `app/src/test`).
  - `json/MiniJson.kt` — tiny dependency-free JSON parser used to decode the
    native bridge's output.
  - `bridge/NativeBenchmark.kt` — `external fun runBenchmark(...)` loaded from
    `memory_bench_jni`.
- `rust/memory-bench-jni/` — Rust `cdylib` that depends on `memory-cache-bench`
  (fetched directly from the `rust-vibe-memory-test` Git repository) and
  exposes a single JNI entry point, `Java_com_..._NativeBenchmark_runBenchmark`,
  returning the benchmark results as a JSON array.

## Building

Requirements: Android SDK, an Android NDK (for cross-compiling the Rust JNI
bridge), and the Rust Android targets:

```sh
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
```

Then build/run the app as usual:

```sh
./gradlew assembleDebug
```

The [`org.mozilla.rust-android-gradle.rust-android`](https://github.com/mozilla/rust-android-gradle)
plugin cross-compiles `rust/memory-bench-jni` for every configured ABI
(`arm`, `arm64`, `x86`, `x86_64`) as part of the Gradle build and copies the
resulting `.so` files into the APK's `jniLibs`.

### Rust crate on its own

```sh
cd rust/memory-bench-jni
cargo test
```

## Tests

- `rust/memory-bench-jni`: `cargo test` covers JSON serialization of the
  benchmark results.
- `app/src/test`: JVM unit tests (`./gradlew testDebugUnitTest`) cover the
  `MiniJson` parser, `Measurement` parsing/formatting, and `SizeSelection`
  validation — none of this logic touches the Android framework, so it can
  also be compiled/run directly with `kotlinc` + JUnit if needed.