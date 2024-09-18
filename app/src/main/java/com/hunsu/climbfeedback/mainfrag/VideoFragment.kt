package com.hunsu.climbfeedback.mainfrag

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hunsu.climbfeedback.VideoActivity
import com.hunsu.climbfeedback.databinding.FragmentVideoBinding

class VideoFragment : Fragment() {

    private var binding: FragmentVideoBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoBinding.inflate(inflater, container, false)

        // 버튼 클릭 시 VideoActivity로 이동
        binding?.selectButton?.setOnClickListener {
            val intent = Intent(activity, VideoActivity::class.java)
            startActivity(intent)
        }

        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
