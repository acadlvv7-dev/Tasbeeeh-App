package com.example.tasbeeeh.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasbeeeh.Adapters.SettingsAdapter
import com.example.tasbeeeh.R
import com.example.tasbeeeh.Models.SettingsModel

class SettingsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var settingsAdapter: SettingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val title: TextView = view.findViewById(R.id.title_Settings)
        title.text = "Settings"

        recyclerView = view.findViewById(R.id.recycle_view_settings)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val list = listOf(
            SettingsModel(R.drawable.ic_soundsss, "Sound Tasbih", "", true),
            SettingsModel(R.drawable.ic_sharee, "Share", "", false),
            SettingsModel(R.drawable.ic_infooo, "About", "", false),
        )

        settingsAdapter = SettingsAdapter(list) { setting ->
            when (setting.textName) {
                "About" -> {
                    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_info, null)
                    val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                        .setView(dialogView)
                        .create()

                    val tvTitle = dialogView.findViewById<TextView>(R.id.tv_info_title)
                    val tvVersion = dialogView.findViewById<TextView>(R.id.tv_version)
                    val tvDescription = dialogView.findViewById<TextView>(R.id.tv_description)
                    val tvFooter = dialogView.findViewById<TextView>(R.id.tv_footer_note)
                    val tvSupportTitle = dialogView.findViewById<TextView>(R.id.tv_support_title)
                    val tvSupportDesc = dialogView.findViewById<TextView>(R.id.tv_support_desc)
                    
                    val btnUz = dialogView.findViewById<TextView>(R.id.tv_lang_uz)
                    val btnEn = dialogView.findViewById<TextView>(R.id.tv_lang_en)
                    val supportUz = dialogView.findViewById<View>(R.id.cl_support_uz)
                    val supportEn = dialogView.findViewById<View>(R.id.cl_support_en)
                    val btnCopy = dialogView.findViewById<View>(R.id.btn_copy_card)

                    var versionName = "1.0.2"
                    try {
                        val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
                        versionName = pInfo.versionName ?: "1.0.2"
                    } catch (_: Exception) { }

                    fun switchLanguage(isUz: Boolean) {
                        if (isUz) {
                            btnUz.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#C5A059")))
                            btnUz.setTextColor(android.graphics.Color.parseColor("#0F1D19"))
                            btnEn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10000000")))
                            btnEn.setTextColor(android.graphics.Color.parseColor("#60000000"))
                            
                            tvTitle.text = "Ilova haqida"
                            tvVersion.text = "Versiya $versionName"
                            tvDescription.text = "Ushbu ilova yordamida bir vaqtning o'zida tasbeh tortishingiz va qibla yo'nalishini topishingiz mumkin.\n\nIlovada Qur'oni Karimning barcha 114 ta surasi, shuningdek tilovat (musiqa) bo'limi ham mavjud."
                            tvFooter.text = "Ilovadan foydalanganingizda, duolaringizda bizni ham eslab qolsangiz — minnatdor bo'lamiz."
                            tvSupportTitle.text = "QO'LLAB-QUVVATLASH"
                            tvSupportDesc.text = "Mehnatimizni qadrlaganingiz uchun rahmat! Ushbu ilovani yaratishga o'z hissangizni qo'shmoqchi bo'lsangiz:"
                            supportUz.visibility = View.VISIBLE
                            supportEn.visibility = View.GONE
                        } else {
                            btnEn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#C5A059")))
                            btnEn.setTextColor(android.graphics.Color.parseColor("#0F1D19"))
                            btnUz.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10000000")))
                            btnUz.setTextColor(android.graphics.Color.parseColor("#60000000"))

                            tvTitle.text = "About the app"
                            tvVersion.text = "Version $versionName"
                            tvDescription.text = "With this app you can count your dhikr on tasbeh and find the Qibla direction at the same time.\n\nIt also includes all 114 surahs of the Holy Quran, along with a recitation (audio) section."
                            tvFooter.text = "If you keep us in your prayers while using this app, we would be truly grateful."
                            tvSupportTitle.text = "SUPPORT US"
                            tvSupportDesc.text = "Thank you for appreciating our work! If you'd like to support the development of this app:"
                            supportUz.visibility = View.GONE
                            supportEn.visibility = View.VISIBLE
                        }
                    }

                    btnUz.setOnClickListener { switchLanguage(true) }
                    btnEn.setOnClickListener { switchLanguage(false) }
                    
                    btnCopy.setOnClickListener {
                        val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Card Number", "8600000000000000")
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(requireContext(), "Nusxa olindi", android.widget.Toast.LENGTH_SHORT).show()
                    }

                    dialog.show()
                    dialog.window?.let { window ->
                        window.setBackgroundDrawableResource(android.R.color.transparent)
                        window.setDimAmount(0.85f)
                    }
                }
                "Share" -> {
                    val shareIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, "Experience the Tasbeeeh app for your daily prayers! Download now: https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                        type = "text/plain"
                    }
                    startActivity(android.content.Intent.createChooser(shareIntent, "Share App"))
                }
            }
        }
        recyclerView.adapter = settingsAdapter

        return view
    }
}