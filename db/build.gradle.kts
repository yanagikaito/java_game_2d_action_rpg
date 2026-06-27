plugins {
    `java-library`
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.h2database:h2:2.2.224")
    compileOnly("org.jetbrains:annotations:24.0.1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.8")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.8")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.18.8")
}

configurations.all {
    resolutionStrategy {
        force("com.fasterxml.jackson.core:jackson-core:2.18.8")
        force("com.fasterxml.jackson.core:jackson-databind:2.18.8")
        force("com.fasterxml.jackson.core:jackson-annotations:2.18.8")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}

tasks.test {
    useJUnitPlatform()
}