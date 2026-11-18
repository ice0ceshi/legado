package io.legado.app.ui.book.read

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import io.legado.app.ui.floatwindow.TransparentFloatService

object TestFloatHelper {
    fun test(ctx: ReadBookActivity) {
        // 1. 检查权限
        if (!Settings.canDrawOverlays(ctx)) {
            Toast.makeText(ctx, "请开启悬浮窗权限", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${ctx.packageName}")
            )
            ctx.startActivity(intent)
            return
        }

        // 2. 启动服务
        try {
            val floatIntent = Intent(ctx, TransparentFloatService::class.java).apply {
                putExtra("text", "🎉 悬浮窗测试成功！\n\n看到这段文字，说明代码全对！")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(floatIntent)
            } else {
                ctx.startService(floatIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(ctx, "启动失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
