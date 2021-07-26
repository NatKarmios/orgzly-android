package com.orgzly.android.tasker

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutputOrInput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoOutputOrInput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigNoInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess


object BasicTaskerAction {
    class Helper(config: TaskerPluginConfig<Unit>) : TaskerPluginConfigHelperNoOutputOrInput<Runner>(config) {
        override val runnerClass: Class<Runner> get() = Runner::class.java
        override fun addToStringBlurb(input: TaskerInput<Unit>, blurbBuilder: StringBuilder) {
            blurbBuilder.append("Will show a toast saying 'Basic'")
        }
    }

    class ConfigActivity : Activity(), TaskerPluginConfigNoInput {
        override val context get() = applicationContext
        private val taskerHelper by lazy { Helper(this) }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            taskerHelper.finishForTasker()
        }
    }

    class Runner : TaskerPluginRunnerActionNoOutputOrInput() {
        override fun run(context: Context, input: TaskerInput<Unit>): TaskerPluginResult<Unit> {
            Handler(Looper.getMainLooper()).post { Toast.makeText(context, "Basic", Toast.LENGTH_LONG).show() }
            return TaskerPluginResultSucess()
        }
    }
}