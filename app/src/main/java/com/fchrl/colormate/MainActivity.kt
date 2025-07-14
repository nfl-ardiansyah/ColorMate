package com.fchrl.colormate

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fchrl.colormate.databinding.ActivityMainBinding
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var colorDetector: ColorDetector? = null
    private lateinit var cameraExecutor: ExecutorService
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Executor untuk CameraX
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Inisialisasi ColorDetector (TFLite + K-means)
        initializeColorDetector()

        // Setup UI listeners
        setupClickListeners()

        // Setup accessibility features
        setupAccessibilityFeatures()

        // Request camera permission
        requestCameraPermission()
    }

    private fun initializeColorDetector() {
        try {
            colorDetector = ColorDetector(assets)
            Log.d("MainActivity", "ColorDetector initialized successfully")
            Toast.makeText(this, "Detektor warna siap digunakan", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing ColorDetector: ${e.message}")
            Toast.makeText(this, "Error menginisialisasi detektor warna", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        // Tombol deteksi warna manual (center)
        binding.detectButton.setOnClickListener {
            if (colorDetector == null || !colorDetector!!.isReady()) {
                Toast.makeText(this, "Detektor warna belum siap...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            detectColorAtCenter()
        }

        binding.backBtn.setOnClickListener {
            finish() // Tutup activity ini dan kembali ke activity sebelumnya
        }
    }

    private fun setupAccessibilityFeatures() {
        // Setup content descriptions untuk aksesibilitas
        binding.detectButton.contentDescription = "Tombol deteksi warna di tengah layar"
        binding.previewView.contentDescription = "Pratinjau kamera untuk deteksi warna. Tekan tombol untuk mendeteksi warna di tengah layar"
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                } else {
                    Toast.makeText(this, "Izin kamera diperlukan untuk aplikasi ini", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (exc: Exception) {
                Log.e("CameraX", "Failed to get camera provider", exc)
                Toast.makeText(this, "Gagal memulai kamera: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = this.cameraProvider ?: return

        try {
            // Konfigurasi Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // Pilih kamera belakang
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Konfigurasi ImageAnalysis
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Unbind semua use case sebelum mengikat ulang
            cameraProvider.unbindAll()

            // Bind use case ke lifecycle
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalysis
            )

            Log.d("CameraX", "Camera started successfully")
        } catch (exc: Exception) {
            Log.e("CameraX", "Failed to bind camera use cases", exc)
            Toast.makeText(this, "Gagal memulai kamera: ${exc.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun captureCurrentFrame(): Bitmap? {
        return try {
            binding.previewView.bitmap
        } catch (e: Exception) {
            Log.e("CaptureFrame", "Error capturing frame: ${e.message}")
            null
        }
    }

    private fun detectColorAtCenter() {
        val bitmap = captureCurrentFrame()
        if (bitmap == null) {
            Toast.makeText(this, "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
            return
        }

        val centerX = bitmap.width / 2
        val centerY = bitmap.height / 2
        detectColorAtPosition(bitmap, centerX, centerY)
    }

    private fun detectColorAtPosition(bitmap: Bitmap, x: Int, y: Int) {
        binding.detectButton.isEnabled = false

        // Show loading indicator
        binding.resultTextView.text = "Mendeteksi warna..."

        // Process di background thread
        Thread {
            try {
                val detector = colorDetector
                if (detector == null || !detector.isReady()) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Detektor warna belum siap", Toast.LENGTH_SHORT).show()
                        resetDetectionState()
                    }
                    return@Thread
                }

                // Deteksi warna di region yang ditentukan menggunakan ColorDetector
                val result = detector.detectColorInRegion(bitmap, x, y, 50)

                runOnUiThread {
                    if (result.primaryColor != "Tidak Diketahui") {
                        displayColorResult(result)
                    } else {
                        Toast.makeText(this@MainActivity, "Gagal mendeteksi warna", Toast.LENGTH_SHORT).show()
                        binding.resultTextView.text = "Gagal mendeteksi warna"
                    }
                    resetDetectionState()
                }
            } catch (e: Exception) {
                Log.e("ColorDetection", "Error detecting color: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.resultTextView.text = "Error dalam deteksi warna"
                    resetDetectionState()
                }
            }
        }.start()
    }

    private fun displayColorResult(result: ColorDetector.ColorDetectionResult) {
        // Update UI dengan hasil deteksi
        val resultText = colorDetector?.getColorBlindFriendlyDescription(result) ?: "Gagal menghasilkan deskripsi"
        binding.resultTextView.text = resultText
        binding.resultTextView.contentDescription = resultText // Untuk TalkBack

        // Log untuk debugging
        Log.d("ColorDetection", "Detected: ${result.primaryColor} (Keyakinan: ${String.format(Locale("id", "ID"), "%.1f", result.confidence * 100)}%)")
    }

    private fun resetDetectionState() {
        binding.detectButton.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()

        // Clean up resources
        try {
            // Clean up camera
            cameraProvider?.unbindAll()
            cameraExecutor.shutdown()

            // Clean up ColorDetector
            colorDetector?.cleanup()
            colorDetector = null

        } catch (e: Exception) {
            Log.e("MainActivity", "Error during cleanup: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        // Reset detection state saat aplikasi tidak aktif
        resetDetectionState()
        binding.resultTextView.text = "Aplikasi dijeda..."
    }

    override fun onResume() {
        super.onResume()
        // Rebind camera saat resume
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraProvider?.let { bindCameraUseCases() }
        }

        // Reset UI
        binding.resultTextView.text = "Tekan tombol untuk mendeteksi warna"
    }
}