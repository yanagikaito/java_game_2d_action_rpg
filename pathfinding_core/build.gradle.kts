// pathfinding_core/build.gradle.kts

plugins {
    `java-library`
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

dependencies {
    // common モジュールへの依存
    api(project(":common"))

    // コメントアウト済みのテスト依存例
    // testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
}