package com.example.appdianosourgame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.appdianosourgame.R
import com.example.appdianosourgame.Constants // 修正: 僅匯入 Constants 類別本身
import kotlin.random.Random

// 遊戲狀態
enum class GameState {
    RUNNING, PAUSED, SCORE_DISPLAY
}

class GameView(context: Context, attrs: AttributeSet? = null) :
    SurfaceView(context, attrs), Runnable, SurfaceHolder.Callback {

    // 執行緒與繪圖
    @Volatile private var isPlaying = false
    private var gameThread: Thread? = null
    private var canvas: Canvas? = null
    private val surfaceHolder: SurfaceHolder = holder

    private var screenWidth = 0f
    private var screenHeight = 0f

    // 遊戲物件
    private lateinit var player: Player
    private val meteorites = mutableListOf<Meteorite>()
    private lateinit var controlButtons: ControlButtons

    // 遊戲狀態與計分
    private var gameState = GameState.RUNNING
    private var score = 0 // 隕石落到螢幕底部消失的次數
    private var gameOverTimerStart = 0L // 計時開始時間

    // 背景圖檔 (已移除，使用純白色背景)
    // private var backgroundBitmap: Bitmap? = null // 移除背景圖片相關宣告

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    // --- SurfaceHolder.Callback 實作 ---
    override fun surfaceCreated(holder: SurfaceHolder) {
        // 取得螢幕尺寸，初始化遊戲物件
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()

        // 移除載入和縮放背景圖的程式碼

        player = Player(context, screenWidth, screenHeight)
        controlButtons = ControlButtons(context, screenWidth, screenHeight)

        startGame()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pauseGame()
    }

    // --- 遊戲迴圈 (Runnable 實作) ---
    override fun run() {
        while (isPlaying) {
            update()
            draw()
            // 這裡可以加入控制幀率的程式碼，例如 Thread.sleep(16)
        }
    }

    // --- 核心遊戲方法 ---

    private fun update() {
        when (gameState) {
            GameState.RUNNING -> {
                // 1. 更新玩家位置
                player.update(screenWidth, screenHeight)

                // 2. 隨機生成隕石
                spawnMeteorite()

                // 3. 更新隕石位置、檢查消失和碰撞
                val iter = meteorites.iterator()
                while (iter.hasNext()) {
                    val m = iter.next()
                    m.update()

                    // 檢查隕石是否碰到螢幕底部
                    if (m.y > screenHeight) {
                        score++ // 增加分數
                        iter.remove()
                        continue
                    }

                    // 4. 碰撞檢測：角色 <-> 隕石
                    if (player.collidesWith(m)) {
                        gameState = GameState.PAUSED
                        gameOverTimerStart = System.currentTimeMillis()
                        // 碰撞後讓隕石停止移動（因為遊戲暫停）
                        break
                    }
                }
            }

            GameState.PAUSED -> {
                // 暫停運作：等待 2 秒計時
                if (System.currentTimeMillis() - gameOverTimerStart >= 2000) {
                    // 2 秒結束，切換到顯示分數畫面
                    gameState = GameState.SCORE_DISPLAY
                }
            }

            GameState.SCORE_DISPLAY -> {
                // 等待用戶點擊或執行其他動作來重啟遊戲
            }
        }
    }

    private fun draw() {
        if (surfaceHolder.surface.isValid) {
            canvas = surfaceHolder.lockCanvas()

            // 繪製背景 (使用白色)
            canvas?.drawColor(Color.WHITE)

            // 繪製遊戲物件
            player.draw(canvas)
            meteorites.forEach { it.draw(canvas) }

            // 只有在 RUNNING 狀態才繪製按鈕
            if (gameState == GameState.RUNNING) {
                controlButtons.draw(canvas)
            }

            // 繪製分數和遊戲狀態文字
            drawScoreAndState(canvas)

            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawScoreAndState(canvas: Canvas?) {
        // 將分數文字顏色改為黑色，以便在白色背景上可見
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 50f
            typeface = Typeface.DEFAULT_BOLD // 加粗字體
        }

        when (gameState) {
            GameState.RUNNING -> {
                // 右上角顯示 Score: (分數)
                paint.textAlign = Paint.Align.RIGHT
                canvas?.drawText("Score: $score", screenWidth - 20, 70f, paint)
            }

            GameState.PAUSED -> {
                // 顯示 "遊戲結束" (3 秒內)
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = 100f
                canvas?.drawText("遊戲結束", screenWidth / 2, screenHeight / 2, paint)
            }

            GameState.SCORE_DISPLAY -> {
                // 顯示最終分數
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = 100f
                canvas?.drawText("Score: $score", screenWidth / 2, screenHeight / 2, paint)

                // 顯示重新開始提示
                paint.textSize = 50f
                canvas?.drawText("點擊螢幕重新開始", screenWidth / 2, screenHeight / 2 + 100, paint)
            }
        }
    }

    // --- 隕石生成邏輯 ---
    private fun spawnMeteorite() {
        // 每幀以 METEORITE_SPAWN_RATE 的機率生成一個隕石
        if (Random.nextInt(100) < Constants.METEORITE_SPAWN_RATE) { // 使用 Constants. 前綴訪問常數
            meteorites.add(Meteorite(context, screenWidth, screenHeight))
        }
    }

    // --- 遊戲控制 ---

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (gameState) {
            GameState.RUNNING -> {
                // 處理按鈕觸控
                controlButtons.handleTouch(event) { action ->
                    when (action) {
                        "left" -> player.isMovingLeft = true
                        "right" -> player.isMovingRight = true
                        "jump" -> player.jump()
                        "stop" -> { // 釋放按鍵時
                            player.isMovingLeft = false
                            player.isMovingRight = false
                        }
                    }
                }
                true
            }

            GameState.SCORE_DISPLAY -> {
                // 點擊螢幕重啟遊戲
                if (event.action == MotionEvent.ACTION_DOWN) {
                    resetGame()
                }
                true
            }

            else -> super.onTouchEvent(event)
        }
    }

    private fun resetGame() {
        // 重設所有狀態
        score = 0
        meteorites.clear()
        player = Player(context, screenWidth, screenHeight) // 重新初始化玩家
        gameState = GameState.RUNNING
    }

    // 啟動/暫停遊戲迴圈
    private fun startGame() {
        isPlaying = true
        gameThread = Thread(this)
        gameThread?.start()
    }

    private fun pauseGame() {
        isPlaying = false
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}