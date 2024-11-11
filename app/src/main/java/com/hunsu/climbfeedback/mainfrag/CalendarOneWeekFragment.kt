package com.hunsu.climbfeedback.mainfrag

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.hunsu.climbfeedback.R
import com.hunsu.climbfeedback.databinding.FragmentOneWeekBinding
import java.time.LocalDate

class CalendarOneWeekFragment : Fragment() {
    private lateinit var binding: FragmentOneWeekBinding
    private lateinit var textViewList: List<TextView>
    private lateinit var dates: List<LocalDate>
    private lateinit var selected_Date: LocalDate
    private var position: Int = 0
    private lateinit var onClickListener: IDateClickListener

    private val todayPosition = Int.MAX_VALUE / 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOneWeekBinding.inflate(inflater)

        Log.d("CalendarFrag", "onCreateView()")
        initViews()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstance: Bundle?){
        super.onViewCreated(view, savedInstance)

        // 달력 페이지 넘겼을 때의 기준 날짜를 받아오기 위함
        val newDate = calculateNewDate()

        calculateDatesOfWeek(newDate)
        Log.d("CalendarFrag", "onViewCreated()")
        Log.d("Calendar", "newDate = $newDate")
        Log.d("Calendar", "initialDates = $dates")

        setOneWeekDateIntoTextView()
    }

    override fun onResume() {
        super.onResume()

        Log.d("CalendarFrag", "onResume()")
        setPrevSelectedDate()
        // 선택 시 동작
        Log.d("Calendar", "dates = $dates")
    }

    override fun onPause() {
        super.onPause()
        Log.d("CalendarFrag", "onPause()")
        resetUi()
    }

    private fun initViews() {
        with(binding) {
            textViewList = listOf( // 텍스트뷰 리스트 초기화
                tv1, tv2, tv3, tv4, tv5, tv6, tv7
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateNewDate(): LocalDate {
        val curDate = selected_Date
        return if (position < todayPosition){ // 이전 페이지로 스크롤
            curDate.minusDays(((todayPosition - position) * 7).toLong())
        } else if (position > todayPosition) { // 다음 페이지로 스크롤
            curDate.plusDays(((position - todayPosition) * 7).toLong())
        } else {
            curDate
        }
    }

    private fun setPrevSelectedDate() {
        val sharedPreference = context?.getSharedPreferences("CALENDAR-APP", Context.MODE_PRIVATE)
        val selectedDate = sharedPreference?.getString("SELECTED-DATE", "")
        for (i in textViewList.indices) {
            if (selectedDate.toString() == dates[i].toString()) {
                setSelectedDate(textViewList[i], LocalDate.parse(selectedDate) < LocalDate.now())
            }
        }
    }

    private fun setOneWeekDateIntoTextView() { // 일주일의 날짜를 넣어주기
        for (i in textViewList.indices) {
            setDate(textViewList[i], dates[i])
        }
    }

    private fun setDate(textView: TextView, date: LocalDate) { // ex. date: 2023-12-23
        val splits = date.toString().split('-')

        textView.text = splits[2].toInt().toString() // 날짜의 뒷 부분(일)만 가져와서 사용
        // 날짜 선택 시의 동작
        textView.setOnClickListener{
            resetUi() // 모든 날짜 선택 해제
            onClickListener.onClickDate(date) // 인터페이스를 통해 클릭한 날짜 전달
            setSelectedDate(textView, date < LocalDate.now())
        }
    }

    private fun resetUi() { // 모든 날짜 선택 해제
        for (i in textViewList.indices) {
            textViewList[i].setTextColor(Color.WHITE)
            textViewList[i].setTypeface(null, Typeface.NORMAL)
            textViewList[i].backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.transparent))
        }
    }

    private fun setSelectedDate(textView: TextView, isPast: Boolean) { // 선택한 날짜 UI
        // 배경색
        if (isPast) textView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.text_alpha_gray))
        else textView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.neon_mint))
        // 글자색
        textView.setTextColor(Color.BLACK)
        // 볼드체
        textView.setTypeface(textView.typeface, Typeface.BOLD)
    }

    private fun calculateDatesOfWeek(today: LocalDate) { // 최초 주간 달력 날짜들을 표시하기 위함
        val dates = ArrayList<LocalDate>() // 일 ~ 토까지 한 주간의 날짜 리스트 추가하기
        val dayOfToday = today.dayOfWeek.value
        Log.e("Calendar", "dayOfToday: $dayOfToday")

        if (dayOfToday == SUNDAY) { // 일요일일 경우 다음 리스트를 받아와야 함 (일요일을 가장 먼저 표시하기 때문)
            for (day in MONDAY..SUNDAY) { // 일(오늘) ~ 그 다음주 토
                dates.add(today.plusDays((day - 1).toLong()))
            }
        } else {
            for (day in (MONDAY - 1) until  dayOfToday) { // 일 ~ 오늘
                dates.add(today.minusDays((dayOfToday - day).toLong()))
            }
            for (day in dayOfToday .. (SUNDAY - 1)) { // 오늘 ~ 토
                dates.add(today.plusDays((day - dayOfToday).toLong()))
            }
        }
        this.dates =  dates
    }

    companion object {
        fun newInstance(position: Int, onClickListener: IDateClickListener, selected_Date: LocalDate): CalendarOneWeekFragment {
            val fragment = CalendarOneWeekFragment()
            fragment.position = position
            fragment.onClickListener = onClickListener
            fragment.selected_Date = selected_Date
            return fragment
        }
        const val MONDAY = 1
        const val SUNDAY = 7
    }
}