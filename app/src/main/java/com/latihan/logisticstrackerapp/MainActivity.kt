package com.latihan.logisticstrackerapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    // 1. Deklarasi Elemen Antarmuka Pengguna (camelCase tanpa singkatan)
    private lateinit var editTextTrackingNumber: EditText
    private lateinit var buttonTrack: Button
    private lateinit var progressBarLoading: ProgressBar
    private lateinit var cardResult: CardView
    private lateinit var textViewErrorMessage: TextView

    // Elemen Informasi Paket Kiriman
    private lateinit var textViewResiTitle: TextView
    private lateinit var textViewStatusBadge: TextView
    private lateinit var textViewCourier: TextView
    private lateinit var textViewLocation: TextView
    private lateinit var textViewRecipient: TextView
    private lateinit var textViewEta: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left, systemBars.top, systemBars.right,
                systemBars.bottom
            )
            insets
        }

        // 2. Inisialisasi Seluruh Elemen Tampilan UI
        editTextTrackingNumber = findViewById(R.id.editTextTrackingNumber)
        buttonTrack = findViewById(R.id.buttonTrack)
        progressBarLoading = findViewById(R.id.progressBarLoading)
        cardResult = findViewById(R.id.cardResult)
        textViewErrorMessage = findViewById(R.id.textViewErrorMessage)
        textViewResiTitle = findViewById(R.id.textViewResiTitle)
        textViewStatusBadge = findViewById(R.id.textViewStatusBadge) // Inisialisasi wajib agar tidak crash
        textViewCourier = findViewById(R.id.textViewCourier)
        textViewLocation = findViewById(R.id.textViewLocation)
        textViewRecipient = findViewById(R.id.textViewRecipient)
        textViewEta = findViewById(R.id.textViewEta)

        // 3. Aksi Tombol Lacak
        buttonTrack.setOnClickListener {
            val nomorResi = editTextTrackingNumber.text.toString().trim()
            if (nomorResi.isNotEmpty()) {
                lacakPaket(nomorResi)
            } else {
                Toast.makeText(
                    this,
                    "Silakan ketik nomor resi terlebih dahulu (contoh: EXP-8801)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun lacakPaket(nomorResi: String) {
        // STATE 1: LOADING STATE (Tampilkan ProgressBar, sembunyikan Card & Pesan Error)
        progressBarLoading.visibility = View.VISIBLE
        cardResult.visibility = View.GONE
        textViewErrorMessage.visibility = View.GONE
        buttonTrack.isEnabled = false

        // Eksekusi Panggilan Jaringan di Background Thread via Coroutine
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Memanggil Retrofit Service
                val response = ApiClient.apiService.getTrackingDetail(nomorResi)
                // Kembali ke Main UI Thread untuk memperbarui tampilan visual
                withContext(Dispatchers.Main) {
                    progressBarLoading.visibility = View.GONE
                    buttonTrack.isEnabled = true
                    if (response.isSuccessful && response.body() != null) {
                        // STATE 2: SUCCESS STATE (Data berhasil ditemukan)
                        val dataPaket = response.body()!!
                        tampilkanDataPaket(dataPaket)
                    } else {
                        // STATE 3: SERVER ERROR STATE (Misal 404 Resi Tidak Ditemukan)
                        textViewErrorMessage.text =
                            "Nomor resi [$nomorResi] tidak ditemukan pada database server (HTTP ${response.code()})"
                        textViewErrorMessage.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                // STATE 4: NETWORK FAILURE STATE (Koneksi mati / DNS Error / Timeout)
                withContext(Dispatchers.Main) {
                    progressBarLoading.visibility = View.GONE
                    buttonTrack.isEnabled = true
                    textViewErrorMessage.text =
                        "Koneksi internet bermasalah: ${e.localizedMessage ?: "Gagal terhubung ke server"}"
                    textViewErrorMessage.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun tampilkanDataPaket(dataPaket: TrackingResponse) {
        cardResult.visibility = View.VISIBLE
        textViewResiTitle.text = "Resi: ${dataPaket.trackingNumber}"
        textViewCourier.text = "Kurir: ${dataPaket.serviceType} (${dataPaket.courierName})"
        textViewLocation.text = "Posisi Terakhir: ${dataPaket.lastLocation}"
        textViewRecipient.text = "Penerima: ${dataPaket.recipientName}"
        textViewEta.text = "Estimasi Tiba: ${dataPaket.estimatedDelivery}"

        // Pengaturan Badge Status Secara Dinamis
        textViewStatusBadge.text = dataPaket.status
        when (dataPaket.status.uppercase()) {
            "DELIVERED" -> {
                textViewStatusBadge.setBackgroundColor(Color.parseColor("#DCFCE7")) // Latar Hijau Muda
                textViewStatusBadge.setTextColor(Color.parseColor("#15803D")) // Teks Hijau Tua
            }
            "IN_TRANSIT" -> {
                textViewStatusBadge.setBackgroundColor(Color.parseColor("#FEF3C7")) // Latar Kuning Amber
                textViewStatusBadge.setTextColor(Color.parseColor("#B45309")) // Teks Cokelat Oranye
            }
            else -> {
                textViewStatusBadge.setBackgroundColor(Color.parseColor("#F1F5F9")) // Latar Abu Netral
                textViewStatusBadge.setTextColor(Color.parseColor("#475569")) // Teks Abu Gelap
            }
        }
    }
}
