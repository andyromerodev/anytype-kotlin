package com.agileburo.anytype.ui.page.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.reactive.clicks
import com.agileburo.anytype.core_utils.ext.hideKeyboard
import com.agileburo.anytype.core_utils.ext.toast
import com.agileburo.anytype.core_utils.ui.BaseBottomSheetFragment
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.page.bookmark.CreateBookmarkViewModel
import com.agileburo.anytype.presentation.page.bookmark.CreateBookmarkViewModel.ViewState
import com.agileburo.anytype.ui.page.OnFragmentInteractionListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_create_bookmark.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class CreateBookmarkFragment : BaseBottomSheetFragment(), Observer<ViewState> {

    private val target: String
        get() = requireArguments()
            .getString(ARG_TARGET)
            ?: throw IllegalStateException(MISSING_TARGET_ERROR)

    @Inject
    lateinit var factory: CreateBookmarkViewModel.Factory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(CreateBookmarkViewModel::class.java)
    }

    companion object {

        private const val ARG_TARGET = "arg.create.bookmark.target"

        private const val MISSING_TARGET_ERROR = "Target missing in args"

        fun newInstance(
            target: String
        ): CreateBookmarkFragment = CreateBookmarkFragment().apply {
            arguments = bundleOf(ARG_TARGET to target)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_create_bookmark, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnShowListener { dg ->
            val bottomSheet = (dg as? BottomSheetDialog)?.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.setBackgroundColor(requireContext().color(android.R.color.transparent))
        }
        cancelBookmarkButton.setOnClickListener {
            it.hideKeyboard()
            this.dismiss()
        }
        createBookmarkButton
            .clicks()
            .onEach {
                vm.onCreateBookmarkClicked(
                    url = urlInput.text.toString()
                )
            }
            .launchIn(lifecycleScope)
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
}