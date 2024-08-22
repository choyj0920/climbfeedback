package com.hunsu.climbfeedback.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class ClimbingLogDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    val createTableQuery = """
    CREATE TABLE ClimbingLog (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        date TEXT NOT NULL,
        time TEXT ,
        location TEXT,
        feedback TEXT,
        logContent TEXT,
        climbingImage BLOB,
        score INTEGER
    )
""".trimIndent()

    companion object {
        private const val DATABASE_NAME = "climbingLogs.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 테이블 생성
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS ClimbingLog")
        onCreate(db)
    }


    fun insertClimbingLog(db: SQLiteDatabase, date: String, time: String, location: String, feedback: String, logContent: String, climbingImage: ByteArray, score: Int): Long {
        val contentValues = ContentValues().apply {
            put("date", date)
            put("time", time)
            put("location", location)
            put("feedback", feedback)
            put("logContent", logContent)
            put("climbingImage", climbingImage)
            put("score", score)
        }

        return db.insert("ClimbingLog", null, contentValues)
    }

    /// 데이터 조회 예제

    fun getAllClimbingLogs(db: SQLiteDatabase): Cursor {
        return db.query("ClimbingLog", null, null, null, null, null, "date DESC")
    }

}