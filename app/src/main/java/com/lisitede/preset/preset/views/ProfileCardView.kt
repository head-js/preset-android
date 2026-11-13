package com.lisitede.preset.preset.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.util.Base64
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.imageview.ShapeableImageView
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
    private val cardAvatar: ShapeableImageView

    var onGoToPlanClick: (() -> Unit)? = null
    var onAvatarClick: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_profile_card, this, true)
        cardUsername = findViewById(R.id.cardUsername)
        cardTokenSummary = findViewById(R.id.cardTokenSummary)
        cardAfId = findViewById(R.id.cardAfId)
        cardGoToPlanButton = findViewById(R.id.cardGoToPlanButton)
        cardGoToPlanButton.setOnClickListener { onGoToPlanClick?.invoke() }
        cardAvatar = findViewById(R.id.cardAvatar)
        cardAvatar.setOnClickListener { onAvatarClick?.invoke() }
    }

    fun bind(username: String?, token: String?) {
        // A new view/bind always starts with the default; selected avatars are not saved.
        val bytes = Base64.decode(DEFAULT_AVATAR_BASE64, Base64.DEFAULT)
        cardAvatar.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        cardUsername.text = "Username: ${username ?: "-"}"
        cardTokenSummary.text = "Token: ${if (token != null) token.take(8) + "..." else "-"}"
        val afId = AppsFlyerHelper.getAfId(context)
        cardAfId.text = "Af Id: ${if (afId.isNotBlank()) afId.take(8) + "..." else "获取中"}"
    }

    fun showAvatar(bitmap: Bitmap) {
        cardAvatar.setImageBitmap(bitmap)
    }

    private companion object {
        // 48 x 48 opaque #0000FF PNG.
        const val DEFAULT_AVATAR_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAIAAADYYG7QAAAAQUlEQVR4nO3OQQ0AMBAEofNvupWx8yBBAHf3YvYDISEhoZj9QEhISChmPxASEhKK2Q+EhISEYvYDISEhoZj9oB368AX3eWaj93YAAAAASUVORK5CYII="
    }
}
