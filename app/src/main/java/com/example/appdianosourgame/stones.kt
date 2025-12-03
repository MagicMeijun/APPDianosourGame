// Meteorite.kt
package com.example.yourgame

import android.content.Context
import android.graphics.*
import com.example.yourgame.R
import com.example.yourgame.Constants.*
import kotlin.random.Random

class Meteorite(context: Context, private val screenWidth: Float, private val screenHeight: Float) {

    // 所有隕石圖檔
    private val allFrames: List<Bitmap>
    private val bitmap: Bitmap

    // 位置和速度
    var x: Float
    var y: Float
    private val dx: Float // 水平速度
    private val dy: Float // 垂直速度

    // 尺寸
    private var width: Float
    private var height: Float

    // 碰撞邊界
    val bounds: RectF

    init {
        // 載入所有隕石圖檔
        allFrames = listOf(
            BitmapFactory.decodeResource(context.resources, R.drawable.stone0),
            BitmapFactory.decodeResource(context.resources, R.drawable.stone1),
            BitmapFactory.decodeResource(context.resources, R.drawable.stone2),
            BitmapFactory.decodeResource(context.resources, R.drawable.stone3),
            BitmapFactory.decodeResource(context.resources, R.drawable.stone4)
        )

        // 隨機選擇一個圖檔
        val originalBitmap = allFrames[Random.nextInt(allFrames.size)]

        // 設定隕石尺寸（螢幕寬度的 8%）
        width = screenWidth * 0.08f
        height = width * (originalBitmap.height.toFloat() / originalBitmap.width.toFloat())

        // 縮放圖檔
        bitmap = Bitmap.createScaledBitmap(originalBitmap, width.toInt(), height.toInt(), true)

        // 初始位置 (隨機 X 座標，從螢幕上方產生)
        x = Random.nextFloat() * (screenWidth - width)
        y = -height // 從螢幕上方邊緣外產生

        // 隨機速度 (隨機方向落下)
        dy = Random.nextFloat() * (METEORITE_MAX_SPEED_Y - METEORITE_MIN_SPEED_Y) + METEORITE_MIN_SPEED_Y
        dx = Random.nextFloat() * (METEORITE_MAX_SPEED_X * 2) - METEORITE_MAX_SPEED_X // 範圍 [-MAX, MAX]

        bounds = RectF(x, y, x + width, y + height)
    }

    fun update() {
        // 更新位置
        x += dx
        y += dy

        // 邊界檢查 (可選：如果隕石飛出左右邊界，讓它從對向出現，或讓它消失)
        // 這裡我們只讓它繼續移動直到落到螢幕底部

        // 更新碰撞邊界
        bounds.set(x, y, x + width, y + height)
    }

    fun draw(canvas: Canvas?) {
        canvas?.drawBitmap(bitmap, x, y, null)

        // Debug 繪製碰撞邊界 (可選)
        // val paint = Paint().apply { color = Color.BLUE; style = Paint.Style.STROKE; strokeWidth = 5f }
        // canvas?.drawRect(bounds, paint)
    }
}