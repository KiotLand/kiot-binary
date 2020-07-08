@file:Suppress("UNUSED_VARIABLE")

plugins {
	kotlin("multiplatform") version "1.3.72"
	id("maven-publish")
}

group = "org.kiot"
version = "1.0.1"

repositories {
	mavenCentral()
}

dependencies {
}

kotlin {
	jvm()
	js {
		browser {}
		nodejs {}
	}

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(kotlin("stdlib-common"))
				implementation("org.jetbrains.kotlinx:kotlinx-io:0.1.8")
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(kotlin("test-common"))
				implementation(kotlin("test-annotations-common"))
			}
		}
		val jsMain by getting {
			dependencies {
				implementation(kotlin("stdlib-js"))
				implementation("org.jetbrains.kotlinx:kotlinx-io-js:0.1.8")
			}
		}
		val jsTest by getting {
			dependencies {
				implementation(kotlin("test-js"))
				api(npm("text-encoding"))
			}
		}
		val jvmMain by getting {
			dependencies {
				implementation(kotlin("stdlib-jdk8"))
				implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.8")
			}
		}
		val jvmTest by getting {
			dependencies {
				implementation(kotlin("test-junit"))
			}
		}
	}
}