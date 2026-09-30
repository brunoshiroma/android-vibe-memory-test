use jni::objects::{JClass, JLongArray};
use jni::sys::{jint, jstring};
use jni::JNIEnv;
use memory_cache_bench::{run_suite, BenchmarkConfig, Measurement};

/// JNI entry point exposed to `com.brunoshiroma.vibememory.bridge.NativeBenchmark`.
///
/// Wraps `memory_cache_bench::run_suite` from
/// https://github.com/brunoshiroma/rust-vibe-memory-test and serializes the
/// resulting measurements as a JSON array. On invalid input the underlying
/// `BenchmarkError` is raised as an `IllegalArgumentException` on the Java side.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_brunoshiroma_vibememory_bridge_NativeBenchmark_runBenchmark<
    'local,
>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    sizes_bytes: JLongArray<'local>,
    iterations: jint,
) -> jstring {
    let result = std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
        run_benchmark(&mut env, &sizes_bytes, iterations)
    }));

    match result {
        Ok(Ok(json)) => match env.new_string(json) {
            Ok(value) => value.into_raw(),
            Err(_) => std::ptr::null_mut(),
        },
        Ok(Err(message)) => {
            let _ = env.throw_new("java/lang/IllegalArgumentException", message);
            std::ptr::null_mut()
        }
        Err(_) => {
            let _ = env.throw_new(
                "java/lang/IllegalStateException",
                "native benchmark panicked",
            );
            std::ptr::null_mut()
        }
    }
}

fn run_benchmark(
    env: &mut JNIEnv,
    sizes_bytes: &JLongArray,
    iterations: jint,
) -> Result<String, String> {
    let length = env
        .get_array_length(sizes_bytes)
        .map_err(|error| error.to_string())? as usize;
    let mut raw_sizes = vec![0_i64; length];
    env.get_long_array_region(sizes_bytes, 0, &mut raw_sizes)
        .map_err(|error| error.to_string())?;

    let config = BenchmarkConfig {
        sizes_bytes: raw_sizes
            .iter()
            .map(|&value| value.max(0) as usize)
            .collect(),
        iterations: iterations.max(0) as usize,
    };

    let measurements = run_suite(&config).map_err(|error| error.to_string())?;
    Ok(measurements_to_json(&measurements))
}

fn measurements_to_json(measurements: &[Measurement]) -> String {
    let items = measurements
        .iter()
        .map(|result| {
            format!(
                "{{\"operation\":\"{}\",\"sizeBytes\":{},\"iterations\":{},\"elapsedSeconds\":{},\"bytesPerSecond\":{},\"nanosecondsPerElement\":{}}}",
                result.operation.name(),
                result.size_bytes,
                result.iterations,
                result.elapsed_seconds,
                result.bytes_per_second,
                result.nanoseconds_per_element
            )
        })
        .collect::<Vec<_>>()
        .join(",");
    format!("[{items}]")
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn serializes_measurements_as_json_array() {
        let measurements = run_suite(&BenchmarkConfig {
            sizes_bytes: vec![64],
            iterations: 2,
        })
        .unwrap();

        let json = measurements_to_json(&measurements);
        assert!(json.starts_with('['));
        assert!(json.ends_with(']'));
        assert!(json.contains("\"operation\":\"read\""));
        assert!(json.contains("\"sizeBytes\":64"));
    }

    #[test]
    fn json_uses_the_field_names_expected_by_the_kotlin_parser() {
        let measurements = run_suite(&BenchmarkConfig {
            sizes_bytes: vec![64],
            iterations: 1,
        })
        .unwrap();
        let json = measurements_to_json(&measurements);
        for key in [
            "\"operation\"",
            "\"sizeBytes\"",
            "\"iterations\"",
            "\"elapsedSeconds\"",
            "\"bytesPerSecond\"",
            "\"nanosecondsPerElement\"",
        ] {
            assert!(json.contains(key), "missing {key} in {json}");
        }
    }
}
