package com.example.labexam3.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.labexam3.EditTaskActivity
import com.example.labexam3.R
import com.example.labexam3.model.Task
import java.util.Calendar
import java.util.concurrent.TimeUnit
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskAdapter(private val taskList: MutableList<Task>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // View Holder class
    class TaskViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val taskName : TextView = itemView.findViewById(R.id.taskName)
        val dueDate : TextView = itemView.findViewById(R.id.dueDate)
        val dueTime : TextView = itemView.findViewById(R.id.dueTime)
        val isSelected : CheckBox = itemView.findViewById(R.id.isSelected)
        val remainingTime: TextView = itemView.findViewById(R.id.remainingTime)
    }

    // Declare the Key
    private val taskListKey = "task_list"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.taskName.text = task.name
        holder.dueDate.text = task.date
        holder.dueTime.text = task.dueTime
        holder.isSelected.isChecked = task.isSelected

        // Calculate remaining time
        val remainingTimeText = calculateRemainingTime(task)
        holder.remainingTime.text = remainingTimeText

        // Click Listener to each task
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EditTaskActivity::class.java)

            // Pass the task details as intent extras
            intent.putExtra("task_name", task.name)
            intent.putExtra("task_date", task.date)
            intent.putExtra("task_time", task.dueTime)
            intent.putExtra("task_position", position)

            context.startActivity(intent)
        }

        // Click Listener for checkbox
        holder.isSelected.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Confirmation dialog for deletion
                showDeleteConfirmationDialog(holder.itemView.context, position)
            }
        }
    }

    // Function to generate confirmation dialog
    private fun showDeleteConfirmationDialog(context: Context, position: Int) {
        // Create AlertDialog for confirmation
        AlertDialog.Builder(context).apply {
            setTitle("Want to Delete?")
            setMessage("Do you want to remove this from the list?")
            setPositiveButton("Yes") { _, _ ->
                deleteTask(context, position) // Call deleteTask on confirmation
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog if "No" is clicked
            }
            show() // Show the dialog
        }
    }

    // Function to delete a task
    private fun deleteTask(context: Context, position: Int) {
        // Remove the task from the in-memory list
        taskList.removeAt(position) // Remove from the adapter's list

        // Load the task list from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("LabExam3Prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val tasksJson = sharedPreferences.getString(taskListKey, null)
        val taskListFromPrefs: MutableList<Task> = if (tasksJson != null) {
            gson.fromJson(tasksJson, object : TypeToken<MutableList<Task>>() {}.type) ?: mutableListOf()
        } else {
            mutableListOf()
        }

        // Also remove from the SharedPreferences list
        taskListFromPrefs.removeAt(position)

        // Save the updated task list back to SharedPreferences
        val updatedTasksJson = gson.toJson(taskListFromPrefs)
        sharedPreferences.edit().putString(taskListKey, updatedTasksJson).apply() // Update SharedPreferences

        notifyItemRemoved(position) // Notify the adapter of the item removal
        notifyItemRangeChanged(position, taskList.size) // Refresh the remaining items

        // Show a toast message
        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show() // Toast message for confirmation
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
    // Function to calculate remaining time
    private fun calculateRemainingTime(task: Task): String {
        val dueDateTime = Calendar.getInstance().apply {
            set(task.year, task.month, task.day, task.hour, task.minute)
        }

        val currentTime = Calendar.getInstance()
        val timeDifference = dueDateTime.timeInMillis - currentTime.timeInMillis

        if (timeDifference > 0) {
            // Calculate the difference in days, hours, and minutes
            val days = TimeUnit.MILLISECONDS.toDays(timeDifference)
            val hours = TimeUnit.MILLISECONDS.toHours(timeDifference) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference) % 60

            return when {
                days > 0 -> "$days days, $hours hours remaining"
                hours > 0 -> "$hours hours, $minutes minutes remaining"
                else -> "$minutes minutes remaining"
            }
        } else {
            return "It's today!"
        }
    }

}