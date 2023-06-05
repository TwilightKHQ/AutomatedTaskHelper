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
                lifecycleScope.launch(Dispatchers.IO) {
                    OperationManager.click(CoordinatePoint(500.0, 500.0))
                    OperationManager.swipe(
                        CoordinatePoint(500.0, 500.0),
                        CoordinatePoint(600.0, 600.0), 100
                    )
//                    val startAppResult = OperationManager.startApp(
//                        this@MainActivity, "jp.co.sinewave.elst"
//                    )
//                    Log.d("twilight", "onCreate: startAppResult=$startAppResult")
                    val killAppResult = OperationManager.killApp("com.twilightkhq.automatedtaskhelper")
                    Log.d("twilight", "onCreate: killAppResult=$killAppResult")
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d("twilight", "onTouchEvent: event=$event")
        return super.onTouchEvent(event)
    }
}