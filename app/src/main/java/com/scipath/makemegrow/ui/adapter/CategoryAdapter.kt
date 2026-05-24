package com.scipath.makemegrow.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.databinding.LayoutCategoryBinding
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

    class ViewHolder(val binding: LayoutCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = LayoutCategoryBinding.inflate(
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
        val category: Category = categories[position]

        // Name
        holder.binding.textCategory.text = category.name

        // Number of Tasks
        holder.binding.textTasks.text = context.getString(
            R.string.task_number,
            taskViewModel.filterTasksByCategory(taskViewModel.allTasks, category).size)

        // Number of Overdue Tasks
        val overdueTasksNumber: Int = taskViewModel.filterTasksByCategory(taskViewModel.overdueTasks, category).size
        if (overdueTasksNumber > 0) {
            holder.binding.textOverdueTasks.text = context.getString(
                R.string.overdue_task_number,
                overdueTasksNumber)
            holder.binding.textOverdueTasks.visibility = View.VISIBLE
        } else {
            holder.binding.textOverdueTasks.visibility = View.GONE
        }

        // Selection
        holder.itemView.setBackgroundColor(
            if (selectedCategories.contains(position))
                context.getColor(R.color.light_gray)
            else
                context.getColor(R.color.dark_gray)
        )

        // Button Edit
        holder.binding.buttonEdit.setOnClickListener {
            onEdit.invoke(category)
        }

        // Button Delete
        holder.binding.buttonDelete.setOnClickListener {
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