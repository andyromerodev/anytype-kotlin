package com.anytypeio.anytype.ui.alert

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentAlertBinding
import com.anytypeio.anytype.ui.editor.OnFragmentInteractionListener

class AlertUpdateAppFragment : BaseBottomSheetFragment<FragmentAlertBinding>() {

    companion object {
        const val TG_PACKAGE = "org.telegram.messenger"
        const val TG_WEB_PACKAGE = "org.thunderdog.challegram"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.later.setOnClickListener {
            (parentFragment as? OnFragmentInteractionListener)?.onExitToDesktopClicked()
            dismiss()
        }
        binding.update.setOnClickListener {
            val intent = telegramIntent(requireContext())
            startActivity(intent)
        }
    }

    private fun telegramIntent(context: Context): Intent =
        try {
            try {
                context.packageManager.getPackageInfo(TG_PACKAGE, 0)
            } catch (e: Exception) {
                context.packageManager.getPackageInfo(TG_WEB_PACKAGE, 0)
            }
            Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.telegram_app)))
        } catch (e: Exception) {
            Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.telegram_web)))
        }

    override fun injectDependencies() {}

    override fun releaseDependencies() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAlertBinding = FragmentAlertBinding.inflate(
        inflater, container, false
    )
}