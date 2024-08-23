package com.hunsu.climbfeedback.db.data
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hunsu.climbfeedback.db.ClimbingLogDatabaseHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.log


class ClimbingLogViewModel : ViewModel() {

    var climbingLogs = mutableMapOf<String,MutableList<ClimbingLog>>()

    private val _updatetime = MutableLiveData<String>("")
    val updatetime: LiveData<String> get() = _updatetime


    fun loadClimbingLogs(dbHelper: ClimbingLogDatabaseHelper) {
        val db = dbHelper.readableDatabase
        val cursor = dbHelper.getAllClimbingLogs(db)

        var count=0;
        val logs = mutableMapOf<String,MutableList<ClimbingLog>>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val time = cursor.getString(cursor.getColumnIndexOrThrow("time"))
                val location = cursor.getString(cursor.getColumnIndexOrThrow("location"))
                val feedback = cursor.getString(cursor.getColumnIndexOrThrow("feedback"))
                val logContent = cursor.getString(cursor.getColumnIndexOrThrow("logContent"))
                val climbingImage = cursor.getBlob(cursor.getColumnIndexOrThrow("climbingImage"))
                val score = cursor.getInt(cursor.getColumnIndexOrThrow("score"))

                val log = ClimbingLog(id, date, time, location, feedback, logContent, climbingImage, score)
                if(logs.containsKey(date)){
                    logs[date]!!.add(log)
                }else{
                    logs[date]= mutableListOf();
                    logs[date]!!.add(log)
                }
                count+=1
            } while (cursor.moveToNext())
        }
        cursor.close()

        climbingLogs = logs;

        db.close()
    }

    fun addClimbingLog(dbHelper: ClimbingLogDatabaseHelper, date: String, time: String, location: String, feedback: String, logContent: String, climbingImage: ByteArray, score: Int){

        val db = dbHelper.writableDatabase

        var id = dbHelper.insertClimbingLog(
            db,
            date = date,
            time = time,
            location = location,
            feedback = feedback,
            logContent = logContent,
            climbingImage = climbingImage,
            score = score

        )

        var currentMap =climbingLogs


        val updatedMap=currentMap.toMutableMap().apply {
            val loglist=this[date]?: mutableListOf()
            loglist.add(ClimbingLog(id.toInt(),
                date = date,
                time = time,
                location = location,
                feedback = feedback,
                logContent = logContent,
                climbingImage = climbingImage,
                score = score ) )
        }



        climbingLogs=updatedMap
        _updatetime.value=Calendar.getInstance().toString()



    }


}
