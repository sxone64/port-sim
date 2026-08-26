plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "portsim"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly("org.jetbrains:annotations:26.1.0")
}

tasks.test {
    useJUnitPlatform()
}

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass = "portsim.Main"
}