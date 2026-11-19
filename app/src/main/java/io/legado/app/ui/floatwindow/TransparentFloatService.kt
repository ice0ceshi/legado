package io.legado.app.ui.floatwindow

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.TextView
import io.legado.app.R

class TransparentFloatService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatView: View
    private lateinit var layoutParams: WindowManager.LayoutParams
    private var isDragging = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        showFloatWindow()
    }

    private fun showFloatWindow() {
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            y = 300 // 初始位置
        }

        floatView = LayoutInflater.from(this).inflate(R.layout.view_float_text, null)
        
        // 设置触摸逻辑
        setupTouchLogic(floatView)
        windowManager.addView(floatView, layoutParams)
    }

    private fun setupTouchLogic(view: View) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isLongPress = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    isLongPress = false
                    // 按下 400 毫秒后判定为长按
                    view.postDelayed({ isLongPress = true }, 400)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // 如果是长按状态，或者移动距离超过20像素，就执行拖动
                    if (isLongPress || (Math.abs(event.rawX - initialTouchX) > 20 || Math.abs(event.rawY - initialTouchY) > 20)) {
                        if (isLongPress || isDragging) {
                            isDragging = true
                            layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                            layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager.updateViewLayout(floatView, layoutParams)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // 如果不是拖拽，且时间很短，就判定为点击（触发翻页）
                    if (!isDragging && !isLongPress) {
                        FloatActionListener.onClickNextPage?.invoke()
                    }
                    isDragging = false
                    isLongPress = false
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                    isLongPress = false
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        private var instance: TransparentFloatService? = null

        fun updateText(text: String) {
            instance?.floatView?.findViewById<TextView>(R.id.tv_content)?.text = text
        }

        fun isShowing() = instance != null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        instance = this
        intent?.getStringExtra("text")?.let { updateText(it) }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        if (::floatView.isInitialized) {
            windowManager.removeView(floatView)
        }
    }
}

// 新加一个全局监听器，用来和阅读界面通信
object FloatActionListener {
    var onClickNextPage: (() -> Unit)? = null
}
