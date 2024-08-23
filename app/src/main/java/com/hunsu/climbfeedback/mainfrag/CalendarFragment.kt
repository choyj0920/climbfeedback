package com.hunsu.climbfeedback.mainfrag

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.hunsu.climbfeedback.R
import com.hunsu.climbfeedback.databinding.FragmentCalendarBinding
import com.hunsu.climbfeedback.db.ClimbingLogDatabaseHelper
import com.hunsu.climbfeedback.db.data.ClimbingLog
import com.hunsu.climbfeedback.db.data.ClimbingLogViewModel
import com.hunsu.climbfeedback.mainfrag.adapter.AdapterMonth
import java.text.SimpleDateFormat
import java.util.Calendar


class CalendarFragment : Fragment() {

    private var binding: FragmentCalendarBinding? =null
    var curpo:Int=0
    val snap = PagerSnapHelper()
    var lastcalendar:Calendar=Calendar.getInstance()
    var curCalendar:Calendar=Calendar.getInstance()
    var curCal=Calendar.getInstance()

    val viewModel: ClimbingLogViewModel by activityViewModels<ClimbingLogViewModel>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentCalendarBinding.inflate(inflater,container,false)

        getData()

        init_rv()
        setBottomText();



        return binding!!.root

    }

    override fun onResume() {
        super.onResume()
        setBottomText()
        updaterv()
    }

    override fun onStart() {
        super.onStart()
        setBottomText()
        updaterv()
    }

    fun getData(){

        val dbHelper = ClimbingLogDatabaseHelper(requireContext())

        // ViewModel에 데이터 로드 요청
        viewModel.loadClimbingLogs(dbHelper)

    }

    // ViewModel 클래스 내에서 메서드 정의
    fun setBottomText() {
        val logMap = viewModel.climbingLogs

        val curMonthLogs = mutableListOf<ClimbingLog>()
        val month = SimpleDateFormat("yyyy-MM").format(curCal.time)



        for ((date, logs) in logMap) {
            if (date.startsWith(month)) { // "2024-08"로 시작하는 날짜만 필터링
                curMonthLogs.addAll(logs)
            }
        }
        var avgScore =curMonthLogs.sumOf { it.score }/curMonthLogs.size.toDouble()
        binding!!.tvClimbAvgScore.setText(String.format("%.2f/100",avgScore))
        binding!!.tvClimbCount.setText("${curMonthLogs.size}번")


    }




    fun init_rv(){




        val onScrollListener = object: RecyclerView.OnScrollListener() {
            override fun onScrolled(@NonNull recyclerView: RecyclerView, dx:Int, dy:Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerView.layoutManager != null) {
                    val view = snap.findSnapView(recyclerView.layoutManager)!!
                    val position = recyclerView.layoutManager!!.getPosition(view)
                    if (curpo != position) {
                        curpo = position
                        var center=Int.MAX_VALUE / 2
                        var newcal= Calendar.getInstance()
                        newcal.time=lastcalendar.time
                        newcal.add(Calendar.MONTH, curpo - center)
                        curCalendar =newcal
                        curCal.time=newcal.time

                        val month = SimpleDateFormat("M").format(curCal.time)
                        binding!!.tvMateWithMonth.text="메이트와 함께 한, ${month}월의 클라이밍"


                    }
                }
            }
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

            }
        }

        val snapHelper = PagerSnapHelper()

        binding!!.rvMonth.let {
            it.clearOnScrollListeners()
            it.addOnScrollListener(onScrollListener)
        }
        lastcalendar.time=curCalendar.time
        curCal.time=lastcalendar.time

        updaterv()
        snapHelper.attachToRecyclerView( binding!!.rvMonth)

        viewModel.updatetime.observe(viewLifecycleOwner,Observer{
            setBottomText()
        }

        )

    }
    fun updaterv(){
        val monthListManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val monthListAdapter = AdapterMonth(this)

        binding!!.rvMonth.apply {
            layoutManager = monthListManager
            adapter = monthListAdapter
            scrollToPosition(Int.MAX_VALUE/2)
        }
    }


}