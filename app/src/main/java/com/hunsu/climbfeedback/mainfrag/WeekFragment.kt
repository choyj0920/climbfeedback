package com.hunsu.climbfeedback.mainfrag

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hunsu.climbfeedback.ClimbLogActivity
import com.hunsu.climbfeedback.R
import com.hunsu.climbfeedback.databinding.FragmentWeekBinding
import com.hunsu.climbfeedback.db.data.ClimbingLog
import com.hunsu.climbfeedback.db.data.ClimbingLogViewModel
import com.hunsu.climbfeedback.db.data.TestAddClimbingLogActivity
import com.hunsu.climbfeedback.mainfrag.adapter.AdapterClimbLog
import com.hunsu.climbfeedback.mainfrag.adapter.CalendarVPAdatper
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


class WeekFragment(var selectedDate:Date) : Fragment(), IDateClickListener {

    private var binding: FragmentWeekBinding? =null

    private lateinit var selected_Date: LocalDate
    val viewModel: ClimbingLogViewModel by activityViewModels<ClimbingLogViewModel>()
    lateinit var selectedDayDateFormat:String
    lateinit var weekDayList :List<Date>;
    var dayLogs: MutableList<ClimbingLog>?=null


    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        selected_Date = dateToLocalDate(selectedDate)
        binding=FragmentWeekBinding.inflate(inflater,container,false)

        initUiText()

