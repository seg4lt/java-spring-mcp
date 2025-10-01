plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.deps.mgmt)
}

group = "com.seg4lt"
version = "0.0.1-SNAPSHOT"
description = "api server for dvd rental"




dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("${libs.spring.ai.bom.get().module}:${libs.spring.ai.bom.get().version}")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { name = "Central Portal Snapshots"; url = uri("https://central.sonatype.com/repository/maven-snapshots/") }
}

tasks.withType<Test> {
    useJUnitPlatform()
}