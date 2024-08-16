package com.hunsu.climbfeedback.mainfrag

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hunsu.climbfeedback.databinding.FragmentVideoBinding


class VideoFragment : Fragment() {

    private var binding: FragmentVideoBinding? =null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentVideoBinding.inflate(inflater,container,false)

        return binding!!.root

    }


}