package com.voicerecorder
import android.content.Context
import android.graphics.*
import android.view.View

class MicView(c: Context) : View(c) {
  private val p = Paint(Paint.ANTI_ALIAS_FLAG)
  override fun onDraw(cv: Canvas) {
    val w = width.toFloat(); val h = height.toFloat()
    val bw = w * 0.56f; val l = (w - bw) / 2; val r = l + bw
    val top = h * 0.04f; val bot = h * 0.80f
    // cradle (metal ring)
    p.shader = LinearGradient(0f, h * 0.55f, 0f, h, intArrayOf(0xFF2A2F36.toInt(), 0xFFB9C3CE.toInt(), 0xFF3A4048.toInt()), null, Shader.TileMode.CLAMP)
    p.style = Paint.Style.STROKE; p.strokeWidth = w * 0.06f
    cv.drawArc(RectF(l - w * 0.07f, h * 0.40f, r + w * 0.07f, h * 0.97f), 15f, 150f, false, p)
    // body
    p.style = Paint.Style.FILL
    p.shader = LinearGradient(l, 0f, r, 0f, intArrayOf(0xFF22262B.toInt(), 0xFF8E98A3.toInt(), 0xFF4A5159.toInt(), 0xFF1B1E22.toInt()), floatArrayOf(0f, .3f, .7f, 1f), Shader.TileMode.CLAMP)
    cv.drawRoundRect(RectF(l, top, r, bot), w * 0.12f, w * 0.12f, p)
    // grille lines
    p.shader = null; p.color = 0x55000000; p.strokeWidth = 1.2f
    var x = l + 6f; while (x < r - 4f) { cv.drawLine(x, top + h * 0.09f, x, bot - h * 0.04f, p); x += 5f }
    // top cap
    p.shader = LinearGradient(0f, top, 0f, top + h * 0.07f, 0xFFD5DCE3.toInt(), 0xFF50575F.toInt(), Shader.TileMode.CLAMP)
    cv.drawRoundRect(RectF(l - 4f, top, r + 4f, top + h * 0.07f), 14f, 14f, p)
  }
}

class LevelView(c: Context) : View(c) {
  val levels = FloatArray(10)
  private val p = Paint(Paint.ANTI_ALIAS_FLAG)
  var active = false
  fun push(v: Float) { System.arraycopy(levels, 1, levels, 0, 9); levels[9] = v; invalidate() }
  override fun onDraw(cv: Canvas) {
    val w = width.toFloat(); val h = height.toFloat()
    p.shader = null; p.style = Paint.Style.FILL; p.color = 0xFF0B0B0D.toInt()
    cv.drawRoundRect(RectF(0f, 0f, w, h), 10f, 10f, p)
    p.style = Paint.Style.STROKE; p.strokeWidth = 4f; p.color = 0xFFC9CED4.toInt()
    cv.drawRoundRect(RectF(2f, 2f, w - 2f, h - 2f), 10f, 10f, p)
    p.style = Paint.Style.FILL
    if (!active) { p.color = 0xFF5A0F0F.toInt(); cv.drawRect(14f, h - 14f, w - 14f, h - 12f, p); return }
    val bw = (w - 28f) / 10f
    for (i in 0 until 10) {
      val bh = (h - 24f) * levels[i].coerceIn(0.08f, 1f)
      val x = 14f + i * bw
      p.shader = LinearGradient(0f, h - 12f - bh, 0f, h - 12f, intArrayOf(0xFFFFD84A.toInt(), 0xFFF08A1C.toInt(), 0xFFC73A12.toInt()), null, Shader.TileMode.CLAMP)
      cv.drawRect(x + 1.5f, h - 12f - bh, x + bw - 1.5f, h - 12f, p)
    }
    p.shader = null; p.color = 0x66000000; p.strokeWidth = 2f
    var y = h - 12f; while (y > 12f) { cv.drawLine(14f, y, w - 14f, y, p); y -= 8f }
  }
}
