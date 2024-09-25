package com.hunsu.climbfeedback.mainfrag

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.hunsu.climbfeedback.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class DiaryFragment() : Fragment() {

    private lateinit var btnCalendar: ImageButton
    private lateinit var btnTime: ImageButton
    private lateinit var btnSubmit: Button
    private lateinit var inputDate: TextView
    private lateinit var inputTime: TextView
    private lateinit var inputLocation: EditText
    private lateinit var inputMemo: EditText

    private var calendar = Calendar.getInstance()
    private var year = calendar.get(Calendar.YEAR)
    private var month = calendar.get(Calendar.MONTH)
    private var day = calendar.get(Calendar.DAY_OF_MONTH)

    private val hour = calendar.get(Calendar.HOUR_OF_DAY)
    private val minute = calendar.get(Calendar.MINUTE)

    interface OnInputListener {
        fun onInputReceived(date: String, time: String, location: String, memo: String,)
    }

    var listener: OnInputListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnInputListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary, container, false)

        inputDate = view.findViewById(R.id.inputDate)
        inputTime = view.findViewById(R.id.inputTime)
        inputLocation = view.findViewById(R.id.inputLocation)
        inputMemo = view.findViewById(R.id.inputMemo)
        btnCalendar = view.findViewById(R.id.btnCalendar)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        btnTime = view.findViewById(R.id.btnTime)

        inputDate.text = String.format("%04d-%02d-%02d", year, month + 1, day)
        inputTime.text = String.format("%02d시 %02d분", hour, minute)

        btnCalendar.setOnClickListener {
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, day ->
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                inputDate.text = formattedDate
            }, year, month, day)
            datePickerDialog.show()
        }

        btnTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(requireContext(),android.R.style.Theme_Holo_Light_Dialog_NoActionBar, { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d시 %02d분", selectedHour, selectedMinute)
                inputTime.text = formattedTime
            }, hour, minute, true)

            timePickerDialog.show()
        }



        btnSubmit.setOnClickListener {

            val date = inputDate.text.toString()
            val time = inputTime.text.toString()
            val location = inputLocation.text.toString()
            val memo = inputMemo.text.toString()

            listener?.onInputReceived(date, time, location, memo)
        }

        return view
    }
}