package com.lisitede.preset.preset.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.lisitede.preset.preset.R

class LottieCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val lottieView: LottieAnimationView

    init {
        lottieView = LottieAnimationView(context, attrs)
        lottieView.enableMergePathsForKitKatAndAbove(true)
        lottieView.setAnimation(R.raw.lottie_card)
        lottieView.repeatCount = 0
        addView(lottieView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        lottieView.playAnimation()
    }
}
