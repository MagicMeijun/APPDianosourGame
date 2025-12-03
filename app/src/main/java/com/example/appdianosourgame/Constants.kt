// Constants.kt
package com.example.yourgame // 替換成你的實際套件名稱

object Constants {
    // 玩家設定
    const val PLAYER_MOVE_SPEED = 15f
    const val PLAYER_JUMP_SPEED = -45f // 負值代表向上
    const val GRAVITY = 3.5f
    const val ANIMATION_FRAME_RATE = 4 // 每幾幀切換一次角色動畫

    // 隕石設定
    const val METEORITE_SPAWN_RATE = 2 // 隕石生成的機率 (每 100 幀中的次數)
    const val METEORITE_MIN_SPEED_Y = 10f
    const val METEORITE_MAX_SPEED_Y = 25f
    const val METEORITE_MAX_SPEED_X = 10f // 隕石隨機水平移動速度

    // 控制按鈕大小 (佔螢幕寬度的比例)
    const val BUTTON_SIZE_RATIO = 0.15f
    const val BUTTON_MARGIN_RATIO = 0.05f
}