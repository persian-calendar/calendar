import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    id("maven-publish")
    alias(libs.plugins.kotest)
    alias(libs.plugins.ksp)
}

group = "io.github.persiancalendar"
version = "1.5.0"

kotlin {
    androidLibrary {
        namespace = "io.github.persiancalendar"
        compileSdk = 36
    }

    // Enable hierarchical structure for better code sharing
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate()

    // JVM target (for desktop apps)
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }

    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "PersianCalendar"
            isStatic = true
        }
    }

    // JavaScript target (for web apps)
    js(IR) {
        browser()
        nodejs()
    }

    // WebAssembly target (experimental, optional)
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonTest {
            dependencies {
                implementation("io.kotest:kotest-assertions-core:6.0.3")
                implementation("io.kotest:kotest-framework-engine:6.0.3")
                implementation(kotlin("test"))
            }
            resources.srcDirs("src/commonTest/resources")
        }

        jvmTest {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:6.0.3")
            }
        }
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            groupId = "io.github.persiancalendar"
            artifactId = when (name) {
                "kotlinMultiplatform" -> "calendar"
                "jvm" -> "calendar-jvm"
                "js" -> "calendar-js"
                "androidRelease" -> "calendar-android"
                else -> "calendar-$name"
            }
            version = project.version.toString()

            pom {
                name.set("Persian Calendar")
                description.set("A Kotlin Multiplatform library for Persian, Islamic, Nepali, and Civil calendar conversions")
                url.set("https://github.com/persian-calendar/calendar")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("persiancalendar")
                        name.set("Persian Calendar Contributors")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/persian-calendar/calendar.git")
                    developerConnection.set("scm:git:ssh://github.com/persian-calendar/calendar.git")
                    url.set("https://github.com/persian-calendar/calendar")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/persian-calendar/calendar")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
