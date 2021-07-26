package com.orgzly.android.tasker

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperEventNoOutputOrInputOrUpdate
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigNoInput
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput


object OnSyncFinishedTaskerEvent {
    fun trigger(context: Context) = ConfigActivity::class.java.requestQuery(context)

    class ConfigHelper(config: TaskerPluginConfig<Unit>) : TaskerPluginConfigHelperEventNoOutputOrInputOrUpdate(config) {
        override fun addToStringBlurb(input: TaskerInput<Unit>, blurbBuilder: StringBuilder) {
            blurbBuilder.append("Triggers when synchronisation finishes")
        }
    }

    class ConfigActivity : Activity(), TaskerPluginConfigNoInput {
        override val context: Context get() = applicationContext
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            ConfigHelper(this).finishForTasker()
        }
    }
}