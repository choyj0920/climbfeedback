/* Feedback data class */

package org.tensorflow.lite.examples.poseestimation.data

import android.content.Context
import android.graphics.PointF
import com.hunsu.climbfeedback.db.ClimbingLogDatabaseHelper

import kotlin.math.max
import kotlin.math.min
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

const val FEEDBACK_LIST_SIZE = 3    // 자세 판단 시 체크할 항목 개수
const val THRESOLD = 5             // 몇 프레임 이상 지속될 시 피드백할지

data class Feedback(val frame:Int) {

    /////////////
    // * [멤버] //
    /////////////

    // [*] 프레임 개수
    val FRAMES = frame   // 프레임 개수, n

    // [*] feedbackLst => 총 n개의 피드백 사항, i번째 <피드백 발생여부>
    var feedbackList = Array(FRAMES){ Array<Boolean>(FEEDBACK_LIST_SIZE){ false }}

    // [*] count 세는 변수 추가
    var feedbackCount = Array<Int>(FEEDBACK_LIST_SIZE){0}


    init{
        //initFeedback()
    }


    ///////////////
    // # [메소드] //
    ///////////////

    // [#] 피드백리스트 초기화
    fun initFeedback(){ // 피드백 전체 초기화
        feedbackList.forEach { feedbackList ->
            feedbackList.forEachIndexed { index, _ ->
                feedbackList[index] = false
            }
        }
        feedbackCount.forEachIndexed { index, _ ->
            feedbackCount[index] = 0
        }
    }

    // [#] 각 프레임에서의 자세가 올바른지 항목 별로 체크
    fun checkFrame(person : Person, frame_num : Int) : Int {

        var err = 1 // test용 변수

        if(person.keyPoints.isEmpty())
            return -1

        val LEFT_HAND = person.keyPoints.get(9).coordinate
        val RIGHT_HAND = person.keyPoints.get(10).coordinate
        val LEFT_FOOT = person.keyPoints.get(15).coordinate
        val RIGHT_FOOT = person.keyPoints.get(16).coordinate

        val LEFT_SHOULDER = person.keyPoints.get(5).coordinate
        val RIGHT_SHOULDER = person.keyPoints.get(6).coordinate
        val LEFT_ELBOW = person.keyPoints.get(7).coordinate
        val RIGHT_ELBOW = person.keyPoints.get(8).coordinate

        val LEFT_HIP = person.keyPoints.get(11).coordinate
        val RIGHT_HIP = person.keyPoints.get(12).coordinate


        val GP = PointF(    // 무게중심
            (LEFT_HAND.x + RIGHT_HAND.x + 2*(LEFT_HIP.x + RIGHT_HIP.x) + LEFT_FOOT.x + RIGHT_FOOT.x) / 8.0f,
            (LEFT_HAND.y + RIGHT_HAND.y + 2*(LEFT_HIP.y + RIGHT_HIP.y) + LEFT_FOOT.y + RIGHT_FOOT.y) / 8.0f
        )

        // [0] 손 기준 발이 치우쳤는가?
        if(min(LEFT_HAND.x,RIGHT_HAND.x) > max(LEFT_FOOT.x,RIGHT_FOOT.x) ||
            max(LEFT_HAND.x,RIGHT_HAND.x) < min(LEFT_FOOT.x,RIGHT_FOOT.x)){
            err *= 2
            if(++feedbackCount[0] >= THRESOLD){
                for(i in 0 until THRESOLD)
                    feedbackList[frame_num - i][0] = true
            }
        }
        else
            feedbackCount[0] = 0

        // [1] 양 팔이 계속 굽어져있는가?
        if(calculateAngle(LEFT_HAND, LEFT_ELBOW, LEFT_SHOULDER) < 30 &&
            calculateAngle(RIGHT_HAND, RIGHT_ELBOW, RIGHT_SHOULDER) < 30){
            err *= 3
            if(++feedbackCount[1] >= THRESOLD)
                for(i in 0 until THRESOLD)
                    feedbackList[frame_num - i][1] = true
        }
        else
            feedbackCount[1] = 0

        // [2] 무게중심이 삼지점 밖에있는가?
        if(!isPointInQuadrilateral(LEFT_HAND, LEFT_FOOT, RIGHT_FOOT, RIGHT_HAND, GP)){
            err *= 5
            if(++feedbackCount[2] >= THRESOLD)
                for(i in 0 until THRESOLD)
                    feedbackList[frame_num - i][2] = true
        }
        else
            feedbackCount[2] = 0

        // [3] 허벅지:종아리 비율이 차이가 많이나는가?
        // 피드백 사항 추가할지 말지 검토 예정


        return err
    }


    // [#] 피드백 데이터 DB에 저장 및 return으로 가져오기
    // 24.09.09 추가
    fun saveFeedbackData(context: Context) {
        val dbHelper = ClimbingLogDatabaseHelper(context)
        val db = dbHelper.writableDatabase

        //dbHelper.saveFeedbackData(feedbackList)

        db.close()
    }


    // [#] 점 3개 각도 계산해주는 함수
    fun calculateAngle(A: PointF, B: PointF, C: PointF): Double {
        val AB = PointF(B.x - A.x, B.y - A.y)
        val BC = PointF(C.x - B.x, C.y - B.y)

        val dotProduct = AB.x * BC.x + AB.y * BC.y
        val magnitudeAB = sqrt(AB.x.pow(2) + AB.y.pow(2))
        val magnitudeBC = sqrt(BC.x.pow(2) + BC.y.pow(2))

        val cosTheta = (dotProduct / (magnitudeAB * magnitudeBC)).coerceIn(-1.0f, 1.0f)
        var angle = Math.toDegrees(acos(cosTheta).toDouble())

        val crossProduct = AB.x * BC.y - AB.y * BC.x

        if (crossProduct < 0) {
            angle = 360 - angle
        }

        return angle
    }

    // [#] 사각형 ABCD 내부에 점 P가 있는지 확인하는 함수
    fun isPointInQuadrilateral(A: PointF, B: PointF, C: PointF, D: PointF, P: PointF): Boolean {
        val isInTriangleABC = isPointInTriangle(A, B, C, P)
        val isInTriangleCDA = isPointInTriangle(C, D, A, P)

        return isInTriangleABC || isInTriangleCDA
    }
    fun isPointInTriangle(A: PointF, B: PointF, C: PointF, P: PointF): Boolean {
        fun crossProduct(A: PointF, B: PointF, P: PointF): Float {
            return (B.x - A.x) * (P.y - A.y) - (B.y - A.y) * (P.x - A.x)
        }
        val cross1 = crossProduct(A, B, P)
        val cross2 = crossProduct(B, C, P)
        val cross3 = crossProduct(C, A, P)

        return (cross1 >= 0 && cross2 >= 0 && cross3 >= 0) || (cross1 <= 0 && cross2 <= 0 && cross3 <= 0)
    }

}
