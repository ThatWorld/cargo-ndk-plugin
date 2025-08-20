# Cargo NDK Plugin

This is a Cargo plugin that simplifies building Rust projects for Android using the Android NDK.

It automates the process of setting up the NDK toolchain, configuring the build environment, and compiling the Rust code for various Android architectures.

## Examples

```kotlin
plugins {
    ...
    id("io.github.thatworld.cargondk") version "<version>"
}

cargoNdk {
    source = /* rust rpoject source path */
    manifestPath = /* rust rpoject Cargo.toml */
    ...
}
```

Please refer to the [app](/app/src/main) module for the default project structure.

## Features

- [cargo-ndk-specific-environment-variables](https://github.com/bbqsrc/cargo-ndk?tab=readme-ov-file#cargo-ndk-specific-environment-variables)
- ...

## Reference

- [cargo-ndk](https://crates.io/crates/cargo-ndk)
