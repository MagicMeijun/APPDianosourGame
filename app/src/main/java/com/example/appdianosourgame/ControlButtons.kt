package com.example.appdianosourgame

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import com.example.appdianosourgame.R
import com.example.appdianosourgame.Constants

class ControlButtons(context: Context, screenWidth: Float, screenHeight: Float) {

    private val leftBitmap: Bitmap
    private val rightBitmap: Bitmap
    private val jumpBitmap: Bitmap

    // 儲存按鈕的矩形區域
    private val leftBounds: RectF
    private val rightBounds: RectF
    private val jumpBounds: RectF

    // 用於繪製的 Paint 物件
    private val normalPaint: Paint // 正常狀態 (高透明度)
    private val pressedPaint: Paint // 按下狀態 (低透明度/較深)

    // 按鈕透明度設定 (0-255)
    private val NORMAL_ALPHA = 150 // 正常透明度 (約 60%)
    private val PRESSED_ALPHA = 230 // 按下透明度 (約 90%，較深)

    // 追蹤當前被按下的按鈕名稱集合，用於繪製 (例如：{"left", "jump"})
    private val pressedButtons = mutableSetOf<String>()

    // 用於追蹤當前哪根手指 (Pointer ID) 按下了哪個移動按鈕 (left/right)，處理多點觸控
    private val movingPointers = mutableMapOf<Int, String>() // <Pointer ID, Button Name: "left" or "right">

    // 追蹤所有按下 "跳躍" 的手指 ID
    private val jumpPointers = mutableSetOf<Int>()

    init {
        // 1. 初始化 Paint 物件
        normalPaint = Paint().apply {
            alpha = NORMAL_ALPHA
        }
        pressedPaint = Paint().apply {
            alpha = PRESSED_ALPHA
        }

        // 2. 尺寸計算 (與前一個版本相同)
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

        // 3. 定義按鈕位置
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
                    // 左鍵按下
                    movingPointers[pointerId] = "left"
                    pressedButtons.add("left") // 更新繪圖狀態
                    actionCallback("left")
                } else if (rightBounds.contains(x, y)) {
                    // 右鍵按下
                    movingPointers[pointerId] = "right"
                    pressedButtons.add("right") // 更新繪圖狀態
                    actionCallback("right")
                } else if (jumpBounds.contains(x, y)) {
                    // 跳躍鍵按下
                    jumpPointers.add(pointerId)
                    pressedButtons.add("jump") // 更新繪圖狀態
                    actionCallback("jump") // 跳躍是單次觸發
                }
            }

            // 這裡省略了 ACTION_MOVE 的邊界滑出邏輯，因為它可能導致複雜性增加，但我們保留了基礎的 UP/DOWN 邏輯

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {

                // 檢查被釋放的 ID 是否為 Left/Right 按鈕
                val buttonReleased = movingPointers.remove(pointerId)

                // 處理 Left/Right 釋放
                if (buttonReleased == "left" || buttonReleased == "right") {
                    // 如果沒有其他手指按住同一個按鈕，則從繪圖集合中移除
                    val isStillMoving = movingPointers.containsValue("left") || movingPointers.containsValue("right")
                    if (!movingPointers.containsValue(buttonReleased)) {
                        pressedButtons.remove(buttonReleased)
                    }

                    // 如果沒有任何移動鍵被按住，發送 "stop"
                    if (!isStillMoving) {
                        actionCallback("stop")
                    }
                }

                // 處理 Jump 釋放
                if (jumpPointers.remove(pointerId)) {
                    // 如果沒有其他手指按住 Jump 按鈕，則從繪圖集合中移除
                    if (jumpPointers.isEmpty()) {
                        pressedButtons.remove("jump")
                    }
                }
            }
        }
    }

    fun draw(canvas: Canvas?) {
        // 判斷每個按鈕的繪圖筆刷
        val leftPaint = if (pressedButtons.contains("left")) pressedPaint else normalPaint
        val rightPaint = if (pressedButtons.contains("right")) pressedPaint else normalPaint
        val jumpPaint = if (pressedButtons.contains("jump")) pressedPaint else normalPaint

        // 繪製 Left Button
        canvas?.drawBitmap(leftBitmap, leftBounds.left, leftBounds.top, leftPaint)
        // 繪製 Right Button
        canvas?.drawBitmap(rightBitmap, rightBounds.left, rightBounds.top, rightPaint)
        // 繪製 Jump Button
        canvas?.drawBitmap(jumpBitmap, jumpBounds.left, jumpBounds.top, jumpPaint)
    }
}