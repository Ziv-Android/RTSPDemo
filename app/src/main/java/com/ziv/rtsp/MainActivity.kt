package com.ziv.rtsp

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.TextView
import com.ziv.rtsplibrary.RTSPLibrary
import com.ziv.rtsplibrary.rtsp.RtspServer
import com.ziv.rtsplibrary.utils.ui.PermissionActivity

class MainActivity : AppCompatActivity() {
    private var mSurfaceView: SurfaceView? = null
    private var mIpView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        mSurfaceView = findViewById(R.id.virtual_surface_view)
        mIpView = findViewById(R.id.txt_address)

        val rtsp = RTSPLibrary.getInstance()
        mIpView?.text = rtsp.getRtspAddress(this)

        rtsp.start(this)

//        val mMediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
//        startActivityForResult(mMediaProjectionManager?.createScreenCaptureIntent(),
//            PermissionActivity.REQUEST_CODE
//        )
    }
}