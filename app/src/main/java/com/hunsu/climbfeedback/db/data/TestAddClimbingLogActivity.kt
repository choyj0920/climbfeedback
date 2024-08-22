package com.hunsu.climbfeedback.db.data

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import com.hunsu.climbfeedback.MainActivity
import com.hunsu.climbfeedback.databinding.ActivityTestAddClimbingLogBinding
import com.hunsu.climbfeedback.db.ClimbingLogDatabaseHelper

class TestAddClimbingLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestAddClimbingLogBinding
    private lateinit var dbHelper: ClimbingLogDatabaseHelper

    lateinit var viewModel: ClimbingLogViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel=MainActivity.mainactivity.viewModel;
        binding=ActivityTestAddClimbingLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedDate = intent.getStringExtra("selectedDate") ?: ""

        binding.etDate.setText(selectedDate)



        dbHelper = ClimbingLogDatabaseHelper(this)

    }
    fun saveClimbingLog(view: View) {
        val date = binding.etDate.text.toString()
        val time = binding.etTime.text.toString()
        val location = binding.etLocation.text.toString()
        val feedback = binding.etFeedback.text.toString()
        val logContent = binding.etLogContent.text.toString()
        val score = binding.etScore.text.toString().toIntOrNull() ?: 0

        // 빈 값을 체크하고, 저장 로직을 수행
        if (date.isNotEmpty() && logContent.isNotEmpty()) {
            val climbingImage: ByteArray = ByteArray(0) // 예시로 빈 이미지 사용

            viewModel.addClimbingLog(
                dbHelper,
                date = date,
                time = time,
                location = location,
                feedback = feedback,
                logContent = logContent,
                climbingImage = climbingImage,
                score = score
            )





            Toast.makeText(this, "일지가 저장되었습니다.", Toast.LENGTH_SHORT).show()
            finish() // 현재 액티비티 종료 (MainActivity로 돌아감)
        } else {
            Toast.makeText(this, "날짜, 시간, 일지 내용은 필수입니다.", Toast.LENGTH_SHORT).show()
        }
    }
}