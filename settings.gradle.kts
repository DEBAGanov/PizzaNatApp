pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // ЮКасса SDK repository убран - используется серверная обработка платежей
        // maven {
        //     url = uri("https://artifactory.yoomoney.ru/artifactory/maven-public")
        // }
    }
}

rootProject.name = "PizzaNat"
include(":app")
