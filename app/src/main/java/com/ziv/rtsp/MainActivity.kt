package com.ziv.rtsp

import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.TextView

import com.ziv.rtsplibrary.RTSPLibrary
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var mSurfaceView: SurfaceView? = null
    private var mIpView: TextView? = null

    private var mCamera: Camera? = null
    private var mParameters: Camera.Parameters? = null
    private var mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
    private var mWidth = 320
    private var mHeight = 480
    private var mOrientation = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        mSurfaceView = findViewById(R.id.virtual_surface_view)
//        mSurfaceView?.scaleX = -1F
        mIpView = findViewById(R.id.txt_address)

        startCamera()

        val rtsp = RTSPLibrary.getInstance()
        mIpView?.text = rtsp.getRtspAddress(this)

        rtsp.start(this)
    }

    private fun startCamera() {
        mSurfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (null == mCamera) {
                    if (isSupport(mCameraId)) {
                        try {
                            mCamera = Camera.open(mCameraId)
                            initParameters(mCamera)

                            //preview
                            if (null != mCamera) {
                                mCamera!!.setPreviewCallback { data, camera ->
                                    Log.d(
                                        "onPreviewFrame",
                                        "camera: $camera, data: ${data?.size}"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                startPreview()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                mCamera?.setPreviewDisplay(holder)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                releaseCamera()
            }
        })
    }

    private fun releaseCamera() {
        mCamera?.let {
            it.stopPreview()
            it.stopFaceDetection()
            it.setPreviewCallback(null)
            it.release()
        }
        mCamera = null
    }

    private fun startPreview() {
        try {
            setCameraDisplayOrientation()
            mCamera?.startPreview()

            startFaceDetect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startFaceDetect() {
        mCamera?.startFaceDetection()
        mCamera?.setFaceDetectionListener(object : Camera.FaceDetectionListener {
            override fun onFaceDetection(faces: Array<out Camera.Face>?, camera: Camera?) {
                Log.d("DEBUG", "##### face length: " + faces?.size);
            }
        })
    }

    private fun setCameraDisplayOrientation() {
        val cameraInfo = CameraInfo()
        Camera.getCameraInfo(mCameraId, cameraInfo)
        val rotation = windowManager.defaultDisplay.rotation //自然方向
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        Log.d("DEBUG", "##### setCameraDisplayOrientation degrees: $degrees")

        degrees = when(mCameraId){
            Camera.CameraInfo.CAMERA_FACING_FRONT -> 270
            Camera.CameraInfo.CAMERA_FACING_BACK -> 90
            else -> 0
        }

        var result: Int
        //cameraInfo.orientation 图像传感方向
        //cameraInfo.orientation 图像传感方向
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360
            result = (360 - result) % 360
        } else {
            result = (cameraInfo.orientation - degrees + 360) % 360
        }

        Log.d("DEBUG", "##### setCameraDisplayOrientation rotation: ${
                rotation}, cameraInfo.orientation: ${cameraInfo.orientation}, result: $result")

        mOrientation = result
        //相机预览方向
        //相机预览方向
        mCamera!!.setDisplayOrientation(result)
    }

    private fun initParameters(camera: Camera?) {
        mParameters = camera?.parameters
        mParameters?.let {
            it.setPreviewFormat(ImageFormat.NV21)
            it.supportedPreviewFormats
            it.supportedPictureFormats
        }

        setPreviewSize()
        // setPictureSize()
    }

    private fun setPreviewSize() {
        val supportSizes = mParameters?.getSupportedPreviewSizes();
        var biggestSize: Camera.Size?  = null
        var fitSize: Camera.Size?  = null
        var targetSize: Camera.Size?  = null
        var targetSiz2: Camera.Size?  = null

        if (null != supportSizes) {
            for (i in 0 until supportSizes.size) {
                val size = supportSizes[i]
                Log.d(
                    "DEBUG",
                    "###### SupportedPreviewSizes: width=" + size.width + ", height=" + size.height
                );
                if (biggestSize == null ||
                    (size.width >= biggestSize.width && size.height >= biggestSize.height)
                ) {
                    biggestSize = size;
                }

                if (size.width == mWidth
                    && size.height == mHeight
                ) {
                    fitSize = size;
                    //如果任一宽或者高等于所支持的尺寸
                } else if (size.width == mWidth
                    || size.height == mHeight
                ) {
                    if (targetSize == null) {
                        targetSize = size;
                        //如果上面条件都不成立 如果任一宽高小于所支持的尺寸
                    } else if (size.width < mWidth
                        || size.height < mHeight
                    ) {
                        targetSiz2 = size;
                    }
                }
            }


            if (fitSize == null) {
                fitSize = targetSize;
            }

            if (fitSize == null) {
                fitSize = targetSiz2;
            }

            if (fitSize == null) {
                fitSize = biggestSize;
            }

            Log.d("DEBUG", "##### fitSize width: " + fitSize?.width + ", height: " + fitSize?.height);
            mParameters?.setPreviewSize(fitSize?.width ?: mWidth, fitSize?.height ?: mHeight);
        }
    }

    private fun isSupport(cameraId: Int): Boolean {
        val cameraInfo = Camera.CameraInfo()
        for (i in 0 until Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == cameraId) {
                return true
            }
        }
        return false
    }
}