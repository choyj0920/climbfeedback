/* Feedback data class */

package org.tensorflow.lite.examples.poseestimation.data

import android.graphics.PointF

import kotlin.math.max
import kotlin.math.min
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

const val FEEDBACK_LIST_SIZE = 3    // 자세 판단 시 체크할 항목 개수
const val THRESOLD = 10             // 몇 프레임 이상 지속될 시 피드백할지

data class Feedback(val person:Person, val frame:Int) {

    /////////////
    // # [멤버] //
    /////////////

    var FRAMES = frame   // 프레임 개수
    var feedbackList = ( // feedbackLst => 총 n개의 피드백 사항, i번째 <피드백 발생여부, 카운트>
            Array(FRAMES){ Array<Pair<Boolean, Int>?>(FEEDBACK_LIST_SIZE){ Pair(false,0)}}
            )

    init{
        //initFeedback()
    }


    ///////////////
    // # [메소드] //
    ///////////////

    // 피드백리스트 초기화
    fun initFeedback(){ // 피드백 전체 초기화
        feedbackList.forEach { feedbackList ->
            feedbackList.forEachIndexed { index, _ ->
                feedbackList[index] = Pair(false, 0)
            }
        }
    }

    // 각 프레임에서의 자세가 올바른지 항목 별로 체크
    fun checkFrame(frame_num : Int) : Int {

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
            (LEFT_HIP.x + RIGHT_HIP.x) / 2.0f,
            (LEFT_HIP.y + RIGHT_HIP.y) / 2.0f
        )

        // [1] 손 기준 발이 치우쳤는가?
        if(min(LEFT_HAND.x,RIGHT_HAND.x) > max(LEFT_FOOT.x,RIGHT_FOOT.x) ||
            max(LEFT_HAND.x,RIGHT_HAND.x) < min(LEFT_FOOT.x,RIGHT_FOOT.x)){
            feedbackList[frame_num][0] = Pair(true, (feedbackList[frame_num][0]?.second ?: 0) + 1)
            err *= 2
        }

        // [2] 양 팔이 계속 굽어져있는가?
        if(calculateAngle(LEFT_HAND, LEFT_ELBOW, LEFT_SHOULDER) < 30 &&
            calculateAngle(RIGHT_HAND, RIGHT_ELBOW, RIGHT_SHOULDER) < 30){
            feedbackList[frame_num][1] = Pair(true, (feedbackList[frame_num][1]?.second ?: 0) + 1)
            err *= 3
        }

        // [3] 무게중심이 삼지점 밖에있는가?
        if(!isPointInQuadrilateral(LEFT_HAND, LEFT_FOOT, RIGHT_FOOT, RIGHT_HAND, GP)){
            feedbackList[frame_num][2] = Pair(true, (feedbackList[frame_num][2]?.second ?: 0) + 1)
            err *= 5
        }

        // [4] 허벅지:종아리 비율이 차이가 많이나는가?
        // 추가할지 말지 검토 예정

        return err
    }

    // 점 3개 각도 계산해주는 함수
    fun calculateAngle(A: PointF, B: PointF, C: PointF): Double {
        val AB = PointF(B.x - A.x, B.y - A.y)
        val BC = PointF(C.x - B.x, C.y - B.y)

        val dotProduct = AB.x * BC.x + AB.y * BC.y

        val magnitudeAB = sqrt(AB.x.pow(2) + AB.y.pow(2))
        val magnitudeBC = sqrt(BC.x.pow(2) + BC.y.pow(2))

        val cosTheta = (dotProduct / (magnitudeAB * magnitudeBC)).coerceIn(-1.0f, 1.0f)
        val angle = acos(cosTheta)

        return Math.toDegrees(angle.toDouble())
    }

    // 사각형 ABCD 내부에 점 P가 있는지 확인하는 함수
    fun isPointInQuadrilateral(A: PointF, B: PointF, C: PointF, D: PointF, P: PointF): Boolean {
        val isInTriangleABC = isPointInTriangle(A, B, C, P)
        val isInTriangleCDA = isPointInTriangle(C, D, A, P)

        return isInTriangleABC || isInTriangleCDA
    }
    fun isPointInTriangle(A: PointF, B: PointF, C: PointF, P: PointF): Boolean {
        val cross1 = crossProduct(A, B, P)
        val cross2 = crossProduct(B, C, P)
        val cross3 = crossProduct(C, A, P)

        return (cross1 >= 0 && cross2 >= 0 && cross3 >= 0) || (cross1 <= 0 && cross2 <= 0 && cross3 <= 0)
    }
    fun crossProduct(A: PointF, B: PointF, P: PointF): Float {
        return (B.x - A.x) * (P.y - A.y) - (B.y - A.y) * (P.x - A.x)
    }


}