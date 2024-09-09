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
import android.view.SurfaceView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.hunsu.climbfeedback.util.VisualizationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.examples.poseestimation.data.Device
import org.tensorflow.lite.examples.poseestimation.data.Feedback
import org.tensorflow.lite.examples.poseestimation.data.Person
import org.tensorflow.lite.examples.poseestimation.ml.ModelType
import org.tensorflow.lite.examples.poseestimation.ml.MoveNet
import kotlin.math.roundToInt


class VideoActivity : AppCompatActivity() {
    companion object {

    }

    lateinit var frameImageList: List<Bitmap>
    lateinit var framePersonList: MutableList<List<Person>?>

    private var device = Device.CPU

    lateinit var myMovenet: MoveNet

    private lateinit var framesRecyclerView: RecyclerView
    private lateinit var selectedFrameImageView: ImageView
    private lateinit var scoreTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        // 동영상 선택 버튼 클릭 리스너 설정
        framesRecyclerView = findViewById(R.id.framesRecyclerView)
        selectedFrameImageView = findViewById(R.id.selectedFrameImageView)
        scoreTv = findViewById(R.id.tvScore)

        initMovenet();

        val selectVideoButton = findViewById<Button>(R.id.btnSelectVideo)
        selectVideoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            startActivityForResult(intent, PICK_VIDEO_REQUEST)
        }
    }

    var MIN_CONFIDENCE =0.1

    // setRv() 함수 ver.6
    fun setRv(feedbackMessages: List<String>) {
        // observe for first type of slider
        val slider: Slider = findViewById(R.id.slider)
        slider.value = 0f
        slider.valueFrom = 0f
        slider.stepSize = 1.0f
        slider.valueTo = frameImageList.size * 1.0f

        slider.addOnChangeListener { slider, value, fromUser ->
            val index = value.roundToInt()
            val selectedBitmap = frameImageList[index]

            val outputBitmap = framePersonList[index]?.let {
                VisualizationUtils.drawBodyKeypoints(selectedBitmap, it.filter { it.score > MIN_CONFIDENCE }, false)
            }

            // 피드백 메시지 출력
            scoreTv.text = feedbackMessages[index]

            val scaledBitmap = outputBitmap?.let {
                Bitmap.createScaledBitmap(
                    it,
                    selectedFrameImageView.width,
                    (outputBitmap.height * selectedFrameImageView.width / selectedFrameImageView.width),
                    false
                )
            }
            selectedFrameImageView.setImageBitmap(scaledBitmap)
        }

        val framesAdapter = FramesAdapter(frameImageList) { selectedBitmap, index ->
            val outputBitmap = framePersonList[index]?.let {
                VisualizationUtils.drawBodyKeypoints(selectedBitmap, it.filter { it.score > MIN_CONFIDENCE }, false)
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

    // runTF() ver.6
    private fun runTF() {
        val loadingDialog = showLoadingDialog()

        lifecycleScope.launch {
            try {
                framePersonList = mutableListOf()
                val feedbackMessages = mutableListOf<String>()  // 각 프레임에 대한 피드백 메시지를 저장

                for ((index, frame) in frameImageList.withIndex()) {
                    val personList = myMovenet.estimatePoses(frame)
                    val personListMutable = mutableListOf<Person>()
                    personListMutable.addAll(personList)
                    framePersonList.add(personListMutable)

                    // `checkFrame` 호출하여 자세 점수 평가
                    val feedback = if (personList.isNotEmpty()) {
                        // ver.8
                        val feedback = Feedback(personList[0], frameImageList.size)
                        //val feedback = Feedback(personList[0], index)
                        feedback.checkFrame(index)
                    } else {
                        1 // 기본 점수 (문제가 없음)
                    }

                    // 피드백 메시지 생성
                    val feedbackMessage = when (feedback) {
                        -1 -> "사람이 감지되지 않음"
                        1 -> "자세 오류 없음"
                        else -> "자세 오류 발생 (ERROR: $feedback)"
                    }

                    // 피드백 메시지 저장
                    feedbackMessages.add(feedbackMessage)

                    // UI 업데이트를 위한 데이터 저장
                    withContext(Dispatchers.Main) {
                        // Ensure index is within bounds
                        if (index < framePersonList.size) {
                            // RecyclerView에서 각 프레임의 이미지를 업데이트
                            val outputBitmap = framePersonList[index]?.let {
                                VisualizationUtils.drawBodyKeypoints(frame, it.filter { it.score > MIN_CONFIDENCE }, false)
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
                        } else {
                            showError("프레임 리스트의 인덱스 범위를 초과했습니다. Index: $index, Size: ${framePersonList.size}")
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    setRv(feedbackMessages) // 피드백 메시지 리스트를 setRv에 전달
                    loadingDialog.dismiss()
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
        frameImageList=frames

        /// tf 수행
        runTF()

//        val intent = Intent(this , frameimageActivity :: class.java)
//        startActivity(intent)
    }



    private fun showLoadingDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.dialog_loading)
        builder.setCancelable(false)
        return builder.create().apply { show() }
    }


}
