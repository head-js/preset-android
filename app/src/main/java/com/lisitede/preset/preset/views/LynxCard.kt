package com.lisitede.preset.preset.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.lisitede.preset.preset.lynx.AssetsTemplateProvider
import com.lynx.tasm.LynxView
import com.lynx.tasm.LynxViewBuilder

class LynxCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val lynxView: LynxView

    init {
        val viewBuilder = LynxViewBuilder()
        viewBuilder.setTemplateProvider(AssetsTemplateProvider(context))
        lynxView = viewBuilder.build(context)
        addView(lynxView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        lynxView.renderTemplateUrl("lynx_card.lynx.bundle", "")
    }
}
