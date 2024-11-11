package com.hunsu.climbfeedback.mainfrag.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.hunsu.climbfeedback.R
import com.hunsu.climbfeedback.databinding.ItemCalendarDaysBinding
import com.hunsu.climbfeedback.databinding.ItemClimblogBinding
import com.hunsu.climbfeedback.databinding.ItemWeekDaysBinding
import com.hunsu.climbfeedback.db.data.ClimbingLog
import com.hunsu.climbfeedback.db.data.TestAddClimbingLogActivity
import com.hunsu.climbfeedback.mainfrag.CalendarFragment
import com.hunsu.climbfeedback.mainfrag.WeekFragment
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt


///
class AdapterClimbLog(
    var parent: WeekFragment,
    val logList: MutableList<ClimbingLog>
): RecyclerView.Adapter<AdapterClimbLog.DayView>() {

    class DayView(val binding: ItemClimblogBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayView {

        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_climblog, parent, false)

        return DayView(ItemClimblogBinding.bind(view))
    }
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: DayView, position: Int) {

        var thisLog=logList[position];
        if(thisLog.shortImageSize!=null){
            Log.d("TAG","[${position}] this shotimagesize${thisLog.shortImageSize}")
            holder.binding.frameImageView.setImageBitmap( loadBitmapFromImagesFolder(context = parent.requireContext(),thisLog.id ))

        }else{
            Log.d("TAG","[${position}]this shotimagesize=> null ${thisLog.shortImageSize}")

        }

        holder.binding.frameImageView.setOnClickListener{
            parent.setCurLog(position,thisLog)
        }
    }

    fun loadBitmapFromImagesFolder(context: Context, insertId: Int): Bitmap? {
        // 내부 저장소의 "Images" 폴더 경로 설정

        val imagesDir = File(context.filesDir, "Images")
        // 파일 이름 설정
        var shortBaseFileName= "short_${insertId}"

        val fileName = "${shortBaseFileName}_0.png"
        val file = File(imagesDir, fileName)

        return try {
            FileInputStream(file).use { fis ->
                BitmapFactory.decodeStream(fis)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return logList.size
    }


}