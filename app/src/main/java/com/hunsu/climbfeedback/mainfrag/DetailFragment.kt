package com.hunsu.climbfeedback.mainfrag

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.hunsu.climbfeedback.R

class DetailFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView: ImageView = view.findViewById(R.id.imageView)
        val textView: TextView = view.findViewById(R.id.textView)
        // Fragment arguments로부터 데이터 가져오기
        val imageResId = requireArguments().getInt("IMAGE_RES_ID")
        val text = requireArguments().getString("TEXT")

        // 데이터 설정
        imageView.setImageDrawable(requireContext().getDrawable(imageResId))
        textView.text = text
        //textView.text = text
        if (text == "클라이밍 입문 지식") {
            val text1 = getString(R.string.climbing_tutorial)
            textView.text = HtmlCompat.fromHtml(text1, HtmlCompat.FROM_HTML_MODE_COMPACT)
        } else if (text == "기본 용어 및 기술") {
            val text1 = getString(R.string.climbing_tutorial_5)
            textView.text = HtmlCompat.fromHtml(text1, HtmlCompat.FROM_HTML_MODE_COMPACT)
        } else if (text == "안전 에티켓") {
            val text1 = getString(R.string.climbing_tutorial_9)
            textView.text = HtmlCompat.fromHtml(text1, HtmlCompat.FROM_HTML_MODE_COMPACT)
        }
    }
}