import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("jacoco")
    id("org.sonarqube") version "3.3"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
}

group = "com.charanya"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.2")
    testImplementation("io.projectreactor:reactor-test")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    // Secret
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap:3.1.5")
    implementation("org.springframework.cloud:spring-cloud-starter-aws-secrets-manager-config:2.2.6.RELEASE")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")

    // Refinitiv
    implementation("com.refinitiv.ema:ema:3.6.7.1") {
        exclude("org.slf4j", "slf4j-jdk14")
    }

    // Testing
    testImplementation("com.ninja-squad:springmockk:3.1.1")

    // MAC M1
    implementation("io.netty:netty-all")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(
        tasks.jacocoTestReport
    ) // report is always generated and docker containers are stopped after tests run
}

tasks.withType<JacocoReport> {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
        csv.required.set(false)
    }
}

// Disable spring generating a *-plain.jar when building
tasks.getByName<Jar>("jar") {
    enabled = false
}
