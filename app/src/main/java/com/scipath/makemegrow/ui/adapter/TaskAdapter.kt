package com.scipath.makemegrow.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel

class TaskAdapter(
    private var tasks: List<Task>,
    private val taskViewModel: TaskViewModel,
    private val onTaskClick: (Task) -> Unit,
    private val onTaskSelect: (Task?) -> Unit
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    var selectedPosition: Int = -1

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
            if (position == selectedPosition)
                context.getColor(R.color.light_gray)
            else
                context.getColor(R.color.dark_gray)
        )

        // Text
        holder.textTask.text = task.name

        // Deadline
        if (task.deadlineDate == 0L) {
            holder.textDeadline.visibility = View.GONE
        } else {
            var deadline: String
            if (task.deadlineTime < 0) {
                deadline = DateAndTimeConverter.dateToString(
                    DateAndTimeConverter.secondsToDate(task.deadlineDate)
                )
            } else {
                deadline = DateAndTimeConverter.dateTimeToString(
                    DateAndTimeConverter.secondsToDateTime(
                        task.deadlineDate + task.deadlineTime
                    )
                )
            }
            holder.textDeadline.text = deadline
        }

        // Completed
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = task.isCompleted
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            task.isCompleted = isChecked
            taskViewModel.updateTask(task)
        }

        // OnClick
        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }

        // OnLongClick
        holder.itemView.setOnLongClickListener {
            val previous = selectedPosition
            selectedPosition = if (selectedPosition == position) -1 else position
            if (previous != -1) notifyItemChanged(previous)
            if (selectedPosition != -1) notifyItemChanged(selectedPosition)
            onTaskSelect(if (selectedPosition == -1) null else task)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun updateTasks(newTasks: List<Task>) {
        clearSelection()
        tasks = newTasks
        notifyDataSetChanged()
    }

    fun clearSelection() {
        val previous = selectedPosition
        selectedPosition = -1
        if (previous != -1) {
            notifyItemChanged(previous)
            onTaskSelect(null)
        }
    }
}