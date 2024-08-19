package com.black.cat.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.tasks.R8Task
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.provider.PropertyHost
import org.gradle.api.internal.provider.ValueState
import java.io.File


class ReplaceProguardRrulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("R8proguardConfig", ProguardExtension::class.java, project)
        val generatedProguardFilesTasks = mutableListOf<String>()
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants(androidComponents.selector().all()) { variant: Variant ->
            val taskName = "minify${variant.name.replaceFirstChar { it.uppercaseChar() }}WithR8"
            generatedProguardFilesTasks.add(taskName)
        }
        project.afterEvaluate {
            generatedProguardFilesTasks.forEach { taskName ->
                val task = project.tasks.findByName(taskName) as R8Task?
                if (task != null) {
                    println(task.javaClass.name)
                    task.doFirst {
                        val addProguardFiles = mutableListOf<File>()
                        val filterFiles = task.configurationFiles.filter { file ->
                            val result = findTargetProguardFile(
                                project.proguardExtension.proguardConfig.get(), file.absolutePath
                            )
                            if (result != null) {
                                addProguardFiles.add(File(result.second))
                            }
                            return@filter result == null
                        }.plus(addProguardFiles)
                        val host = Reflect.on(task.configurationFiles).field("host").get<Any>() as PropertyHost
                        Reflect.on(task.configurationFiles).set("valueState", ValueState.newState<Any>(host))
                        task.configurationFiles.setFrom(filterFiles)
                        task.configurationFiles.disallowChanges()
                    }
                }
            }
        }
    }

    private fun findTargetProguardFile(
        r8proguardConfig: MutableMap<String, String>, proguardFilePath: String
    ): Pair<String, String>? {
        val resultFilePaths = r8proguardConfig.filter {
            proguardFilePath.endsWith(it.key)
        }
        if (resultFilePaths.isEmpty()) return null
        return resultFilePaths.toList()[0]
    }
}

internal inline val Project.proguardExtension: ProguardExtension
    get() = extensions.getByType(ProguardExtension::class.java)

