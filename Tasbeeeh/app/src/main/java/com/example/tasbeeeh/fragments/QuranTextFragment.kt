package com.example.tasbeeeh.fragments

import SurahDetailResponse
import android.app.Fragment
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasbeeeh.API.RetrofitClient
import com.example.tasbeeeh.Adapters.AyahAdapter
import com.example.tasbeeeh.R
import com.example.tasbeeeh.Utils.AssetJsonReader
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class QuranTextFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var ayahAdapter: AyahAdapter
    
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var btnPlayPause: ImageView
    private lateinit var seekBar: SeekBar
    private lateinit var tvTime: TextView
    private lateinit var tvReciter: TextView
    private val handler = Handler(Looper.getMainLooper())

    private var suraNumber: Int = 1
    private var suraName: String = ""
    private var suraEnglishName: String = ""
    
    private var isAudioPrepared = false
    private var isWaitingForAudio = false
    private var currentAudioUrl: String = ""

    private val timeoutHandler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        if (isAdded && isWaitingForAudio && !isAudioPrepared) {
            Toast.makeText(requireContext(), "Slow internet, buffering 64kbps stream...", Toast.LENGTH_SHORT).show()
        }
    }

    private val updateSeekBarTask = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    val currentPos = it.currentPosition
                    val duration = it.duration
                    if (duration > 0) {
                        seekBar.progress = currentPos
                        updateTimeDisplay(currentPos, duration)
                    }
                    handler.postDelayed(this, 1000)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quran_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        suraNumber = arguments?.getInt("suraNumber") ?: 1
        suraName = arguments?.getString("suraName") ?: ""
        suraEnglishName = arguments?.getString("suraEnglishName") ?: ""

        val titleEnglish: TextView = view.findViewById(R.id.title_english)
        val titleArabic: TextView = view.findViewById(R.id.title_arabic)
        val btnBack: View = view.findViewById(R.id.btn_back_container)
        val btnInfo: View = view.findViewById(R.id.btn_info_container)
        recyclerView = view.findViewById(R.id.recycle_view_quran_text)
        progressBar = view.findViewById(R.id.progress_bar_quran_text)
        
        btnPlayPause = view.findViewById(R.id.btn_play_pause)
        seekBar = view.findViewById(R.id.audio_seekbar)
        tvTime = view.findViewById(R.id.tv_audio_time)
        tvReciter = view.findViewById(R.id.tv_reciter)

        titleEnglish.text = suraEnglishName
        titleArabic.text = suraName
        tvReciter.text = suraEnglishName
        tvTime.text = "0:00 / 0:00"

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnInfo.setOnClickListener {
            showInfoDialog()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        ayahAdapter = AyahAdapter(emptyList())
        recyclerView.adapter = ayahAdapter

        val audioEntry = AssetJsonReader.getSurahAudioEntry(requireContext(), suraNumber)
        audioEntry?.audio_url?.let {
            currentAudioUrl = it.replace("/128/", "/64/")
            initiateAudioBuffering(currentAudioUrl)
        }
        
        loadSurahText()
        setupAudioPlayer()
    }

    private fun showInfoDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_quran_info, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()

        val btnUz = dialogView.findViewById<TextView>(R.id.tv_lang_uz)
        val btnEn = dialogView.findViewById<TextView>(R.id.tv_lang_en)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val tvDesc = dialogView.findViewById<TextView>(R.id.tv_dialog_desc)
        val btnGotIt = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_got_it)

        fun updateLang(isUz: Boolean) {
            val activeBg = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0F1D19"))
            val inactiveBg = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
            val activeTextColor = android.graphics.Color.WHITE
            val inactiveTextColor = android.graphics.Color.parseColor("#60000000")

            if (isUz) {
                btnUz.backgroundTintList = activeBg
                btnUz.setTextColor(activeTextColor)
                btnEn.backgroundTintList = inactiveBg
                btnEn.setTextColor(inactiveTextColor)

                tvTitle.text = "Ma'lumot"
                tvDesc.text = "Ba'zi suralar audiosi uzun va yuqori sifatli bo'lgani uchun serverdan yuklanishi bir necha soniya vaqt olishi mumkin. Iltimos, sabr qiling. Tushunganingiz uchun minnatdormiz. Quron nuri qalbingizni munavvar qilsin!"
                btnGotIt.text = "Tushunarli"
            } else {
                btnEn.backgroundTintList = activeBg
                btnEn.setTextColor(activeTextColor)
                btnUz.backgroundTintList = inactiveBg
                btnUz.setTextColor(inactiveTextColor)

                tvTitle.text = "Info"
                tvDesc.text = "Some surah audios are long and high quality, so loading from the server may take a few seconds. Please be patient, and thank you for your understanding. May the light of the Quran illuminate your heart!"
                btnGotIt.text = "Got it"
            }
        }

        btnUz.setOnClickListener { updateLang(true) }
        btnEn.setOnClickListener { updateLang(false) }
        btnGotIt.setOnClickListener { dialog.dismiss() }

        dialog.show()

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setDimAmount(0.85f)
        }
    }

    private fun setupAudioPlayer() {
        btnPlayPause.setOnClickListener {
            if (isAudioPrepared) {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.pause()
                        btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
                    } else {
                        it.start()
                        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                        handler.post(updateSeekBarTask)
                    }
                }
            } else {
                isWaitingForAudio = true
                progressBar.visibility = View.VISIBLE
                timeoutHandler.postDelayed(timeoutRunnable, 5000)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    mediaPlayer?.let { updateTimeDisplay(progress, it.duration) }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initiateAudioBuffering(url: String) {
        mediaPlayer?.release()
        isAudioPrepared = false
        
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            try {
                setDataSource(url)
                prepareAsync()
            } catch (e: Exception) {
                isAudioPrepared = false
            }
            
            setOnPreparedListener {
                if (!isAdded) return@setOnPreparedListener
                isAudioPrepared = true

                if (isWaitingForAudio) {
                    isWaitingForAudio = false
                    progressBar.visibility = View.GONE
                    timeoutHandler.removeCallbacks(timeoutRunnable)

                    it.start()
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                    seekBar.max = it.duration
                    updateTimeDisplay(it.currentPosition, it.duration)
                    handler.post(updateSeekBarTask)
                } else {
                    seekBar.max = it.duration
                    updateTimeDisplay(0, it.duration)
                }
            }

            setOnErrorListener { _, _, _ ->
                if (isAdded) {
                    if (currentAudioUrl.contains("/64/")) {
                        currentAudioUrl = currentAudioUrl.replace("/64/", "/128/")
                        initiateAudioBuffering(currentAudioUrl)
                    } else if (isWaitingForAudio) {
                        progressBar.visibility = View.GONE
                        timeoutHandler.removeCallbacks(timeoutRunnable)
                        Toast.makeText(requireContext(), "Error loading audio", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
        }
    }

    private fun updateTimeDisplay(currentPos: Int, totalDuration: Int) {
        if (!isAdded) return
        val current = formatTime(currentPos)
        val total = formatTime(totalDuration)
        tvTime.text = String.format(Locale.getDefault(), "%s / %s", current, total)
    }

    private fun formatTime(millis: Int): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    private fun loadSurahText() {
        if (!isAdded) return
        RetrofitClient.quranApi.getSurah(suraNumber, "en.transliteration").enqueue(object : Callback<SurahDetailResponse> {
            override fun onResponse(
                call: Call<SurahDetailResponse>,
                response: Response<SurahDetailResponse>
            ) {
                if (!isAdded) return
                if (response.isSuccessful) {
                    val ayahs = response.body()?.data?.ayahs
                    if (ayahs != null) {
                        ayahAdapter.updateData(ayahs)
                    }
                }
            }
            override fun onFailure(call: Call<SurahDetailResponse>, t: Throwable) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
        timeoutHandler.removeCallbacks(timeoutRunnable)
    }
}
