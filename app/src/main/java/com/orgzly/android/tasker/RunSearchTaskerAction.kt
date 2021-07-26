package com.orgzly.android.tasker

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import com.google.gson.GsonBuilder
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.orgzly.R
import com.orgzly.android.App
import com.orgzly.android.data.DataRepository
import com.orgzly.android.db.entity.NoteView
import com.orgzly.android.query.user.InternalQueryParser
import com.orgzly.databinding.ActivityTaskerSearchTermConfigBinding
import javax.inject.Inject

object RunSearchTaskerAction {
    class Helper(config: TaskerPluginConfig<Input>) : TaskerPluginConfigHelper<Input, Output, Runner>(config) {
        override val runnerClass: Class<Runner> get() = Runner::class.java
        override val inputClass: Class<Input> get() = Input::class.java
        override val outputClass: Class<Output> get() = Output::class.java
        override fun addToStringBlurb(input: TaskerInput<Input>, blurbBuilder: StringBuilder) {
            val searchTerm = input.regular.searchTerm
            blurbBuilder.insert(0, "Searches headlines by \"$searchTerm\"")
        }
    }

    class ConfigActivity : Activity(), TaskerPluginConfig<Input> {
        override val context: Context get() = applicationContext
        private val taskerHelper by lazy { Helper(this) }
        private lateinit var binding: ActivityTaskerSearchTermConfigBinding
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityTaskerSearchTermConfigBinding.inflate(layoutInflater)
            val view = binding.root
            setContentView(view)
            taskerHelper.onCreate()
        }

        override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
                if (binding.editSearchTerm.text?.toString()?.trim() ?: "" == "") {
                    val alertDialog = AlertDialog.Builder(this).create()
                    alertDialog.setTitle("Warning")
                    alertDialog.setMessage("Search term must be non-empty")
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ -> dialog.dismiss() }
                    alertDialog.show()
                    return false
                }
                return taskerHelper.finishForTasker().success
            }
            return super.onKeyDown(keyCode, event)
        }

        override fun onBackPressed() {}

        override fun assignFromInput(input: TaskerInput<Input>) = input.regular.run {
            binding.editSearchTerm.setText(searchTerm)
        }

        override val inputForTasker: TaskerInput<Input>
            get() = TaskerInput(Input(binding.editSearchTerm.text?.toString()?.trim() ?: ""))
    }

    class Runner : TaskerPluginRunnerAction<Input, Output>() {
        @Inject
        lateinit var dataRepository: DataRepository

        init {
            App.appComponent.inject(this)
        }

        override fun run(context: Context, input: TaskerInput<Input>): TaskerPluginResult<Output> {
            val searchTerm = input.regular.searchTerm
            val query = InternalQueryParser().parse(searchTerm)
            val notes = dataRepository.selectNotesFromQuery(query)
            val output = notes.map { Output.Note.from(it) }.toTypedArray()

            return TaskerPluginResultSucess(Output(output))
        }
    }

    @TaskerInputRoot
    class Input @JvmOverloads constructor(
            @field:TaskerInputField("searchTerm", R.string.tasker_search_term, ignoreInStringBlurb = true)
            var searchTerm: String = ""
    )

    @TaskerOutputObject
    class Output(
            @get:TaskerOutputVariable("results", R.string.tasker_results, R.string.tasker_results_desc)
            var results: Array<Note>?
    ) {
        @TaskerOutputObject
        data class Note(
                val title: String,
                val content: String?,
                val tags: List<String>,
                val inheritedTags: List<String>,
                val bookName: String,
                val scheduled: Timestamp?,
                val deadline: Timestamp?,
                val closed: Timestamp?,
                val priority: String?,
                val state: String?,
                val createdAt: Long?

        ) {
            override fun toString(): String {
                val gson = GsonBuilder().serializeNulls().create()
                return gson.toJson(this)
            }

            companion object {
                fun from(view: NoteView): Note {
                    val note = view.note
                    return Note(
                            note.title,
                            note.content,
                            note.tags?.split(" ") ?: emptyList(),
                            view.getInheritedTagsList(),
                            view.bookName,
                            Timestamp.from(
                                    view.scheduledTimeTimestamp,
                                    view.scheduledTimeString,
                                    view.scheduledTimeEndString,
                                    view.scheduledRangeString
                            ),
                            Timestamp.from(
                                    view.deadlineTimeTimestamp,
                                    view.deadlineTimeString,
                                    view.deadlineTimeEndString,
                                    view.deadlineRangeString
                            ),
                            Timestamp.from(
                                    view.closedTimeTimestamp,
                                    view.closedTimeString,
                                    view.closedTimeEndString,
                                    view.closedRangeString
                            ),
                            note.priority,
                            note.state,
                            note.createdAt
                    )
                }
            }
        }

        @TaskerOutputObject
        data class Timestamp(
                val timeTimestamp: Long,
                val timeString: String,
                val timeEndString: String? = null,
                val rangeString: String?,
        ) {
            companion object {
                fun from(
                        timeTimestamp: Long?,
                        timeString: String?,
                        timeEndString: String?,
                        rangeString: String?
                ): Timestamp? {
                    return if (timeTimestamp != null && timeString != null)
                        Timestamp(timeTimestamp, timeString, timeEndString, rangeString)
                    else null
                }
            }
        }
    }
}
