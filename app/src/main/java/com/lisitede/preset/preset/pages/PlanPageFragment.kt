package com.lisitede.preset.preset.pages

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.lisitede.preset.preset.MainActivity
import com.lisitede.preset.preset.R

class PlanPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.page_plan, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { root, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        view.findViewById<Button>(R.id.backToHomeButton).setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_TARGET_PAGE, MainActivity.TARGET_PAGE_HOME)
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
