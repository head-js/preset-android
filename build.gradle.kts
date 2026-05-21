buildscript {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        mavenCentral()
    }
    dependencies {
        classpath("cn.therouter:plugin:1.3.2")
    }
}

plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.kapt") version "2.1.0" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}
