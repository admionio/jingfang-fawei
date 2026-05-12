plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "cn.lanzp.hdnj"
    compileSdk = 36

    defaultConfig {
        applicationId = "cn.lanzp.hdnj"
        minSdk = 23
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.3"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    applicationVariants.all {
        outputs.all {
            val variant = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            variant.outputFileName = "huangdi_neijing_${defaultConfig.versionName}.apk"
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
