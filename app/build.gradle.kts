plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.worthit.gastospersonales"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.worthit.gastospersonales"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Upload key for Google Play. Path and passwords come from ~/.gradle/gradle.properties
    // (GASTOS_UPLOAD_STORE_FILE, _STORE_PASSWORD, _KEY_ALIAS, _KEY_PASSWORD), never from the repo.
    signingConfigs {
        val storeFile = providers.gradleProperty("GASTOS_UPLOAD_STORE_FILE").orNull
        if (storeFile != null) {
            create("upload") {
                this.storeFile = file(storeFile)
                storePassword = providers.gradleProperty("GASTOS_UPLOAD_STORE_PASSWORD").orNull
                keyAlias = providers.gradleProperty("GASTOS_UPLOAD_KEY_ALIAS").orNull
                keyPassword = providers.gradleProperty("GASTOS_UPLOAD_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            // R8 shrinks and obfuscates the code, and removes unused resources.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("upload")?.let { signingConfig = it }
        }

        // For checking the release build on a phone: same code and R8 settings as release, signed
        // with the debug key, installed next to the real app (own package, own data).
        // It is not what goes to Google Play: the store upload is the signed release bundle.
        create("releaseCheck") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".check"
            matchingFallbacks += "release"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

// Google Play rejects an unsigned bundle, so fail early instead of producing one.
gradle.taskGraph.whenReady {
    val buildsRelease = allTasks.any { it.path == ":app:bundleRelease" || it.path == ":app:assembleRelease" }
    if (buildsRelease && android.signingConfigs.findByName("upload") == null) {
        throw GradleException(
            "Release needs the upload key: set GASTOS_UPLOAD_STORE_FILE, GASTOS_UPLOAD_STORE_PASSWORD, " +
                "GASTOS_UPLOAD_KEY_ALIAS and GASTOS_UPLOAD_KEY_PASSWORD in ~/.gradle/gradle.properties"
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
