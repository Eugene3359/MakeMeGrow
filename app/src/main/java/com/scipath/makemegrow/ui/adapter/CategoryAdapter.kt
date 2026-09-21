package com.scipath.makemegrow.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.databinding.LayoutCategoryBinding
import com.scipath.makemegrow.ui.mapper.CategoryColorMapper.toResourceId
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel

class CategoryAdapter(
    private var categories: List<Category?>,
    private val taskViewModel: TaskViewModel,
    private val onEdit: (Category) -> Unit,
    private val onDelete: (Category) -> Unit,
    private val onCategoryClick: (category: Category?) -> Unit,
    private val onCategoryLongClick: (category: Category?, isSelected: Boolean) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedCategoryPositions: MutableList<Int?> = mutableListOf()

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
        val category: Category? = categories[position]

        // Name
        holder.binding.textCategory.text = category?.name
            ?: context.getString(R.string.default_category)

        // Number of Tasks
        holder.binding.textTasks.text = context.getString(
            R.string.task_number,
            taskViewModel.filterTasksByCategory(
                taskViewModel.allTasks,
                category?.id
            ).size)

        // Number of Overdue Tasks
        val overdueTasksNumber: Int = taskViewModel.filterTasksByCategory(
            taskViewModel.overdueTasks,
            category?.id
        ).size
        if (overdueTasksNumber > 0) {
            holder.binding.textOverdueTasks.text = context.getString(
                R.string.overdue_task_number,
                overdueTasksNumber)
            holder.binding.textOverdueTasks.visibility = View.VISIBLE
        } else {
            holder.binding.textOverdueTasks.visibility = View.GONE
        }

        // Category Indicator
        holder.binding.indicatorCategory.setBackgroundColor(
            context.getColor(
                category?.color?.toResourceId() ?:
                Category.Color.BLACK.toResourceId()
            )
        )

        if (category != null) {
            // Button Edit
            holder.binding.buttonEdit.visibility = View.VISIBLE
            holder.binding.buttonEdit.setOnClickListener {
                onEdit.invoke(category)
            }

            // Button Delete
            holder.binding.buttonDelete.visibility = View.VISIBLE
            holder.binding.buttonDelete.setOnClickListener {
                onDelete.invoke(category)
            }
        } else {
            holder.binding.buttonEdit.visibility = View.GONE
            holder.binding.buttonDelete.visibility = View.GONE
        }

        // Selection
        holder.itemView.setBackgroundColor(
            if (selectedCategoryPositions.contains(position))
                context.getColor(R.color.white)
            else
                context.getColor(R.color.dark_gray)
        )

        // OnClick
        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }

        // OnLongClick
        holder.itemView.setOnLongClickListener {
            if (selectedCategoryPositions.contains(position)) {
                selectedCategoryPositions.remove(position)
            } else {
                selectedCategoryPositions.add(position)
            }
            notifyItemChanged(position)
            onCategoryLongClick(category, selectedCategoryPositions.contains(position))
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    fun updateCategories(newCategories: List<Category?>) {
        categories = newCategories
        selectedCategoryPositions.clear()
        notifyDataSetChanged()
    }
}