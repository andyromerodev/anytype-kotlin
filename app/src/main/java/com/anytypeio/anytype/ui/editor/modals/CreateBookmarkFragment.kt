package com.anytypeio.anytype.ui.editor.modals

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.syncFocusWithImeVisibility
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetImeOffsetFragment
import com.anytypeio.anytype.databinding.DialogCreateBookmarkBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.editor.bookmark.CreateBookmarkViewModel
import com.anytypeio.anytype.presentation.editor.bookmark.CreateBookmarkViewModel.ViewState
import com.anytypeio.anytype.ui.editor.OnFragmentInteractionListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class CreateBookmarkFragment : BaseBottomSheetImeOffsetFragment<DialogCreateBookmarkBinding>(), Observer<ViewState> {

    private val target: String
        get() = requireArguments()
            .getString(ARG_TARGET)
            ?: throw IllegalStateException(MISSING_TARGET_ERROR)

    @Inject
    lateinit var factory: CreateBookmarkViewModel.Factory
    private val vm by viewModels<CreateBookmarkViewModel> { factory }

    companion object {

        private const val ARG_TARGET = "arg.create.bookmark.target"

        private const val MISSING_TARGET_ERROR = "Target missing in args"

        fun newInstance(
            target: String
        ): CreateBookmarkFragment = CreateBookmarkFragment().apply {
            arguments = bundleOf(ARG_TARGET to target)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnShowListener { dg ->
            val bottomSheet = (dg as? BottomSheetDialog)?.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.setBackgroundColor(requireContext().color(android.R.color.transparent))
        }

        binding.cancelBookmarkButton.setOnClickListener {
            it.hideKeyboard()
            this.dismiss()
        }

        binding.createBookmarkButton
            .clicks()
            .onEach {
                vm.onCreateBookmarkClicked(
                    url = binding.urlInput.text.toString()
                )
            }
            .launchIn(lifecycleScope)

        setupWindowInsetAnimation()
    }

    private fun setupWindowInsetAnimation() {
        if (BuildConfig.USE_NEW_WINDOW_INSET_API && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.urlInput.syncFocusWithImeVisibility()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
    }

    override fun onChanged(state: ViewState) {
        when (state) {
            is ViewState.Success -> {
                (parentFragment as OnFragmentInteractionListener).onAddBookmarkUrlClicked(
                    target = target,
                    url = state.url
                )
                view?.rootView?.hideKeyboard()
                dismiss()
            }
            is ViewState.Error -> toast(state.message)
            is ViewState.Exit -> dismiss()
        }
    }

    override fun injectDependencies() {
        componentManager().createBookmarkSubComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createBookmarkSubComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogCreateBookmarkBinding = DialogCreateBookmarkBinding.inflate(
        inflater, container, false
    )
}