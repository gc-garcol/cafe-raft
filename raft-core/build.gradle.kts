plugins {
    `java-library`
}

group = "io.github.gc-garcol"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

var lombokVersion = "1.18.34"
var agronaVersion = "1.23.1"
var cafeRingBufferVersion = "1.0.0"
var jupiterVersion = "5.11.3"

dependencies {
    compileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")

    implementation("org.agrona:agrona:${agronaVersion}")
    implementation("io.github.gc-garcol:cafe-ringbuffer:${cafeRingBufferVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
}

tasks.test {
    jvmArgs("--add-opens", "java.base/java.nio=ALL-UNNAMED")
    useJUnitPlatform()
}
