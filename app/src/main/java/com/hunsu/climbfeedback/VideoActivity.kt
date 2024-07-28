package com.hunsu.climbfeedback


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.SurfaceView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
    fun setRv(){

        // observe for first type of slider
        val slider: Slider = findViewById(R.id.slider)
        slider.value=0f
        slider.valueFrom=0f
        slider.stepSize=1.0f
        slider.valueTo=frameImageList.size*1.0f

        slider.addOnChangeListener { slider, value, fromUser ->
            var index=value.roundToInt()
            var selectedBitmap =frameImageList[index]

            val outputBitmap = framePersonList[index]?.let {
                VisualizationUtils.drawBodyKeypoints(selectedBitmap, it.filter { it.score > MIN_CONFIDENCE }, false)


            }
            if( framePersonList[index] !=null){
                scoreTv.text= framePersonList[index]!!.first().score.toString();
            }


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


        val framesAdapter = FramesAdapter(frameImageList) { selectedBitmap,index ->

            val outputBitmap = framePersonList[index]?.let {
                VisualizationUtils.drawBodyKeypoints(selectedBitmap, it.filter { it.score > MIN_CONFIDENCE }, false)


            }
            if( framePersonList[index] !=null){
                scoreTv.text= framePersonList[index]!!.first().score.toString();
            }


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

        framesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = framesAdapter
        }
    }

    private val PICK_VIDEO_REQUEST = 1



    private fun runTF(){
        val loadingDialog = showLoadingDialog()

        GlobalScope.launch {

            framePersonList= mutableListOf()

            for(frame in frameImageList){
                var person = myMovenet.estimatePoses(frame)
                var personList:MutableList<Person>?  = mutableListOf<Person>()
                personList!!.addAll(person)
                if(personList.isEmpty()){
                    personList=null
                }
                framePersonList.add(personList);


            }

            withContext(Dispatchers.Main){
                setRv()


                loadingDialog.dismiss()
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
