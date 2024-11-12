package com.hunsu.climbfeedback.mainfrag

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hunsu.climbfeedback.ClimbLogActivity
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
        binding!!.scoreTitle.text = "${curindex+1}번 등반 안정성 점수"
        var score = log.score
        binding!!.tvScore.text="${score}점"
        binding!!.trackScore.progress=score
        binding!!.tvPlace.text="${log.location}"
        binding!!.tvTime.text="${log.time}"
        binding!!.tvFeedback.text="${log.feedback}"
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
        binding!!.tvDate.text = dateFormat(date)

        selectedDayDateFormat = date.toString()  // selectedDate를 String 형식으로 변환
        dayLogs = viewModel.climbingLogs.get(selectedDayDateFormat)
        binding!!.scoreTitle.text = "일일 평균 안정성 점수"
        binding!!.tvClimbCount.text = "등반 횟수 : ${if (dayLogs == null) 0 else dayLogs!!.size}"

        if (dayLogs != null) {
            val score = dayLogs!!.sumOf { it.score } / dayLogs!!.size.toDouble()
            binding!!.trackScore.progress = score.roundToInt()
            binding!!.tvScore.text = "${score.roundToInt()}점"
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
            binding!!.tvClimbCount.text = "등반 횟수 : 0"
            binding!!.trackScore.progress = 0
            binding!!.tvScore.text = " "
            binding!!.rvLogs.visibility = View.INVISIBLE
            binding!!.tvPlace.text="- memo1"
            binding!!.tvTime.text="- memo2"
            binding!!.tvFeedback.text="- memo3"
            binding!!.tvLogcontent.text="- memo4"

        }

    }

    fun dateToLocalDate(date: Date): LocalDate {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }



}