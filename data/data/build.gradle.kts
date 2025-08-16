// data/build.gradle.kts
plugins {
    `java-library`
}
group = "org.example"
version = "1.0-SNAPSHOT"
repositories { mavenCentral() }
dependencies {
    implementation(project(":common"))
    implementation("com.h2database:h2:2.1.214")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.hibernate:hibernate-core:6.2.5.Final")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}