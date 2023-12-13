plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    // APIキーを隠すため
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.sw.gurumemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sw.gurumemo"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }

}

dependencies {
    // Room Dependencies
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // To use Kotlin Symbol Processing (KSP)
    ksp("androidx.room:room-compiler:$room_version")

//    implementation("androidx.room:room-runtime:$2.5.0")
//    annotationProcessor("androidx.room:room-compiler:$2.5.0")
//    ksp("androidx.room:room-compiler:$2.5.0")

    // Coroutine Dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0-RC")

    // 戻るボタンのイベント処理 (MainActivity)
    implementation("androidx.activity:activity-ktx:1.9.0-alpha01")

    // Navigation Dependencies
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")

    // Retrofit & API Dependencies
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Google Maps & Location Dependencies
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // UI related Dependencies
    implementation("de.hdodenhof:circleimageview:3.1.0")    // for circle image view
    implementation("com.github.bumptech.glide:glide:4.16.0")    // getting images from URL
    implementation("androidx.viewpager2:viewpager2:1.0.0")  // for ViewPager
    implementation("com.tbuonomo:dotsindicator:5.0")    // indicator for ViewPager

    // Default
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}