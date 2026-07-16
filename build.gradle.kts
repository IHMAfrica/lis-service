import com.google.protobuf.gradle.id

plugins {
    java
    id("org.springframework.boot") version "4.0.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.5"
}

group = "moh.gov.zm"
version = "0.0.1-SNAPSHOT"
description = "lis"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

extra["springGrpcVersion"] = "1.0.3"

dependencies {
    implementation("ca.uhn.hapi:hapi-base:2.5.1")
    implementation("ca.uhn.hapi:hapi-structures-v25:2.5.1")
    implementation("org.apache.poi:poi-ooxml:5.4.0")
    implementation("io.projectreactor.kafka:reactor-kafka:1.3.22")
    // Zebra Link-OS SDK (unzipped under resources; not published to Maven Central).
    // Only the SDK-unique jars needed for USB printing are listed — the SDK also ships
    // slf4j-simple / log4j / jackson / httpclient (would collide with Spring Boot's
    // managed versions; slf4j-simple breaks Logback) and commons-lang3 / commons-io
    // (already provided transitively by Apache POI, so declaring them here duplicated
    // BOOT-INF/lib entries). Those are all left off; only the jars below are declared.
    val zebraSdkLib = "src/main/resources/Link-OS_SDK/PC/v2.15.5569/lib"
    implementation(files(
        "$zebraSdkLib/ZSDK_API.jar",
        "$zebraSdkLib/usb4java-1.3.0.jar",
        "$zebraSdkLib/libusb4java-1.3.0-linux-x86_64.jar",
        "$zebraSdkLib/libusb4java-1.3.0-linux-x86.jar",
        "$zebraSdkLib/libusb4java-1.3.0-linux-arm.jar",
        "$zebraSdkLib/libusb4java-1.3.0-darwin-x86-64.jar",
        "$zebraSdkLib/snmp6_1z.jar",
    ))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.grpc:grpc-services")
    implementation("org.apache.kafka:kafka-streams")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:3.0.2")
    implementation("org.springframework:spring-jdbc")
    implementation("org.springframework.grpc:spring-grpc-server-spring-boot-starter")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.postgresql:r2dbc-postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-r2dbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis-reactive-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-kafka-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.springframework.grpc:spring-grpc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    // Testcontainers 2.0 renamed modules with a `testcontainers-` prefix. Only core
    // (GenericContainer/DockerImageName) and the postgresql module are needed — Boot's
    // @ServiceConnection beans are used instead of the JUnit @Testcontainers extension.
    testImplementation("org.testcontainers:testcontainers:2.0.5")
    testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")
    // Bridges the Postgres container to an R2DBC ConnectionFactory for @ServiceConnection.
    testImplementation("org.testcontainers:testcontainers-r2dbc:2.0.5")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:${property("springGrpcVersion")}")
        // Also resolves testcontainers versions on the protobuf plugin's proto-path configs.
        mavenBom("org.testcontainers:testcontainers-bom:2.0.5")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") {
                    option("@generated=omit")
                }
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// The Zebra SDK jars are consumed as a compile/runtime dependency (see above), so
// keep the unzipped SDK out of the packaged resources to avoid bundling them twice.
tasks.withType<org.gradle.language.jvm.tasks.ProcessResources> {
    exclude("Link-OS_SDK/**")
}


