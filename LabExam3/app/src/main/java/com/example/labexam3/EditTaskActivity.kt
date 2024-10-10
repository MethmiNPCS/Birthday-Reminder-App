package com.example.labexam3

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.labexam3.model.Task
import com.example.labexam3.recievers.AlarmReceiver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class EditTaskActivity : AppCompatActivity() {

    // Declare shared preferences
    private lateinit var sharedPreferences: SharedPreferences

    // Declare the key
    private val taskListKey = "task_list"

    // Track which task is being edited
    private var taskPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_task)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initializing sharedPreferences
        sharedPreferences = getSharedPreferences("LabExam3Prefs", MODE_PRIVATE)

        // Get references of UI components
        val editTaskName: EditText = findViewById(R.id.edit_taskname)
        val editDate: DatePicker = findViewById(R.id.edit_date)
        val editTime: TimePicker = findViewById(R.id.edit_time)
        val saveBtn: Button = findViewById(R.id.edit_btn)

        // Retrive task details from the intent
        val taskName = intent.getStringExtra("task_name")
        val taskDate = intent.getStringExtra("task_date")
        val taskTime = intent.getStringExtra("task_time")
        taskPosition = intent.getIntExtra("task_position", -1)

        // Split date (dd/MM/yyyy) and populate DatePicker
        editTaskName.setText(taskName)
        val dateParts = taskDate?.split("/")
        if(dateParts != null && dateParts.size == 3){
            editDate.updateDate(dateParts[2].toInt(), dateParts[1].toInt(), dateParts[0].toInt())
        }


        // Split time (hh:mm) and populate TimePicker
        val timeParts = taskTime?.split(":")
        if (timeParts != null && timeParts.size == 2){
            editTime.hour = timeParts[0].toInt()
            editTime.minute = timeParts[1].toInt()
        }

        // Save Button Click Listener
        saveBtn.setOnClickListener {
            saveTask(editTaskName.text.toString(), editDate,editTime)
        }
    }

    // Method to save the edited Task
    private fun saveTask(taskName: String, datePicker: DatePicker, timePicker: TimePicker){
        val newDate = "${datePicker.dayOfMonth}/${datePicker.month + 1}/${datePicker.year}"
        val newTime = "${timePicker.hour}:${timePicker.minute}"

        // Load the task list from SharedPreferences
        val gson = Gson()
        val tasksJson = sharedPreferences.getString(taskListKey, null)
        val taskList: MutableList<Task> = if (tasksJson != null) {
            gson.fromJson(tasksJson, object : TypeToken<MutableList<Task>>() {}.type) ?: mutableListOf()
        } else {
            mutableListOf()
        }

        // Update the specific task in the list
        if (taskPosition != -1) {
            val task = taskList[taskPosition]
            task.name = taskName
            task.date = newDate
            task.dueTime = newTime

            // /* Update the task's year, month, day, hour, and minute */
            task.year = datePicker.year // Update year
            task.month = datePicker.month // Update month
            task.day = datePicker.dayOfMonth // Update day
            task.hour = timePicker.hour // Update hour
            task.minute = timePicker.minute // Update minute

            // Save the updated list back to SharedPreferences
            val updatedTasksJson = gson.toJson(taskList)
            sharedPreferences.edit().putString(taskListKey, updatedTasksJson).apply();

            Toast.makeText(this, "Details updated successfully", Toast.LENGTH_SHORT).show()

            // Call updateNotification to set the alarm with updated details
            updateNotification(task) // Update the notification for the edited task
            
            // Navigate back to the main screen
            setResult(RESULT_OK) // Set the result to OK before finishing the activity
            finish() // Closes the activity and returns to the task list
        }
    }

    // Function to update notifications
    private fun updateNotification(task: Task) {
        // Create an Intent for the AlarmReceiver
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            // Pass the updated task details to the receiver
            putExtra("birthday_name", task.name)
            putExtra("task_date", task.date)
            putExtra("task_time", task.dueTime)
        }

        // Create a unique request code for the PendingIntent using the task's position
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            taskPosition, // Use task position as a unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create a Calendar instance to set the alarm with updated task details
        val calendar: Calendar = Calendar.getInstance().apply {
            set(task.year, task.month, task.day, task.hour, task.minute, 0)
        }

        // Cancel any existing alarms for this task
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        // Set a new alarm with the updated time
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}