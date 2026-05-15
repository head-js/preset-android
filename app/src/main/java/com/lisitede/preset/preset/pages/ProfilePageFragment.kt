package com.lisitede.preset.preset.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lisitede.preset.preset.PageRouter
import com.lisitede.preset.preset.R
import com.lisitede.preset.preset.TokenStorage
import com.lisitede.preset.preset.views.ProfileCardView
import com.therouter.router.Route

@Route(path = "/main/profile")
class ProfilePageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.page_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenStorage = TokenStorage(requireActivity().getSharedPreferences("preset_prefs", 0))
        val token = tokenStorage.getToken()
        val username = tokenStorage.getUsername()

        val profileCardView = view.findViewById<ProfileCardView>(R.id.profileCardView)
        profileCardView.bind(username, token)
        profileCardView.onGoToPlanClick = {
            PageRouter.navigate(requireContext(), "/plan/plan")
        }
    }
}
