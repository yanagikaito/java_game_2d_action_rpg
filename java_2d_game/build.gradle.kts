plugins {
    id("java")
    id("application")
    id("com.gradleup.shadow") version "9.1.0"
}

application {
    mainClass.set("game.Main")
}

tasks.shadowJar {
    archiveClassifier.set("all") // 出力名に -all をつける
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
    mergeServiceFiles() // META-INF/services をマージ（H2などに必要）
    // H2のパッケージをリネームして衝突を防ぐ
    relocate("org.h2", "shaded.org.h2")
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
    implementation(project(":db"))
    implementation(project(":data"))
    implementation(project(":pathfinding_editor"))
    implementation("com.h2database:h2:2.2.224")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.5.25")
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("-Dfile.encoding=UTF-8")
}

tasks.named<Javadoc>("javadoc") {
    // 文字コード設定
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        docEncoding = "UTF-8"
        charSet = "UTF-8"
        // DocLint をオフ
        addStringOption("-Xdoclint:none", "-quiet")
    }
    // エラーがあってもビルド失敗にしない
    isFailOnError = false
    // 出力先をプロジェクト直下の docs/javadoc に変更
    destinationDir = file("$projectDir/docs/javadoc")
}

subprojects {
    dependencies {
        compileOnly("org.jetbrains:annotations:24.0.1")
    }
}

tasks.named<JavaExec>("run") {
    workingDir = projectDir.resolve("src/main/resources")
    jvmArgs("-Dfile.encoding=UTF-8")
}