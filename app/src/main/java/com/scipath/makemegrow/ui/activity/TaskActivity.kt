package com.scipath.makemegrow.ui.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import com.scipath.makemegrow.R
import com.scipath.makemegrow.app.MakeMeGrowApp
import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.converter.TaskShareConverter.toShareString
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.databinding.ActivityTaskBinding
import com.scipath.makemegrow.ui.dialog.DeleteTaskDialog
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.SettingsViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.LocalTime

class TaskActivity : AppCompatActivity() {

    private lateinit var originalTask: Task
    private var selectedDate: LocalDate? = null
    private var selectedTime: LocalTime? = null
    private var selectedCategoryId: Int? = null
    private lateinit var binding: ActivityTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as MakeMeGrowApp
        val taskViewModel = ViewModelProvider(this, app.taskFactory)[TaskViewModel::class.java]
        val categoryViewModel = ViewModelProvider(this, app.categoryFactory)[CategoryViewModel::class.java]
        val settingsViewModel = ViewModelProvider(this, app.settingsFactory)[SettingsViewModel::class.java].apply {
            firstDayOfWeek.observe(this@TaskActivity) {}
            timeFormat24.observe(this@TaskActivity) {}
        }

        // Load Data
        if (intent.hasExtra("task")) {
            originalTask = intent.getSerializableExtra("task") as Task

            // Cancel notification
            if (intent.getBooleanExtra("cancel_notification", false)) {
                NotificationManagerCompat.from(this).cancel(originalTask.id)
            }
        } else {
            originalTask = Task()
        }

