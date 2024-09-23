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
import com.hunsu.climbfeedback.db.data.ClimbingLog
import com.hunsu.climbfeedback.db.data.TestAddClimbingLogActivity
import com.hunsu.climbfeedback.mainfrag.CalendarFragment
import com.hunsu.climbfeedback.mainfrag.WeekFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class AdapterDay(
    var parent: CalendarFragment,
    val tempMonth:Int, val dayList: MutableList<Date>,
    val logMap: Map<String, MutableList<ClimbingLog>>?
): RecyclerView.Adapter<AdapterDay.DayView>() {
    val ROW = 6

    class DayView(val binding: ItemCalendarDaysBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayView {

        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_days, parent, false)

        return DayView(ItemCalendarDaysBinding.bind(view))
    }
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: DayView, position: Int) {
        holder.binding.ivDate .setOnClickListener {

        }

        var transFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        var today = transFormat.format(Calendar.getInstance().time);

        var curDay = transFormat.format(dayList[position]);




        if(tempMonth != dayList.get(position).month) {
            holder.binding.ivDate.isInvisible=true
            holder.binding.tvDate.isInvisible=true
            holder.binding.track.visibility=View.INVISIBLE

        }else{
            holder.binding.tvDate.setOnClickListener {
                val intent = Intent(parent.requireContext(), TestAddClimbingLogActivity::class.java)
                intent.putExtra("selectedDate", curDay)
                parent.startActivity(intent)
            }
            holder.binding.ivDate.setOnClickListener{
                parent.changeNewFrag(
                    WeekFragment(dayList[position])
                )

            }

            var logList= logMap?.get(curDay)
            if(logList!=null && logList!!.size!=0){

                var score =logList.sumOf { it.score }/logList.size.toDouble()
                holder.binding.track.progress=score.roundToInt()
                holder.binding.track.visibility= View.VISIBLE
                holder.binding.ivDate.setImageResource(R.drawable.ic_logo)

            }else{

                holder.binding.ivDate.setImageResource(R.drawable.ic_day_default)
                holder.binding.track.visibility= View.GONE

//                holder.binding.ivDate.setImageResource(R.drawable.ic_logo)
//                holder.binding.track.progress=70
//
//                holder.binding.track.visibility= View.VISIBLE

            }


            if(today==curDay){
                holder.binding.tvDate.setBackgroundResource(R.drawable.rounded_box_mint)
                holder.binding.tvDate.text = "오늘"
                holder.binding.tvDate.setTextColor(Color.parseColor("#FF000000"))



            }
            else{

                holder.binding.tvDate.setBackgroundColor(0x00000000)
                holder.binding.tvDate.setTextColor(Color.parseColor("#FFffffff"))


                holder.binding.tvDate.text = dayList[position].date.toString()

            }

        }




    }



    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return ROW * 7
    }
}