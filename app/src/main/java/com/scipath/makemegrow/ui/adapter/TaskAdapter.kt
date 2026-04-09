package com.scipath.makemegrow.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.handler.ErrorHandler
import com.scipath.makemegrow.data.handler.ErrorHandlerToast
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.model.Task.RepeatType.*
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalTime

class TaskAdapter(
    private var tasks: List<Task>,
    private val taskViewModel: TaskViewModel,
    private val onTaskClick: (task: Task) -> Unit,
    private val onTaskSelect: (task: Task, isSelected: Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    private var selectedTasks: MutableList<Int> = mutableListOf()
    private val errorHandler: ErrorHandler = ErrorHandlerToast

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
        val textTask: TextView = itemView.findViewById(R.id.text_task)
        val textDeadline: TextView = itemView.findViewById(R.id.text_deadline)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val context = holder.itemView.context
        val task = tasks[position]

        // Selection
        holder.itemView.setBackgroundColor(
            if (selectedTasks.contains(position))
                context.getColor(R.color.light_gray)
            else
                context.getColor(R.color.dark_gray)
        )

        // Text
        holder.textTask.text = task.name

        // Deadline
        if (task.deadlineDate == DateAndTimeConverter.NO_DATE) {
            holder.textDeadline.visibility = View.GONE
        } else {
            holder.textDeadline.visibility = View.VISIBLE
            val deadline: String = DateAndTimeConverter.dateAndTimeToString(
                date = DateAndTimeConverter.secondsToDate(task.deadlineDate),
                time = DateAndTimeConverter.secondsToTime(task.deadlineTime),
                context = context
            )
            holder.textDeadline.text = deadline
            holder.textDeadline.setTextColor(
                if (isDeadlineMissed(task)) {
                    context.getColor(R.color.red)
                } else {
                    context.getColor(R.color.white)
                }
            )
        }

        // Completed
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = task.isCompleted
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            val deadlineDate: LocalDate? = DateAndTimeConverter.secondsToDate(task.deadlineDate)
            try {
                when (task.repeat) {
                    NO_REPEAT -> task.isCompleted = isChecked
                    ONCE_A_DAY -> {
                        if (deadlineDate != null)
                            task.deadlineDate = DateAndTimeConverter
                                .dateToSeconds(deadlineDate.plusDays(1))
                    }
                    ON_WEEKDAYS -> {
                        if (deadlineDate != null)
                            task.deadlineDate = DateAndTimeConverter
                                .dateToSeconds(deadlineDate.plusDays(
                                    if (deadlineDate.dayOfWeek.value < 5) 1
                                    else 8L - deadlineDate.dayOfWeek.value))
                    }
                    ON_WEEKENDS -> {
                        if (deadlineDate != null)
                            task.deadlineDate = DateAndTimeConverter
                                .dateToSeconds(deadlineDate.plusDays(
                                    if (deadlineDate.dayOfWeek.value == 6) 1
                                    else if (deadlineDate.dayOfWeek.value == 7) 6
                                    else 6L - deadlineDate.dayOfWeek.value))
                    }
                    ONCE_A_WEEK -> {
                        if (deadlineDate != null)
                            task.deadlineDate = DateAndTimeConverter
                                .dateToSeconds(deadlineDate.plusWeeks(1))
                    }
                    ONCE_A_MONTH -> {
                        if (deadlineDate != null)
                            task.deadlineDate = DateAndTimeConverter
                                .dateToSeconds(deadlineDate.plusMonths(1))
                    }
                    ONCE_A_YEAR -> {
                        if (deadlineDate != null)
                            task.deadlineDate = DateAndTimeConverter
                                .dateToSeconds(deadlineDate.plusYears(1))
                    }
                }
                taskViewModel.updateTask(task)
            } catch (exception: Exception) {
                errorHandler.handle(exception, context)
            }
        }

        // OnClick
        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }

        // OnLongClick
        holder.itemView.setOnLongClickListener {
            if (selectedTasks.contains(position)) {
                selectedTasks.remove(position)
            } else {
                selectedTasks.add(position)
            }
            notifyItemChanged(position)
            onTaskSelect(task, selectedTasks.contains(position))
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        clearSelection()
    }

    fun clearSelection() {
        selectedTasks.clear()
        notifyDataSetChanged()
    }

    private fun isDeadlineMissed(task: Task): Boolean {
        val currentDate: Long = DateAndTimeConverter.dateToSeconds(LocalDate.now())
        val currentTime: Int = DateAndTimeConverter.timeToSeconds(LocalTime.now())
        return  task.deadlineDate < currentDate ||
                task.deadlineDate == currentDate &&
                task.deadlineTime < currentTime
    }
}