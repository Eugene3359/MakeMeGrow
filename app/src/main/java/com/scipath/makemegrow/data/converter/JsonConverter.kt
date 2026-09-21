package com.scipath.makemegrow.data.converter

import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.repository.CategoryRepository
import com.scipath.makemegrow.data.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class JsonConverter(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository
) {

    @Serializable
    data class BackupData(
        val categories: List<CategoryBackup>,
        val tasks: List<TaskBackup>
    )

    @Serializable
    data class CategoryBackup(
        val id: Int,
        val name: String,
        val color: String
    )

    @Serializable
    data class TaskBackup(
        val id: Int,
        val name: String,
        val description: String,
        val isCompleted: Boolean,
        val deadlineDate: Long,
        val deadlineTime: Int,
        val repeat: String,
        val categoryId: Int?
    )

    private val json = Json {
        prettyPrint = true
    }

    suspend fun toJson(): String {
        val backup = BackupData(
            categories = categoryRepository.allCategories.first().map { category ->
                CategoryBackup(
                    id = category.id,
                    name = category.name,
                    color = category.color.key
                )
            },
            tasks = taskRepository.allTasks.first().map { task ->
                TaskBackup(
                    id = task.id,
                    name = task.name,
                    description = task.description,
                    isCompleted = task.isCompleted,
                    deadlineDate = task.deadlineDate,
                    deadlineTime = task.deadlineTime,
                    repeat = task.repeatType.name,
                    categoryId = task.categoryId
                )
            }
        )

        return json.encodeToString(backup)
    }

    suspend fun fromJson(jsonString: String) {
        val backup: BackupData = json.decodeFromString<BackupData>(jsonString)

        backup.categories.forEach { category ->
            categoryRepository.upsertCategory(
                Category(
                    id = category.id,
                    name = category.name,
                    color = Category.Color.entries.first {
                        it.key == category.color
                    }
                )
            )
        }

        backup.tasks.forEach { task ->
            taskRepository.upsertTask(
                Task(
                    id = task.id,
                    name = task.name,
                    description = task.description,
                    isCompleted = task.isCompleted,
                    deadlineDate = task.deadlineDate,
                    deadlineTime = task.deadlineTime,
                    repeatType = Task.RepeatType.valueOf(task.repeat),
                    categoryId = task.categoryId
                )
            )
        }
    }
}