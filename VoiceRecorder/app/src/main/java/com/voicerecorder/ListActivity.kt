package com.voicerecorder
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ListActivity : Activity() {
  private var mp: MediaPlayer? = null
  private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
  override fun onCreate(s: Bundle?) {
    super.onCreate(s)
    val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL
      background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0xFF0A1424.toInt(), 0xFF020305.toInt())) }
    val head = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(10), dp(12), dp(10))
      background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0xFF1C4A8A.toInt(), 0xFF0B1E3A.toInt())) }
    head.addView(ImageView(this).apply { setImageResource(com.voicerecorder.R.mipmap.ic_launcher) }, LinearLayout.LayoutParams(dp(36), dp(36)))
    head.addView(TextView(this).apply { text = "Recorded files"; textSize = 20f; setTextColor(Color.WHITE); setPadding(dp(12), 0, 0, 0) })
    root.addView(head)
    val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    root.addView(ScrollView(this).apply { addView(list) })
    val files = File(getExternalFilesDir(null), "recordings").listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    for (f in files) {
      val d = try { MediaMetadataRetriever().run { setDataSource(f.absolutePath); extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong() / 1000 } } catch (e: Exception) { 0L }
      val row = RelativeLayout(this).apply { setPadding(dp(14), dp(12), dp(14), dp(12)) }
      row.addView(TextView(this).apply { id = 1; text = f.nameWithoutExtension; textSize = 26f; setTextColor(Color.WHITE) })
      row.addView(TextView(this).apply { text = df.format(Date(f.lastModified())); textSize = 15f; setTextColor(0xFF3F8EE0.toInt()) },
        RelativeLayout.LayoutParams(-2, -2).apply { addRule(RelativeLayout.BELOW, 1) })
      row.addView(TextView(this).apply { text = "%02d:%02d".format(d / 60, d % 60); textSize = 15f; setTextColor(0xFFB0B4BA.toInt()) },
        RelativeLayout.LayoutParams(-2, -2).apply { addRule(RelativeLayout.ALIGN_PARENT_RIGHT); addRule(RelativeLayout.ALIGN_PARENT_BOTTOM) })
      row.setOnClickListener { mp?.release(); mp = MediaPlayer().apply { setDataSource(f.absolutePath); prepare(); start() } }
      row.setOnLongClickListener { f.delete(); recreate(); true }
      list.addView(row)
    }
    setContentView(root)
  }
  override fun onDestroy() { super.onDestroy(); mp?.release() }
}
