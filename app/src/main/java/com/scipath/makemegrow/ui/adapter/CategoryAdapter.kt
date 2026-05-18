package com.scipath.makemegrow.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel

class CategoryAdapter(
    private var categories: List<Category>,
    private val taskViewModel: TaskViewModel,
    private val onEdit: (Category) -> Unit,
    private val onDelete: (Category) -> Unit,
    private val onCategoryClick: (category: Category) -> Unit,
    private val onCategorySelect: (category: Category, isSelected: Boolean) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedCategories: MutableList<Int> = mutableListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textCategory: TextView = itemView.findViewById(R.id.text_category)
        val textTasks: TextView = itemView.findViewById(R.id.text_tasks)
        val textOverdueTasks: TextView = itemView.findViewById(R.id.text_overdue_tasks)
        val buttonEdit: Button = itemView.findViewById(R.id.button_edit)
        val buttonDelete: Button = itemView.findViewById(R.id.button_delete)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val context = holder.itemView.context
        val category: Category = categories[position]

        // Name
        holder.textCategory.text = category.name

        // Number of Tasks
        holder.textTasks.text = context.getString(
            R.string.task_number,
            taskViewModel.filterTasksByCategory(taskViewModel.allTasks, category).size)

        // Number of Overdue Tasks
        val overdueTasksNumber: Int = taskViewModel.filterTasksByCategory(taskViewModel.overdueTasks, category).size
        if (overdueTasksNumber > 0) {
            holder.textOverdueTasks.text = context.getString(
                R.string.overdue_task_number,
                overdueTasksNumber)
            holder.textOverdueTasks.visibility = View.VISIBLE
        } else {
            holder.textOverdueTasks.visibility = View.GONE
        }

        // Selection
        holder.itemView.setBackgroundColor(
            if (selectedCategories.contains(position))
                context.getColor(R.color.light_gray)
            else
                context.getColor(R.color.dark_gray)
        )

        // Button Edit
        holder.buttonEdit.setOnClickListener {
            onEdit.invoke(category)
        }

        // Button Delete
        holder.buttonDelete.setOnClickListener {
            onDelete.invoke(category)
        }

        // OnClick
        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }

        // OnLongClick
        holder.itemView.setOnLongClickListener {
            if (selectedCategories.contains(position)) {
                selectedCategories.remove(position)
            } else {
                selectedCategories.add(position)
            }
            notifyItemChanged(position)
            onCategorySelect(category, selectedCategories.contains(position))
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        selectedCategories.clear()
        notifyDataSetChanged()
    }
}