plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "myandroid.app.hhobzic.pool360"
    compileSdk = 34

    defaultConfig {
        applicationId = "myandroid.app.hhobzic.pool360"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures{
        dataBinding=true
        viewBinding=true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.auth)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation("com.intuit.ssp:ssp-android:1.0.5")
    implementation("com.intuit.sdp:sdp-android:1.0.5")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.android.gms:play-services-base:18.1.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.makeramen:roundedimageview:2.3.0")

    //camera
    implementation("androidx.camera:camera-core:1.2.0")
    implementation("androidx.camera:camera-camera2:1.2.0")
    implementation("androidx.camera:camera-lifecycle:1.2.0")
    implementation("androidx.camera:camera-view:1.2.0")
    implementation ("com.google.firebase:firebase-ml-vision:24.0.3")
    implementation ("com.google.firebase:firebase-ml-vision-face-model:20.0.1")
    implementation ("com.quickbirdstudios:opencv:4.5.3.0")
    implementation ("com.google.android.exoplayer:exoplayer:2.18.3")
//    implementation ("com.google.android.gms:play-services-ads:22.0.0")
    implementation ("androidx.work:work-runtime:2.8.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("io.github.chaosleung:pinview:1.4.4")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")



}