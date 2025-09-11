import kotlinx.kover.gradle.plugin.dsl.tasks.KoverReport

import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.serialization") version "2.2.10"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.1"

    application
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
    maven { url = uri("https://build.shibboleth.net/maven/releases/") }
}

val ktorVersion = "3.2.3"
val logbackVersion = "1.5.18"
val logstashVersion = "8.1"
val micrometerVersion = "1.15.3"
val kotlinLoggingVersion = "3.0.5"
val janinoVersion = "3.1.12"
val natpryceVersion = "1.6.10.0"
val kotestVersion = "6.0.1"
val kotlinxSerializationVersion = "1.9.0"
val kotlinxDatetimeVersion = "0.7.1-0.6.x-compat"
val mockkVersion = "1.14.5"

val aallamUlidVersion = "1.5.0"

val cxfVersion = "4.1.3"
val ibmmqVersion = "9.4.3.0"
val glassfishJaxbVersion = "4.0.5"
val tjenestespesifikasjonVersion = "1.0_20250715173022_23638f4"
val opentelemetryVersion = "2.19.0-alpha"

dependencies {

    // Ktor server
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

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
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")

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
    implementation("org.codehaus.janino:janino:$janinoVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    // Config
    implementation("com.natpryce:konfig:$natpryceVersion")

    // Util
    implementation("com.aallam.ulid:ulid-kotlin:$aallamUlidVersion")

    // Opentelemetry
    implementation("io.opentelemetry.instrumentation:opentelemetry-ktor-3.0:$opentelemetryVersion")

    // Test
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

// Vulnerability fix because of id("org.jlleitschuh.gradle.ktlint") uses ch.qos.logback:logback-classic:1.3.5
configurations.ktlint {
    resolutionStrategy.force("ch.qos.logback:logback-classic:$logbackVersion")
}

application {
    mainClass.set("no.nav.sokos.trekk.ApplicationKt")
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

//    withType<ShadowJar>().configureEach {
//        enabled = true
//        archiveFileName.set("app.jar")
//        manifest {
//            attributes["Main-Class"] = "no.nav.sokos.trekk.ApplicationKt"
//        }
//        finalizedBy(koverHtmlReport)
//
//        duplicatesStrategy = DuplicatesStrategy.INCLUDE
//        mergeServiceFiles()
//        // Make sure the cxf service files are handled correctly so that the SOAP services work.
//        transform(ServiceFileTransformer::class.java) {
//            path = "META-INF/cxf"
//            include("bus-extensions.txt")
//        }
//    }

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

    named("build") {
        dependsOn("copyPreCommitHook")
    }

    withType<Wrapper> {
        gradleVersion = "9.0.0"
    }
}
