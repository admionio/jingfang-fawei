plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "cn.lanzp.jffw"
    compileSdk = 36

    defaultConfig {
        applicationId = "cn.lanzp.jffw"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.3"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = "jffw123"
            keyAlias = "jffw"
            keyPassword = "jffw123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    applicationVariants.all {
        outputs.all {
            val variant = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            variant.outputFileName = "jingfang_fawei_v${defaultConfig.versionName}.apk"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
