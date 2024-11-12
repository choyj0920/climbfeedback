package com.hunsu.climbfeedback.db.data
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hunsu.climbfeedback.db.ClimbingLogDatabaseHelper
import java.util.Calendar


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
                val climbingImageindex = cursor.getInt(cursor.getColumnIndexOrThrow("climbingImage"))
                val shortImageindex = cursor.getInt(cursor.getColumnIndexOrThrow("shortImage"))
                val score = cursor.getInt(cursor.getColumnIndexOrThrow("score"))

                val log = ClimbingLog(id, date, time, location, feedback, logContent,climbingImageindex,shortImageindex,score)
                if(climbingImageindex ==0 || shortImageindex ==0){
                    log.shortImageSize=null
                    log.climbingImageSize=null
                }
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

    ///  clibingImageList->등반 전체 사진 ,
    fun addClimbingLog(
        context: Context,
        dbHelper: ClimbingLogDatabaseHelper,
        date: String,
        time: String,
        location: String,
        feedback: String,
        logContent: String,
        score: Int,
        climbingImageList: List<Bitmap>? = null,
        shortImageList: List<Bitmap>? = null,
        callBack:(() -> Unit)?=null /// callback 함수 입력시 이미지 전부 저장 후 실행
    ){

        val db = dbHelper.writableDatabase

        var id = dbHelper.insertClimbingLog(
            context,
            db,
            date = date,
            time = time,
            location = location,
            feedback = feedback,
            logContent = logContent,
            score = score,climbingImageList=climbingImageList,shortImageList=shortImageList,callBack=callBack

        )

        var currentMap =climbingLogs


        val updatedMap=currentMap.toMutableMap().apply {
            val loglist=this[date]?: mutableListOf()
            loglist.add(
                ClimbingLog(
                    id.toInt(),
                    date = date,
                    time = time,
                    location = location,
                    feedback = feedback,
                    logContent = logContent,
                    climbingImageSize = climbingImageList?.size,
                    shortImageSize = shortImageList?.size,
                    score = score,
                )
            )

        }



        climbingLogs=updatedMap
        _updatetime.value=Calendar.getInstance().toString()



    }


}
