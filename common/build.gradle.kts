// common/build.gradle.kts

plugins {
    `java-library`
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.h2database:h2:2.2.224")
    compileOnly("org.jetbrains:annotations:24.0.1")
}

// エンコーディング設定だけ
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

dependencies {
    // 他の依存関係…
    compileOnly("org.jetbrains:annotations:24.0.1")
}