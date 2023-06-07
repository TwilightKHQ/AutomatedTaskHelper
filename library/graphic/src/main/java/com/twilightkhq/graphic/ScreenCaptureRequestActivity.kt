package com.twilightkhq.graphic

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ScreenCaptureRequestActivity : AppCompatActivity() {

    private val startResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        ScreenCaptureHelper.setRequestResult(result)
        ScreenCaptureHelper.startMediaProjection(this)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, ScreenCaptureService::class.java))
        val mediaProjectionManager = ScreenCaptureHelper.getMediaProjectionManager(this)
        startResult.launch(mediaProjectionManager?.createScreenCaptureIntent())
    }
}