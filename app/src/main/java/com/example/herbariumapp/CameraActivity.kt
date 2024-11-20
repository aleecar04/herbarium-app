package com.example.herbariumapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraView: TextureView
    private lateinit var backButton: Button
    private lateinit var captureButton: Button
    private lateinit var cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private lateinit var imageReader: ImageReader
    private val cameraId = CameraCharacteristics.LENS_FACING_BACK.toString()
    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cameraView = findViewById(R.id.cameraView)
        backButton = findViewById(R.id.btnBack)
        captureButton = findViewById(R.id.btnCapture)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            startCamera()
        }

        backButton.setOnClickListener {
            finish()
        }

        captureButton.setOnClickListener {
            capturePhoto()
        }
    }

    private fun startCamera() {
        cameraView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    private fun openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCameraPreview()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun createCameraPreview() {
        try {
            val texture = cameraView.surfaceTexture ?: return
            texture.setDefaultBufferSize(cameraView.width, cameraView.height)
            val surface = Surface(texture)

            imageReader = ImageReader.newInstance(cameraView.width, cameraView.height, ImageFormat.JPEG, 1)
            imageReader.setOnImageAvailableListener({ reader ->
                val image = reader.acquireNextImage()
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                saveImage(bytes)
                image.close()
            }, null)

            val captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(surface)

            cameraDevice?.createCaptureSession(listOf(surface, imageReader.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    if (cameraDevice == null) return
                    cameraCaptureSession = session
                    captureRequestBuilder?.build()?.let {
                        cameraCaptureSession?.setRepeatingRequest(it, null, null)
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Toast.makeText(this@CameraActivity, "Failed to configure camera", Toast.LENGTH_SHORT).show()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun capturePhoto() {
        try {
            val captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureRequestBuilder?.addTarget(imageReader.surface)

            cameraCaptureSession?.capture(captureRequestBuilder!!.build(), object : CameraCaptureSession.CaptureCallback() {}, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun saveImage(bytes: ByteArray) {
        val file = File(filesDir, "captured_image.jpg")
        FileOutputStream(file).use {
            it.write(bytes)
        }
        Toast.makeText(this, "Foto guardada en: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraDevice?.close()
        cameraCaptureSession?.close()
    }
}
