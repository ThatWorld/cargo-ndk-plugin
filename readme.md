# Cargo NDK Plugin

![maven-central](https://img.shields.io/maven-central/v/io.github.thatworld/cargondk)

This is a Cargo plugin that simplifies building Rust projects for Android using the Android NDK.

It automates the process of setting up the NDK toolchain, configuring the build environment, and compiling the Rust code for various Android architectures.

## Examples

```kotlin
plugins {
    ...
    id("io.github.thatworld.cargondk") version "<version>"
}

cargoNdk {
    source = /* rust project source path */
    manifestPath = /* rust project Cargo.toml */
    ...
}
```

Please refer to the [app](/app/src/main) module for the default project structure.

## Features

- ...

## Reference

- [cargo-ndk](https://github.com/bbqsrc/cargo-ndk)
- [ktoml](https://github.com/orchestr7/ktoml)
