package com.charanya.app.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "app")
data class ConfigProperties(
    val refinitiv: Refinitiv
) {
    data class Refinitiv(
        val machineId: String,
        val appKey: String,
        val password: String,
        val service: String,
        val region: String
    )
}

