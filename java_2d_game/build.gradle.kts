plugins {
    `java-library`
    application
}
application {
    mainClass.set("game.Main")
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("com.h2database:h2:2.2.224")
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
    workingDir = projectDir.resolve("/src/main/resources")
}