package com.lisitede.preset.preset.pages

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lisitede.preset.deviceinfo.AppInfoRepository
import com.lisitede.preset.deviceinfo.DeviceIdentityRepository
import com.lisitede.preset.deviceinfo.DeviceInfoEntry
import com.lisitede.preset.deviceinfo.DeviceInfoRepository
import com.lisitede.preset.preset.App
import com.lisitede.preset.preset.R
import com.lisitede.preset.preset.dialog.DetailInfoDialogData
import com.lisitede.preset.preset.dialog.DetailInfoDialogRenderer
import com.therouter.router.Route

private data class DisplayItem(
    val title: String,
    val content: String,
    val isSection: Boolean = false
)

@Route(path = "/main/detail")
class DetailPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.page_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireContext().applicationContext as App
        val identityRepo = app.deviceIdentityRepository
        val deviceInfoRepo = app.deviceInfoRepository
        val appInfoRepo = AppInfoRepository(requireContext())

        val items = buildDisplayItems(deviceInfoRepo)

        val container = view.findViewById<LinearLayout>(R.id.detailList)
        items.forEach { item ->
            if (item.isSection) {
                container.addView(buildSectionTitle(item.title))
            } else {
                container.addView(buildTitle(item.title))
                container.addView(buildContent(item.content))
            }
        }

        view.findViewById<Button>(R.id.backButton).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<Button>(R.id.showDetailInfoDialogButton).setOnClickListener {
            val dialogData = DetailInfoDialogData(
                appInfo = appInfoRepo.getAppInfo().toList(),
                identityInfo = identityRepo.getIdentity().toList()
            )
            DetailInfoDialogRenderer(dialogData).show()
        }
    }

    private fun buildDisplayItems(deviceInfoRepo: DeviceInfoRepository): List<DisplayItem> {
        val groups: List<Pair<String, Array<DeviceInfoEntry>>> = listOf(
            "Profile" to deviceInfoRepo.getProfile(),
            "Fingerprint" to deviceInfoRepo.getFingerprint(),
            "Risk" to deviceInfoRepo.getRisk()
        )
        return buildList {
            groups.forEach { (groupName, values) ->
                val visibleValues = values
                    .filter { entry -> entry.value.isNotEmpty() }
                if (visibleValues.isNotEmpty()) {
                    add(DisplayItem(groupName, "", isSection = true))
                    visibleValues.forEach { entry ->
                        add(DisplayItem(entry.label, entry.value))
                    }
                }
            }
        }
    }

    private fun buildTitle(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        }
    }

    private fun buildSectionTitle(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(20)
                bottomMargin = dp(4)
            }
        }
    }

    private fun buildContent(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun dp(value: Int): Int {
        val density = resources.displayMetrics.density
        return (value * density + 0.5f).toInt()
    }
}
