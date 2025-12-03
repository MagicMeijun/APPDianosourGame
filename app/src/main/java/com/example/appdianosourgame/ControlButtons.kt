// ControlButtons.kt
package com.example.yourgame

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import com.example.yourgame.R
import com.example.yourgame.Constants.*

class ControlButtons(context: Context, screenWidth: Float, screenHeight: Float) {

    private val leftBitmap: Bitmap
    private val rightBitmap: Bitmap
    private val jumpBitmap: Bitmap

    // 儲存按鈕的矩形區域
    private val leftBounds: RectF
    private val rightBounds: RectF
    private val jumpBounds: RectF

    init {
        // 載入並縮放圖檔
        val buttonSize = screenWidth * BUTTON_SIZE_RATIO
        val margin = screenWidth * BUTTON_MARGIN_RATIO
        val targetSize = buttonSize.toInt()
        val bottomY = screenHeight - margin - buttonSize

        leftBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.left).let {
            Bitmap.createScaledBitmap(it, targetSize, targetSize, true)
        }
        rightBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.right).let {
            Bitmap.createScaledBitmap(it, targetSize, targetSize, true)
        }
        jumpBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.jump).let {
            Bitmap.createScaledBitmap(it, targetSize, targetSize, true)
        }

        // 定義按鈕位置 (左下角和右下角)
        leftBounds = RectF(margin, bottomY, margin + buttonSize, bottomY + buttonSize)
        rightBounds = RectF(margin * 2 + buttonSize, bottomY, margin * 2 + buttonSize * 2, bottomY + buttonSize)
        jumpBounds = RectF(screenWidth - margin - buttonSize, bottomY, screenWidth - margin, bottomY + buttonSize)
    }

    fun handleTouch(event: MotionEvent, actionCallback: (String) -> Unit) {
        val x = event.x
        val y = event.y

        // 檢查多點觸控
        for (i in 0 until event.pointerCount) {
            val pointerX = event.getX(i)
            val pointerY = event.getY(i)

            // 按下事件或移動事件時
            if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_POINTER_DOWN || event.actionMasked == MotionEvent.ACTION_MOVE) {
                if (leftBounds.contains(pointerX, pointerY)) {
                    actionCallback("left")
                } else if (rightBounds.contains(pointerX, pointerY)) {
                    actionCallback("right")
                } else if (jumpBounds.contains(pointerX, pointerY)) {
                    // 跳躍是單次事件，只需要在按下時觸發
                    if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                        actionCallback("jump")
                    }
                }
            }
        }

        // 釋放事件時 (停止左右移動)
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_POINTER_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            // 由於多點觸控，需要檢查哪個按鈕被釋放了。
            // 這裡簡單處理：只要有釋放就清除移動狀態 (更複雜的處理需要追蹤每個 Pointer ID)
            actionCallback("stop")
        }
    }

    fun draw(canvas: Canvas?) {
        canvas?.drawBitmap(leftBitmap, leftBounds.left, leftBounds.top, null)
        canvas?.drawBitmap(rightBitmap, rightBounds.left, rightBounds.top, null)
        canvas?.drawBitmap(jumpBitmap, jumpBounds.left, jumpBounds.top, null)
    }
}