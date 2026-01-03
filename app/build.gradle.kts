plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.simats.rentohub"
    compileSdk = 34   // <-- Use 34 or 35 (36 not fully released yet)

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.simats.rentohub"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-g" +
            "" +
            "son:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.android.volley:volley:1.2.1")
// Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")
    implementation("com.razorpay:checkout:1.6.38")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
