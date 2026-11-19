package io.legado.app.ui.book.read

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import io.legado.app.ui.floatwindow.TransparentFloatService

object TestFloatHelper {
    var isShowing = false

    // 接收 Activity 和 当前页面的文字
    fun toggle(ctx: ReadBookActivity, currentText: String) {
        if (!Settings.canDrawOverlays(ctx)) {
            Toast.makeText(ctx, "请先开启悬浮窗权限", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}"))
            ctx.startActivity(intent)
            return
        }

        if (isShowing) {
            // 关闭悬浮窗
            ctx.stopService(Intent(ctx, TransparentFloatService::class.java))
            isShowing = false
            Toast.makeText(ctx, "悬浮窗已关闭", Toast.LENGTH_SHORT).show()
        } else {
            // 启动悬浮窗并传入真实文字
            val floatIntent = Intent(ctx, TransparentFloatService::class.java).apply {
                putExtra("text", currentText)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(floatIntent)
            } else {
                ctx.startService(floatIntent)
            }
            isShowing = true
            Toast.makeText(ctx, "悬浮窗已开启", Toast.LENGTH_SHORT).show()
        }
    }
}
