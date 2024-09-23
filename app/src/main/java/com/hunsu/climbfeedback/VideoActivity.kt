/* VideoActivity.kt */

package com.hunsu.climbfeedback

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.hunsu.climbfeedback.db.ClimbingLogDatabaseHelper
import com.hunsu.climbfeedback.util.VisualizationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.examples.poseestimation.data.Device
import org.tensorflow.lite.examples.poseestimation.data.Feedback
import org.tensorflow.lite.examples.poseestimation.data.Person
import org.tensorflow.lite.examples.poseestimation.ml.ModelType
import org.tensorflow.lite.examples.poseestimation.ml.MoveNet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random
import kotlin.math.roundToInt


class VideoActivity : AppCompatActivity() {
    companion object {

    }

    lateinit var frameImageList: List<Bitmap>
    lateinit var framePersonList: MutableList<List<Person>?>

    lateinit var feedback : Feedback

    private var device = Device.CPU

    lateinit var myMovenet: MoveNet

    private lateinit var framesRecyclerView: RecyclerView
    private lateinit var selectedFrameImageView: ImageView
    private lateinit var scoreTv: TextView

    private lateinit var btnNext : Button
    private lateinit var btnBack : Button

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        progressBar=findViewById(R.id.progressBar)
        framesRecyclerView = findViewById(R.id.framesRecyclerView)
        selectedFrameImageView = findViewById(R.id.selectedFrameImageView)
        scoreTv = findViewById(R.id.tvScore)

        initMovenet();

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startActivityForResult(intent, PICK_VIDEO_REQUEST)

        btnNext = findViewById(R.id.btnNext)
        btnNext.setOnClickListener {
            showSaveConfirmationDialog(this)
        }

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

    var MIN_CONFIDENCE =0.1


    fun setRv(feedbackMessages: List<String>) {
        // observe for first type of slider
        val slider: Slider = findViewById(R.id.slider)
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
                scoreTv.text = feedbackMessages[index]

                val scaledBitmap = outputBitmap?.let {
                    Bitmap.createScaledBitmap(
                        it,
                        selectedFrameImageView.width,
                        (outputBitmap.height * selectedFrameImageView.width / outputBitmap.width),
                        false
                    )
                }
                selectedFrameImageView.setImageBitmap(scaledBitmap)
            }
            else if(index == frameImageList.size){
                // 다 넘기면 저장할건지 물어봐야지
//                showSaveConfirmationDialog()
            }
            else{
                Log.e("VideoActivity", "Invalid index: $index for frameImageList of size ${frameImageList.size}")
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
            scoreTv.text = feedbackMessages[index]

            val scaledBitmap = outputBitmap?.let {
                Bitmap.createScaledBitmap(
                    it,
                    selectedFrameImageView.width,
                    (outputBitmap.height * selectedFrameImageView.width / outputBitmap.width),
                    false
                )
            }
            selectedFrameImageView.setImageBitmap(scaledBitmap)
        }

        framesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = framesAdapter
        }
    }

    private val PICK_VIDEO_REQUEST = 1

    // +) 오류 알려주는 함수
    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }


    private fun runTF() {
        val loadingDialog = showLoadingDialog()

        lifecycleScope.launch {
            try {
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

                            val scaledBitmap = outputBitmap?.let {
                                Bitmap.createScaledBitmap(
                                    it,
                                    selectedFrameImageView.width,
                                    (it.height * selectedFrameImageView.width / it.width),
                                    false
                                )
                            }

                            selectedFrameImageView.setImageBitmap(scaledBitmap)
                        }
                        else {
                            showError("프레임 리스트의 인덱스 범위를 초과했습니다. Index: $index, Size: ${framePersonList.size}")
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    setRv(feedbackMessages) // 피드백 메시지 리스트를 setRv에 전달
                    loadingDialog.dismiss()
                    btnNext.visibility = Button.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showError("오류가 발생했습니다: ${e.localizedMessage}")
                    loadingDialog.dismiss()
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_VIDEO_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // 여기서 uri로 선택된 영상의 경로를 얻고 처리합니다.
                processVideoUri(uri)
            }
        }
    }


    fun initMovenet(){
        myMovenet= MoveNet.create(this, device, ModelType.Thunder);
    }


    fun extractFrames(context: Context, videoUri: Uri, frameIntervalMs: Long): List<Bitmap> {
        val retriever = MediaMetadataRetriever()
        val frames = mutableListOf<Bitmap>()

        try {
            val fileDescriptor = context.contentResolver.openFileDescriptor(videoUri, "r")?.fileDescriptor
            fileDescriptor?.let {
                retriever.setDataSource(it)

                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                var currentTime = 0L

                while (currentTime < duration) {
                    val frame = retriever.getFrameAtTime(currentTime * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    frames.add(frame!!);
                    currentTime += frameIntervalMs
                }
            }
        } finally {
            retriever.release()
        }

        return frames
    }


    private fun processVideoUri(uri: Uri) {
        val frames = extractFrames(this, uri, 300)
        frameImageList = frames

        /// tf 수행
        runTF()
    }


    private fun showLoadingDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.dialog_loading)
        builder.setCancelable(false)
        return builder.create().apply { show() }
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

    private fun showSaveConfirmationDialog(context: Context) {
        AlertDialog.Builder(this)
            .setTitle("저장 확인")
            .setMessage("피드백 데이터를 저장하시겠습니까?")
            .setPositiveButton("예") { dialog, _ ->
                // 데이터를 저장하는 함수 호출 ~~ 09.16 이후로 해야함
                saveFeedbackData(context = context)
                dialog.dismiss()
            }
            .setNegativeButton("아니오") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .create()
            .show()
    }

    private fun saveFeedbackData(context: Context) {

        progressBar.visibility= View.VISIBLE

        MainActivity.mainactivity.viewModel.addClimbingLog(
            context,
            dbHelper = ClimbingLogDatabaseHelper(this),
            date = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Calendar.getInstance().time),
            time = "14:00",
            location = "place${Random().nextInt()}",
            feedback = "feedback",
            logContent = "------",
            score = 55,
            climbingImageList = frameImageList,
            shortImageList = listOf(frameImageList[0]), ::closeProgressBar
        )

        Toast.makeText(this, "피드백 데이터가 저장되었습니다.", Toast.LENGTH_SHORT).show()
    }
    fun closeProgressBar(){
        progressBar.visibility=View.GONE
    }
}

