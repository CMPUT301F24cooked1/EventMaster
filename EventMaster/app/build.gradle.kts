plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.eventmaster"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.eventmaster"
        minSdk = 26
        targetSdk = 35
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
    buildFeatures {
        viewBinding = true
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
    }

}

dependencies {
    implementation ("com.google.android.gms:play-services-maps:18.0.2")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.google.android.libraries.places:places:3.0.0")
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation("com.google.android.gms:play-services-auth:20.3.0")  // For authentication
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.firebaseui:firebase-ui-storage:8.0.0")  // Firebase UI Glide Storage integration
    implementation("com.google.android.gms:play-services-base:18.5.0")
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.google.android.material:material:1.8.0")
    implementation("com.google.firebase:firebase-messaging:24.1.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation("com.android.volley:volley:1.2.1")

    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")

}



tasks.withType<Test> {
    useJUnitPlatform()
}