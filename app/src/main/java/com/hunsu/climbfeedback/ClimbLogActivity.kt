package com.hunsu.climbfeedback

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.slider.Slider
import com.hunsu.climbfeedback.databinding.ActivityClimbLogBinding
import com.hunsu.climbfeedback.db.data.ClimbingLog
import com.hunsu.climbfeedback.db.data.ClimbingLogViewModel
import com.hunsu.climbfeedback.util.VisualizationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.examples.poseestimation.data.Device
import org.tensorflow.lite.examples.poseestimation.data.Feedback
import org.tensorflow.lite.examples.poseestimation.data.Person
import org.tensorflow.lite.examples.poseestimation.ml.ModelType
import org.tensorflow.lite.examples.poseestimation.ml.MoveNet
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import kotlin.math.roundToInt

class ClimbLogActivity : AppCompatActivity() {

    lateinit var frameImageList: List<Bitmap>
    lateinit var framePersonList: MutableList<List<Person>?>
    lateinit var feedback : Feedback
    lateinit var myMovenet: MoveNet
    var MIN_CONFIDENCE =0.1

    private var timer: Timer? = null
    private var isIncreasing = false

    lateinit var viewModel: ClimbingLogViewModel
    private lateinit var binding: ActivityClimbLogBinding
    lateinit var climblog:ClimbingLog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding= ActivityClimbLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var curDateFormat =intent.getStringExtra("selectedDateFormat");
        var curIndex=intent.getIntExtra("selectedIndex",-1);

        viewModel=MainActivity.mainactivity.viewModel;
        myMovenet= MoveNet.create(this, Device.CPU, ModelType.Thunder);




