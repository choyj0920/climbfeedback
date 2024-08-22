package com.hunsu.climbfeedback.mainfrag.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hunsu.climbfeedback.MainActivity
import com.hunsu.climbfeedback.R
import com.hunsu.climbfeedback.databinding.ItemMonthBinding
import com.hunsu.climbfeedback.mainfrag.CalendarFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class AdapterMonth(var parent: CalendarFragment): RecyclerView.Adapter<AdapterMonth.MonthView>() {
    val center = Int.MAX_VALUE / 2
    private var calendar = Calendar.getInstance()


    inner class MonthView(val binding: ItemMonthBinding): RecyclerView.ViewHolder(binding.root){
        var month: TextView =binding.tvMonthLabel
        var llayout=binding.parentlayout
        var rvcalendar: RecyclerView =binding.rvCalendar

        var daylayout=binding.`daynamelayout`


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthView {

        val view : View?

        view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month, parent, false)

        return MonthView(ItemMonthBinding.bind(view))
    }

    override fun onBindViewHolder(holder: MonthView, position: Int) {
        calendar.time = parent.lastcalendar.time
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, position - center)

        val year = SimpleDateFormat("yyyy").format(calendar.time)

        val month = SimpleDateFormat("yyyy.MM").format(calendar.time)

        holder.month.text=month



        val tempMonth = calendar.get(Calendar.MONTH)

        var dayList: MutableList<Date> = MutableList(6 * 7) { Date() }
        for(i in 0..5) {
            for(k in 0..6) {
                calendar.add(Calendar.DAY_OF_MONTH, (1-calendar.get(Calendar.DAY_OF_WEEK)) + k)
                dayList[i * 7 + k] = calendar.time
            }
            calendar.add(Calendar.WEEK_OF_MONTH, 1)
        }

        val dayListManager = GridLayoutManager(this.parent.requireContext(), 7)
        val dayListAdapter = AdapterDay(parent,tempMonth, dayList,parent.viewModel.climbingLogs)




        parent.viewModel.updatetime.observe(parent.viewLifecycleOwner,Observer{
            val _dayListAdapter = AdapterDay(parent,tempMonth, dayList,parent.viewModel.climbingLogs)
            holder.rvcalendar.apply {
                layoutManager=dayListManager
                adapter=_dayListAdapter
            }
        })


    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }
}
