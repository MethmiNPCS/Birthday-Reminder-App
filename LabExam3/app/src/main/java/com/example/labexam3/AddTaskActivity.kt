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

class AddTaskActivity : AppCompatActivity() {

    // Declare SharedPreference variable
    private lateinit var sharedPreferences: SharedPreferences

    // Declare the Key
    private val taskListKey = "task_list"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_task)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("LabExam3Prefs", MODE_PRIVATE)

        // Get References from UI components
        val addTaskname: EditText = findViewById(R.id.edit_taskname)
        val addDate: DatePicker = findViewById(R.id.edit_date)
        val addTime: TimePicker = findViewById(R.id.edit_time)
        val addBtn: Button = findViewById(R.id.edit_btn)

        // Click Listener of "ADD" button
        addBtn.setOnClickListener {

            // Assign user entered task details to variables
            val taskName = addTaskname.text.toString()
            val dueDate = "${addDate.dayOfMonth}/${addDate.month + 1}/${addDate.year}" // Formatting due date

            // Check for empty task name
            if (taskName.isEmpty()) {
                Toast.makeText(this, "Please enter the name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Assign the time value to variable due time
            val dueTime = "${addTime.hour}:${addTime.minute}"

            // Create a calendar instance to set alarm
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(addDate.year, addDate.month, addDate.dayOfMonth, addTime.hour, addTime.minute, 0)

            // Get the current time
            val currentTime = Calendar.getInstance()

            // Check if the selected date and time are in the future
            if (calendar.before(currentTime)) {
                Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create an AlarmManager instance
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Create an intent for the AlarmReceiver
            val intent = Intent(this, AlarmReceiver::class.java).apply {
                putExtra("birthday_name", taskName) // Pass task name to receiver
            }

            // Set a unique request code for the pending intent
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                // Change: Use the size of the task list as a unique request code
                getUniqueRequestCode(), // Replace 0 with unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            ) /* Use the size of the task list as a unique request code */

            // Set the alarm to go off at the specified time
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

            // Store task details in SharedPreferences
            storeTaskInPreferences(taskName, dueDate, dueTime, calendar)

            // Show a toast message to inform user of successful task addition
            Toast.makeText(this, "Birthday Added Successfully!", Toast.LENGTH_SHORT).show()

            // Finish activity and return to MainActivity
            finish()
        }

    }

    // Function to get a unique request code for the PendingIntent
    private fun getUniqueRequestCode(): Int {
        val existingTasksJson = sharedPreferences.getString(taskListKey, null)
        val taskList: MutableList<Task> = if (existingTasksJson != null) {
            val gson = Gson()
            gson.fromJson(existingTasksJson, object : TypeToken<MutableList<Task>>() {}.type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
        return taskList.size // Use the size of the task list as a unique request code
    }

    // Function to store task in SharedPreferences
    private fun storeTaskInPreferences(taskName: String, dueDate: String, dueTime: String, calendar: Calendar) {
        val task = Task(taskName, dueDate, dueTime, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

        val gson = Gson()
        val existingTasksJson = sharedPreferences.getString(taskListKey, null)
        val taskList: MutableList<Task> = if (existingTasksJson != null) {
            gson.fromJson(existingTasksJson, object : TypeToken<MutableList<Task>>() {}.type) ?: mutableListOf()
        } else {
            mutableListOf()
        }

        taskList.add(task) // Add new task to the list

        // Convert the updated task list back to JSON
        val updatedTasksJson = gson.toJson(taskList)
        // Save the updated task list to SharedPreferences
        sharedPreferences.edit().putString(taskListKey, updatedTasksJson).apply()
    }


}