package com.black.cat.plugin

import org.gradle.api.Action
import org.gradle.api.Project

abstract class ProguardExtension(project: Project) {
    internal val proguardConfig = project.objects.mapProperty(String::class.java, String::class.java)
    fun replaceConfig(action: Action<MutableMap<String, String>>) {
        val data = mutableMapOf<String,String>()
        action.execute(data)
        proguardConfig.set(data)
        proguardConfig.finalizeValueOnRead()
    }
}