package com.lisitede.preset.preset.pages

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.lisitede.preset.preset.PlanActivity
import com.lisitede.preset.preset.R
import com.lisitede.preset.preset.TokenStorage

class ProfilePageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.page_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenStorage = TokenStorage(requireActivity().getSharedPreferences("preset_prefs", 0))
        val token = tokenStorage.getToken()
        val username = tokenStorage.getUsername()

        view.findViewById<TextView>(R.id.profileInfo).text = buildString {
            appendLine("用户: ${username ?: "未登录"}")
            appendLine("Token: ${token?.take(8)?.plus("...") ?: "无"}")
        }

        view.findViewById<Button>(R.id.openPlanButton).setOnClickListener {
            startActivity(Intent(requireContext(), PlanActivity::class.java))
        }
    }
}
