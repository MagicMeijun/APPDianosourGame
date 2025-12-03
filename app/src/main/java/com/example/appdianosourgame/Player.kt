// Player.kt
package com.example.yourgame

import android.content.Context
import android.graphics.*
import com.example.yourgame.R
import com.example.yourgame.Constants.*

class Player(context: Context, private val screenWidth: Float, private val screenHeight: Float) {

    // 角色所有圖檔
    private val animationFrames: List<Bitmap>
    private val jumpFrame: Bitmap

    // 狀態變數
    var isMovingLeft = false
    var isMovingRight = false
    private var isJumping = false
    private var isGrounded = true

    // 位置和速度
    var x: Float
    var y: Float
    private var yVelocity = 0f

    // 尺寸
    private var width: Float
    private var height: Float

    // 動畫相關
    private var currentFrameIndex = 0
    private var frameCounter = 0

    // 碰撞邊界
    val bounds: RectF

    init {
        // 載入所有圖檔
        animationFrames = listOf(
            BitmapFactory.decodeResource(context.resources, R.drawable.diano0),
            BitmapFactory.decodeResource(context.resources, R.drawable.diano1),
            BitmapFactory.decodeResource(context.resources, R.drawable.diano2),
            BitmapFactory.decodeResource(context.resources, R.drawable.diano3)
        )
        jumpFrame = BitmapFactory.decodeResource(context.resources, R.drawable.diano4)

        // 設定角色尺寸（以第一個圖檔為準，並縮小至螢幕寬度的 10%）
        width = screenWidth * 0.1f
        height = width * (animationFrames[0].height.toFloat() / animationFrames[0].width.toFloat())

        // 縮放圖檔
        val targetWidth = width.toInt()
        val targetHeight = height.toInt()

        // 處理動畫圖檔的縮放
        val scaledFrames = mutableListOf<Bitmap>()
        animationFrames.forEach { bmp ->
            scaledFrames.add(Bitmap.createScaledBitmap(bmp, targetWidth, targetHeight, true))
        }
        animationFrames.clear()
        animationFrames.addAll(scaledFrames)

        // 處理跳躍圖檔的縮放
        jumpFrame = Bitmap.createScaledBitmap(jumpFrame, targetWidth, targetHeight, true)

        // 初始位置 (螢幕底部中央)
        x = (screenWidth / 2f) - (width / 2f)
        y = screenHeight - height - 50f // 預留一點空間，模擬站在地面上

        bounds = RectF(x, y, x + width, y + height)
    }

    fun update(screenWidth: Float, screenHeight: Float) {
        // --- 1. 水平移動 ---
        if (isMovingLeft) {
            x -= PLAYER_MOVE_SPEED
        } else if (isMovingRight) {
            x += PLAYER_MOVE_SPEED
        }

        // 邊界檢查
        x = x.coerceIn(0f, screenWidth - width)

        // --- 2. 垂直移動 (跳躍/重力) ---
        if (!isGrounded || isJumping) {
            yVelocity += GRAVITY
            y += yVelocity
            isGrounded = false
        }

        // 落地檢查 (假設地面在 y = screenHeight - height - 50f)
        val groundY = screenHeight - height - 50f
        if (y >= groundY) {
            y = groundY
            yVelocity = 0f
            isGrounded = true
            isJumping = false
        }

        // --- 3. 動畫更新 ---
        frameCounter++
        if (frameCounter >= ANIMATION_FRAME_RATE) {
            if (isMovingLeft || isMovingRight) {
                currentFrameIndex = (currentFrameIndex + 1) % animationFrames.size
            } else {
                currentFrameIndex = 0 // 靜止時顯示第一張
            }
            frameCounter = 0
        }

        // --- 4. 更新碰撞邊界 ---
        bounds.set(x, y, x + width, y + height)
    }

    fun jump() {
        if (isGrounded) {
            isJumping = true
            isGrounded = false
            yVelocity = PLAYER_JUMP_SPEED
        }
    }

    fun collidesWith(meteorite: Meteorite): Boolean {
        // 檢查角色和隕石的矩形邊界是否重疊
        return RectF.intersects(bounds, meteorite.bounds)
    }

    fun draw(canvas: Canvas?) {
        val currentBitmap = when {
            isJumping -> jumpFrame
            isMovingLeft || isMovingRight -> animationFrames[currentFrameIndex]
            else -> animationFrames[0]
        }

        // 左右移動時翻轉圖片 (可選，但讓角色更自然)
        val matrix = Matrix()
        if (isMovingLeft) {
            // 翻轉 Bitmap
            matrix.preScale(-1f, 1f)
            // 將畫布往左移動一個寬度，才能讓圖檔繪製在正確的位置
            matrix.postTranslate(x + width, y)
        } else {
            // 不翻轉
            matrix.postTranslate(x, y)
        }

        canvas?.drawBitmap(currentBitmap, matrix, null)

        // Debug 繪製碰撞邊界 (可選)
        // val paint = Paint().apply { color = Color.RED; style = Paint.Style.STROKE; strokeWidth = 5f }
        // canvas?.drawRect(bounds, paint)
    }
}