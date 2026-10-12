package com.example.tasbeeeh.fragments

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.example.tasbeeeh.R

import android.widget.Toast

class TasbeehFragment : Fragment() {

    private lateinit var btnCount: ImageView
    private lateinit var btnRestart: ImageView
    private lateinit var tvCount: TextView

    private lateinit var card33: MaterialCardView
    private lateinit var card66: MaterialCardView
    private lateinit var card99: MaterialCardView

    private lateinit var clickPlayer: MediaPlayer

    private var count = 0
    private var target: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_tasbeeh, container, false)

        btnCount = view.findViewById(R.id.btn_count_tasbeh)
        btnRestart = view.findViewById(R.id.btn_restart_tasbeh)
        tvCount = view.findViewById(R.id.tv_count)

        card33 = view.findViewById(R.id.card_33)
        card66 = view.findViewById(R.id.card_66)
        card99 = view.findViewById(R.id.card_99)

        clickPlayer = MediaPlayer.create(requireContext(), R.raw.click)

        tvCount.text = count.toString()

        card33.setOnClickListener { toggleTarget(33) }
        card66.setOnClickListener { toggleTarget(66) }
        card99.setOnClickListener { toggleTarget(99) }

        btnCount.setOnClickListener {
            if (target != null) {
                count--
                tvCount.text = count.toString()

                if (count <= 0) {
                    playAlarmTwice()
                    target = null
                    unhighlightAll()
                } else {
                    playClick()
                }
            } else {
                if (count >= 999) {
                    Toast.makeText(requireContext(), "Maximum count reached", Toast.LENGTH_SHORT).show()
                } else {
                    count++
                    tvCount.text = count.toString()
                    playClick()
                }
            }
        }

        btnRestart.setOnClickListener {
            count = 0
            target = null
            tvCount.text = count.toString()
            unhighlightAll()
            playClick()
        }

        return view
    }

    private fun toggleTarget(value: Int) {
        playClick()
        if (target == value) {
            target = null
            count = 0
            tvCount.text = count.toString()
            unhighlightAll()
        } else {
            target = value
            count = value
            tvCount.text = count.toString()
            highlightSelected(value)
        }
    }

    private fun highlightSelected(value: Int) {
        val strokeColor = ContextCompat.getColor(requireContext(), R.color.tasbeeh_card_selected)
        val transparent = ContextCompat.getColor(requireContext(), android.R.color.transparent)

        card33.strokeColor = if (value == 33) strokeColor else transparent
        card66.strokeColor = if (value == 66) strokeColor else transparent
        card99.strokeColor = if (value == 99) strokeColor else transparent
    }

    private fun unhighlightAll() {
        val transparent = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        card33.strokeColor = transparent
        card66.strokeColor = transparent
        card99.strokeColor = transparent
    }

    private fun playClick() {
        val prefs = requireContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        val isSoundOn = prefs.getBoolean("Sound Tasbih", true)
        
        if (isSoundOn) {
            if (clickPlayer.isPlaying) {
                clickPlayer.seekTo(0)
            }
            clickPlayer.start()
        }
    }

    private fun playAlarmTwice() {
        val prefs = requireContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        val isSoundOn = prefs.getBoolean("Sound Tasbih", true)

        if (!isSoundOn) return

        val alarmPlayer = MediaPlayer.create(requireContext(), R.raw.click_alarm)
        var playCount = 0

        alarmPlayer.setOnCompletionListener {
            playCount++
            if (playCount < 2) {
                it.seekTo(0)
                it.start()
            } else {
                it.release()
            }
        }
        alarmPlayer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        clickPlayer.release()
    }
}