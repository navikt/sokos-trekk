import kotlinx.kover.gradle.plugin.dsl.tasks.KoverReport

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

group = "no.nav.sokos"

repositories {
    mavenCentral()

    val githubToken = System.getenv("GITHUB_TOKEN")
    if (githubToken.isNullOrEmpty()) {
        maven {
            name = "external-mirror-github-navikt"
            url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
        }
    } else {
        maven {
            name = "github-package-registry-navikt"
            url = uri("https://maven.pkg.github.com/navikt/maven-release")
            credentials {
                username = "token"
                password = githubToken
            }
        }
    }

    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

val ktorVersion = "3.0.3"
val jschVersion = "0.2.22"
val logbackVersion = "1.5.16"
val logstashVersion = "8.0"
val micrometerVersion = "1.14.3"
val kotlinLoggingVersion = "3.0.5"
val janionVersion = "3.1.12"
val natpryceVersion = "1.6.10.0"
val kotestVersion = "6.0.0.M1"
val wiremockVersion = "3.10.0"
val kotlinxSerializationVersion = "1.8.0"
val kotlinxDatetimeVersion = "0.6.1"
val mockOAuth2ServerVersion = "2.1.10"
val mockkVersion = "1.13.16"
val kotliqueryVersion = "1.9.1"
val testcontainersVersion = "1.20.4"
val vaultVersion = "1.3.10"
val activemqVersion = "2.39.0"
val cxfVersion = "4.0.5"
val ibmmqVersion = "9.4.1.1"
val glassfishJaxbVersion = "4.0.5"
val tjenestespesifikasjonVersion = "1.0_20250211133838_7726a80"
val commonVersion = "3.2025.01.14_14.19-79b3041cae56"

dependencies {

    // Ktor server
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")

    // Ktor client
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-apache-jvm:$ktorVersion")

    // Security
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")

    // Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinxSerializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")

    // MQ
    implementation("com.ibm.mq:com.ibm.mq.jakarta.client:$ibmmqVersion")

    // SOAP
    implementation("no.nav.common:cxf:$commonVersion") {
        exclude(group = "org.opensaml")
    }

    // Jaxb
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:$glassfishJaxbVersion")

    // Tjenestespesifikasjon
    implementation("no.nav.sokos.tjenestespesifikasjoner:nav-maskinelletrekk-trekk-v1:$tjenestespesifikasjonVersion")
    implementation("no.nav.sokos.tjenestespesifikasjoner:nav-ytelsevedtak-v1-tjenestespesifikasjon:$tjenestespesifikasjonVersion")

    // Monitorering
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    runtimeOnly("org.codehaus.janino:janino:$janionVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    // Config
    implementation("com.natpryce:konfig:$natpryceVersion")

    // Util
    implementation("com.aallam.ulid:ulid-kotlin:1.3.0")

    // Test
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOAuth2ServerVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.apache.activemq:artemis-jakarta-server:$activemqVersion")
    testImplementation("org.wiremock:wiremock:$wiremockVersion")
}

// Vulnerability fix because of id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
configurations.ktlint {
    resolutionStrategy.force("ch.qos.logback:logback-classic:$logbackVersion")
}

sourceSets {
    main {
        java {
            srcDirs("${layout.buildDirectory.get()}/generated/src/main/kotlin")
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    withType<KotlinCompile>().configureEach {
        dependsOn("ktlintFormat")
    }

    ktlint {
        filter {
            exclude { element -> element.file.path.contains("generated/") }
        }
    }

    withType<KoverReport>().configureEach {
        kover {
            reports {
                filters {
                    excludes {
                        // exclusion rules - classes to exclude from report
                        classes("no.nav.tjeneste.*", "no.nav.maskinelletrekk.*")
                    }
                }
            }
        }
    }

    withType<ShadowJar>().configureEach {
        enabled = true
        archiveFileName.set("app.jar")
        manifest {
            attributes["Main-Class"] = "no.nav.sokos.trekk.ApplicationKt"
        }
        finalizedBy(koverHtmlReport)
        mergeServiceFiles {
            setPath("META-INF/services/org.flywaydb.core.extensibility.Plugin")
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform()

        testLogging {
            showExceptions = true
            showStackTraces = true
            exceptionFormat = FULL
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }

        reports.forEach { report -> report.required.value(false) }
    }

    register<Copy>("copyPreCommitHook") {
        from(".scripts/pre-commit")
        into(".git/hooks")
        filePermissions {
            user {
                execute = true
            }
        }
        doFirst {
            println("Installing git hooks...")
        }
        doLast {
            println("Git hooks installed successfully.")
        }
        description = "Copy pre-commit hook to .git/hooks"
        group = "git hooks"
        outputs.upToDateWhen { false }
    }

    named("jar") {
        enabled = false
    }

    named("build") {
        dependsOn("copyPreCommitHook")
    }

    withType<Wrapper> {
        gradleVersion = "8.12"
    }
}
