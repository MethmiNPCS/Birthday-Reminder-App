package com.example.labexam3.model

data class Task(
    var name: String,
    var date: String,
    var dueTime: String,
    var year: Int,
    var month: Int,
    var day: Int,
    var hour: Int,
    var minute: Int,
    var isSelected: Boolean = false
)
