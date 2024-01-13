pluginManagement {
	plugins {
		// Update this in libs.version.toml when you change it here.
		kotlin("jvm") version "1.9.22"
		kotlin("plugin.serialization") version "1.9.22"

		// Update this in libs.version.toml when you change it here.
		id("io.gitlab.arturbosch.detekt") version "1.23.4"

		id("com.github.jakemarsden.git-hooks") version "0.0.1"
		id("com.github.johnrengelman.shadow") version "8.1.1"

		id("dev.yumi.gradle.licenser") version "1.0.+"
	}
}

rootProject.name = "general-bot"
