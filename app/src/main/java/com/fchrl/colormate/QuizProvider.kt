package com.fchrl.colormate

object QuizDataProvider {

    fun getQuizQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = 1,
                category = "Persepsi Merah - Konteks Makanan",
                question = "Saat melihat tomat yang sudah matang, warna apa yang paling dominan terlihat bagimu?",
                options = listOf(
                    QuizOption("A", "Merah terang yang jelas"),
                    QuizOption("B", "Merah kecoklatan atau gelap"),
                    QuizOption("C", "Hijau kekuningan"),
                    QuizOption("D", "Abu-abu atau tidak jelas warnanya")
                ),
                correctAnswer = "A",
                explanation = "Tomat matang memiliki warna merah cerah khas. Jawaban B, C, atau D mengindikasikan protanopia/protanomaly (kesulitan melihat warna merah).",
                type = "Tes persepsi merah"
            ),

            QuizQuestion(
                id = 2,
                category = "Persepsi Hijau - Konteks Alam",
                question = "Ketika melihat daun pohon yang masih segar di musim hujan, bagaimana warna hijau terlihat bagimu?",
                options = listOf(
                    QuizOption("A", "Hijau segar yang vibrant"),
                    QuizOption("B", "Hijau kecoklatan atau kusam"),
                    QuizOption("C", "Kuning kehijauan"),
                    QuizOption("D", "Sulit membedakan dari coklat")
                ),
                correctAnswer = "A",
                explanation = "Daun segar memiliki warna hijau yang khas. Jawaban lain mengindikasikan deuteranopia/deuteranomaly (kesulitan melihat warna hijau).",
                type = "Tes persepsi hijau"
            ),

            QuizQuestion(
                id = 3,
                category = "Persepsi Biru-Kuning - Tritanopia Test",
                question = "Saat melihat langit biru cerah di siang hari, bagaimana warna biru terlihat bagimu?",
                options = listOf(
                    QuizOption("A", "Biru cerah yang jelas"),
                    QuizOption("B", "Biru kehijauan"),
                    QuizOption("C", "Abu-abu atau putih kebiruan"),
                    QuizOption("D", "Tidak bisa membedakan dari hijau")
                ),
                correctAnswer = "A",
                explanation = "Langit biru adalah referensi warna biru paling umum. Jawaban lain mengindikasikan tritanopia/tritanomaly (buta warna biru-kuning).",
                type = "Tes persepsi biru"
            ),

            QuizQuestion(
                id = 4,
                category = "Gradasi Warna - Tes Saturasi",
                question = "Dari deretan warna ini: merah muda, merah, merah tua, manakah yang paling mudah kamu lihat?",
                options = listOf(
                    QuizOption("A", "Merah muda (paling terang)"),
                    QuizOption("B", "Merah (saturasi sedang)"),
                    QuizOption("C", "Merah tua (paling gelap)"),
                    QuizOption("D", "Semuanya sama saja")
                ),
                correctAnswer = "B",
                explanation = "Normalnya, merah dengan saturasi sedang paling mudah dilihat. Jawaban D mengindikasikan protanomaly (kesulitan membedakan gradasi merah).",
                type = "Tes saturasi merah"
            ),

            QuizQuestion(
                id = 5,
                category = "Kontras Kompleks - Traffic Light",
                question = "Bagaimana kamu biasanya tahu kapan harus jalan saat di lampu merah?",
                options = listOf(
                    QuizOption("A", "Melihat lampu berubah menjadi hijau"),
                    QuizOption("B", "Melihat lampu paling bawah menyala"),
                    QuizOption("C", "Mengikuti kendaraan lain yang mulai jalan"),
                    QuizOption("D", "Menghitung waktu dari lampu merah")
                ),
                correctAnswer = "A",
                explanation = "Jawaban normal adalah melihat perubahan warna ke hijau. Jawaban B menunjukkan strategi posisi (adaptasi buta warna). Jawaban C atau D menunjukkan strategi kompensasi.",
                type = "Tes navigasi lalu lintas"
            ),

            QuizQuestion(
                id = 6,
                category = "Konteks Teknologi - LED Indicator",
                question = "Pada charger HP atau laptop, bagaimana cara paling akurat untuk mengetahui device sudah fully charged?",
                options = listOf(
                    QuizOption("A", "Melihat lampu berubah dari merah ke hijau"),
                    QuizOption("B", "Melihat posisi atau bentuk indikator berubah"),
                    QuizOption("C", "Menunggu sampai lampu mati total"),
                    QuizOption("D", "Mengecek persentase di layar device")
                ),
                correctAnswer = "A",
                explanation = "Cara paling umum dan akurat adalah perubahan warna LED dari merah ke hijau. Jawaban lain menunjukkan strategi adaptasi jika sulit membedakan warna.",
                type = "Tes indikator teknologi"
            ),

            QuizQuestion(
                id = 7,
                category = "Tes Praktis - Kabel Listrik",
                question = "Saat memasang kabel listrik (merah=positif, hijau=ground), bagaimana cara terbaik untuk membedakannya dengan aman?",
                options = listOf(
                    QuizOption("A", "Mudah dibedakan dari warnanya saja"),
                    QuizOption("B", "Menggunakan alat tester untuk memastikan"),
                    QuizOption("C", "Melihat marking/label di kabel"),
                    QuizOption("D", "Tidak berani mengerjakan sendiri")
                ),
                correctAnswer = "A",
                explanation = "Jika persepsi warna normal, membedakan dari warna adalah cara yang paling efisien. Jawaban B atau C menunjukkan strategi safety yang baik saat persepsi warna terbatas.",
                type = "Tes safety-critical"
            ),

            QuizQuestion(
                id = 8,
                category = "Tes Memori Warna - Asosiasi",
                question = "Warna apa yang pertama kali terpikirkan ketika mendengar kata \"strawberry\"?",
                options = listOf(
                    QuizOption("A", "Merah"),
                    QuizOption("B", "Pink/merah muda"),
                    QuizOption("C", "Tidak yakin, mungkin merah"),
                    QuizOption("D", "Hijau (karena daunnya)")
                ),
                correctAnswer = "A",
                explanation = "Strawberry identik dengan warna merah. Jawaban C menunjukkan ketidakpastian persepsi merah, jawaban D menunjukkan kompensasi dengan detail lain.",
                type = "Tes asosiasi warna"
            ),

            QuizQuestion(
                id = 9,
                category = "Self-Assessment - Pengalaman Pribadi",
                question = "Dalam kehidupan sehari-hari, seberapa sering kamu merasa yakin dengan pilihan warna yang kamu buat?",
                options = listOf(
                    QuizOption("A", "Selalu yakin dan percaya diri"),
                    QuizOption("B", "Biasanya yakin, tapi kadang ragu"),
                    QuizOption("C", "Sering ragu dan perlu konfirmasi orang lain"),
                    QuizOption("D", "Hampir selalu tidak yakin dengan pilihan warna")
                ),
                correctAnswer = "A",
                explanation = "Orang dengan persepsi warna normal biasanya yakin dengan pilihan warna. Jawaban C atau D mengindikasikan ketidakpastian yang mungkin terkait dengan kesulitan persepsi warna.",
                type = "Tes kepercayaan diri warna"
            ),

            QuizQuestion(
                id = 10,
                category = "Kesadaran Diri - Final Assessment",
                question = "Berdasarkan pengalaman hidupmu, apakah kamu pernah mengalami kesulitan yang konsisten dengan kombinasi warna tertentu?",
                options = listOf(
                    QuizOption("A", "Tidak pernah, semua kombinasi warna mudah dibedakan"),
                    QuizOption("B", "Ya, terutama kombinasi merah dan hijau"),
                    QuizOption("C", "Ya, terutama kombinasi biru dan kuning"),
                    QuizOption("D", "Ya, dengan berbagai kombinasi warna")
                ),
                correctAnswer = "A",
                explanation = "Orang dengan persepsi warna normal tidak mengalami kesulitan konsisten dengan kombinasi warna. Jawaban B, C, atau D mengindikasikan adanya kesulitan persepsi warna tertentu.",
                type = "Tes kesadaran kondisi"
            )
        ).shuffled()
    }

    fun calculateDiagnosis(userAnswers: List<UserAnswer>): QuizResult {
        val totalQuestions = 10
        val answeredQuestions = userAnswers.size
        var score = 0

        // Counters for different types of color deficiency indicators
        var protanopiaIndicators = 0
        var deuteranopiaIndicators = 0
        var tritanopiaIndicators = 0
        var adaptiveStrategies = 0
        var realWorldImpact = 0
        var selfAwareness = 0

        // Buat mapping dari question ID ke original question untuk mendapatkan correctAnswer
        val originalQuestions = getQuizQuestions().associateBy { it.id }

        for (answer in userAnswers) {
            // Calculate score
            if (answer.isCorrect == true) {
                score++
            }

            // Dapatkan original question dan pilihan yang dipilih user
            val originalQuestion = originalQuestions[answer.questionId]
            val selectedOption = originalQuestion?.options?.get(answer.selectedOptionIndex)

            when (answer.questionId) {
                // Protanopia indicators (red perception issues)
                1 -> if (selectedOption?.id != "A") protanopiaIndicators++
                4 -> if (selectedOption?.id == "D") protanopiaIndicators++
                8 -> if (selectedOption?.id == "C" || selectedOption?.id == "D") protanopiaIndicators++

                // Deuteranopia indicators (green perception issues)
                2 -> if (selectedOption?.id != "A") deuteranopiaIndicators++
                5 -> if (selectedOption?.id != "A") deuteranopiaIndicators++
                6 -> if (selectedOption?.id != "A") deuteranopiaIndicators++
                7 -> if (selectedOption?.id != "A") deuteranopiaIndicators++

                // Tritanopia indicators (blue-yellow perception issues)
                3 -> if (selectedOption?.id != "A") tritanopiaIndicators++

                // Self-confidence and awareness indicators
                9 -> if (selectedOption?.id != "A") {
                    realWorldImpact++
                    if (selectedOption?.id == "C" || selectedOption?.id == "D") {
                        realWorldImpact++
                    }
                }

                10 -> {
                    when (selectedOption?.id) {
                        "B" -> {
                            selfAwareness++
                            deuteranopiaIndicators++
                            protanopiaIndicators++
                        }
                        "C" -> {
                            selfAwareness++
                            tritanopiaIndicators++
                        }
                        "D" -> {
                            selfAwareness++
                            protanopiaIndicators++
                            deuteranopiaIndicators++
                            tritanopiaIndicators++
                        }
                    }
                }

                // Adaptive strategies
                5 -> if (selectedOption?.id == "B" || selectedOption?.id == "C" || selectedOption?.id == "D") adaptiveStrategies++
                6 -> if (selectedOption?.id == "B" || selectedOption?.id == "D") adaptiveStrategies++
                7 -> if (selectedOption?.id == "B" || selectedOption?.id == "C") adaptiveStrategies++
            }
        }

        // Determine primary deficiency type
        val totalRedGreenIndicators = protanopiaIndicators + deuteranopiaIndicators
        val primaryDeficiency = when {
            protanopiaIndicators >= deuteranopiaIndicators && protanopiaIndicators >= tritanopiaIndicators && protanopiaIndicators > 0 -> "Protanopia/Protanomaly"
            deuteranopiaIndicators >= tritanopiaIndicators && deuteranopiaIndicators > 0 -> "Deuteranopia/Deuteranomaly"
            tritanopiaIndicators > 0 -> "Tritanopia/Tritanomaly"
            else -> "Normal"
        }

        // Calculate severity level
        val totalIndicators = protanopiaIndicators + deuteranopiaIndicators + tritanopiaIndicators
        val severity = when {
            totalIndicators >= 5 -> "Signifikan"
            totalIndicators >= 3 -> "Sedang"
            totalIndicators >= 2 -> "Ringan"
            else -> "Normal"
        }

        // Generate diagnosis based on score and indicators
        val scorePercentage = if (answeredQuestions > 0) (score * 100) / answeredQuestions else 0

        val diagnosis = when {
            scorePercentage >= 80 && totalIndicators <= 1 -> {
                "Tidak terindikasi mengalami kesulitan signifikan dalam membedakan warna. " +
                        "Kemampuan persepsi warna dalam rentang normal."
            }
            scorePercentage >= 60 && totalIndicators <= 2 -> {
                "Kemampuan persepsi warna cukup baik dengan sedikit kesulitan pada situasi tertentu. " +
                        if (adaptiveStrategies >= 2) "Memiliki strategi adaptif yang baik." else ""
            }
            totalIndicators >= 5 && realWorldImpact >= 2 -> {
                "Kemungkinan besar mengalami ${primaryDeficiency} dengan tingkat $severity. " +
                        "Kondisi ini berdampak signifikan pada aktivitas sehari-hari."
            }
            totalIndicators >= 3 || scorePercentage < 60 -> {
                "Terindikasi mengalami kesulitan persepsi warna tipe ${primaryDeficiency} tingkat $severity. " +
                        "Disarankan untuk pemeriksaan lebih lanjut."
            }
            totalIndicators >= 2 -> {
                "Kemungkinan mengalami kesulitan ringan dalam membedakan warna tertentu, " +
                        "terutama dalam spektrum ${if (totalRedGreenIndicators > tritanopiaIndicators) "merah-hijau" else "biru-kuning"}."
            }
            else -> {
                "Kemampuan persepsi warna dalam rentang normal dengan skor $scorePercentage%."
            }
        }

        // Generate recommendations
        val recommendations = mutableListOf<String>()

        when {
            totalIndicators >= 5 || scorePercentage < 50 -> {
                recommendations.addAll(listOf(
                    "Segera konsultasi dengan dokter mata untuk diagnosis definitif",
                    "Gunakan aplikasi ColorMate secara rutin untuk membantu identifikasi warna",
                    "Informasikan kondisi ini kepada keluarga dan rekan kerja",
                    "Pertimbangkan untuk menggunakan kacamata khusus buta warna",
                    "Manfaatkan petunjuk visual selain warna (bentuk, posisi, tekstur)",
                    "Hindari pekerjaan yang memerlukan akurasi warna tinggi tanpa alat bantu"
                ))
            }
            totalIndicators >= 3 || scorePercentage < 70 -> {
                recommendations.addAll(listOf(
                    "Konsultasi dengan dokter mata untuk pemeriksaan lebih detail",
                    "Gunakan aplikasi pendeteksi warna untuk aktivitas penting",
                    "Pelajari strategi adaptasi untuk situasi sehari-hari",
                    "Manfaatkan kontras dan pencahayaan yang baik",
                    "Minta bantuan orang lain untuk memilih kombinasi warna"
                ))
            }
            totalIndicators >= 2 || scorePercentage < 80 -> {
                recommendations.addAll(listOf(
                    "Monitor perkembangan kondisi secara berkala",
                    "Gunakan aplikasi ColorMate saat diperlukan",
                    "Tingkatkan awareness terhadap kombinasi warna yang sulit",
                    "Gunakan label atau penanda untuk barang-barang dengan warna penting"
                ))
            }
            else -> {
                recommendations.addAll(listOf(
                    "Lanjutkan penggunaan ColorMate untuk deteksi warna akurat",
                    "Tetap waspada terhadap perubahan kemampuan penglihatan",
                    "Lakukan tes berkala untuk monitoring kesehatan mata",
                    "Gunakan pencahayaan yang baik saat bekerja dengan warna"
                ))
            }
        }

        // Add specific recommendations based on deficiency type
        when (primaryDeficiency) {
            "Protanopia/Protanomaly" -> {
                recommendations.add("Hati-hati dengan kombinasi merah-hijau dalam situasi safety-critical")
                recommendations.add("Gunakan indikator selain warna untuk mengenali kematangan buah")
            }
            "Deuteranopia/Deuteranomaly" -> {
                recommendations.add("Perhatikan posisi lampu lalu lintas, bukan hanya warnanya")
                recommendations.add("Gunakan aplikasi untuk membedakan warna hijau dalam berbagai nuansa")
            }
            "Tritanopia/Tritanomaly" -> {
                recommendations.add("Hati-hati dengan kombinasi biru-kuning dalam desain")
                recommendations.add("Gunakan kontras yang tinggi untuk membedakan warna biru")
            }
        }

        // Calculate confidence level
        val consistencyScore = if (answeredQuestions >= 8) {
            val selfReportedIssues = if (selfAwareness > 0) 1 else 0
            val objectiveIndicators = if (totalIndicators >= 2) 1 else 0
            when {
                selfReportedIssues == objectiveIndicators -> "Tinggi"
                Math.abs(selfReportedIssues - objectiveIndicators) == 1 -> "Sedang"
                else -> "Rendah"
            }
        } else "Rendah - Jawab semua soal untuk hasil yang lebih akurat"

        return QuizResult(
            totalQuestions = totalQuestions,
            answeredQuestions = answeredQuestions,
            score = score,
            diagnosis = diagnosis,
            recommendations = recommendations,
            deficiencyType = primaryDeficiency,
            severityLevel = severity,
            confidenceLevel = consistencyScore,
            protanopiaScore = protanopiaIndicators,
            deuteranopiaScore = deuteranopiaIndicators,
            tritanopiaScore = tritanopiaIndicators,
            adaptiveScore = adaptiveStrategies,
            impactScore = realWorldImpact
        )
    }
}