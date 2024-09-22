package com.hunsu.climbfeedback.mainfrag.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.hunsu.climbfeedback.R
import com.hunsu.climbfeedback.databinding.ItemCalendarDaysBinding
import com.hunsu.climbfeedback.databinding.ItemWeekDaysBinding
import com.hunsu.climbfeedback.db.data.ClimbingLog
import com.hunsu.climbfeedback.db.data.TestAddClimbingLogActivity
import com.hunsu.climbfeedback.mainfrag.CalendarFragment
import com.hunsu.climbfeedback.mainfrag.WeekFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt


///
class AdapterWeekDay(
    var parent: WeekFragment,
    val dayList: List<Date>,
    val logMap: Map<String, MutableList<ClimbingLog>>?
): RecyclerView.Adapter<AdapterWeekDay.DayView>() {
    val ROW = 1

    class DayView(val binding: ItemWeekDaysBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayView {

        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_week_days, parent, false)

        return DayView(ItemWeekDaysBinding.bind(view))
    }
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: DayView, position: Int) {


        var transFormat = SimpleDateFormat("yyyy-MM-dd")
        var today = transFormat.format(Calendar.getInstance().time);

        var curDay = transFormat.format(dayList[position]);




//        if(tempMonth != dayList.get(position).month) {
//            holder.binding.ivDate.isInvisible=true
//            holder.binding.tvDate.isInvisible=true
//            holder.binding.track.visibility=View.INVISIBLE
//
//        }

        holder.binding.tvDate.setOnClickListener {

        }


        holder.binding.tvDate.text = dayList[position].date.toString()


        if(parent.selectedDayDateFormat==curDay){
            holder.binding.tvDate.setTextColor(Color.parseColor("#FFFFFFFF"))
            var logList= logMap?.get(curDay)
            if(logList!=null && logList!!.size!=0){

                var score =logList.sumOf { it.score }/logList.size.toDouble()
                holder.binding.track.progress=score.roundToInt()
                holder.binding.track.visibility= View.VISIBLE

            }


        }
        else{




        }



    }



    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return ROW * 7
    }
}