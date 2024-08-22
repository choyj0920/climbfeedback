package com.hunsu.climbfeedback.db.data

data class ClimbingLog(
    val id: Int,
    val date: String,
    val time: String,
    val location: String,
    val feedback: String,
    val logContent: String,
    val climbingImage: ByteArray?,
    val score: Int
)
