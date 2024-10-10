package com.example.labexam3

import android.content.Intent
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.labexam3.model.Task
import android.content.SharedPreferences
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.labexam3.adapters.TaskAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build


class MainActivity : AppCompatActivity() {

    // Declare variables
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter

    // Declare mutable list to load tasks
    private var taskList : MutableList<Task> = mutableListOf()

    // Declare SharedPreference variable
    private lateinit var sharedPreferences: SharedPreferences

    // Declare the Key
    private val taskListKey = "task_list"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Create the notification channel
        createNotificationChannel()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("LabExam3Prefs", MODE_PRIVATE)

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize taskAdapter with taskList
        taskAdapter = TaskAdapter(taskList)

        // Assigns the taskAdapter to the RecycleView
        recyclerView.adapter = taskAdapter

        // Load and display tasks
        loadTasks()

        // Navigate to Add task activity
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener{
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }
    }

    // Function onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            loadTasks() // Reload the tasks to ensure the UI is updated
        }
    }

    // Function to load tasks for SharedPreference
    private fun loadTasks() {
        val gson = Gson()
        val existingTasksJson = sharedPreferences.getString(taskListKey, null)

        // Check if the existingTasksJson is null
        if (existingTasksJson != null) {
            val taskListType = object : TypeToken<MutableList<Task>>() {}.type
            val loadedTasks: List<Task>? = gson.fromJson(existingTasksJson, taskListType)

            // If loadedTasks is not null, update the taskList
            if (loadedTasks != null) {
                taskList.clear() // Clear the existing list
                taskList.addAll(loadedTasks) // Add loaded tasks to the task list
            }
        } else {
            // Handle the case when there are no tasks stored
            Toast.makeText(this, "Click on add button to Add a new birthday", Toast.LENGTH_SHORT).show()
        }

        taskAdapter.notifyDataSetChanged() // Notify the adapter of the changes
    }

    // Override onResume to refresh the task list when returning to MainActivity
    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    // Method to create a notification channel (for Android 8.0 and above)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "birthday_notifications"
            val channelName = "Birthday Notifications"
            val channelDescription = "Channel for birthday notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH

            // Create the NotificationChannel object
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            // Register the channel with the system
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

}