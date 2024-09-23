package com.hunsu.climbfeedback.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ClimbingLogDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    val createTableQuery = """
    CREATE TABLE ClimbingLog (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        date TEXT NOT NULL,
        time TEXT ,
        location TEXT,
        feedback TEXT,
        logContent TEXT,
        climbingImage INTEGER,
        shortImage INTEGER,
        score INTEGER
    )
""".trimIndent()

    companion object {
        private const val DATABASE_NAME = "climbingLogs.db"
        private const val DATABASE_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 테이블 생성
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS ClimbingLog")
        onCreate(db)
    }


    fun insertClimbingLog( /// 등반기록 db저장 ,이미지저장
        context: Context,
        db: SQLiteDatabase,
        date: String,
        time: String,
        location: String,
        feedback: String,
        logContent: String,
        score: Int,
        climbingImageList: List<Bitmap>?,
        shortImageList: List<Bitmap>?,
        callBack: (() -> Unit)?
    ): Long {

        val contentValues = ContentValues().apply {
            put("date", date)
            put("time", time)
            put("location", location)
            put("feedback", feedback)
            put("logContent", logContent)
            if(climbingImageList !=null && shortImageList !=null){
                put("climbingImage", climbingImageList?.size)
                put("shortImage", shortImageList?.size)
            }

            put("score", score)
        }
        var result = db.insert("ClimbingLog", null, contentValues)


        saveBitmapList(context,climbingImageList,shortImageList,result,callBack)

        return result
    }

    fun saveBitmapList(context: Context, climbingImageList: List<Bitmap>?, shortImageList:  List<Bitmap>?, insertId: Long,callBack: (() -> Unit)?) {///이미지 파일에 저장함수 + callback 함수 넣으면 다 저장후 실행
        if(shortImageList==null || climbingImageList==null){
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imagesDir = File(context.filesDir, "Images")

                // 폴더가 존재하지 않으면 생성
                if (!imagesDir.exists()) {
                    imagesDir.mkdir()
                }
                Log.d("TAG","---------------saveClimb---------------")

                var climbBaseFileName= "climb_${insertId}"

                climbingImageList.forEachIndexed { index, bitmap ->
                    Log.d("TAG","---------------saveClimb-[${index}]--------------")

                    val fileName = "${climbBaseFileName}_$index.png"
                    val file = File(imagesDir, fileName)
                    try {
                        FileOutputStream(file).use { fos ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                var shortBaseFileName= "short_${insertId}"

                Log.d("TAG","---------------saveShort---------------")

                shortImageList.forEachIndexed { index, bitmap ->
                    Log.d("TAG","---------------saveShort-[${index}]--------------")

                    val fileName = "${shortBaseFileName}_$index.png"
                    val file = File(imagesDir, fileName)

                    try {
                        FileOutputStream(file).use { fos ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }





            } catch (e: IOException) {
                e.printStackTrace()
            }
            if(callBack!=null){
                withContext(Dispatchers.Main){
                    callBack()
                }
            }

        }

    }


    /// 데이터 조회 예제

    fun getAllClimbingLogs(db: SQLiteDatabase): Cursor {
        return db.query("ClimbingLog", null, null, null, null, null, "date DESC")
    }

}