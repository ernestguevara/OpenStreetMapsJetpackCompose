package com.simplifier.jetpackosm.presentation.util

//fun main() {
//    var timeList = listOf(120.0,58.0,136.0,4000.0,2457.0,11150.0,1243545.0)
//
//    timeList.forEach {
//        println(convertTime(it))
//    }
//}

fun Double.convertDistance(): String {
    return if (this < 1000.0) {
        String.format("%.0f", this) + "m"
    } else {
        String.format("%.1f", this / 1000.0) + "km"
    }
}

fun Double.convertTime(): String {
    val secondsInMinute = 60
    val secondsInHour = 3600
    val secondsInDay = 86400

    return when {
        this < secondsInMinute -> String.format("%.0fs", this)
        this < secondsInHour -> {
            val minutes = (this / secondsInMinute).toInt()
            val seconds = (this % secondsInMinute).toInt()
            if (seconds == 0) "${minutes}m" else "${minutes}m ${seconds}s"
        }
        this < secondsInDay -> {
            val hours = (this / secondsInHour).toInt()
            val minutes = ((this % secondsInHour) / secondsInMinute).toInt()
            if (minutes == 0) "${hours}h" else "${hours}h ${minutes}m"
        }
        else -> {
            val days = (this / secondsInDay).toInt()
            val hours = ((this % secondsInDay) / secondsInHour).toInt()
            if (hours == 0) "${days}d" else "${days}d ${hours}h"
        }
    }
}