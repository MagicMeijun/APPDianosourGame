// MainActivity.kt
package com.example.yourgame

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.example.appdianosourgame.R

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 設定為全螢幕
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // 載入佈局，其中包含 GameView
        setContentView(R.layout.activity_main)

        // 如果想在代碼中設置，可以使用下面這行代替 setContentView(R.layout.activity_main)
        // val gameView = GameView(this)
        // setContentView(gameView)
    }

    // 遊戲在背景時暫停
    override fun onPause() {
        super.onPause()
        // 這裡通常需要一個機制讓 GameView 內部可以暫停執行緒
        // (在 GameView 中已實現，但實際應用中可能需要公開方法來呼叫)
    }

    // 遊戲回到前景時恢復
    override fun onResume() {
        super.onResume()
        // 這裡通常需要一個機制讓 GameView 內部可以恢復執行緒
    }
}