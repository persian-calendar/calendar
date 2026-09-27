@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    kotlin("multiplatform") version "2.4.20"
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            // The library is the root project of this build.
            implementation(project(":"))
        }
    }
}

val webIconsSvg = file("src/webMain/resources/icons/icon.svg")
val webIconsDir = file("src/webMain/resources/icons")

val generateWebIcons = tasks.register("generateWebIcons") {
    description = "Generate PNG app icons from icon.svg using rsvg-convert"
    inputs.file(webIconsSvg)
    outputs.files(
        webIconsDir.resolve("icon-192.png"),
        webIconsDir.resolve("icon-512.png"),
    )
    doLast {
        for (size in intArrayOf(192, 512)) {
            val exit = ProcessBuilder(
                "rsvg-convert",
                "-w", size.toString(),
                "-h", size.toString(),
                webIconsSvg.absolutePath,
                "-o", webIconsDir.resolve("icon-$size.png").absolutePath,
            ).redirectErrorStream(true).start().waitFor()
            check(exit == 0) { "rsvg-convert failed for size $size with exit code $exit" }
        }
    }
}
