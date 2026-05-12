package com.lisitede.preset.preset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("preset_prefs", 0)
        val token = prefs.getString("auth_token", null)
        val username = prefs.getString("username", null)

        view.findViewById<TextView>(R.id.profileInfo).text = buildString {
            appendLine("用户: ${username ?: "未登录"}")
            appendLine("Token: ${token?.take(8)?.plus("...") ?: "无"}")
        }
    }
}