        // Setup Loaded Data
        var bulletPointsOn = false
        if (!originalTask.isEmpty()) {
            binding.textTitle.text = originalTask.name
            binding.inputTask.setText(originalTask.name)
            binding.inputDescription.setText(originalTask.description)
            if (originalTask.description.contains(getString(R.string.bullet_point))) {
                bulletPointsOn = true
                binding.buttonBulletPoint.text = ""
            } else {
                binding.buttonBulletPoint.setText(R.string.bullet_point_sign)
            }
            selectedDate = DateAndTimeConverter.secondsToDate(originalTask.deadlineDate)
            onDateChanged()
            selectedTime = DateAndTimeConverter.secondsToTime(originalTask.deadlineTime)
            settingsViewModel.timeFormat24.observe(this) { timeFormat24 ->
                onTimeChanged(timeFormat24)
            }
            selectedCategoryId = originalTask.categoryId

            // Button Share
            binding.buttonShare.visibility = View.VISIBLE
            binding.buttonShare.setOnClickListener {
                val text = originalTask.toShareString(settingsViewModel.isTimeFormat24(), this)
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }

            // Button Delete
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
                        taskViewModel.deleteTask(originalTask)
                        finish()
                    }
                }
            )
        } else {
            categoryViewModel.selectedCategoryId.observe(this) {}
            selectedCategoryId = categoryViewModel.selectedCategoryId.value
        }

        // Description
        binding.inputDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                text: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                text: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (bulletPointsOn &&
                    before == 0 &&
                    count == 1 &&
                    text?.get(start) == '\n'
                ) {
                    val bulletPoint: String = getString(R.string.bullet_point)
                    val description = StringBuilder(text)
                        .insert(start + 1, bulletPoint)
                    binding.inputDescription.setText(description)
                    binding.inputDescription.setSelection(start + bulletPoint.length + 1)
                }
            }

            override fun afterTextChanged(text: Editable?) {}
        })

        binding.buttonBulletPoint.setOnClickListener {
            if (bulletPointsOn) {
                bulletPointsOn = false
                binding.buttonBulletPoint.setText(R.string.bullet_point_sign)
                removeBulletPoints()
            } else {
                bulletPointsOn = true
                binding.buttonBulletPoint.text = ""
                addBulletPoints()
            }
        }

        // Input Date
        binding.inputDate.setOnClickListener {
            val currentDate: LocalDate = LocalDate.now()
            val year: Int = selectedDate?.year ?: currentDate.year
            val month: Int = selectedDate?.month?.value?.minus(1) ?: (currentDate.month.value - 1)
            val dayOfMonth: Int = selectedDate?.dayOfMonth ?: currentDate.dayOfMonth

            DatePickerDialog(
                this, /*R.style.DatePickerDialog*/
                { _, year, month, day -> run {
                    selectedDate = LocalDate.of(year, month + 1, day)
                    onDateChanged()
                }},
                year, month, dayOfMonth
            ).apply {
                datePicker.firstDayOfWeek = settingsViewModel.getFirstDayOfWeek().value + 1
                show()
            }
        }

        // Button Clear Date
        binding.buttonClearDate.setOnClickListener {
            selectedDate = null
            selectedTime = null
            binding.spinnerRepeatType.setSelection(0)
            onDateChanged()
            onTimeChanged(settingsViewModel.isTimeFormat24())
        }

        // Input Time
        binding.inputTime.setOnClickListener {
            val hourOfDay: Int = selectedTime?.hour ?: 12
            val minute: Int = selectedTime?.minute ?: 0

            TimePickerDialog(
                this, /*R.style.TimePickerDialog,*/
                { _, hour, minute -> run {
                    selectedTime = LocalTime.of(hour, minute)
                    onTimeChanged(settingsViewModel.isTimeFormat24())
                }},
                hourOfDay, minute, settingsViewModel.isTimeFormat24()
            ).apply {
                show()
            }
        }

        // Button Clear Time
        binding.buttonClearTime.setOnClickListener {
            selectedTime = null
            onTimeChanged(settingsViewModel.isTimeFormat24())
        }

        // Repeat Type Spinner
        val repeatTypes = resources.getStringArray(R.array.repeat_types)
        binding.spinnerRepeatType.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item_small,
            repeatTypes)
        binding.spinnerRepeatType.setSelection(originalTask.repeatType.ordinal)

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

            selectedCategoryId?.let { id ->
                val index = categories.indexOfFirst { it.id == id }
                binding.spinnerCategory.setSelection(index + 1)
            }

            binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    selectedCategoryId =
                        if (position == 0) null
                        else categories[position - 1].id
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        // Button Confirm
        binding.buttonConfirm.setOnClickListener {
            val taskName: String = binding.inputTask.text.trim().toString()
            if (taskName.isBlank()) {
                Toast.makeText(
                    this,
                    getString(R.string.enter_task_request),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                var resultTask = collectInput()
                if (originalTask.isEmpty()) { // Add new task
                    taskViewModel.addTask(resultTask)
                } else { // Modify existing task
                    resultTask = resultTask.copy(id = originalTask.id)
                    taskViewModel.updateTask(resultTask)
                }
                finish()
            }
        }

        // Button Back
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun addBulletPoints() {
        val bulletPoint: String = getString(R.string.bullet_point)
        var description: String = binding.inputDescription.text.toString()
        var selection: Int = binding.inputDescription.selectionEnd
        val n = description.take(selection).count { it == '\n' } + 1
        selection += n * bulletPoint.length
        description = bulletPoint + description
        description = description.replace("\n", "\n" + bulletPoint)
        binding.inputDescription.setText(description)
        binding.inputDescription.setSelection(selection)
    }

    private fun removeBulletPoints() {
        val bulletPoint: String = getString(R.string.bullet_point)
        var description: String = binding.inputDescription.text.toString()
        var selection: Int = binding.inputDescription.selectionEnd
        val n = description.take(selection).count { it == '•' }
        selection -= n * bulletPoint.length
        description = description.replace(bulletPoint, "")
        binding.inputDescription.setText(description)
        binding.inputDescription.setSelection(selection)
    }

    private fun onDateChanged() {
        val text: String = DateAndTimeConverter.dateToString(selectedDate, this)
        binding.inputDate.setText(text)

        if (text.isBlank()) {
            binding.buttonClearDate.visibility = View.GONE
            binding.layoutTimeSelection.visibility = View.GONE
            binding.buttonClearTime.visibility = View.GONE
            binding.layoutRepeatType.visibility = View.GONE
        } else {
            binding.buttonClearDate.visibility = View.VISIBLE
            binding.layoutTimeSelection.visibility = View.VISIBLE
            binding.layoutRepeatType.visibility = View.VISIBLE
        }
    }

    private fun onTimeChanged(timeFormat24: Boolean) {
        val text: String = DateAndTimeConverter.timeToString(selectedTime, timeFormat24, this)
        binding.inputTime.setText(text)

        if (text.isBlank()) {
            binding.buttonClearTime.visibility = View.GONE
        } else {
            binding.buttonClearTime.visibility = View.VISIBLE
        }
    }

    private fun collectInput(): Task {
        return Task(
            name = binding.inputTask.text.trim().toString(),
            description = binding.inputDescription.text.trim().toString(),
            deadlineDate = DateAndTimeConverter.dateToSeconds(selectedDate),
            deadlineTime = DateAndTimeConverter.timeToSeconds(selectedTime),
            repeatType = Task.RepeatType.entries[binding.spinnerRepeatType.selectedItemPosition],
            categoryId = selectedCategoryId
        )
    }
}