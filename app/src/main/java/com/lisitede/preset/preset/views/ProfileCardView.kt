package com.lisitede.preset.preset.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.lisitede.preset.preset.AppsFlyerHelper
import com.lisitede.preset.preset.R

class ProfileCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val cardUsername: TextView
    private val cardTokenSummary: TextView
    private val cardAfId: TextView
    private val cardGoToPlanButton: Button

    var onGoToPlanClick: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_profile_card, this, true)
        cardUsername = findViewById(R.id.cardUsername)
        cardTokenSummary = findViewById(R.id.cardTokenSummary)
        cardAfId = findViewById(R.id.cardAfId)
        cardGoToPlanButton = findViewById(R.id.cardGoToPlanButton)
        cardGoToPlanButton.setOnClickListener { onGoToPlanClick?.invoke() }
    }

    fun bind(username: String?, token: String?) {
        cardUsername.text = "Username: ${username ?: "-"}"
        cardTokenSummary.text = "Token: ${if (token != null) token.take(8) + "..." else "-"}"
        val afId = AppsFlyerHelper.getAfId(context)
        cardAfId.text = "Af Id: ${if (afId.isNotBlank()) afId.take(8) + "..." else "获取中"}"
    }
}
