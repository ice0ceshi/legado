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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        showFloatWindow()
    }

    private fun showFloatWindow() {
        // 1. 初始化布局参数
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            // 允许穿透点击、不抢占焦点、让背景透明
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            y = 200 // 初始Y轴位置
        }

        // 2. 加载布局
        floatView = LayoutInflater.from(this).inflate(R.layout.view_float_text, null)

        // 3. 设置拖拽逻辑
        val dragHandle = floatView.findViewById<View>(R.id.drag_handle)
        setupDrag(dragHandle)

        // 4. 添加到窗口
        windowManager.addView(floatView, layoutParams)
    }

    private fun setupDrag(dragView: View) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        dragView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatView, layoutParams)
                    true
                }
                else -> false
            }
        }
    }

    // 供外部调用的静态方法，更新文字内容
    companion object {
        private var instance: TransparentFloatService? = null

        fun updateText(text: String) {
            instance?.floatView?.findViewById<TextView>(R.id.tv_content)?.text = text
        }

        fun isShowing() = instance != null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        instance = this
        // 如果通过Intent传递了初始文字
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
