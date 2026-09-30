# Add project specific ProGuard rules here.
# The JNI bridge only exposes the `runBenchmark` native method by class/method
# name, so it must be kept if code shrinking is ever enabled.
-keepclasseswithmembernames class com.brunoshiroma.vibememory.bridge.NativeBenchmark {
    native <methods>;
}
