package com.voicerecorder
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.*
import java.io.File

class MainActivity : Activity() {
  private lateinit var timer: TextView; private lateinit var name: TextView; private lateinit var level: LevelView
  private lateinit var bar1: LinearLayout; private lateinit var bar2: LinearLayout; private lateinit var pauseTxt: TextView
  private var rec: MediaRecorder? = null; private var file: File? = null
  private var elapsed = 0L; private var lastTick = 0L; private var paused = false; private var recording = false
  private val h = Handler(Looper.getMainLooper())
  private val tick = object : Runnable { override fun run() {
    if (recording && !paused) { val n = System.currentTimeMillis(); elapsed += n - lastTick; lastTick = n
      level.push((rec?.maxAmplitude ?: 0) / 12000f); timer.text = fmt(elapsed) }
    h.postDelayed(this, 100) } }
  private fun fmt(ms: Long): String { val s = ms / 1000; return "%02d:%02d:%02d".format(s / 3600, s / 60 % 60, s % 60) }
  private fun dir() = File(getExternalFilesDir(null), "recordings").apply { mkdirs() }
  private fun nextName(): String { val n = (dir().listFiles()?.size ?: 0) + 1; return "Voice %03d".format(n) }
  private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

  private fun btn(icon: String, label: String, tint: Int, click: () -> Unit): LinearLayout = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
    addView(TextView(context).apply { text = icon; textSize = 20f; setTextColor(tint); gravity = Gravity.CENTER })
    addView(TextView(context).apply { text = label; textSize = 14f; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER })
    setOnClickListener { click() }
  }
  private fun bar(vararg b: LinearLayout) = LinearLayout(this).apply {
    orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(6), 0, dp(6))
    background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0xFF2B2E33.toInt(), 0xFF0C0D0F.toInt()))
    b.forEach { addView(it, LinearLayout.LayoutParams(0, dp(52), 1f)) }
  }

  override fun onCreate(s: Bundle?) {
    super.onCreate(s)
    val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL
      background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0xFF1B1D21.toInt(), 0xFF050506.toInt())) }
    root.addView(MicView(this), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply { setMargins(dp(40), dp(24), dp(40), 0) })
    timer = TextView(this).apply { text = "00:00:00"; textSize = 46f; setTextColor(Color.WHITE); typeface = Typeface.create("sans-serif-light", Typeface.NORMAL); gravity = Gravity.CENTER }
    name = TextView(this).apply { text = nextName(); textSize = 13f; setTextColor(0xFFA0A4AA.toInt()); gravity = Gravity.CENTER }
    level = LevelView(this)
    root.addView(timer); root.addView(name)
    root.addView(level, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(70)).apply { setMargins(dp(16), dp(14), dp(16), dp(14)) })
    pauseTxt = TextView(this)
    bar1 = bar(btn("▤", "List", 0xFFCCCCCC.toInt()) { startActivity(Intent(this, ListActivity::class.java)) },
               btn("●", "Record", 0xFFE53935.toInt()) { start() })
    bar2 = bar(btn("■", "Stop", 0xFFDDDDDD.toInt()) { stop(true) },
               btn("❚❚", "Pause", 0xFFDDDDDD.toInt()) { togglePause() },
               btn("◉", "Cancel", 0xFFDDDDDD.toInt()) { stop(false) })
    bar2.visibility = View.GONE
    root.addView(bar1); root.addView(bar2)
    setContentView(root); h.post(tick)
  }
  private fun start() {
    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1); return }
    val n = nextName(); file = File(dir(), "$n.m4a")
    rec = MediaRecorder(this).apply { setAudioSource(MediaRecorder.AudioSource.MIC); setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
      setAudioEncoder(MediaRecorder.AudioEncoder.AAC); setOutputFile(file!!.absolutePath); prepare(); start() }
    name.text = n; elapsed = 0; lastTick = System.currentTimeMillis(); paused = false; recording = true; level.active = true
    bar1.visibility = View.GONE; bar2.visibility = View.VISIBLE
  }
  private fun togglePause() {
    val r = rec ?: return; val t = (bar2.getChildAt(1) as LinearLayout).getChildAt(1) as TextView
    if (!paused) { r.pause(); paused = true; t.text = "Resume" } else { r.resume(); paused = false; lastTick = System.currentTimeMillis(); t.text = "Pause" }
  }
  private fun stop(keep: Boolean) {
    try { rec?.stop() } catch (_: Exception) {}
    rec?.release(); rec = null; recording = false; level.active = false; level.levels.fill(0f); level.invalidate()
    if (!keep) file?.delete()
    timer.text = "00:00:00"; name.text = nextName()
    ((bar2.getChildAt(1) as LinearLayout).getChildAt(1) as TextView).text = "Pause"
    bar2.visibility = View.GONE; bar1.visibility = View.VISIBLE
  }
  override fun onRequestPermissionsResult(c: Int, p: Array<out String>, r: IntArray) { if (r.firstOrNull() == PackageManager.PERMISSION_GRANTED) start() }
  override fun onDestroy() { super.onDestroy(); h.removeCallbacksAndMessages(null); rec?.release() }
}
