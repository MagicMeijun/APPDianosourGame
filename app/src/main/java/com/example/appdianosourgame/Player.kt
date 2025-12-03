package com.example.appdianosourgame

import android.content.Context
import android.graphics.*
import com.example.appdianosourgame.R
import com.example.appdianosourgame.Constants // 修正: 僅匯入 Constants 類別本身
import com.example.appdianosourgame.Meteorite

class Player(context: Context, private val screenWidth: Float, private val screenHeight: Float) {

    // 角色所有圖檔
    // 修正: 將 val 改為 var 以允許後續的重新賦值
    private var animationFrames: List<Bitmap>
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
        // 載入所有圖檔 (diano0~diano4)
        animationFrames = listOf(
            BitmapFactory.decodeResource(context.resources, R.drawable.diano0),
            BitmapFactory.decodeResource(context.resources, R.drawable.diano1),
            BitmapFactory.decodeResource(context.resources, R.drawable.diano2),
            BitmapFactory.decodeResource(context.resources, R.drawable.diano3)
        )
        // diano4 為跳躍圖檔
        jumpFrame = BitmapFactory.decodeResource(context.resources, R.drawable.diano4).let { original ->
            // 由於 jumpFrame 在 update 中使用，也需要縮放
            val playerRatio = original.height.toFloat() / original.width.toFloat()
            val targetWidth = (screenWidth * 0.1f).toInt()
            val targetHeight = (targetWidth * playerRatio).toInt()
            Bitmap.createScaledBitmap(original, targetWidth, targetHeight, true)
        }

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

        // 修正: 將原始 animationFrames 列表替換為 scaledFrames 列表
        animationFrames = scaledFrames

        // 初始位置 (螢幕底部中央)
        x = (screenWidth / 2f) - (width / 2f)
        // 地面位置：距離底部 50f
        y = screenHeight - height - 50f

        bounds = RectF(x, y, x + width, y + height)
    }

    fun update(screenWidth: Float, screenHeight: Float) {
        // --- 1. 水平移動 ---
        if (isMovingLeft) {
            x -= Constants.PLAYER_MOVE_SPEED // 使用 Constants. 前綴訪問常數
        } else if (isMovingRight) {
            x += Constants.PLAYER_MOVE_SPEED // 使用 Constants. 前綴訪問常數
        }

        // 邊界檢查
        x = x.coerceIn(0f, screenWidth - width)

        // --- 2. 垂直移動 (跳躍/重力) ---
        if (!isGrounded || isJumping) {
            yVelocity += Constants.GRAVITY // 使用 Constants. 前綴訪問常數
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
        if (frameCounter >= Constants.ANIMATION_FRAME_RATE) { // 使用 Constants. 前綴訪問常數
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
            yVelocity = Constants.PLAYER_JUMP_SPEED // 使用 Constants. 前綴訪問常數
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

        // 左右移動時翻轉圖片，使角色面朝移動方向
        val matrix = Matrix()
        if (isMovingLeft) {
            // 翻轉 Bitmap
            matrix.preScale(-1f, 1f)
            // 將畫布往左移動一個寬度，才能讓圖檔繪製在正確的位置
            matrix.postTranslate(x + width, y)
        } else {
            // 不翻轉 (或向右移動)
            matrix.postTranslate(x, y)
        }

        canvas?.drawBitmap(currentBitmap, matrix, null)
    }
}