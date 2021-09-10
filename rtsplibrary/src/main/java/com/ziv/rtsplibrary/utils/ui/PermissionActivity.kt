package com.ziv.rtsplibrary.utils.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.ziv.rtsplibrary.R
import com.ziv.rtsplibrary.rtsp.RtspServer
import com.ziv.rtsplibrary.stream.video.screen.ScreenRecordThread
import com.ziv.rtsplibrary.stream.video.screen.media.DataUtil
import com.ziv.rtsplibrary.stream.video.screen.media.H264Data
import com.ziv.rtsplibrary.stream.video.screen.media.H264DataCollector
import com.ziv.rtsplibrary.utils.LogUtil
import com.ziv.rtsplibrary.utils.ToastUtil
import java.lang.Exception

class PermissionActivity : AppCompatActivity(), H264DataCollector {
    companion object {
        const val TAG = "PermissionActivity"
        const val REQUEST_CODE = 1002
    }
    private var mMediaProjectionManager: MediaProjectionManager? = null
    private var mScreenRecord: ScreenRecordThread? = null
    private var mRtspServer: RtspServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        LogUtil.d("$TAG onCreate")
        mMediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mMediaProjectionManager?.createScreenCaptureIntent(), REQUEST_CODE)
        startService(Intent(this, RtspServer::class.java))
//        bindService(Intent(this, RtspServer::class.java), mRtspServiceConnection, BIND_AUTO_CREATE)
        LogUtil.d("$TAG onCreate finish.")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogUtil.d("$TAG requestCode: $requestCode ,resultCode: $resultCode")
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                val mediaProjection = mMediaProjectionManager?.getMediaProjection(resultCode, data!!)
                mScreenRecord = ScreenRecordThread(this, mediaProjection)
                mScreenRecord?.start()
                finish()
            } catch (e: Exception) {
                LogUtil.e("${e.message}")
            }
        } else {
            LogUtil.e("$TAG 授权错误")
        }
    }

    private val mRtspServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mRtspServer = (service as RtspServer.LocalBinder).service
            mRtspServer?.addCallbackListener(mRtspCallbackListener)
            mRtspServer?.start()
            LogUtil.d("$TAG onServiceConnected finish.")
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    private val mRtspCallbackListener: RtspServer.CallbackListener = object :
        RtspServer.CallbackListener {
        override fun onError(server: RtspServer?, e: Exception?, error: Int) {
            // We alert the user that the port is already used by another app.
            if (error == RtspServer.ERROR_BIND_FAILED) {
                AlertDialog.Builder(this@PermissionActivity)
                    .setTitle("端口被占用用")
                    .setMessage("你需要选择另外一个端口")
                    .show()
            }
        }

        override fun onMessage(server: RtspServer?, message: Int) {
            if (message == RtspServer.MESSAGE_STREAMING_STARTED) {
                runOnUiThread { ToastUtil.showShort(this@PermissionActivity, "用户接入，推流开始") }
            } else if (message == RtspServer.MESSAGE_STREAMING_STOPPED) {
                runOnUiThread { ToastUtil.showShort(this@PermissionActivity, "推流结束") }
            }
        }
    }

    private var lastTime = 0L
    private var fps = 0
    override fun collect(data: H264Data?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime > 1000 + lastTime) {
            LogUtil.d("$TAG fps $fps.")
            fps = 0
            lastTime = currentTime
        } else {
            fps++
        }
        DataUtil.getInstance().putData(data)
        // LogUtil.d("$TAG collect ${data?.data?.size}.")
    }
}