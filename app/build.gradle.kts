plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
    // alias(libs.plugins.google.services) // Временно отключено до настройки Firebase
    // alias(libs.plugins.firebase.crashlytics) // Временно отключено до настройки Firebase
}

android {
    namespace = "com.pizzanat.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pizzanat.app"
        minSdk = 25
        targetSdk = 35
        versionCode = 5
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            // Development/Testing Backend URL - ОБНОВЛЕН на новый API
            buildConfigField("String", "BASE_API_URL", "\"https://api.dimbopizza.ru/api/v1/\"")
            buildConfigField("String", "ENVIRONMENT", "\"DEBUG\"")
            buildConfigField("boolean", "USE_MOCK_DATA", "false")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Production Backend URL
            buildConfigField("String", "BASE_API_URL", "\"https://api.dimbopizza.ru/api/v1/\"")
            buildConfigField("String", "ENVIRONMENT", "\"PRODUCTION\"")
            buildConfigField("boolean", "USE_MOCK_DATA", "false")
            
            // Включаем отладочные символы для нативного кода
            ndk {
                debugSymbolLevel = "FULL"
            }
        }

        create("staging") {
            initWith(getByName("release"))
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"

            // Staging Backend URL (для тестирования production окружения)
            buildConfigField("String", "BASE_API_URL", "\"https://api.dimbopizza.ru/api/v1/\"")
            buildConfigField("String", "ENVIRONMENT", "\"STAGING\"")
            buildConfigField("boolean", "USE_MOCK_DATA", "false")
            
            // Включаем отладочные символы для нативного кода
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    // Настройки для Android App Bundle с отладочными символами
    bundle {
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
        language {
            enableSplit = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Настройки для нативного кода и символьных файлов
    androidComponents {
        onVariants(selector().withBuildType("release")) { variant ->
            // Настройки для включения символьных файлов в bundle
            variant.packaging.resources.pickFirsts.add("**/libc++_shared.so")
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.lifecycle.runtime)

    // Core library desugaring для поддержки java.time на API < 26
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)

    // Material 2 для Pull-to-Refresh (совместимо с Material 3)
    implementation("androidx.compose.material:material:1.7.6")

    // Lifecycle & ViewModel
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt DI
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Local Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // DataStore
    implementation(libs.datastore)

    // Image Loading
    implementation(libs.coil)

    // SMS Retriever API
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-auth-api-phone:18.1.0")

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Firebase & Notifications (временно отключено)
    // implementation(platform(libs.firebase.bom))
    // implementation(libs.firebase.messaging)
    // implementation(libs.firebase.analytics)

    // WebSocket & Real-time
    implementation(libs.okhttp.sse)

    // Work Manager
    implementation(libs.work.runtime)
    implementation(libs.work.hilt)

    // ЮКасса Payment SDK убран - используется серверная обработка платежей
    // implementation("ru.yoomoney.sdk.kassa.payments:yookassa-android-sdk:6.6.0")
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)

    // Debug tools
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}