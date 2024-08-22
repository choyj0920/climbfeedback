package com.hunsu.climbfeedback.mainfrag.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.hunsu.climbfeedback.R
import com.hunsu.climbfeedback.databinding.ItemCalendarDaysBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class AdapterDay(val tempMonth:Int, val dayList: MutableList<Date>): RecyclerView.Adapter<AdapterDay.DayView>() {
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

        var transFormat = SimpleDateFormat("yyyy-MM-dd")
        var today = transFormat.format(Calendar.getInstance().time);

        var to = transFormat.format(dayList[position]);


        if(tempMonth != dayList.get(position).month) {
            holder.binding.ivDate.isInvisible=true
            holder.binding.tvDate.isInvisible=true
        }

        if(today==to){
            holder.binding.tvDate.setBackgroundResource(R.drawable.rounded_box_mint)
            holder.binding.tvDate.text = "오늘"
            holder.binding.tvDate.setTextColor(Color.parseColor("#FF000000"))



        }else{

            holder.binding.tvDate.setBackgroundColor(0x00000000)
            holder.binding.tvDate.setTextColor(Color.parseColor("#FFffffff"))


            holder.binding.tvDate.text = dayList[position].date.toString()

        }


    }



    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return ROW * 7
    }
}