// pathfinding_editor/build.gradle.kts

plugins {
    `java-library`
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    // Java 23 をソース／ターゲット互換性に設定
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

dependencies {
    // 共通ドメインモデル
    implementation(project(":db"))

    // H2 データベース（DB 保存／読み込み）
    implementation("com.h2database:h2:2.2.224")
}

// fat JAR（依存込み実行可能 JAR）を作成
tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
    manifest {
        attributes["Main-Class"] = "pathfinding.editor.PathfindingEditorFrame"
    }
}

// build タスク実行時に fatJar も組み込む
tasks.named("build") {
    dependsOn("fatJar")
}