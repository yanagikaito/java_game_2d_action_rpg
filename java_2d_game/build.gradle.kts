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

    // --- メインおよび各サブプロジェクトのクラス出力を同梱 ---
    from(sourceSets.main.get().output)
    from(project(":db").sourceSets.main.get().output)
    from(project(":data_module").sourceSets.main.get().output)
    from(project(":pathfinding_editor").sourceSets.main.get().output)

    // --- ★ここが最重要：各サブプロジェクトの「リソースディレクトリ（画像等）」自体を確実に同梱する ---
    from(project(":db").sourceSets.main.get().resources)
    from(project(":data_module").sourceSets.main.get().resources)
    from(project(":pathfinding_editor").sourceSets.main.get().resources)

    // エディター関連の除外
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
    implementation(project(":db"))
    implementation(project(":data_module"))

    // モジュール自体の依存は維持する（モンスターのAIがcore.PathFinder等を利用するため）
    implementation(project(":pathfinding_editor"))

    implementation("com.h2database:h2:2.2.224")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.5.25")

    // --- JUnit 5 の依存関係 ---
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
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
    // java_2d_game モジュールのディレクトリを基準にする（ここに data フォルダやリソースを置くため）
    workingDir = projectDir
    jvmArgs("-Dfile.encoding=UTF-8")
}