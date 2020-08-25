@file:Suppress("UNUSED_VARIABLE")

plugins {
	kotlin("multiplatform") version "1.4.0"
	id("maven-publish")
}

group = "org.kiot"
version = "1.0.4"

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
				api(npm("text-encoding", "0.7.0"))
			}
		}
		val jsTest by getting {
			dependencies {
				implementation(kotlin("test-js"))
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