import java.util.Properties

plugins {
    kotlin("jvm") version "2.3.0"
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation(kotlin("stdlib"))
    implementation("io.insert-koin:koin-core:4.2.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.18.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")

    testImplementation(kotlin("test"))
}

javafx {
    version = "21.0.6"

    modules = listOf(
        "javafx.base",
        "javafx.graphics",
        "javafx.controls"
    )
}

kotlin {
    jvmToolchain(21)
}

// ./gradlew build -Pcustomer=customerA
// ./gradlew build -Pcustomer=customerB
// ./gradlew build -Pcustomer=customerC

val customer =
    providers
        .gradleProperty("customer")
        .orElse("customerC")
        .get()

val customerDir =
    file("src/customers/$customer")

require(customerDir.isDirectory) {
    "Unknown customer: '$customer'. Available: " +
            (file("src/customers").list()?.sorted()?.joinToString(", ") ?: "-")
}

// =====================================================
// SCREEN SELECTION
// =====================================================
//
// Musterinin ekran listesi customer.properties dosyasindan
// okunur. Kotlin kaynaklari bu noktada henuz derlenmedigi
// icin build, listeyi kaynak koddan ogrenemez; bu yuzden
// build'in okuyabilecegi bir dosyada tutulur.
//
// Yalnizca burada yazan ekranlarin kaynak dizini derlemeye
// eklenir. Secilmeyen ekranin kodu derlenmez ve jar icine
// girmez.
//
// =====================================================

val screensDir =
    file("src/screens")

val customerProperties =
    Properties().apply {

        val propertiesFile =
            customerDir.resolve("customer.properties")

        require(propertiesFile.isFile) {
            "customer.properties not found for '$customer': " +
                    propertiesFile.absolutePath
        }

        propertiesFile.inputStream().use {
            load(it)
        }
    }

val enabledScreens =
    customerProperties
        .getProperty("screens")
        .orEmpty()
        .split(",")
        .map { it.trim().uppercase() }
        .filter { it.isNotEmpty() }

val availableScreens =
    screensDir.list()?.sorted().orEmpty()

enabledScreens.forEach { screen ->

    require(screen in availableScreens) {
        "Unknown screen '$screen' in $customer/customer.properties. " +
                "Available: ${availableScreens.joinToString(", ")}"
    }
}

val excludedScreens =
    availableScreens - enabledScreens.toSet()

println("==============================================")
println("Building customer: $customer")
println("Customer directory: ${customerDir.absolutePath}")
println("Compiled screens:   ${enabledScreens.joinToString(", ").ifEmpty { "-" }}")
println("Excluded screens:   ${excludedScreens.joinToString(", ").ifEmpty { "-" }}")
println("==============================================")

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {

    source(
        fileTree(customerDir.resolve("kotlin")) {
            include("**/*.kt")
        }
    )

    enabledScreens.forEach { screen ->

        source(
            fileTree(screensDir.resolve("$screen/kotlin")) {
                include("**/*.kt")
            }
        )
    }
}


tasks.processResources {

    from(
        customerDir.resolve("resources")
    )

    enabledScreens.forEach { screen ->

        from(
            screensDir.resolve("$screen/resources")
        )
    }
}

// =====================================================
// APPLICATION
// =====================================================

application {
    mainClass.set("MainKt")
}

tasks.named<JavaExec>("run") {

    dependsOn(tasks.named("compileKotlin"))

    systemProperty(
        "customer",
        customer
    )

    val appMode =
        providers
            .gradleProperty("app_mode")
            .orElse("none")
            .get()

    systemProperty(
        "app_mode",
        appMode
    )
}
tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runCustomerCreator") {

    dependsOn(tasks.named("compileKotlin"))

    classpath = sourceSets["main"].runtimeClasspath

    mainClass.set("CustomerCreatorMainKt")
}