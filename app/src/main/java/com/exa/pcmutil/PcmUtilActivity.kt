package com.exa.pcmutil

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.exa.pcmutil.databinding.ActivityMainBinding
import com.exa.pcmutil.util.AudioCallBack
import com.exa.pcmutil.util.AudioPlayerUtil
import com.exa.pcmutil.util.AudioUtil
import com.exa.pcmutil.util.L
import java.io.File

class PcmUtilActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var audioUtil: AudioUtil
    private var playPath: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val path = filesDir.absolutePath + File.separator + "temp.wav"
        audioUtil = AudioUtil(this, path, object : AudioCallBack {
            override fun onRecordAudioErr(msg: String?) {
                L.de(msg)
            }

            override fun onRecordComplete(path: String?) {
                playPath = path
                playPath?.apply {
                    L.d("onRecordComplete:" + File(this).length())
                }
                L.dw(path)
            }
        })
        initView()
    }

    private fun initView() {
        checkPermission()
        findViewById<LinearLayout>(R.id.container).children.forEach {
            if (it is Button) {
                it.setOnClickListener(this)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.startBtn -> {
                if (checkPermission()) {
                    audioUtil.startRecord()
                }
            }
            R.id.stopBtn -> {
                audioUtil.stopRecord()
            }
            R.id.convertBtn -> {}
            R.id.playBtn -> {
                AudioPlayerUtil.getInstance().play(playPath)
            }
        }
    }

    private fun checkPermission(): Boolean {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            L.w("未授权录音")
            requestPermissions(Array(1) { Manifest.permission.RECORD_AUDIO }, 1)
            return false
        }
        return true
    }
}