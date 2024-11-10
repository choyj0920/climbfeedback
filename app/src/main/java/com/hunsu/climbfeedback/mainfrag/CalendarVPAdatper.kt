package com.hunsu.climbfeedback.mainfrag

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.time.LocalDate

class CalendarVPAdatper(
    fragmentActivity: WeekFragment,
    private val onClickListener: IDateClickListener,
    private val selected_Date: LocalDate,
): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        return CalendarOneWeekFragment.newInstance(position, onClickListener, selected_Date)
    }
}