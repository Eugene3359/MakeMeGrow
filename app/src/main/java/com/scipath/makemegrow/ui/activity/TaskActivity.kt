package com.scipath.makemegrow.ui.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.local.AppDatabase
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.repository.CategoryRepository
import com.scipath.makemegrow.data.repository.TaskRepository
import com.scipath.makemegrow.ui.dialog.DeleteTaskDialog
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModelFactory
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModelFactory
import java.time.LocalDate
import java.time.LocalTime

class TaskActivity : AppCompatActivity() {

    private var task: Task? = null
    private var selectedDate: LocalDate? = null
    private var selectedTime: LocalTime? = null
    private var repeatPosition = 0
    private var selectedCategoryId: Int? = null

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
        val spinnerRepeat: Spinner = findViewById(R.id.spinner_repeat)
        val spinnerCategory: Spinner = findViewById(R.id.spinner_category)
        val layoutTimeSelection: LinearLayout = findViewById(R.id.layout_time_selection)
        val layoutRepeat: LinearLayout = findViewById(R.id.container_repeat)
        val buttonClearDate: Button = findViewById(R.id.button_clear_date)
        val buttonClearTime: Button = findViewById(R.id.button_clear_time)

        val taskDao = AppDatabase.getDatabase(applicationContext).taskDao()
        val taskRepository = TaskRepository(taskDao)
        val taskViewModelFactory = TaskViewModelFactory(taskRepository)
        val taskViewModel = ViewModelProvider(this, taskViewModelFactory)[TaskViewModel::class.java]
        val categoryDao = AppDatabase.getDatabase(applicationContext).categoryDao()
        val categoryRepository = CategoryRepository(categoryDao)
        val categoryFactory = CategoryViewModelFactory(categoryRepository)
        val categoryViewModel = ViewModelProvider(this, categoryFactory)[CategoryViewModel::class.java]

        // Input Date
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
                }},
                year,
                month,
                dayOfMonth)
            dialog.show()
        }

        inputDate.doAfterTextChanged { text ->
            text?.let {
                if (it.isBlank()) {
                    buttonClearDate.visibility = View.GONE
                    layoutTimeSelection.visibility = View.GONE
                    buttonClearTime.visibility = View.GONE
                    layoutRepeat.visibility = View.GONE
                } else {
                    buttonClearDate.visibility = View.VISIBLE
                    layoutTimeSelection.visibility = View.VISIBLE
                    layoutRepeat.visibility = View.VISIBLE
                }
            }
        }

        buttonClearDate.setOnClickListener {
            selectedDate = null
            selectedTime = null
            repeatPosition = 0
            inputDate.setText("")
            inputTime.setText("")
            spinnerRepeat.setSelection(0)
        }

        // Input Time
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

        inputTime.doAfterTextChanged { text ->
            text?.let {
                if (it.isBlank()) {
                    buttonClearTime.visibility = View.GONE
                } else {
                    buttonClearTime.visibility = View.VISIBLE
                }
            }
        }

        buttonClearTime.setOnClickListener {
            selectedTime = null
            inputTime.setText("")
        }

        // Repeat type spinner
        val repeatTypes = resources.getStringArray(R.array.repeat_types)
        spinnerRepeat.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item_small,
            repeatTypes)
        spinnerRepeat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                repeatPosition = position
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Category spinner
        categoryViewModel.allCategories.observe(this) { categories ->
            val categoryNames = listOf(getString(R.string.default_category)) + categories.map { it.name }
            spinnerCategory.adapter = ArrayAdapter(
                this,
                R.layout.spinner_item_small,
                categoryNames
            )

            spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    selectedCategoryId =
                        if (position == 0) null
                        else categories[position - 1].id
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            task?.let {
                selectedCategoryId = it.categoryId
                selectedCategoryId?.let { id ->
                    val index = categories.indexOfFirst { it.id == id }
                    spinnerCategory.setSelection(index + 1)
                }
            }
        }

        // Setup values in case of task modification
        task?.let {
            val textTitle: TextView = findViewById(R.id.text_title)
            textTitle.text = it.name
            inputTask.setText(it.name)
            selectedDate = DateAndTimeConverter.secondsToDate(it.deadlineDate)
            inputDate.setText(DateAndTimeConverter.dateToString(selectedDate, this))
            selectedTime = DateAndTimeConverter.secondsToTime(it.deadlineTime)
            inputTime.setText(DateAndTimeConverter.timeToString(selectedTime, this))
            repeatPosition = it.repeat.ordinal
            spinnerRepeat.setSelection(repeatPosition)

            val buttonDelete: Button = findViewById(R.id.button_delete)
            buttonDelete.visibility = View.VISIBLE
            buttonDelete.setOnClickListener {
                DeleteTaskDialog().show(supportFragmentManager, "DeleteTaskDialog")
            }

            supportFragmentManager.setFragmentResultListener(
                DeleteTaskDialog.REQUEST_KEY,
                this
            ) { _, bundle ->
                val isConfirmed = bundle.getBoolean(DeleteTaskDialog.RESULT_KEY)
                if (isConfirmed) {
                    taskViewModel.deleteTask(task!!)
                    finish()
                }
            }
        }

        // Button Confirm
        val buttonConfirm: Button = findViewById(R.id.button_confirm)
        buttonConfirm.setOnClickListener {
            val taskName: String = inputTask.text.toString()
            if (taskName.isBlank()) {
                Toast.makeText(this, getString(R.string.enter_task_first), Toast.LENGTH_LONG)
                    .show()
            } else {
                val deadlineDate: Long = DateAndTimeConverter.dateToSeconds(selectedDate)
                val deadlineTime: Int = DateAndTimeConverter.timeToSeconds(selectedTime)
                val repeat: Task.RepeatType = Task.RepeatType.entries[repeatPosition]
                if (task == null) {
                    // Add new task
                    taskViewModel.addTask(
                        Task(0, taskName, false,
                            deadlineDate, deadlineTime, repeat, selectedCategoryId))
                } else {
                    // Modify existing task
                    task?.let{
                        it.name = taskName
                        it.deadlineDate = deadlineDate
                        it.deadlineTime = deadlineTime
                        it.repeat = repeat
                        it.categoryId = selectedCategoryId
                        taskViewModel.updateTask(it)
                    }
                }
                finish()
            }
        }

        // Button Back
        val buttonBack: Button = findViewById(R.id.button_back)
        buttonBack.setOnClickListener {
            finish()
        }
    }
}