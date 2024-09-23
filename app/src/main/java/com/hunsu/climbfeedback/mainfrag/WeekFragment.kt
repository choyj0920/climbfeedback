package com.hunsu.climbfeedback.mainfrag

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.hunsu.climbfeedback.R
import com.hunsu.climbfeedback.databinding.FragmentCalendarBinding
import com.hunsu.climbfeedback.databinding.FragmentWeekBinding
import com.hunsu.climbfeedback.db.ClimbingLogDatabaseHelper
import com.hunsu.climbfeedback.db.data.ClimbingLog
import com.hunsu.climbfeedback.db.data.ClimbingLogViewModel
import com.hunsu.climbfeedback.mainfrag.adapter.AdapterClimbLog
import com.hunsu.climbfeedback.mainfrag.adapter.AdapterDay
import com.hunsu.climbfeedback.mainfrag.adapter.AdapterMonth
import com.hunsu.climbfeedback.mainfrag.adapter.AdapterWeekDay
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


class WeekFragment(var selectedDate:Date) : Fragment() {

    private var binding: FragmentWeekBinding? =null


    val viewModel: ClimbingLogViewModel by activityViewModels<ClimbingLogViewModel>()
    lateinit var selectedDayDateFormat:String
    lateinit var weekDayList :List<Date>;
    var dayLogs: MutableList<ClimbingLog>?=null


    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        weekDayList=getWeekDates(selectedDate);
        binding=FragmentWeekBinding.inflate(inflater,container,false)

        initUiText()




        return binding!!.root

    }

    fun initUiText(){
        val strDate = SimpleDateFormat("MM.dd E", Locale.KOREAN).format(selectedDate)
        binding!!.tvDate.text=strDate;

        var transFormat = SimpleDateFormat("yyyy-MM-dd",Locale.KOREA)

        selectedDayDateFormat = transFormat.format(selectedDate);

        dayLogs =viewModel.climbingLogs.get(selectedDayDateFormat);

        binding!!.tvClimbCount.text="등반 횟수 : ${if(dayLogs==null)  0 else dayLogs!!.size }"
        if(dayLogs!=null){
            var score =dayLogs!!.sumOf { it.score }/dayLogs!!.size.toDouble()
            binding!!.trackScore.progress=score.roundToInt()
            binding!!.tvScore.text="${score.roundToInt()}점"


        }




        initRV()
    }

    fun initRV(){
        val dayListManager = GridLayoutManager(requireContext(), 7)

        val _dayListAdapter = AdapterWeekDay(this, weekDayList,viewModel.climbingLogs)
        binding!!.rvMonth.apply {
            layoutManager=dayListManager
            adapter=_dayListAdapter
        }

        val logListManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        var thisDayLogs=viewModel.climbingLogs.get(selectedDayDateFormat)
        if(thisDayLogs !=null){
            val _logListAdapter = AdapterClimbLog(this, thisDayLogs)
            binding!!.rvLogs.apply {
                layoutManager=logListManager
                adapter=_logListAdapter
            }
        }


    }


    fun getWeekDates(selectedDate: Date): List<Date> {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate

        // 주의 첫 번째 날로 이동 (월요일로 설정)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        val weekDates = mutableListOf<Date>()

        // 월요일부터 일요일까지의 날짜를 리스트에 추가
        for (i in 0..6) {
            weekDates.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return weekDates
    }

    fun setCurLog(log: ClimbingLog){
        binding!!.tvPlaceTime.text="[${log.location}]  ${log.time}"
        binding!!.tvFeedback.text="${log.feedback}"
        binding!!.tvLogcontent.text="${log.logContent}"
    }



}