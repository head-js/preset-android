package com.lisitede.preset.preset.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputEditText
import com.lisitede.preset.preset.AuthRepository
import com.lisitede.preset.preset.AuthViewModel
import com.lisitede.preset.preset.PageRouter
import com.lisitede.preset.preset.R
import com.lisitede.preset.preset.TokenStorage
import com.therouter.router.Route
import kotlinx.coroutines.launch

@Route(path = "/login/login")
class LoginPageFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels {
        val prefs = requireActivity().getSharedPreferences("preset_prefs", 0)
        AuthViewModel.Factory(TokenStorage(prefs), AuthRepository())
    }

    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var loginProgress: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.page_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameInput = view.findViewById(R.id.usernameInput)
        passwordInput = view.findViewById(R.id.passwordInput)
        loginButton = view.findViewById(R.id.loginButton)
        loginProgress = view.findViewById(R.id.loginProgress)

        loginButton.setOnClickListener {
            val username = usernameInput.text?.toString()?.trim() ?: ""
            val password = passwordInput.text?.toString()?.trim() ?: ""
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Username and password must not be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authViewModel.login(username, password)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.state.collect { state ->
                    loginProgress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    loginButton.isEnabled = !state.isLoading
                    if (state.error != null) {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                    if (state.isLoggedIn) {
                        PageRouter.navigateAndFinish(requireActivity(), "/main/home")
                    }
                }
            }
        }
    }
}
