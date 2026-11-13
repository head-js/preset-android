package com.lisitede.preset.preset.pages

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.lisitede.preset.preset.PageRouter
import com.lisitede.preset.preset.R
import com.lisitede.preset.preset.TokenStorage
import com.lisitede.preset.preset.album.AlbumImageLoader
import com.lisitede.preset.preset.album.AlbumPermissions
import com.lisitede.preset.preset.album.AlbumWriter
import com.lisitede.preset.preset.views.ProfileCardView
import com.therouter.router.Route
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Route(path = "/main/profile")
class ProfilePageFragment : Fragment() {

    private var avatarJob: Job? = null

    private val selectImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && view != null) saveAndShowAvatar(uri)
    }

    private val requestAlbumPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Request the permissions without inspecting or validating the grant result.
        if (view != null) {
            selectImage.launch(PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                .build())
        }
    }

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
        profileCardView.onAvatarClick = {
            requestAlbumPermissions.launch(AlbumPermissions.requiredPermissions())
        }
    }

    private fun saveAndShowAvatar(uri: Uri) {
        val root = requireView()
        val context = root.context
        val avatar = root.findViewById<View>(R.id.cardAvatar)
        val profileCard = root.findViewById<ProfileCardView>(R.id.profileCardView)
        val writer = AlbumWriter(context)
        val resolver = context.contentResolver
        avatarJob?.cancel()
        avatarJob = viewLifecycleOwner.lifecycleScope.launch {
            avatar.isEnabled = false
            Toast.makeText(context, R.string.album_saving, Toast.LENGTH_SHORT).show()
            try {
                val saved = writer.copyImage(uri)
                Toast.makeText(context, context.getString(R.string.album_save_success, saved.name), Toast.LENGTH_LONG).show()
                try {
                    profileCard.showAvatar(AlbumImageLoader.readAvatar(resolver, saved.uri))
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    Toast.makeText(context, R.string.avatar_load_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                Toast.makeText(context, R.string.album_save_failed, Toast.LENGTH_LONG).show()
            } finally {
                avatar.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        avatarJob?.cancel()
        avatarJob = null
        super.onDestroyView()
    }
}
