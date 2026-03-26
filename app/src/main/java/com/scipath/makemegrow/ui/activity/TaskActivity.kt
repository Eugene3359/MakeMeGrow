package com.scipath.makemegrow.ui.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.local.AppDatabase
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.repository.TaskRepository
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModelFactory
import java.time.LocalDate
import java.time.LocalTime

class TaskActivity : AppCompatActivity() {

    private var task: Task? = null
    private var selectedDate: LocalDate? = null
    private var selectedTime: LocalTime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_task)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (intent.hasExtra("task")) {
            task = intent.getSerializableExtra("task") as Task
        }

        val inputTask: EditText = findViewById(R.id.input_task)
        val inputDate: EditText = findViewById(R.id.input_date)
        val inputTime: EditText = findViewById(R.id.input_time)
        val layoutTimeSelection: LinearLayout = findViewById(R.id.layout_time_selection)
        val buttonClearDate: Button = findViewById(R.id.button_clear_date)
        val buttonClearTime: Button = findViewById(R.id.button_clear_time)
        val buttonConfirm: Button = findViewById(R.id.button_confirm)

        val dao = AppDatabase.getDatabase(applicationContext).taskDao()
        val repository = TaskRepository(dao)
        val factory = TaskViewModelFactory(repository)
        val taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        task?.let {
            val textTitle: TextView = findViewById(R.id.text_title)
            textTitle.text = it.name
            inputTask.setText(it.name)
            selectedDate = DateAndTimeConverter.secondsToDate(it.deadlineDate)
            inputDate.setText(DateAndTimeConverter.dateToString(selectedDate, this))
            if (!inputDate.text.isBlank()) {
                selectedTime = DateAndTimeConverter.secondsToTime(it.deadlineTime)
                inputTime.setText(DateAndTimeConverter.timeToString(selectedTime, this))
                layoutTimeSelection.visibility = View.VISIBLE
            }

            val buttonDelete: Button = findViewById(R.id.button_delete)
            buttonDelete.visibility = View.VISIBLE
            buttonDelete.setOnClickListener {
                taskViewModel.deleteTask(task!!)
                finish()
            }
        }

        inputDate.setOnClickListener {
            val currentDate: LocalDate = LocalDate.now()
            val year: Int = selectedDate?.year ?: currentDate.year
            val month: Int = selectedDate?.month?.value?.minus(1) ?: (currentDate.month.value - 1)
            val dayOfMonth: Int = selectedDate?.dayOfMonth ?: currentDate.dayOfMonth
            val dialog = DatePickerDialog(
                this,
                /*R.style.DatePickerDialog*/
                { _, year, month, day -> run {
                    selectedDate = LocalDate.of(year, month + 1, day)
                    inputDate.setText(DateAndTimeConverter.dateToString(selectedDate, this))
                    layoutTimeSelection.visibility = View.VISIBLE
                }},
                year,
                month,
                dayOfMonth)
            dialog.show()
        }

        buttonClearDate.setOnClickListener {
            selectedDate = null
            selectedTime = null
            inputDate.setText("")
            inputTime.setText("")
            layoutTimeSelection.visibility = View.GONE
        }

        inputTime.setOnClickListener {
            val hourOfDay: Int = selectedTime?.hour ?: 12
            val minute: Int = selectedTime?.minute ?: 0
            val dialog = TimePickerDialog(
                this,
                /*R.style.TimePickerDialog,*/
                { _, hour, minute -> run {
                    selectedTime = LocalTime.of(hour, minute)
                    inputTime.setText(DateAndTimeConverter.timeToString(selectedTime, this))
                }},
                hourOfDay,
                minute,
                true)
            dialog.show()
        }

        buttonClearTime.setOnClickListener {
            selectedTime = null
            inputTime.setText("")
        }

        buttonConfirm.setOnClickListener {
            val taskName: String = inputTask.text.toString()
            if (taskName.isBlank()) {
                Toast.makeText(this, getString(R.string.enter_task_first), Toast.LENGTH_LONG)
                    .show()
            } else {
                val deadlineDate: Long = DateAndTimeConverter.dateToSeconds(selectedDate)
                val deadlineTime: Int = DateAndTimeConverter.timeToSeconds(selectedTime)
                if (task == null) {
                    taskViewModel.addTask(
                        Task(0, taskName, false, deadlineDate, deadlineTime))
                } else {
                    task?.let{
                        it.name = taskName
                        it.deadlineDate = deadlineDate
                        it.deadlineTime = deadlineTime
                        taskViewModel.updateTask(it)
                    }
                }
                finish()
            }
        }
    }
}