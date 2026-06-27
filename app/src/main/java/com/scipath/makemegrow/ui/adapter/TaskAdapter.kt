package com.scipath.makemegrow.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.model.Task.RepeatType.*
import com.scipath.makemegrow.data.repository.SettingsRepository.Companion.DEFAULT_TIME_FORMAT_24
import com.scipath.makemegrow.databinding.LayoutTaskBinding
import java.time.LocalDate
import java.time.LocalTime

class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskClick: (task: Task) -> Unit,
    private val onTaskSelect: (task: Task, isSelected: Boolean) -> Unit,
    private val onTaskChecked: (
        task: Task,
        isChecked: Boolean,
        onCancel: () -> Unit
    ) -> Unit
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    private var isTimeFormat24: Boolean = DEFAULT_TIME_FORMAT_24
    private var selectedTasks: MutableList<Int> = mutableListOf()

    class ViewHolder(val binding: LayoutTaskBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = LayoutTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val context = holder.itemView.context
        val task = tasks[position]

        // Name
        holder.binding.textTask.text = task.name

        // Deadline
        if (task.deadlineDate == DateAndTimeConverter.NO_DATE) {
            holder.binding.textDeadline.visibility = View.GONE
        } else {
            holder.binding.textDeadline.visibility = View.VISIBLE
            val deadline: String = DateAndTimeConverter.dateAndTimeToString(
                date = DateAndTimeConverter.secondsToDate(task.deadlineDate),
                time = DateAndTimeConverter.secondsToTime(task.deadlineTime),
                isTimeFormat24 = isTimeFormat24,
                context = context
            )
            holder.binding.textDeadline.text = deadline
            holder.binding.textDeadline.setTextColor(
                if (isDeadlineMissed(task)) {
                    context.getColor(R.color.red)
                } else {
                    context.getColor(R.color.white)
                }
            )
        }

        // Repeat
        if (task.repeat == NO_REPEAT) {
           holder.binding.imageRepeat.visibility = View.GONE
        } else {
            holder.binding.imageRepeat.visibility = View.VISIBLE
        }

        // Completed
        holder.binding.checkbox.setOnCheckedChangeListener(null)
        holder.binding.checkbox.isChecked = task.isCompleted
        holder.binding.checkbox.setOnCheckedChangeListener { checkbox, isChecked ->
            onTaskChecked(task, isChecked) { checkbox.isChecked = false }
        }

        // Selection
        holder.itemView.setBackgroundColor(
            if (selectedTasks.contains(position))
                context.getColor(R.color.light_gray)
            else
                context.getColor(R.color.dark_gray)
        )

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
        selectedTasks.clear()
        notifyDataSetChanged()
    }

    fun updateTimeFormat(isTimeFormat24: Boolean) {
        this.isTimeFormat24 = isTimeFormat24
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