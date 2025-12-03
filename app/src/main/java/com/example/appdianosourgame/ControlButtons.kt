package com.example.appdianosourgame

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import com.example.appdianosourgame.R
import com.example.appdianosourgame.Constants // <-- 修正: 僅匯入 Constants 類別本身

class ControlButtons(context: Context, screenWidth: Float, screenHeight: Float) {

    private val leftBitmap: Bitmap
    private val rightBitmap: Bitmap
    private val jumpBitmap: Bitmap

    // 儲存按鈕的矩形區域
    private val leftBounds: RectF
    private val rightBounds: RectF
    private val jumpBounds: RectF

    // 用於追蹤當前是否有按鈕被按住 (處理多點觸控)
    private val pressedPointers = mutableMapOf<Int, String>() // <Pointer ID, Button Name>


    init {
        // 載入並縮放圖檔
        // 修正: 使用 Constants. 前綴訪問常數
        val buttonSize = screenWidth * Constants.BUTTON_SIZE_RATIO
        val margin = screenWidth * Constants.BUTTON_MARGIN_RATIO
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

    /**
     * 處理觸控事件，並呼叫回調函數通知 GameView 進行角色操作
     * @param event 觸控事件
     * @param actionCallback 回調函數，傳回 "left", "right", "jump", 或 "stop"
     */
    fun handleTouch(event: MotionEvent, actionCallback: (String) -> Unit) {

        // 取得當前的觸控索引 (Pointer Index) 和 ID
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val x = event.getX(pointerIndex)
                val y = event.getY(pointerIndex)

                if (leftBounds.contains(x, y)) {
                    pressedPointers[pointerId] = "left"
                    actionCallback("left")
                } else if (rightBounds.contains(x, y)) {
                    pressedPointers[pointerId] = "right"
                    actionCallback("right")
                } else if (jumpBounds.contains(x, y)) {
                    // Jump 是單次觸發，不需儲存狀態，直接呼叫
                    actionCallback("jump")
                }
            }

            MotionEvent.ACTION_MOVE -> {
                // 處理移動：檢查是否有按鈕持續被按住
                for (i in 0 until event.pointerCount) {
                    val id = event.getPointerId(i)
                    val x = event.getX(i)
                    val y = event.getY(i)

                    val currentPress = pressedPointers[id]

                    if (currentPress == "left" && !leftBounds.contains(x, y)) {
                        // 如果手指滑出邊界，釋放按鍵
                        pressedPointers.remove(id)
                        actionCallback("stop")
                    } else if (currentPress == "right" && !rightBounds.contains(x, y)) {
                        // 如果手指滑出邊界，釋放按鍵
                        pressedPointers.remove(id)
                        actionCallback("stop")
                    }
                }

                // 檢查是否還有任何移動鍵被按住
                val isStillMoving = pressedPointers.containsValue("left") || pressedPointers.containsValue("right")
                if (!isStillMoving) {
                    actionCallback("stop")
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                // 檢查被釋放的 ID 是否為 Left/Right 按鈕
                val buttonReleased = pressedPointers.remove(pointerId)

                if (buttonReleased == "left" || buttonReleased == "right") {
                    // 如果釋放的是移動鍵，並且沒有其他移動鍵被按住，則發送 "stop"
                    val isStillMoving = pressedPointers.containsValue("left") || pressedPointers.containsValue("right")
                    if (!isStillMoving) {
                        actionCallback("stop")
                    }
                }
            }
        }
    }

    fun draw(canvas: Canvas?) {
        // 繪製 Left Button
        canvas?.drawBitmap(leftBitmap, leftBounds.left, leftBounds.top, null)
        // 繪製 Right Button
        canvas?.drawBitmap(rightBitmap, rightBounds.left, rightBounds.top, null)
        // 繪製 Jump Button
        canvas?.drawBitmap(jumpBitmap, jumpBounds.left, jumpBounds.top, null)
    }
}