        return binding!!.root

    }


    fun initUiText(){
        val strDate = SimpleDateFormat("MM.dd E", Locale.KOREAN).format(selectedDate)
        binding!!.tvDate.text=strDate;

        var transFormat = SimpleDateFormat("yyyy-MM-dd",Locale.KOREA)

        selectedDayDateFormat = transFormat.format(selectedDate);
        binding!!.diaryitem.visibility = View.GONE
        dayLogs =viewModel.climbingLogs.get(selectedDayDateFormat);

        if(dayLogs!=null){
            var score =dayLogs!!.sumOf { it.score }/dayLogs!!.size.toDouble()
            binding!!.trackScore.progress=score.roundToInt()
            binding!!.tvScore.text="${score.roundToInt()}점"
            binding!!.todayClimbnum.text="오늘의 등반 (등반 횟수 : ${dayLogs!!.size}회)"

        }
        initRV()
    }

    fun initRV(){

        saveSelectedDate(selected_Date)
        val _dayListAdapter = CalendarVPAdatper(this, this, dateToLocalDate(selectedDate))

        binding!!.rvMonth.apply {
            adapter = _dayListAdapter
            setCurrentItem(Int.MAX_VALUE / 2, false)
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

    fun setCurLog(curindex:Int,log: ClimbingLog){
        binding!!.scoreTitle.text = "등반 #${curindex+1} 안정성 점수"
        binding!!.diaryitem.visibility = View.VISIBLE
        var score = log.score
        binding!!.tvScore.text="${score}점"
        binding!!.trackScore.progress=score



        binding!!.tvPlace.text="${log.location}"
        binding!!.tvTime.text="${log.time}"
       // binding!!.tvFeedback.text="${log.feedback}"
        binding!!.gslot.setBackgroundResource(R.drawable.rounded_box_gray3)
        binding!!.armslot.setBackgroundResource(R.drawable.rounded_box_gray3)
        binding!!.legslot.setBackgroundResource(R.drawable.rounded_box_gray3)
        val numbers = Regex("\\d+").findAll(log.feedback)
            .map {it.value.toInt()}
            .toList()
        when(numbers[0]){
            in 0..33 -> {
                binding!!.legtitle.text = "다리 : 훌륭"
                binding!!.legstb.setImageResource(R.drawable.good)
            }
            in 34..66 -> {
                binding!!.legtitle.text = "다리 : 주의"
                binding!!.legstb.setImageResource(R.drawable.hmm)
            }
            in 67..100 -> {
                binding!!.legtitle.text = "다리 : 경고"
                binding!!.legstb.setImageResource(R.drawable.bad)
            }
        }
        when(numbers[1]){
            in 0..33 -> {
                binding!!.armtitle.text = "팔 : 훌륭"
                binding!!.armstb.setImageResource(R.drawable.good)
            }
            in 34..66 -> {
                binding!!.armtitle.text = "팔 : 주의"
                binding!!.armstb.setImageResource(R.drawable.hmm)
            }
            in 67..100 -> {
                binding!!.armtitle.text = "팔 : 경고"
                binding!!.armstb.setImageResource(R.drawable.bad)
            }
        }
        when(numbers[2]){
            in 0..33 -> {
                binding!!.gtitle.text = "무게중심 : 훌륭"
                binding!!.gstb.setImageResource(R.drawable.good)
            }
            in 34..66 -> {
                binding!!.gtitle.text = "무게중심 : 주의"
                binding!!.gstb.setImageResource(R.drawable.hmm)
            }
            in 67..100 -> {
                binding!!.gtitle.text = "무게중심 : 경고"
                binding!!.gstb.setImageResource(R.drawable.bad)
            }
        }



        binding!!.tvLogcontent.text="${log.logContent}"

        binding!!.btnShowDetail.isEnabled=true
        binding!!.btnShowDetail.setOnClickListener {
            val intent = Intent(requireContext(), ClimbLogActivity::class.java)
            intent.putExtra("selectedDateFormat", selectedDayDateFormat)
            intent.putExtra("selectedIndex", curindex)
            startActivity(intent)
        }

    }

    private fun saveSelectedDate(date: LocalDate) {
        val sharedPreference = requireActivity().getSharedPreferences("CALENDAR-APP", AppCompatActivity.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreference.edit()
        editor.putString("SELECTED-DATE", date.toString())
        editor.apply()
    }

    private fun dateFormat(date: LocalDate): String{
        val formatter = DateTimeFormatter.ofPattern("MM.dd E", Locale.KOREAN)
        return date.format(formatter)
    }

    override fun onClickDate(date: LocalDate) {
        selected_Date = date
        saveSelectedDate(date)
        binding!!.btnShowDetail.isEnabled=false
        binding!!.tvDate.text = dateFormat(date)
        binding!!.diaryitem.visibility = View.GONE
        binding!!.gslot.setBackgroundResource(0)
        binding!!.armslot.setBackgroundResource(0)
        binding!!.legslot.setBackgroundResource(0)
        binding!!.legstb.setImageResource(0)
        binding!!.armstb.setImageResource(0)
        binding!!.gstb.setImageResource(0)
        binding!!.armtitle.text = ""
        binding!!.legtitle.text = ""
        binding!!.gtitle.text = ""
        selectedDayDateFormat = date.toString()  // selectedDate를 String 형식으로 변환
        dayLogs = viewModel.climbingLogs.get(selectedDayDateFormat)
        binding!!.scoreTitle.text = "일일 평균 안정성 점수"
        binding!!.todayClimbnum.text="오늘의 등반"
        if (dayLogs != null) {
            val score = dayLogs!!.sumOf { it.score } / dayLogs!!.size.toDouble()
            binding!!.trackScore.progress = score.roundToInt()
            binding!!.tvScore.text = "${score.roundToInt()}점"
            binding!!.todayClimbnum.text="오늘의 등반 (등반 횟수 : ${dayLogs!!.size}회)"
        }
        val logListManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        var thisDayLogs=viewModel.climbingLogs.get(selectedDayDateFormat)
        if(thisDayLogs != null){
            binding!!.rvLogs.visibility = View.VISIBLE
            val _logListAdapter = AdapterClimbLog(this, thisDayLogs)
            binding!!.rvLogs.apply {
                layoutManager=logListManager
                adapter=_logListAdapter
            }
        }
        else{

            binding!!.trackScore.progress = 0
            binding!!.tvScore.text = " "
            binding!!.rvLogs.visibility = View.INVISIBLE
            binding!!.tvPlace.text="- memo1"
            binding!!.tvTime.text="- memo2"
            //binding!!.tvFeedback.text="- memo3"
            binding!!.tvLogcontent.text="- memo4"

        }

    }

    fun dateToLocalDate(date: Date): LocalDate {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }



}