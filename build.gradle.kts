// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.aboutlibraries) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.modules.graph.assert)
}

moduleGraphAssert {
    maxHeight = 4
    allowed = arrayOf(
        ":app -> :core:.*",
        ":app -> :navigation",
        ":navigation -> :feature:.*",
        ":core:service -> :core:domain",
        ":core:service -> :core:data",
        ":core:data -> :core:domain",
        ":feature:.* -> :core:domain",
    )
    restricted = arrayOf(
        ":app -X> :feature:.*",
        ":feature:.* -X> :core:data",
        ":navigation -X> :core:.*",
    )
}

dependencies {
    kover(project(":app"))
    kover(project(":core:domain"))
    kover(project(":core:data"))
    kover(project(":core:service"))
    kover(project(":feature:settings"))
    kover(project(":feature:onboarding"))
    kover(project(":feature:licenses"))
    kover(project(":navigation"))
}