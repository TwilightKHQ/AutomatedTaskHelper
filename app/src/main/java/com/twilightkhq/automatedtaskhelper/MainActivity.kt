package com.twilightkhq.automatedtaskhelper

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.elvishew.xlog.XLog
import com.twilightkhq.base.CoordinatePoint
import com.twilightkhq.operation.OperationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XLog.init()
        setContentView(R.layout.activity_main)
        findViewById<AppCompatButton>(R.id.helloBtn).apply {
            setOnClickListener {
                lifecycleScope.launch {
                    OperationManager.click(CoordinatePoint(500.0, 500.0))
                    OperationManager.swipe(
                        CoordinatePoint(500.0, 500.0),
                        CoordinatePoint(600.0, 600.0), 100
                    )
//                    val startAppResult = OperationManager.startApp(
//                        this@MainActivity, "jp.co.sinewave.elst"
//                    )
//                    Log.d("twilight", "onCreate: startAppResult=$startAppResult")
//                    val killAppResult = OperationManager.killApp(
//                        "com.twilightkhq.automatedtaskhelper"
//                    )
//                    Log.d("twilight", "onCreate: killAppResult=$killAppResult")
//                    if (ScreenCaptureHelper.isInitialized()) {
//                        val color = ScreenCaptureHelper.getScreenCaptureBitmap()?.getPixel(500, 500)
//                        color?.let {
//                            val alpha = Color.alpha(color)
//                            val red = Color.red(color)
//                            val green = Color.green(color)
//                            val blue = Color.blue(color)
//                            Log.d(
//                                "twilight", "onCreate: color=$color alpha=$alpha red=$red green=$green blue=$blue"
//                            )
//                        }
//                        val savedPath = "${PathUtils().getAppDirPath(this@MainActivity)}/Screen.png"
//                        ScreenCaptureHelper.saveScreenCapture(savedPath)
//                    } else {
//                        ScreenCaptureHelper.requestPermission(this@MainActivity)
//                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d("twilight", "onTouchEvent: event=$event")
        return super.onTouchEvent(event)
    }
}