import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	application

	kotlin("jvm")
	kotlin("plugin.serialization")

	id("com.github.johnrengelman.shadow")
	id("dev.yumi.gradle.licenser")
	id("io.gitlab.arturbosch.detekt")
	id("io.sentry.jvm.gradle")
}

group = "wiki.moderation"
version = "1.0-SNAPSHOT"

repositories {
	google()
	mavenCentral()

	maven {
		name = "Sonatype Snapshots (Legacy)"
		url = uri("https://oss.sonatype.org/content/repositories/snapshots")
	}

	maven {
		name = "Sonatype Snapshots"
		url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
	}
}

dependencies {
	detektPlugins(libs.detekt)

	implementation(libs.bundles.kordex)
	implementation(libs.bundles.mongodb)

	implementation(libs.kotlin.stdlib)
	implementation(libs.kx.ser)

	// Logging dependencies
	implementation(libs.groovy)
	implementation(libs.jansi)
	implementation(libs.logback)
	implementation(libs.logback.groovy)
	implementation(libs.logging)
}

application {
	mainClass.set("wiki.moderation.bot.general.AppKt")
}

tasks.withType<KotlinCompile> {
	// Current LTS version of Java
	kotlinOptions.jvmTarget = "17"

	kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

tasks.jar {
	manifest {
		attributes(
			"Main-Class" to "wiki.moderation.bot.general.AppKt"
		)
	}
}

java {
	// Current LTS version of Java
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

detekt {
	buildUponDefaultConfig = true

	config.from(rootProject.files("detekt.yml"))
}

license {
	rule(
		file("codeformat/HEADER")
	)
}

if (System.getenv().containsKey("SENTRY_AUTH_TOKEN")) {
	sentry {
		debug = true
		includeSourceContext = true

		additionalSourceDirsForSourceContext.set(
			listOf("src/main/kotlin")
		)

		org = "community-management-community"
		projectName = "bot-general"
		authToken = System.getenv("SENTRY_AUTH_TOKEN")
	}
} else {
	logger.info("Not sending sources to Sentry as the 'SENTRY_AUTH_TOKEN' env var isn't set.")
}
