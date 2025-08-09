package com.fchrl.colormate

import android.graphics.Bitmap
import android.util.Log
import java.util.Locale
import kotlin.math.*
import kotlin.random.Random

/**
 * Enhanced K-Means Color Detector dengan confidence >50%
 * Berdasarkan insights dari train3.py dan research color detection
 */
class KMeansColorDetector(
    private val kClusters: Int = 6, // Optimal berdasarkan research
    private val maxIterations: Int = 30,
    private val epsilon: Double = 0.5,
    private val maxSampleSize: Int = 500,
    private val minSaturationThreshold: Float = 5f, // Minimum untuk confidence
    private val minBrightnessThreshold: Float = 5f
) {

    companion object {
        private const val TAG = "EnhancedKMeansColorDetector"
    }

    /**
     * Enhanced ColorResult dengan cluster consistency
     */
    data class ColorResult(
        val colorName: String,
        val dominantColor: Triple<Double, Double, Double>,
        var percentage: Double,
        val clusterConsistency: Double, // New field untuk confidence calculation
        val saturationLevel: Float,
        val brightnessLevel: Float,
        val hexColor: String = String.format(
            "#%02X%02X%02X",
            dominantColor.first.toInt().coerceIn(0, 255),
            dominantColor.second.toInt().coerceIn(0, 255),
            dominantColor.third.toInt().coerceIn(0, 255)
        ),
        val description: String,
        val confidenceScore: Double // Enhanced confidence score
    )

    private data class PixelRGB(val r: Double, val g: Double, val b: Double)
    private data class ClusterCenter(var r: Double, var g: Double, var b: Double) {
        fun distanceTo(pixel: PixelRGB): Double {
            return sqrt((r - pixel.r).pow(2) + (g - pixel.g).pow(2) + (b - pixel.b).pow(2))
        }

        fun update(pixels: List<PixelRGB>) {
            if (pixels.isEmpty()) return
            r = pixels.map { it.r }.average()
            g = pixels.map { it.g }.average()
            b = pixels.map { it.b }.average()
        }
    }

    /**
     * Enhanced dominant color detection dengan confidence calculation
     */
    fun detectDominantColorsEnhanced(bitmap: Bitmap): List<ColorResult> {
        try {
            Log.d(TAG, "Starting enhanced K-means detection on ${bitmap.width}x${bitmap.height}")

            val pixels = extractEnhancedPixels(bitmap)
            if (pixels.isEmpty()) return emptyList()

            val clusters = performEnhancedKMeans(pixels)
            val results = convertToEnhancedColorResults(clusters, pixels.size)

            return results.filter { it.confidenceScore >= 0.5 } // Only return high confidence results
                .sortedByDescending { it.percentage }

        } catch (e: Exception) {
            Log.e(TAG, "Error in enhanced K-means detection: ${e.message}", e)
            return emptyList()
        }
    }

    /**
     * Backward compatibility method
     */
    fun detectDominantColors(bitmap: Bitmap): List<ColorResult> {
        return detectDominantColorsEnhanced(bitmap)
    }

    /**
     * Enhanced pixel extraction dengan intelligent sampling
     */
    private fun extractEnhancedPixels(bitmap: Bitmap): List<PixelRGB> {
        val pixels = mutableListOf<PixelRGB>()

        val scaleFactor = minOf(1.0, sqrt(maxSampleSize.toDouble() / (bitmap.width * bitmap.height)))
        val scaledWidth = (bitmap.width * scaleFactor).toInt()
        val scaledHeight = (bitmap.height * scaleFactor).toInt()

        val scaledBitmap = if (scaleFactor < 1.0) {
            Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
        } else {
            bitmap
        }

        try {
            val sampleRate = maxOf(1, (scaledBitmap.width * scaledBitmap.height) / maxSampleSize)
            val gridSize = sqrt(sampleRate.toDouble()).toInt()

            for (y in 0 until scaledBitmap.height step gridSize) {
                for (x in 0 until scaledBitmap.width step gridSize) {
                    val pixel = scaledBitmap.getPixel(x, y)
                    val r = android.graphics.Color.red(pixel).toDouble()
                    val g = android.graphics.Color.green(pixel).toDouble()
                    val b = android.graphics.Color.blue(pixel).toDouble()

                    // --- PERBAIKAN: Logika filter piksel disederhanakan agar lebih inklusif ---
                    // Cukup pastikan piksel tidak benar-benar transparan, K-Means akan mengelompokkan sisanya.
                    if (android.graphics.Color.alpha(pixel) > 50) {
                        pixels.add(PixelRGB(r, g, b))
                    }
                }
            }

        } finally {
            if (scaledBitmap != bitmap && !scaledBitmap.isRecycled) {
                scaledBitmap.recycle()
            }
        }

        Log.d(TAG, "Extracted ${pixels.size} enhanced pixels")
        return pixels
    }

    /**
     * Enhanced K-means dengan improved initialization
     */
    private fun performEnhancedKMeans(pixels: List<PixelRGB>): Map<Int, List<PixelRGB>> {
        if (pixels.isEmpty()) return emptyMap()

        // K-means++ initialization untuk better convergence
        val centers = initializeKMeansPlusPlus(pixels)
        val assignments = IntArray(pixels.size)
        var bestCenters = centers.map { it.copy() }
        var bestInertia = Double.MAX_VALUE

        // Multiple runs untuk stability
        repeat(3) { run ->
            val currentCenters = if (run == 0) centers else initializeKMeansPlusPlus(pixels)

            for (iteration in 0 until maxIterations) {
                var changed = false

                // Assign pixels to clusters
                for (i in pixels.indices) {
                    val pixel = pixels[i]
                    val closestCluster = currentCenters.indices.minByOrNull {
                        currentCenters[it].distanceTo(pixel)
                    } ?: 0

                    if (assignments[i] != closestCluster) {
                        assignments[i] = closestCluster
                        changed = true
                    }
                }

                // Update centers
                val oldCenters = currentCenters.map { it.copy() }
                for (clusterIndex in currentCenters.indices) {
                    val clusterPixels = pixels.filterIndexed { index, _ ->
                        assignments[index] == clusterIndex
                    }
                    if (clusterPixels.isNotEmpty()) {
                        currentCenters[clusterIndex].update(clusterPixels)
                    }
                }

                // Check convergence
                val centerShift = currentCenters.zip(oldCenters).sumOf { (new, old) ->
                    sqrt((new.r - old.r).pow(2) + (new.g - old.g).pow(2) + (new.b - old.b).pow(2))
                }

                if (centerShift < epsilon) break
                if (!changed && iteration > 5) break
            }

            // Calculate inertia for this run
            val inertia = pixels.indices.sumOf { i ->
                val clusterIndex = assignments[i]
                currentCenters[clusterIndex].distanceTo(pixels[i]).pow(2)
            }

            if (inertia < bestInertia) {
                bestInertia = inertia
                bestCenters = currentCenters.map { it.copy() }
            }
        }

        // Final assignment dengan best centers
        for (i in pixels.indices) {
            val pixel = pixels[i]
            assignments[i] = bestCenters.indices.minByOrNull {
                bestCenters[it].distanceTo(pixel)
            } ?: 0
        }

        val clusters = mutableMapOf<Int, List<PixelRGB>>()
        for (i in pixels.indices) {
            val clusterIndex = assignments[i]
            clusters[clusterIndex] = clusters.getOrDefault(clusterIndex, emptyList()) + pixels[i]
        }

        return clusters
    }

    /**
     * K-means++ initialization untuk better cluster centers
     */
    private fun initializeKMeansPlusPlus(pixels: List<PixelRGB>): MutableList<ClusterCenter> {
        if (pixels.isEmpty()) return mutableListOf()
        val centers = mutableListOf<ClusterCenter>()

        // First center: random
        val firstPixel = pixels.random()
        centers.add(ClusterCenter(firstPixel.r, firstPixel.g, firstPixel.b))

        // Subsequent centers: weighted by distance
        for (i in 1 until kClusters) {
            val distances = pixels.map { pixel ->
                centers.minOf { center -> center.distanceTo(pixel).pow(2) }
            }

            val totalDistance = distances.sum()
            if (totalDistance == 0.0) break

            val probabilities = distances.map { it / totalDistance }
            val cumulativeProbabilities = probabilities.runningReduce { acc, prob -> acc + prob }

            val randomValue = Random.nextDouble()
            val selectedIndex = cumulativeProbabilities.indexOfFirst { it >= randomValue }
                .takeIf { it >= 0 } ?: (pixels.size - 1)

            val selectedPixel = pixels[selectedIndex]
            centers.add(ClusterCenter(selectedPixel.r, selectedPixel.g, selectedPixel.b))
        }

        return centers
    }

    /**
     * Convert clusters to enhanced ColorResult dengan confidence calculation and strictly normalized percentage
     */
    private fun convertToEnhancedColorResults(
        clusters: Map<Int, List<PixelRGB>>,
        totalSampledPixels: Int
    ): List<ColorResult> {
        val results = mutableListOf<ColorResult>()

        for ((_, clusterPixels) in clusters) {
            if (clusterPixels.isEmpty()) continue

            val avgR = clusterPixels.map { it.r }.average()
            val avgG = clusterPixels.map { it.g }.average()
            val avgB = clusterPixels.map { it.b }.average()
            var percentage = (clusterPixels.size.toDouble() / totalSampledPixels) * 100

            // Calculate cluster consistency
            val centerPoint = PixelRGB(avgR, avgG, avgB)
            val distances = clusterPixels.map {
                sqrt((it.r - avgR).pow(2) + (it.g - avgG).pow(2) + (it.b - avgB).pow(2))
            }
            val averageDistance = distances.average()
            val maxDistance = distances.maxOrNull() ?: 0.0
            val consistency = if (maxDistance > 0) 1.0 - (averageDistance / maxDistance) else 1.0

            val (_, s, v) = rgbToHsv(avgR, avgG, avgB)
            val colorName = mapRGBToEnhancedColorName(avgR.toFloat(), avgG.toFloat(), avgB.toFloat())

            // Enhanced confidence calculation
            val confidenceScore = calculateClusterConfidence(percentage, consistency, s, v)

            val description = getEnhancedColorDescription(colorName, percentage, confidenceScore)

            results.add(
                ColorResult(
                    colorName = colorName,
                    dominantColor = Triple(avgR, avgG, avgB),
                    percentage = percentage,
                    clusterConsistency = consistency,
                    saturationLevel = s,
                    brightnessLevel = v,
                    description = description,
                    confidenceScore = confidenceScore
                )
            )
        }

        // Strictly normalize percentages to sum to 100%
        if (results.isNotEmpty()) {
            val totalPercentage = results.sumOf { it.percentage }
            if (totalPercentage > 0) {
                results.forEach { it.percentage = (it.percentage / totalPercentage) * 100 }
            } else {
                // If total is zero (unlikely), distribute evenly
                val evenPercentage = 100.0 / results.size
                results.forEach { it.percentage = evenPercentage }
            }
        }

        return results
    }

    /**
     * Enhanced confidence calculation berdasarkan multiple factors
     */
    private fun calculateClusterConfidence(
        percentage: Double,
        consistency: Double,
        saturation: Float,
        brightness: Float
    ): Double {
        // Factor 1: Dominance (weight: 0.3)
        val dominanceFactor = when {
            percentage >= 30.0 -> 1.0
            percentage >= 20.0 -> 0.9
            percentage >= 15.0 -> 0.8
            percentage >= 10.0 -> 0.7
            else -> 0.6
        }

        // Factor 2: Consistency (weight: 0.25)
        val consistencyFactor = consistency.coerceIn(0.0, 1.0)

        // PERBAIKAN: Saturation factor yang lebih baik untuk warna gelap
        val saturationFactor = when {
            saturation >= 50f -> 1.0
            saturation >= 30f -> 0.9
            saturation >= 20f -> 0.8
            saturation >= 10f -> 0.7
            saturation >= 5f -> 0.6
            else -> 0.5 // Untuk warna grayscale, tetap berikan confidence yang wajar
        }

        // PERBAIKAN: Brightness factor yang lebih baik untuk warna gelap
        val brightnessFactor = when {
            brightness >= 50f -> 1.0
            brightness >= 30f -> 0.9
            brightness >= 20f -> 0.8
            brightness >= 10f -> 0.7
            brightness >= 5f -> 0.6
            else -> 0.5 // Untuk warna hitam, tetap berikan confidence yang wajar
        }

        // Weighted combination
        val baseConfidence = dominanceFactor * 0.3 + consistencyFactor * 0.25 +
                saturationFactor * 0.25 + brightnessFactor * 0.2

        // Boost sedikit untuk warna yang terdefinisi dengan baik
        val colorBoost = if (saturation > 25f && brightness > 25f) 1.05 else 1.0

        return (baseConfidence * colorBoost).coerceIn(0.0, 1.0)
    }

    // --- PERBAIKAN UTAMA DIMULAI DI SINI ---
    /**
     * REVISED: Menggunakan logika yang sama persis dengan ColorDetector.kt untuk konsistensi.
     */
    private fun mapRGBToEnhancedColorName(r: Float, g: Float, b: Float): String {
        val (h, s, v) = rgbToHsv(r.toDouble(), g.toDouble(), b.toDouble())

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
            in 161f..190f -> "Hijau Biru"
            in 191f..220f -> "Cyan"
            in 221f..260f -> "Biru"
            in 261f..300f -> "Ungu"
            in 301f..330f -> "Magenta"
            else -> "Merah Muda"
        }
    }


    /**
     * Enhanced color description dengan confidence information
     */
    private fun getEnhancedColorDescription(
        colorName: String,
        percentage: Double,
        confidence: Double
    ): String {
        val description = StringBuilder()
        description.append("🎨 Warna: $colorName")
        description.append("\n📊 Dominasi: ${String.format(Locale("id", "ID"), "%.1f", percentage)}%")
        description.append("\n🎯 Keyakinan: ${String.format(Locale("id", "ID"), "%.1f", confidence * 100)}%")

        // Add contextual descriptions
        when (colorName.lowercase()) {
            "merah" -> description.append("\n💡 Seperti warna darah atau tomat matang")
            "hijau" -> description.append("\n💡 Seperti warna daun atau rumput")
            "biru" -> description.append("\n💡 Seperti warna langit atau laut")
            "kuning" -> description.append("\n💡 Seperti warna matahari atau pisang")
            "jingga" -> description.append("\n💡 Seperti warna jeruk atau sunset")
            "ungu" -> description.append("\n💡 Seperti warna lavender atau anggur")
            "hitam" -> description.append("\n💡 Warna gelap seperti malam")
            "putih" -> description.append("\n💡 Warna terang seperti salju")
            "cokelat" -> description.append("\n💡 Seperti warna kayu atau tanah")
            "abu-abu" -> description.append("\n💡 Warna netral antara hitam dan putih")
            "cyan" -> description.append("\n💡 Seperti warna air laut tropis")
            "magenta" -> description.append("\n💡 Seperti warna bunga fuchsia")
            "krem" -> description.append("\n💡 Seperti warna krim atau vanilla")
            "kuning hijau" -> description.append("\n💡 Seperti warna daun muda")
            "biru muda" -> description.append("\n💡 Seperti warna langit cerah")
            "merah muda" -> description.append("\n💡 Seperti warna bunga sakura")
            "biru ungu" -> description.append("\n💡 Transisi antara biru dan ungu")
        }

        return description.toString()
    }

    // Helper functions
    private fun rgbToHsv(r: Double, g: Double, b: Double): Triple<Float, Float, Float> {
        val rNorm = r / 255.0
        val gNorm = g / 255.0
        val bNorm = b / 255.0

        val maxVal = maxOf(rNorm, gNorm, bNorm)
        val minVal = minOf(rNorm, gNorm, bNorm)
        val delta = maxVal - minVal

        val hue = when {
            delta == 0.0 -> 0.0
            maxVal == rNorm -> 60 * (((gNorm - bNorm) / delta) % 6)
            maxVal == gNorm -> 60 * (((bNorm - rNorm) / delta) + 2)
            else -> 60 * (((rNorm - gNorm) / delta) + 4)
        }.let { if (it < 0) it + 360 else it }.toFloat()

        val saturation = (if (maxVal == 0.0) 0.0 else (delta / maxVal) * 100).toFloat()
        val value = (maxVal * 100).toFloat()

        return Triple(hue, saturation, value)
    }

    /**
     * Get most dominant color dengan enhanced confidence
     */
    fun getMostDominantColorEnhanced(bitmap: Bitmap): ColorResult? {
        val colors = detectDominantColorsEnhanced(bitmap)
        return colors.filter { it.confidenceScore >= 0.5 }
            .maxByOrNull { it.percentage }
    }

    /**
     * Backward compatibility method
     */
    fun getMostDominantColor(bitmap: Bitmap): ColorResult? {
        return getMostDominantColorEnhanced(bitmap)
    }

    /**
     * Region detection dengan enhanced confidence
     */
    fun detectColorInRegionEnhanced(
        bitmap: Bitmap,
        centerX: Int,
        centerY: Int,
        regionSize: Int = 60
    ): ColorResult? {
        try {
            val halfSize = regionSize / 2
            val left = maxOf(0, centerX - halfSize)
            val top = maxOf(0, centerY - halfSize)
            val right = minOf(bitmap.width, centerX + halfSize)
            val bottom = minOf(bitmap.height, centerY + halfSize)

            if (right <= left || bottom <= top) return null

            val regionBitmap = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
            return try {
                getMostDominantColorEnhanced(regionBitmap)
            } finally {
                if (!regionBitmap.isRecycled) {
                    regionBitmap.recycle()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in enhanced region detection: ${e.message}")
            return null
        }
    }

    /**
     * Backward compatibility method
     */
    fun detectColorInRegion(
        bitmap: Bitmap,
        centerX: Int,
        centerY: Int,
        regionSize: Int = 50
    ): ColorResult? {
        return detectColorInRegionEnhanced(bitmap, centerX, centerY, regionSize)
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        Log.d(TAG, "Enhanced KMeansColorDetector cleaned up")
    }
}