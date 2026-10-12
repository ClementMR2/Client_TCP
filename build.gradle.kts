plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "2.26.0"
}

group = "fr.btsciel"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {url =uri("https://jitpack.io")}
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec>().configureEach {
    // Keep automatic modules such as googleauth on IntelliJ's module path.
    modularity.inferModulePath.set(false)
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        mainModule.orNull?.let { listOf("--add-modules", it) } ?: emptyList()
    })
}

application {
    mainModule.set("fr.btsciel")
    mainClass.set("fr.btsciel.HelloApplication")
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("com.github.MoserBTS:totp:1.0.2")
    implementation("com.warrenstrange:googleauth:1.5.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.1")
    implementation("com.google.code.gson:gson:2.14.0")
}

tasks.named<JavaCompile>("compileTestJava") {
    extensions.getByType<org.javamodularity.moduleplugin.extensions.CompileTestModuleOptions>()
        .setCompileOnClasspath(true)
}

tasks.withType<Test> {
    extensions.getByType<org.javamodularity.moduleplugin.extensions.TestModuleOptions>()
        .setRunOnClasspath(true)
    useJUnitPlatform()
}

jlink {
    imageZip.set(layout.buildDirectory.file("distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}
