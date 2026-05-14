package com.lisitede.preset.preset.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lisitede.preset.preset.AuthRepository
import com.lisitede.preset.preset.AuthViewModel
import com.lisitede.preset.preset.ConnectivityHelper
import com.lisitede.preset.preset.HomeViewModel
import com.lisitede.preset.preset.PageRouter
import com.lisitede.preset.preset.R
import com.lisitede.preset.preset.TokenStorage
import com.therouter.router.Route
import kotlinx.coroutines.launch

@Route(path = "/main/home")
class HomePageFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(requireActivity().getSharedPreferences("preset_prefs", 0))
    }

    private val authViewModel: AuthViewModel by activityViewModels {
        val prefs = requireActivity().getSharedPreferences("preset_prefs", 0)
        AuthViewModel.Factory(TokenStorage(prefs), AuthRepository())
    }

    private lateinit var connectivityHelper: ConnectivityHelper
    private lateinit var authStatusText: TextView
    private lateinit var logoutButton: Button
    private lateinit var statusText: TextView
    private lateinit var logText: TextView
    private lateinit var postButton: Button
    private lateinit var countText: TextView
    private lateinit var logBuilder: StringBuilder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.page_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        connectivityHelper = ConnectivityHelper(requireContext())

        logBuilder = StringBuilder()

        authStatusText = view.findViewById(R.id.authStatusText)
        logoutButton = view.findViewById(R.id.logoutButton)
        statusText = view.findViewById(R.id.statusText)
        logText = view.findViewById(R.id.logText)

        logoutButton.setOnClickListener {
            authViewModel.logout()
        }

        view.findViewById<Button>(R.id.postButton).also { postButton = it }.setOnClickListener {
            appendLog("POST sending...")
            viewModel.sendPost(mapOf("key" to "value", "from" to "preset-android"))
        }
        countText = view.findViewById(R.id.countText)
        view.findViewById<Button>(R.id.decrementButton).setOnClickListener { viewModel.decrement() }
        view.findViewById<Button>(R.id.incrementButton).setOnClickListener { viewModel.increment() }

        updateStatus()
        connectivityHelper.registerCallback { type ->
            requireActivity().runOnUiThread {
                appendLog("Network changed: $type")
                updateStatus()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.httpPostState.collect { state ->
                    postButton.isEnabled = !state.isLoading
                    if (!state.isLoading) {
                        if (state.error != null) {
                            appendLog("POST ERROR: ${state.error}")
                        } else if (state.response.isNotEmpty()) {
                            appendLog("POST OK: ${state.response}")
                        }
                    }
                }}
                launch { viewModel.count.collect { count ->
                    countText.text = count.toString()
                }}
                launch { authViewModel.state.collect { state ->
                    if (state.isLoggedIn) {
                        authStatusText.text = "Logged in: ${state.username}"
                        logoutButton.visibility = View.VISIBLE
                    } else {
                        authStatusText.text = "Not logged in"
                        logoutButton.visibility = View.GONE
                        PageRouter.navigateAndFinish(requireActivity(), "/login")
                    }
                }}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        connectivityHelper.unregisterCallback()
    }

    private fun updateStatus() {
        val type = connectivityHelper.getCurrentConnectivity()
        statusText.text = "Network: $type"
    }

    private fun appendLog(message: String) {
        logBuilder.insert(0, "$message\n")
        logText.text = logBuilder.toString()
    }
}
