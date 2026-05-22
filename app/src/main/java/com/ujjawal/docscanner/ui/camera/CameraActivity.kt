package com.ujjawal.docscanner.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ujjawal.docscanner.databinding.ActivityCameraBinding
import com.ujjawal.docscanner.ui.editor.EditorActivity
import com.ujjawal.docscanner.ui.gallery.GalleryActivity
import com.ujjawal.docscanner.utils.EdgeDetector
import org.opencv.android.OpenCVLoader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var flashOn = false
    private lateinit var cameraExecutor: ExecutorService
    private var selectedTab: TextView? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { loadImageFromGallery(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!OpenCVLoader.initLocal()) {
            Toast.makeText(this, "OpenCV init failed", Toast.LENGTH_LONG).show()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        binding.btnCapture.setOnClickListener { captureImage() }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnImport.setOnClickListener { pickImage.launch("image/*") }
        binding.btnGallery.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }
        binding.btnFlash.setOnClickListener { toggleFlash() }

        // Scan mode tabs
        setupTabs()
    }

    private fun setupTabs() {
        val tabs = listOf(binding.tabPhoto, binding.tabIdCard, binding.tabDocument, binding.tabBook, binding.tabQrScan)
        selectedTab = binding.tabDocument
        tabs.forEach { tab ->
            tab.setOnClickListener {
                selectedTab?.let { prev ->
                    prev.setTextColor(0xFFAAAAAA.toInt())
                    prev.setTypeface(null, android.graphics.Typeface.NORMAL)
                }
                tab.setTextColor(ContextCompat.getColor(this, com.ujjawal.docscanner.R.color.primary_light))
                tab.setTypeface(null, android.graphics.Typeface.BOLD)
                selectedTab = tab
            }
        }
    }

    private fun toggleFlash() {
        flashOn = !flashOn
        camera?.cameraControl?.enableTorch(flashOn)
    }

    private fun loadImageFromGallery(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap != null) {
                ImageHolder.bitmap = bitmap
                startActivity(Intent(this, EditorActivity::class.java))
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                analyzeFrame(imageProxy)
            }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture, imageAnalysis
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Camera init failed", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun analyzeFrame(imageProxy: ImageProxy) {
        try {
            val bitmap = imageProxy.toBitmap()
            val rotation = imageProxy.imageInfo.rotationDegrees
            val corrected = if (rotation != 0) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else bitmap

            val corners = EdgeDetector.detectEdges(corrected)

            // Scale corners to overlay dimensions
            val overlayW = binding.edgeOverlay.width.toFloat()
            val overlayH = binding.edgeOverlay.height.toFloat()
            if (overlayW > 0 && overlayH > 0) {
                val scaleX = overlayW / corrected.width
                val scaleY = overlayH / corrected.height
                val scaled = corners.map { PointF(it.x * scaleX, it.y * scaleY) }.toTypedArray()
                runOnUiThread { binding.edgeOverlay.setCorners(scaled) }
            }
        } catch (_: Exception) {
            runOnUiThread { binding.edgeOverlay.setCorners(null) }
        } finally {
            imageProxy.close()
        }
    }

    private fun captureImage() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                val bitmap = imageProxy.toBitmap()
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                imageProxy.close()

                val correctedBitmap = if (rotationDegrees != 0) {
                    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                } else bitmap

                ImageHolder.bitmap = correctedBitmap
                startActivity(Intent(this@CameraActivity, EditorActivity::class.java))
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(this@CameraActivity, "Capture failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) startCamera()
            else { Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show(); finish() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}

object ImageHolder {
    var bitmap: Bitmap? = null
}
