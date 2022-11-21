package com.charanya.app

import com.charanya.app.config.ConfigProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ConfigProperties::class)
class Issue218ReproApplication

fun main(args: Array<String>) {
    runApplication<Issue218ReproApplication>(*args)
}
