plugins {
    `java-library`
    `maven-publish`
    jacoco
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "roxtools"
version = "1.8.5"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withSourcesJar()
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    val filtersVersion: String by project
    val slf4jVersion: String by project
    val javaAssistVersion: String by project
    val junitVersion: String by project

    api("com.jhlabs:filters:$filtersVersion")
    api("org.slf4j:slf4j-api:$slf4jVersion")
    api("org.javassist:javassist:$javaAssistVersion")

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("org.slf4j:slf4j-simple:$slf4jVersion")
}

tasks.register<Test>("testsOn16") {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(16))
    })
}

tasks.register<Test>("testsOn17") {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
}

tasks.register<Test>("testsOn18") {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(18))
    })
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components.getByName("java"))
        }
    }
    repositories {
        maven {
            val nexusUrl: String by project
            val nexusRepository: String by project

            name = "nexus"
            url = uri("${nexusUrl}/repository/${nexusRepository}/")
            credentials(PasswordCredentials::class)
        }
    }
}