        if(curIndex!=-1){
            loadBitmap(curDateFormat!!,curIndex)
        }
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnStartPause.setOnClickListener {
            clickStart()
        }


    }

    private fun startIncreasingSlider() {
        isIncreasing = true
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (isIncreasing && binding.slider.value < binding.slider.valueTo) {
                    runOnUiThread {
                        binding.slider.value = binding.slider.value + binding.slider.stepSize
                    }
                } else {
                    timer?.cancel()
                    isIncreasing = false
                    runOnUiThread {
                        binding.btnStartPause.text="재생"
                    }
                }
            }
        }, 0, 300) // 0.3초(300밀리초) 간격으로 실행
    }
    fun clickStart(){
        if(isIncreasing){
            timer?.cancel()
            isIncreasing=false;
            binding.btnStartPause.text="재생"

        }else{
            binding.btnStartPause.text="정지"
            
            startIncreasingSlider();
        }

    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun loadBitmap(curdate:String, curIndex:Int) {
        binding.progressBar.visibility= View.VISIBLE


        GlobalScope.launch {
            try {

                climblog = viewModel.climbingLogs.get(curdate)?.get(curIndex)!!;

                val frames = mutableListOf<Bitmap>()

                for( i in 0..climblog.climbingImageSize!!-1){

                    var frame =loadBitmapFromImagesFolder(context = this@ClimbLogActivity, climblog.id,i)
                    if(frame==null){
                        return@launch
                    }
                    frames.add(frame!!)

                }
                frameImageList=frames;


                framePersonList = mutableListOf()
                val feedbackMessages = mutableListOf<String>()  // 각 프레임에 대한 피드백 메시지를 저장

                // Feedback 객체를 영상의 전체 프레임 개수로 초기화
                feedback = Feedback(frameImageList.size)
                feedback.initFeedback()

                for ((index, frame) in frameImageList.withIndex()) {
                    val personList = myMovenet.estimatePoses(frame)
                    val personListMutable = mutableListOf<Person>()
                    personListMutable.addAll(personList)
                    framePersonList.add(personListMutable)

                    // `checkFrame` 호출하여 자세 점수 평가
                    val frameFeedback = if (personList.isNotEmpty()) {
                        feedback.checkFrame(personList[0], index)
                    } else {
                        -1 // 사람이 감지되지 않은 경우
                    }

                    // 피드백 메시지 생성
                    val feedbackMessage = getFeedbackMessage(frameFeedback)

                    // 피드백 메시지 저장
                    feedbackMessages.add(feedbackMessage)

                    // UI 업데이트를 위한 데이터 저장
                    withContext(Dispatchers.Main) {
                        // Ensure index is within bounds
                        if (index < framePersonList.size) {
                            // RecyclerView에서 각 프레임의 이미지를 업데이트
                            val outputBitmap = framePersonList[index]?.let {
                                VisualizationUtils.drawBodyKeypoints(frame, it.filter { it.score > MIN_CONFIDENCE }, false, false)
                            }

                            if(index==0){
                                binding.selectedFrameImageView.setImageBitmap(outputBitmap)


                            }

//                            val scaledBitmap = outputBitmap?.let {
//                                Bitmap.createScaledBitmap(
//                                    it,
//                                    binding.selectedFrameImageView.width,
//                                    (it.height * binding.selectedFrameImageView.width / it.width),
//                                    false
//                                )
//                            }

//                            binding.selectedFrameImageView.setImageBitmap(scaledBitmap)
                        }
                        else {
                            showError("프레임 리스트의 인덱스 범위를 초과했습니다. Index: $index, Size: ${framePersonList.size}")
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    setRv(feedbackMessages) // 피드백 메시지 리스트를 setRv에 전달
                    binding.progressBar.visibility= View.GONE

                    binding!!.tvPlace.text="${climblog.location}"
                    binding!!.tvTime.text="${climblog.time}"
                    binding!!.tvFeedback.text="${climblog.feedback}"
                    binding!!.tvLogcontent.text="${climblog.logContent}"

                    binding.slider



//                    btnNext.visibility = Button.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showError("오류가 발생했습니다: ${e.localizedMessage}")
                    binding.progressBar.visibility= View.GONE
                }
            }
        }
    }
    fun setRv(feedbackMessages: List<String>) {
        // observe for first type of slider
        val slider: Slider = binding.slider
        slider.value = 0f
        slider.valueFrom = 0f
        slider.stepSize = 1.0f
        slider.valueTo = frameImageList.size * 1.0f

        slider.addOnChangeListener { slider, value, fromUser ->
            val index = value.roundToInt()

            if(index >= 0 && index < frameImageList.size) {
                val selectedBitmap = frameImageList[index]

                val outputBitmap = framePersonList[index]?.let {
                    if (feedbackMessages[index] == "자세 오류 없음")
                        VisualizationUtils.drawBodyKeypoints(
                            selectedBitmap,
                            it.filter { it.score > MIN_CONFIDENCE },
                            false,
                            false
                        )
                    else
                        VisualizationUtils.drawBodyKeypoints(
                            selectedBitmap,
                            it.filter { it.score > MIN_CONFIDENCE },
                            false,
                            true
                        )
                }

                // 피드백 메시지 출력
                binding.tvScore.text = feedbackMessages[index]

                val scaledBitmap = outputBitmap?.let {
                    Bitmap.createScaledBitmap(
                        it,
                        binding.selectedFrameImageView.width,
                        (outputBitmap.height * binding.selectedFrameImageView.width / outputBitmap.width),
                        false
                    )
                }
                binding.selectedFrameImageView.setImageBitmap(scaledBitmap)
            }

            else{
                Log.e("ClimblogActivity", "Invalid index: $index for frameImageList of size ${frameImageList.size}")
                //showError("슬라이더 값이 범위를 초과했습니다.")
            }
        }

        val framesAdapter = FramesAdapter(frameImageList) { selectedBitmap, index ->
            val outputBitmap = framePersonList[index]?.let {
                if (feedbackMessages[index] == "자세 오류 없음")
                    VisualizationUtils.drawBodyKeypoints(
                        selectedBitmap,
                        it.filter { it.score > MIN_CONFIDENCE },
                        false,
                        false
                    )
                else
                    VisualizationUtils.drawBodyKeypoints(
                        selectedBitmap,
                        it.filter { it.score > MIN_CONFIDENCE },
                        false,
                        true
                    )
            }

            // 피드백 메시지 출력
            binding.tvScore.text = feedbackMessages[index]

            val scaledBitmap = outputBitmap?.let {
                Bitmap.createScaledBitmap(
                    it,
                    binding.selectedFrameImageView.width,
                    (outputBitmap.height * binding.selectedFrameImageView.width / outputBitmap.width),
                    false
                )
            }
            binding.selectedFrameImageView.setImageBitmap(scaledBitmap)
        }

        binding.framesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false )
            adapter = framesAdapter
        }
    }


    fun loadBitmapFromImagesFolder(context: Context, logId: Int,indexId:Int): Bitmap? {
        // 내부 저장소의 "Images" 폴더 경로 설정

        val imagesDir = File(context.filesDir, "Images")
        // 파일 이름 설정
        var shortBaseFileName= "climb_${logId}"

        val fileName = "${shortBaseFileName}_${indexId}.png"
        val file = File(imagesDir, fileName)

        return try {
            FileInputStream(file).use { fis ->
                BitmapFactory.decodeStream(fis)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun getFeedbackMessage(feedback : Int) : String{
        if(feedback == -1)
            return "사람이 감지되지 않음"
        else if(feedback == 1)
            return "자세 오류 없음"
        else {
            var str = "[ERR]"
            if(feedback % 2 == 0)
                str += " 발 정리하세요."
            if(feedback % 3 == 0)
                str += " 팔을 피세요."
            if(feedback % 5 == 0)
                str += " 무게중심 확인."
            return str
        }
    }

}