plugins {
    id("com.android.application")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.cytocheck"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cytocheck"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

repositories {
    google()
    mavenCentral()
    // Add JitPack repository here
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // MPAndroidChart dependency
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation(files("libs/fit_21.105.00.jar"))
    implementation(files("libs/antpluginlib_3-9-0.aar"))

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))

    // When using the BoM, you don't specify versions in Firebase library dependencies

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")

    // TODO: Add the dependencies for any other Firebase products you want to use
    // See https://firebase.google.com/docs/android/setup#available-libraries
    // For example, add the dependencies for Firebase Authentication and Cloud Firestore
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")

//    val activityVersion = "1.8.2"
//    implementation("androidx.activity:activity:$activityVersion")
//
//    val fragmentVersion = "1.6.2"
//    implementation("androidx.fragment:fragment:$fragmentVersion")\

    // AndroidX Activity KTX for ActivityResultLauncher
    implementation("androidx.activity:activity-ktx:1.4.0")

    // AndroidX Fragment KTX for Fragment use
    implementation("androidx.fragment:fragment-ktx:1.4.0")

}
