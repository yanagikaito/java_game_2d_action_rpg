// data/build.gradle.kts
plugins {
    `java-library` // ← 必須
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}