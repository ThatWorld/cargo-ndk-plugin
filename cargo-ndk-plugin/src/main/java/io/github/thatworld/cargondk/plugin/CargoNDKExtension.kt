package io.github.thatworld.cargondk.plugin

open class CargoNDKExtension {

    /**
     * The path to the Rust source code directory.
     * This is relative to the project root directory.
     * The default value is "src/main/rust".
     * You can change it to point to your Rust source code directory.
     */
    var source: String = "src/main/rust"

    /**
     * Path to Cargo.toml
     * This is the manifest file for the Rust project.
     * It is used to define the project metadata and dependencies.
     * The default value is "Cargo.toml", which means it expects the file to be
     * located in the root of the Rust source code directory.
     * You can change it to point to a different location if needed.
     */
    var manifestPath: String = "Cargo.toml"

    /**
     * The output directory for the compiled native libraries.
     * This is where the compiled libraries will be placed after the build process.
     * The default value is "intermediates/merged_native_libs/{variant}/merge{Variant}NativeLibs/out/lib",
     * which is a common location for native libraries in Android projects.
     * You can change it to point to a different directory if needed.
     * The `{variant}` placeholder will be replaced with the actual build variant name,
     * such as "debug" or "release", during the build process.
     *
     * more please see: https://github.com/bbqsrc/cargo-ndk?tab=readme-ov-file#building-a-library-for-32-bit-and-64-bit-arm-systems
     */
    var output: String = "intermediates/merged_native_libs/{variant}/merge{Variant}NativeLibs/out/lib"

    /**
     * Platform (also known as API level) [env: CARGO_NDK_PLATFORM=] [default: 21]
     *
     * more please see: https://github.com/bbqsrc/cargo-ndk/tree/main/example/openssl#building-openssl-for-android
     */
    var platform: Int = 21

    /**
     * Environment variables to pass to the cargo ndk command.
     * You can use this to set any additional environment variables needed for the build process.
     * The default value is an empty map, which means no additional environment variables are set.
     *
     * more please see: https://github.com/bbqsrc/cargo-ndk?tab=readme-ov-file#cargo-ndk-specific-environment-variables
     */
    val environment: MutableMap<String, String> = mutableMapOf()

    /**
     * The arguments to pass to the `cargo ndk` command.
     * You can use this to specify additional options for the build process.
     * For example, you can specify the target architecture or other cargo options.
     * The default value is an empty list, which means no additional arguments are passed.
     *
     *      --link-builtins         Links Clang builtins library [env: CARGO_NDK_LINK_BUILTINS=]
     *      --link-libcxx-shared    Links libc++_shared library [env: CARGO_NDK_LINK_LIBCXX_SHARED=]
     *
     * more please see: https://github.com/bbqsrc/cargo-ndk/blob/main/README.md#troubleshooting
     */
    var args: Set<String> = emptySet()

    /**
     * If `true`, the rust/target directory is cleaned when the `clean task` is run.
     * default is `false`.
     */
    var cleanTarget: Boolean = false
}