package com.fchrl.colormate

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.Locale
import kotlin.math.*

class ColorDetector(private val assetManager: AssetManager) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private var clusterToLabelMapping: Map<Int, String> = emptyMap()
    private var isInitialized = false
    private val inputBuffer = ByteBuffer.allocateDirect(4 * 15).order(ByteOrder.nativeOrder()) // Enhanced features
    private var outputBuffer: ByteBuffer? = null
    private var numClusters = 0

    // Enhanced K-means detector
    private val kMeansDetector = KMeansColorDetector()

    // Enhanced color calibration matrix dari train3.py
    private val calibrationMatrix = arrayOf(
        floatArrayOf(1.02f, -0.01f, 0.01f),
        floatArrayOf(-0.01f, 1.03f, -0.01f),
        floatArrayOf(0.01f, -0.01f, 1.02f)
    )

    // Enhanced normalization parameters
    private var featureScaler: Triple<FloatArray, FloatArray, Int>? = null // mean, std, feature_count

    init {
        try {
            loadModel()
            loadLabels()
            loadClusterMapping()
            loadEnhancedNormalizationParams()
            isInitialized = true
            Log.d("ColorDetector", "Enhanced Hybrid Color Detector initialized successfully")
        } catch (e: Exception) {
            Log.e("ColorDetector", "Failed to initialize AI model: ${e.message}")
            isInitialized = false
            Log.i("ColorDetector", "Fallback to enhanced K-means mode")
        }
    }

    private fun loadModel() {
        try {
            val modelFile = loadModelFile("enhanced_color_model.tflite")
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(modelFile, options)
            Log.d("ColorDetector", "AI model loaded successfully")

            val inputShape = interpreter?.getInputTensor(0)?.shape()
            val outputShape = interpreter?.getOutputTensor(0)?.shape()
            Log.d("ColorDetector", "Input shape: ${inputShape?.contentToString()}, Output shape: ${outputShape?.contentToString()}")

            // Initialize output buffer based on actual output shape
            numClusters = outputShape?.get(1) ?: 20
            outputBuffer = ByteBuffer.allocateDirect(4 * numClusters).order(ByteOrder.nativeOrder())

        } catch (e: Exception) {
            Log.e("ColorDetector", "Error loading AI model: ${e.message}")
            throw e
        }
    }

    private fun loadModelFile(filename: String): ByteBuffer {
        return try {
            val assetFileDescriptor: AssetFileDescriptor = assetManager.openFd(filename)
            val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel: FileChannel = fileInputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            fileInputStream.close()
            assetFileDescriptor.close()
            buffer
        } catch (e: IOException) {
            Log.e("ColorDetector", "Error loading model file: ${e.message}")
            throw e
        }
    }

    private fun loadLabels() {
        try {
            labels = assetManager.open("label_encoder_classes.txt").use { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    reader.readLines().filter { it.isNotBlank() }
                }
            }
            Log.d("ColorDetector", "Labels loaded: ${labels.size} items")
        } catch (e: Exception) {
            Log.e("ColorDetector", "Error loading labels: ${e.message}")
            // Fallback to default labels if file not found
            labels = listOf(
                "Merah", "Hijau", "Biru", "Kuning", "Ungu", "Jingga", "Merah Muda", "Cokelat",
                "Hitam", "Putih", "Abu-abu", "Abu-abu Gelap", "Abu-abu Terang", "Krem",
                "Cyan", "Magenta", "Hijau Gelap", "Biru Tua", "Kuning Hijau", "Cokelat Muda",
                "Merah Tua", "Hijau Pucat", "Biru Pucat", "Ungu Pucat", "Merah Muda Pucat"
            )
            Log.w("ColorDetector", "Using default enhanced labels")
        }
    }

    private fun loadClusterMapping() {
        try {
            val mappingLines = assetManager.open("cluster_mapping.txt").use { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    reader.readLines().filter { it.isNotBlank() }
                }
            }

            val mapping = mutableMapOf<Int, String>()
            for (line in mappingLines) {
                val parts = line.split(":")
                if (parts.size == 2) {
                    val clusterId = parts[0].trim().toIntOrNull()
                    val labelIndex = parts[1].trim().toIntOrNull()
                    if (clusterId != null && labelIndex != null && labelIndex < labels.size) {
                        mapping[clusterId] = labels[labelIndex]
                    }
                }
            }
            clusterToLabelMapping = mapping
            Log.d("ColorDetector", "Cluster mapping loaded: ${mapping.size} mappings")

        } catch (e: Exception) {
            Log.w("ColorDetector", "Could not load cluster mapping, creating default mapping: ${e.message}")
            // Create default mapping if file not found
            val defaultMapping = mutableMapOf<Int, String>()
            for (i in 0 until minOf(numClusters, labels.size)) {
                defaultMapping[i] = labels[i]
            }
            clusterToLabelMapping = defaultMapping
        }
    }

    private fun loadEnhancedNormalizationParams() {
        try {
            val paramLines = assetManager.open("enhanced_normalization_params.txt").use { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    reader.readLines().filter { it.isNotBlank() }
                }
            }

            if (paramLines.size >= 3) {
                val means = paramLines[0].split(",").map { it.trim().toFloat() }.toFloatArray()
                val stds = paramLines[1].split(",").map { it.trim().toFloat() }.toFloatArray()
                val featureCount = paramLines[2].trim().toInt()

                if (means.size == featureCount && stds.size == featureCount) {
                    featureScaler = Triple(means, stds, featureCount)
                    Log.d("ColorDetector", "Enhanced normalization loaded: ${featureCount} features")
                }
            }
        } catch (e: Exception) {
            Log.w("ColorDetector", "Using default enhanced normalization: ${e.message}")
            // Default untuk 15 fitur enhanced
            featureScaler = Triple(
                FloatArray(15) { 0.5f },
                FloatArray(15) { 0.5f },
                15
            )
        }
    }

    /**
     * Enhanced feature extraction seperti di train3.py
     */
    private fun createEnhancedFeatures(r: Float, g: Float, b: Float): FloatArray {
        val features = mutableListOf<Float>()

        // 1. Normalized RGB (3 features)
        features.add(r / 255f)
        features.add(g / 255f)
        features.add(b / 255f)

        // 2. HSV features (3 features)
        val (h, s, v) = rgbToHsv(r, g, b)
        features.add(h / 360f)
        features.add(s / 100f)
        features.add(v / 100f)

        // 3. LAB approximation (3 features)
        val (l, a, bLab) = rgbToLabApproximation(r, g, b)
        features.add(l)
        features.add(a)
        features.add(bLab)

        // 4. Color ratios (3 features)
        val total = r + g + b + 1f // Avoid division by zero
        features.add(r / total)
        features.add(g / total)
        features.add(b / total)

        // 5. Brightness and contrast (2 features)
        val brightness = (r + g + b) / 3f
        val contrast = sqrt(((r - brightness).pow(2) + (g - brightness).pow(2) + (b - brightness).pow(2)) / 3f)
        features.add(brightness / 255f)
        features.add(contrast / 255f)

        // 6. Dominant channel (1 feature)
        val dominantChannel = when {
            r >= g && r >= b -> 0f
            g >= r && g >= b -> 1f
            else -> 2f
        }
        features.add(dominantChannel / 2f)

        return features.toFloatArray()
    }

    /**
     * Enhanced confidence calculation berdasarkan multiple factors with strict capping
     */
    private fun calculateEnhancedConfidence(
        r: Float, g: Float, b: Float,
        dominantColorPercentage: Double,
        clusterConsistency: Double,
        aiConfidence: Float = 0f
    ): Double {
        val (h, s, v) = rgbToHsv(r, g, b)

        // Factor 1: Dominance (weight: 0.3), capped at 1.0
        val dominanceFactor = when {
            dominantColorPercentage >= 25.0 -> 1.0
            dominantColorPercentage >= 20.0 -> 0.9
            dominantColorPercentage >= 15.0 -> 0.8
            dominantColorPercentage >= 10.0 -> 0.7
            else -> 0.6
        }.coerceAtMost(1.0)

        // Factor 2: Consistency (weight: 0.25), capped at 1.0
        val consistencyFactor = clusterConsistency.coerceIn(0.0, 1.0)

        // PERBAIKAN: Saturation factor yang lebih baik untuk warna gelap, capped at 1.0
        val saturationFactor = when {
            s >= 50f -> 1.0
            s >= 30f -> 0.9
            s >= 20f -> 0.8
            s >= 10f -> 0.7
            s >= 5f -> 0.6
            else -> 0.5
        }.coerceAtMost(1.0)

        // PERBAIKAN: Brightness factor yang lebih baik untuk warna gelap, capped at 1.0
        val brightnessFactor = when {
            v >= 50f -> 1.0
            v >= 30f -> 0.9
            v >= 20f -> 0.8
            v >= 10f -> 0.7
            v >= 5f -> 0.6
            else -> 0.5
        }.coerceAtMost(1.0)

        // Factor 5: AI confidence boost, capped at 1.0
        val aiBoost = if (aiConfidence > 0.5f) 1.0 else 1.0 // Removed 1.1 boost to prevent overshoot

        // Weighted combination
        val baseConfidence = (dominanceFactor * 0.3 + consistencyFactor * 0.25 +
                saturationFactor * 0.25 + brightnessFactor * 0.2) * aiBoost

        // PERBAIKAN: Minimum confidence yang lebih baik untuk warna gelap
        val minimumConfidence = when {
            s > 30f && v > 30f -> 0.55 // Warna cerah
            s < 15f && v < 25f -> 0.50 // Warna gelap/grayscale
            else -> 0.45
        }

        return maxOf(baseConfidence, minimumConfidence).coerceIn(0.0, 1.0)
    }

    /**
     * Enhanced main detection method
     */
    fun detectColor(r: Int, g: Int, b: Int): String {
        return try {
            val rFloat = r.toFloat()
            val gFloat = g.toFloat()
            val bFloat = b.toFloat()

            // Apply enhanced calibration
            val (calibratedR, calibratedG, calibratedB) = applyEnhancedCalibration(rFloat, gFloat, bFloat)

            // Try AI model first with enhanced features
            if (isInitialized && interpreter != null) {
                try {
                    val aiResult = detectWithEnhancedAI(calibratedR, calibratedG, calibratedB)
                    if (aiResult.isNotBlank() && aiResult != "Unknown") {
                        return aiResult
                    }
                } catch (e: Exception) {
                    Log.w("ColorDetector", "Enhanced AI detection failed: ${e.message}")
                }
            }

            // Enhanced rule-based fallback
            getEnhancedRuleBasedColor(calibratedR, calibratedG, calibratedB)

        } catch (e: Exception) {
            Log.e("ColorDetector", "Error in enhanced color detection: ${e.message}")
            "Tidak Diketahui"
        }
    }

    /**
     * Enhanced AI detection with multiple features
     */
    private fun detectWithEnhancedAI(r: Float, g: Float, b: Float): String {
        val currentInterpreter = interpreter ?: return ""
        val currentOutputBuffer = outputBuffer ?: return ""

        try {
            // Create enhanced features
            val enhancedFeatures = createEnhancedFeatures(r, g, b)

            // Apply normalization
            val scaler = featureScaler
            if (scaler != null) {
                val (means, stds, _) = scaler
                for (i in enhancedFeatures.indices) {
                    enhancedFeatures[i] = (enhancedFeatures[i] - means[i]) / stds[i]
                }
            }

            // Prepare input buffer
            inputBuffer.rewind()
            enhancedFeatures.forEach { inputBuffer.putFloat(it) }

            // Run inference
            currentOutputBuffer.rewind()
            currentInterpreter.run(inputBuffer, currentOutputBuffer)

            // Parse output
            currentOutputBuffer.rewind()
            val probabilities = FloatArray(numClusters)
            currentOutputBuffer.asFloatBuffer().get(probabilities)

            // Find best cluster with confidence
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            val confidence = probabilities[maxIndex]

            // Enhanced confidence threshold
            return if (confidence > 0.4f) { // Lowered threshold, will be boosted by other factors
                clusterToLabelMapping[maxIndex] ?: getEnhancedRuleBasedColor(r, g, b)
            } else {
                getEnhancedRuleBasedColor(r, g, b)
            }

        } catch (e: Exception) {
            Log.w("ColorDetector", "Enhanced AI inference error: ${e.message}")
            return ""
        }
    }

    // --- PERBAIKAN UTAMA DIMULAI DI SINI ---
    /**
     * REVISED: Enhanced rule-based detection dengan logika yang lebih robust untuk warna gelap.
     */
    private fun getEnhancedRuleBasedColor(r: Float, g: Float, b: Float): String {
        val (h, s, v) = rgbToHsv(r, g, b)

        // 1. Prioritas utama: Jika sangat gelap, itu HITAM.
        if (v < 20f) {
            return "Hitam"
        }

        // 2. Cek Grayscale (selain hitam)
        if (s < 18f) {
            return when {
                v < 50f -> "Abu-abu Gelap"
                v < 75f -> "Abu-abu"
                v < 90f -> "Abu-abu Terang"
                else -> "Putih"
            }
        }

        // 3. Cek Warna Gelap (kecerahan rendah tapi masih berwarna)
        if (v < 50f) {
            return when (h) {
                in 0f..20f, in 340f..360f -> "Merah Tua"
                in 21f..45f -> "Cokelat"
                in 46f..150f -> "Hijau Gelap"
                in 151f..260f -> "Biru Tua"
                in 261f..339f -> "Ungu Tua"
                else -> "Cokelat"
            }
        }

        // 4. Cek Warna Pucat (saturasi rendah, tapi terang)
        if (s < 35f) {
            return when (h) {
                in 0f..25f, in 335f..360f -> "Merah Muda Pucat"
                in 26f..55f -> "Krem"
                in 56f..160f -> "Hijau Pucat"
                in 161f..270f -> "Biru Pucat"
                else -> "Ungu Pucat"
            }
        }

        // 5. Warna Standar (terang dan jenuh)
        return when (h) {
            in 0f..15f, in 345f..360f -> "Merah"
            in 16f..40f -> "Jingga"
            in 41f..65f -> "Kuning"
            in 66f..85f -> "Kuning Hijau"
            in 86f..160f -> "Hijau"
            in 161f..190f -> "Hijau Biru" // Cyan-ish Green
            in 191f..220f -> "Cyan"
            in 221f..260f -> "Biru"
            in 261f..300f -> "Ungu"
            in 301f..330f -> "Magenta"
            else -> "Merah Muda"
        }
    }
    // --- PERBAIKAN UTAMA SELESAI DI SINI ---


    /**
     * Enhanced bitmap detection dengan confidence calculation
     */
    fun detectColorFromBitmap(bitmap: Bitmap): ColorDetectionResult {
        return try {
            val kMeansResults = kMeansDetector.detectDominantColorsEnhanced(bitmap)

            if (kMeansResults.isEmpty()) {
                // Fallback dengan enhanced confidence
                val centerX = bitmap.width / 2
                val centerY = bitmap.height / 2
                val centerPixel = bitmap.getPixel(centerX, centerY)
                val r = android.graphics.Color.red(centerPixel)
                val g = android.graphics.Color.green(centerPixel)
                val b = android.graphics.Color.blue(centerPixel)

                val colorName = detectColor(r, g, b)
                val confidence = calculateEnhancedConfidence(
                    r.toFloat(), g.toFloat(), b.toFloat(),
                    100.0, 0.8, 0.6f
                )

                ColorDetectionResult(
                    primaryColor = colorName,
                    confidence = confidence,
                    dominantColors = listOf(
                        DominantColor(colorName, Triple(r.toDouble(), g.toDouble(), b.toDouble()), 100.0)
                    ),
                    rgbValues = Triple(r, g, b)
                )
            } else {
                val primaryResult = kMeansResults.first()
                val confidence = calculateEnhancedConfidence(
                    primaryResult.dominantColor.first.toFloat(),
                    primaryResult.dominantColor.second.toFloat(),
                    primaryResult.dominantColor.third.toFloat(),
                    primaryResult.percentage,
                    primaryResult.clusterConsistency,
                    0.7f
                )

                ColorDetectionResult(
                    primaryColor = primaryResult.colorName,
                    confidence = confidence,
                    dominantColors = kMeansResults.map { result ->
                        DominantColor(
                            name = result.colorName,
                            rgb = result.dominantColor,
                            percentage = result.percentage
                        )
                    },
                    rgbValues = Triple(
                        primaryResult.dominantColor.first.toInt(),
                        primaryResult.dominantColor.second.toInt(),
                        primaryResult.dominantColor.third.toInt()
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("ColorDetector", "Error in enhanced bitmap detection: ${e.message}")
            ColorDetectionResult(
                primaryColor = "Tidak Diketahui",
                confidence = 0.0,
                dominantColors = emptyList(),
                rgbValues = Triple(0, 0, 0)
            )
        }
    }

    /**
     * Detect color in specific region of bitmap
     */
    fun detectColorInRegion(bitmap: Bitmap, centerX: Int, centerY: Int, regionSize: Int = 60): ColorDetectionResult {
        return try {
            val result = kMeansDetector.detectColorInRegionEnhanced(bitmap, centerX, centerY, regionSize)
            if (result != null) {
                val confidence = calculateEnhancedConfidence(
                    result.dominantColor.first.toFloat(),
                    result.dominantColor.second.toFloat(),
                    result.dominantColor.third.toFloat(),
                    result.percentage,
                    result.clusterConsistency,
                    0.6f
                )

                ColorDetectionResult(
                    primaryColor = result.colorName,
                    confidence = confidence,
                    dominantColors = listOf(
                        DominantColor(
                            name = result.colorName,
                            rgb = result.dominantColor,
                            percentage = result.percentage
                        )
                    ),
                    rgbValues = Triple(
                        result.dominantColor.first.toInt(),
                        result.dominantColor.second.toInt(),
                        result.dominantColor.third.toInt()
                    )
                )
            } else {
                // Fallback to single pixel
                if (centerX in 0 until bitmap.width && centerY in 0 until bitmap.height) {
                    val pixel = bitmap.getPixel(centerX, centerY)
                    val r = android.graphics.Color.red(pixel)
                    val g = android.graphics.Color.green(pixel)
                    val b = android.graphics.Color.blue(pixel)
                    val colorName = detectColor(r, g, b)
                    val confidence = calculateEnhancedConfidence(
                        r.toFloat(), g.toFloat(), b.toFloat(),
                        100.0, 0.8, 0.5f
                    )

                    ColorDetectionResult(
                        primaryColor = colorName,
                        confidence = confidence,
                        dominantColors = listOf(
                            DominantColor(colorName, Triple(r.toDouble(), g.toDouble(), b.toDouble()), 100.0)
                        ),
                        rgbValues = Triple(r, g, b)
                    )
                } else {
                    ColorDetectionResult(
                        primaryColor = "Tidak Diketahui",
                        confidence = 0.0,
                        dominantColors = emptyList(),
                        rgbValues = Triple(0, 0, 0)
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("ColorDetector", "Error detecting color in region: ${e.message}")
            ColorDetectionResult(
                primaryColor = "Tidak Diketahui",
                confidence = 0.0,
                dominantColors = emptyList(),
                rgbValues = Triple(0, 0, 0)
            )
        }
    }

    /**
     * Get formatted description for color blind users
     */
    fun getColorBlindFriendlyDescription(result: ColorDetectionResult): String {
        val primary = result.primaryColor
        val confidence = String.format(Locale("id", "ID"), "%.1f", result.confidence * 100)

        val description = StringBuilder()
        description.append("🎨 Warna utama: $primary")
        description.append("\n📊 Keyakinan: $confidence%")

        if (result.dominantColors.size > 1) {
            description.append("\nWarna dominan lainnya:")
            result.dominantColors.drop(1).take(2).forEach { color ->
                description.append("\n- ${color.name} (${String.format(Locale("id", "ID"), "%.1f", color.percentage)}%)")
            }
        }

        // DITAMBAHKAN: Contextual descriptions yang sama lengkap dengan KMeansColorDetector
        when (primary.lowercase()) {
            "merah" -> description.append("\n💡 Warna seperti buah tomat matang")
            "hijau" -> description.append("\n💡 Warna seperti rumput")
            "biru" -> description.append("\n💡 Warna seperti langit cerah tanpa awan")
            "kuning" -> description.append("\n💡 Warna seperti kulit pisang matang")
            "ungu" -> description.append("\n💡 Warna seperti kulit buah terong")
            "jingga", "oranye" -> description.append("\n💡 Warna seperti buah jeruk mandarin matang")
            "merah muda" -> description.append("\n💡 Warna seperti bunga mawar muda")
            "cokelat" -> description.append("\n💡 Warna seperti tanah liat yang lembab")
            "hitam" -> description.append("\n💡 Warna seperti bulu burung gagak")
            "putih" -> description.append("\n💡 Warna seperti salju")
            "abu-abu" -> description.append("\n💡 Warna seperti batu kali yang kering")
            "abu-abu gelap" -> description.append("\n💡 Warna seperti batu basalt")
            "abu-abu terang" -> description.append("\n💡 Warna seperti batu kerikil halus berpasir")
            "krem" -> description.append("\n💡 Warna seperti pasir pantai yang halus")
            "cyan" -> description.append("\n💡 Warna seperti air laut jernih di pantai tropis")
            "magenta" -> description.append("\n💡 Warna seperti buah delima matang")
            "hijau gelap" -> description.append("\n💡 Warna seperti daun pinus atau lumut tua")
            "biru tua" -> description.append("\n💡 Warna seperti air danau yang dalam")
            "kuning hijau" -> description.append("\n💡 Warna seperti daun muda yang mulai tumbuh")
            "cokelat muda" -> description.append("\n💡 Warna seperti batang pohon cemara muda")
            "merah tua" -> description.append("\n💡 Warna seperti buah ceri ranum")
            "hijau pucat" -> description.append("\n💡 Warna seperti daging melon yang matang muda")
            "biru pucat" -> description.append("\n💡 Warna seperti sayap kupu-kupu biru muda")
            "ungu pucat" -> description.append("\n💡 Warna seperti kelopak bunga violet yang sangat muda")
            "merah muda pucat" -> description.append("\n💡 Warna seperti kelopak bunga sakura muda")
            "hijau biru" -> description.append("\n💡 Warna seperti permukaan laut di perairan dangkal")
            "ungu tua" -> description.append("\n💡 Warna seperti buah anggur sangat matang")
        }

        return description.toString()
    }

    // Helper functions
    private fun applyEnhancedCalibration(r: Float, g: Float, b: Float): Triple<Float, Float, Float> {
        val inputRGB = floatArrayOf(r, g, b)
        val calibratedRGB = floatArrayOf(0f, 0f, 0f)

        for (i in 0..2) {
            for (j in 0..2) {
                calibratedRGB[i] += inputRGB[j] * calibrationMatrix[j][i]
            }
        }

        return Triple(
            calibratedRGB[0].coerceIn(0f, 255f),
            calibratedRGB[1].coerceIn(0f, 255f),
            calibratedRGB[2].coerceIn(0f, 255f)
        )
    }

    private fun rgbToHsv(r: Float, g: Float, b: Float): Triple<Float, Float, Float> {
        val rNorm = r / 255f
        val gNorm = g / 255f
        val bNorm = b / 255f

        val max = maxOf(rNorm, gNorm, bNorm)
        val min = minOf(rNorm, gNorm, bNorm)
        val delta = max - min

        val hue = when {
            delta == 0f -> 0f
            max == rNorm -> 60f * (((gNorm - bNorm) / delta) % 6f)
            max == gNorm -> 60f * (((bNorm - rNorm) / delta) + 2f)
            else -> 60f * (((rNorm - gNorm) / delta) + 4f)
        }.let { if (it < 0) it + 360f else it }

        val saturation = if (max == 0f) 0f else (delta / max) * 100f
        val value = max * 100f

        return Triple(hue, saturation, value)
    }

    private fun rgbToLabApproximation(r: Float, g: Float, b: Float): Triple<Float, Float, Float> {
        val rNorm = r / 255f
        val gNorm = g / 255f
        val bNorm = b / 255f

        val l = 0.2126f * rNorm + 0.7152f * gNorm + 0.0722f * bNorm
        val a = (rNorm - gNorm) * 0.5f
        val bLab = (gNorm - bNorm) * 0.5f

        return Triple(l, a, bLab)
    }

    /**
     * Data classes for structured results
     */
    data class ColorDetectionResult(
        val primaryColor: String,
        val confidence: Double,
        val dominantColors: List<DominantColor>,
        val rgbValues: Triple<Int, Int, Int>
    )

    data class DominantColor(
        val name: String,
        val rgb: Triple<Double, Double, Double>,
        val percentage: Double
    )

    /**
     * Check if detector is ready
     */
    fun isReady(): Boolean = isInitialized || true // K-means always available as fallback

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            interpreter?.close()
            interpreter = null
            isInitialized = false
            Log.d("ColorDetector", "ColorDetector cleaned up")
        } catch (e: Exception) {
            Log.e("ColorDetector", "Error during cleanup: ${e.message}")
        }
    }
}