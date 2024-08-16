package com.hunsu.climbfeedback.mainfrag

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hunsu.climbfeedback.R
import com.hunsu.climbfeedback.databinding.FragmentBookBinding


class BookFragment : Fragment() {

    private var binding: FragmentBookBinding? =null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentBookBinding.inflate(inflater,container,false)

        return binding!!.root

    }


}