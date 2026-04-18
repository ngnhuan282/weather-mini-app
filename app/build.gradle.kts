import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
}

// Đọc file local.properties để bảo mật Key
val localProperties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.btqt02"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.btqt02"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Lấy giá trị từ local.properties
        val apiKey = localProperties.getProperty("WEATHER_API_KEY") ?: "YOUR_API_KEY_HERE"
        val mapsKey = localProperties.getProperty("MAPS_API_KEY") ?: "NO_KEY"

        // Đưa biến vào Manifest
        manifestPlaceholders["WEATHER_API_KEY"] = apiKey
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey

        // Tạo biến để dùng trong code Java
        buildConfigField("String", "WEATHER_API_KEY", "\"$apiKey\"")
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Xử lý dữ liệu & API
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Hình ảnh & Vị trí
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

}