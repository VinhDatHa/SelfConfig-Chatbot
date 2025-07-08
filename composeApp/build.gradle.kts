import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.composeHotReload)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.ksp)
	alias(libs.plugins.room)
	alias(libs.plugins.googleServices)
}

kotlin {
	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_11)
		}
	}

	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64()
	).forEach { iosTarget ->
		iosTarget.binaries.framework {
			baseName = "ComposeApp"
			isStatic = true
		}
	}

	jvm("desktop")

	sourceSets {
		all {
			languageSettings {
				optIn("androidx.compose.material3.ExperimentalMaterial3Api")
				optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
				optIn("kotlin.uuid.ExperimentalUuidApi")
				optIn("kotlin.time.ExperimentalTime")
			}
		}
		val desktopMain by getting

		androidMain.dependencies {
			implementation(compose.preview)
			implementation(libs.androidx.activity.compose)
			implementation(libs.androidx.constraintlayout)
			implementation(libs.ktor.client.okhttp)
			implementation(libs.koin.android)
			implementation(libs.koin.androidx.compose)
			implementation(libs.androidx.datastore)
			implementation(libs.jetbrains.markdown)
			implementation(project.dependencies.platform(libs.android.firebase.bom))
			implementation(libs.android.firebase.analytics)
			implementation(libs.androidx.core.splashscreen)
		}
		commonMain.dependencies {
			implementation(compose.runtime)
			implementation(compose.foundation)
			implementation(compose.material3)
			implementation(compose.ui)
			implementation(compose.components.resources)
			implementation(compose.components.uiToolingPreview)
			implementation(libs.androidx.lifecycle.viewmodel)
			implementation(libs.androidx.lifecycle.runtimeCompose)
			implementation(libs.androidx.lifecycle.viewmodel)
			implementation(libs.androidx.lifecycle.runtimeCompose)
			implementation(libs.kotlinx.coroutineCore)
			implementation(libs.lucide.icons)
			implementation(libs.kotlinx.serialization.json)
			implementation(libs.bundles.ktor)
			implementation(libs.bundles.coil)
			implementation(libs.compose.navigation)
			api(libs.koin.core)
			implementation(libs.kotlinx.collections.immutable)
			implementation(libs.androidx.datastore.preferences)
			implementation(libs.androidx.datastore)
			implementation(libs.kotlinx.reflect)
			implementation(libs.jetbrains.markdown)
			implementation(libs.androidx.room.runtime)
			implementation(libs.sqlite.bundled)
			implementation(libs.kotlinx.date.time)
			implementation(libs.ksoup.core)
			implementation(libs.ksoup.kotlin)
			implementation(libs.calf.picker)
			implementation(libs.calf.coil)
		}
		appleMain.dependencies {
			implementation(libs.ktor.client.darwin)
		}

		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
		desktopMain.dependencies {
			implementation(compose.desktop.currentOs)
			implementation(libs.kotlinx.coroutinesSwing)
			implementation(libs.ktor.client.okhttp)
			implementation(libs.androidx.datastore.core)
			implementation(libs.androidx.datastore.preferences.core)
		}
		nativeMain.dependencies {
			implementation(libs.ktor.client.darwin)
		}
	}
}

android {
	namespace = "io.curri.dictionary.chatbot"
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	defaultConfig {
		applicationId = "io.curri.dictionary.chatbot"
		minSdk = libs.versions.android.minSdk.get().toInt()
		targetSdk = libs.versions.android.targetSdk.get().toInt()
		versionCode = 1
		versionName = "1.0"
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}

	buildFeatures {
		compose = true
		buildConfig = true
	}
	buildTypes {
		getByName("debug") {
			isMinifyEnabled = false
			isShrinkResources = false
			versionNameSuffix = "-DEBUG"
			isDebuggable = true
		}

		getByName("release") {
			isMinifyEnabled = true
			isShrinkResources = true
			isDebuggable = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}

dependencies {
	implementation(libs.androidx.navigation.common.android)
	debugImplementation(compose.uiTooling)
	ksp(libs.androidx.room.compiler)
	add("kspAndroid", libs.androidx.room.compiler)
	add("kspIosSimulatorArm64", libs.androidx.room.compiler)
	add("kspIosX64", libs.androidx.room.compiler)
	add("kspIosArm64", libs.androidx.room.compiler)
}
room {
	schemaDirectory("$projectDir/schemas")
}


compose.desktop {
	application {
		mainClass = "io.curri.dictionary.chatbot.MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
			packageName = "io.curri.dictionary.chatbot"
			packageVersion = "1.0.0"
		}
	}
}