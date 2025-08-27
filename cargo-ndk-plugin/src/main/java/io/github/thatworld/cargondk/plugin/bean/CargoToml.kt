package io.github.thatworld.cargondk.plugin.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CargoToml(
    val `package`: Package,
    val lib: Lib,
) {
    @Serializable
    data class Package(
        val name: String,
    )

    @Serializable
    data class Lib(
        @SerialName("crate-type")
        val crateType: List<String>,
    )
}