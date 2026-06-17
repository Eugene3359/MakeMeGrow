package com.scipath.makemegrow.ui.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.scipath.makemegrow.R
import com.scipath.makemegrow.app.MakeMeGrowApp
import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.databinding.ActivityTaskBinding
import com.scipath.makemegrow.ui.dialog.DeleteTaskDialog
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.LocalTime

class TaskActivity : AppCompatActivity() {

    private var task: Task? = null
    private var selectedDate: LocalDate? = null
    private var selectedTime: LocalTime? = null
    private var repeatPosition = 0
    private var selectedCategoryId: Int? = null
    private lateinit var binding: ActivityTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load Data
        if (intent.hasExtra("task")) {
            task = intent.getSerializableExtra("task") as Task
        }

        val app = application as MakeMeGrowApp
        val taskViewModel = ViewModelProvider(this, app.taskFactory)[TaskViewModel::class.java]
        val categoryViewModel = ViewModelProvider(this, app.categoryFactory)[CategoryViewModel::class.java]

        // Input Date
        binding.inputDate.setOnClickListener {
            val currentDate: LocalDate = LocalDate.now()
            val year: Int = selectedDate?.year ?: currentDate.year
            val month: Int = selectedDate?.month?.value?.minus(1) ?: (currentDate.month.value - 1)
            val dayOfMonth: Int = selectedDate?.dayOfMonth ?: currentDate.dayOfMonth
            val dialog = DatePickerDialog(
                this,
                /*R.style.DatePickerDialog*/
                { _, year, month, day -> run {
                    selectedDate = LocalDate.of(year, month + 1, day)
                    binding.inputDate.setText(DateAndTimeConverter.dateToString(selectedDate, this))
                }},
                year,
                month,
                dayOfMonth)
            dialog.show()
        }

        binding.inputDate.doAfterTextChanged { text ->
            text?.let {
                if (it.isBlank()) {
                    binding.buttonClearDate.visibility = View.GONE
                    binding.layoutTimeSelection.visibility = View.GONE
                    binding.buttonClearTime.visibility = View.GONE
                    binding.layoutRepeat.visibility = View.GONE
                } else {
                    binding.buttonClearDate.visibility = View.VISIBLE
                    binding.layoutTimeSelection.visibility = View.VISIBLE
                    binding.layoutRepeat.visibility = View.VISIBLE
                }
            }
        }

        // Button Clear Date
        binding.buttonClearDate.setOnClickListener {
            selectedDate = null
            selectedTime = null
            repeatPosition = 0
            binding.inputDate.setText("")
            binding.inputTime.setText("")
            binding.spinnerRepeat.setSelection(0)
        }

        // Input Time
        binding.inputTime.setOnClickListener {
            val hourOfDay: Int = selectedTime?.hour ?: 12
            val minute: Int = selectedTime?.minute ?: 0
            val dialog = TimePickerDialog(
                this,
                /*R.style.TimePickerDialog,*/
                { _, hour, minute -> run {
                    selectedTime = LocalTime.of(hour, minute)
                    binding.inputTime.setText(DateAndTimeConverter.timeToString(selectedTime, this))
                }},
                hourOfDay,
                minute,
                true)
            dialog.show()
        }

        binding.inputTime.doAfterTextChanged { text ->
            text?.let {
                if (it.isBlank()) {
                    binding.buttonClearTime.visibility = View.GONE
                } else {
                    binding.buttonClearTime.visibility = View.VISIBLE
                }
            }
        }

        // Button Clear Time
        binding.buttonClearTime.setOnClickListener {
            selectedTime = null
            binding.inputTime.setText("")
        }

        // Repeat Type Spinner
        val repeatTypes = resources.getStringArray(R.array.repeat_types)
        binding.spinnerRepeat.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item_small,
            repeatTypes)
        binding.spinnerRepeat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                repeatPosition = position
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Category Spinner
        categoryViewModel.allCategories.observe(this) { categories ->
            val categoryNames = buildList {
                add(getString(R.string.default_category))
                addAll(categories.map { it.name })
            }

            binding.spinnerCategory.adapter = ArrayAdapter(
                this,
                R.layout.spinner_item_small,
                categoryNames
            )

            binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    selectedCategoryId =
                        if (position == 0) null
                        else categories[position - 1].id
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            if (task == null) { // New Task
                categoryViewModel.selectedCategoryId.observe(this) {}
                selectedCategoryId = categoryViewModel.selectedCategoryId.value
            } else { // Modify Task
                selectedCategoryId = task!!.categoryId
            }
            selectedCategoryId?.let { id ->
                val index = categories.indexOfFirst { it.id == id }
                binding.spinnerCategory.setSelection(index + 1)
            }
        }

        // Setup Loaded Data
        task?.let {
            binding.textTitle.text = it.name
            binding.inputTask.setText(it.name)
            selectedDate = DateAndTimeConverter.secondsToDate(it.deadlineDate)
            binding.inputDate.setText(DateAndTimeConverter.dateToString(selectedDate, this))
            selectedTime = DateAndTimeConverter.secondsToTime(it.deadlineTime)
            binding.inputTime.setText(DateAndTimeConverter.timeToString(selectedTime, this))
            repeatPosition = it.repeat.ordinal
            binding.spinnerRepeat.setSelection(repeatPosition)

            // Botton Delete
            binding.buttonDelete.visibility = View.VISIBLE
            binding.buttonDelete.setOnClickListener {
                DeleteTaskDialog().show(supportFragmentManager, "DeleteTaskDialog")
            }

            supportFragmentManager.setFragmentResultListener(
                DeleteTaskDialog.REQUEST_KEY,
                this,
                { _, bundle ->
                    val isConfirmed = bundle.getBoolean(DeleteTaskDialog.RESULT_KEY)
                    if (isConfirmed) {
                        taskViewModel.deleteTask(it)
                        finish()
                    }
                }
            )
        }

        // Button Confirm
        binding.buttonConfirm.setOnClickListener {
            val taskName: String = binding.inputTask.text.toString()
            if (taskName.isBlank()) {
                Toast.makeText(
                    this,
                    getString(R.string.enter_task_first),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val deadlineDate: Long = DateAndTimeConverter.dateToSeconds(selectedDate)
                val deadlineTime: Int = DateAndTimeConverter.timeToSeconds(selectedTime)
                val repeat: Task.RepeatType = Task.RepeatType.entries[repeatPosition]
                if (task == null) {
                    // Add new task
                    taskViewModel.addTask(
                        Task(0, taskName, false, deadlineDate,
                            deadlineTime, repeat, selectedCategoryId))
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

        // Taskbar Elements
        // Button Back
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}