package com.hunsu.climbfeedback.mainfrag

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hunsu.climbfeedback.R
import com.hunsu.climbfeedback.databinding.FragmentCalendarBinding


class CalendarFragment : Fragment() {

    private var binding: FragmentCalendarBinding? =null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentCalendarBinding.inflate(inflater,container,false)

        return binding!!.root

    }


}