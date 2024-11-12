package com.hunsu.climbfeedback.mainfrag.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hunsu.climbfeedback.R
import com.hunsu.climbfeedback.databinding.ItemTodayLogBinding
import com.hunsu.climbfeedback.db.data.ClimbingLog
import com.hunsu.climbfeedback.mainfrag.CalendarFragment
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import kotlin.random.Random


///
class AdapterTodayClimbLog(
    var parent: CalendarFragment,
    val logList: MutableList<ClimbingLog>,
    var todaystr: String
): RecyclerView.Adapter<AdapterTodayClimbLog.DayView>() {

    class DayView(val binding: ItemTodayLogBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayView {

        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_today_log, parent, false)

        return DayView(ItemTodayLogBinding.bind(view))
    }
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: DayView, position: Int) {

        var thisLog=logList[position];
        if(thisLog.shortImageSize!=null){
            Log.d("TAG","[${position}] this shotimagesize${thisLog.shortImageSize}")
            holder.binding.ivTodayImage.setImageBitmap( loadBitmapFromImagesFolder(context = parent.requireContext(),thisLog.id,thisLog.climbingImageSize!! ))

        }else{
            Log.d("TAG","[${position}]this shotimagesize=> null ${thisLog.shortImageSize}")

        }
        holder.binding.tvPlace.text = thisLog.location
        holder.binding.tvTime.text = "${todaystr} ${thisLog.time}"
        holder.binding.tvScore.text = "${thisLog.score} / 100"

    }

    fun loadBitmapFromImagesFolder(context: Context, insertId: Int,imgListSize:Int): Bitmap? {
        // 내부 저장소의 "Images" 폴더 경로 설정

        val imagesDir = File(context.filesDir, "Images")
        // 파일 이름 설정
        var baseFileName= "climb_${insertId}"


        val fileName = "${baseFileName}_${Random.nextInt(imgListSize-1)}.png"
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