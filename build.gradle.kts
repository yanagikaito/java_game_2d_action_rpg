plugins {
    id("java")
    id("application")
    id("com.gradleup.shadow") version "9.1.0"
}

application {
    mainClass.set("game.Main")
}

tasks.shadowJar {
    archiveClassifier.set("all")
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
    mergeServiceFiles()
//    relocate("org.h2", "shaded.org.h2")

    // 単一プロジェクトのため、メインのソースとリソースはShadow JARに自動的に含まれます

    // エディター関連などを除外する場合の設定
    exclude("**/pathfinding_editor/ui/**")
    exclude("**/editor/**")
    exclude("**/TilemapEditor*.class")
    exclude("**/PathfindingEditor*.class")
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    // データベース、JSON、ログなどの外部ライブラリ
    implementation("com.h2database:h2:2.2.224")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.5.25")

    // --- JUnit 5 の依存関係 ---
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // 開発用アノテーション
    compileOnly("org.jetbrains:annotations:24.0.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("-Dfile.encoding=UTF-8")
}

tasks.named<Javadoc>("javadoc") {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        docEncoding = "UTF-8"
        charSet = "UTF-8"
        addStringOption("-Xdoclint:none", "-quiet")
    }
    isFailOnError = false
    destinationDir = file("$projectDir/docs/javadoc")
}

tasks.named<JavaExec>("run") {
    workingDir = projectDir
    jvmArgs("-Dfile.encoding=UTF-8")
}