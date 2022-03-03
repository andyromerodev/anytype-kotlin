package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.relations.LimitObjectTypeAdapter
import com.anytypeio.anytype.core_ui.features.relations.RelationConnectWithAdapter
import com.anytypeio.anytype.core_ui.features.relations.RelationFormatAdapter
import com.anytypeio.anytype.core_ui.features.relations.RelationNameInputAdapter
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentRelationCreateFromScratchBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.RelationCreateFromScratchBaseViewModel
import com.anytypeio.anytype.presentation.relations.RelationCreateFromScratchForDataViewViewModel
import com.anytypeio.anytype.presentation.relations.RelationCreateFromScratchForObjectBlockViewModel
import com.anytypeio.anytype.presentation.relations.RelationCreateFromScratchForObjectViewModel
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchFormatPickerFragment.Companion.FLOW_BLOCK
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchFormatPickerFragment.Companion.FLOW_DV
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchFormatPickerFragment.Companion.FLOW_OBJECT
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchFormatPickerFragment.Companion.FLOW_TYPE
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.Serializable
import javax.inject.Inject

abstract class RelationCreateFromScratchBaseFragment :
    BaseBottomSheetFragment<FragmentRelationCreateFromScratchBinding>() {

    abstract val vm: RelationCreateFromScratchBaseViewModel

    protected val ctx get() = arg<Id>(CTX_KEY)
    private val query get() = arg<Id>(QUERY_KEY)

    private val nameInputAdapter = RelationNameInputAdapter {
        vm.onNameChanged(it)
    }

    private val connectWithAdapter = RelationConnectWithAdapter {
        onConnectWithClicked()
    }

    private val limitObjectTypeAdapter = LimitObjectTypeAdapter {
        onLimitObjectTypeClicked()
    }

    private val relationAdapter = RelationFormatAdapter(
        onItemClick = { relation -> vm.onRelationFormatClicked(relation.format) }
    )

    private val concatAdapter = ConcatAdapter(
        nameInputAdapter,
        connectWithAdapter,
        limitObjectTypeAdapter
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            dialog?.setOnShowListener { dg ->
                val bottomSheet = (dg as? BottomSheetDialog)?.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet
                )
                bottomSheet?.let {
                    val behavior = BottomSheetBehavior.from(it)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.skipCollapsed = true
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nameInputAdapter.query = query
        binding.rvCreateRelationFromScratch.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = concatAdapter
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_relations_with_padding))
                }
            )
        }
        binding.btnAction.setOnClickListener { onCreateRelationClicked() }


        with(lifecycleScope) {
            subscribe(vm.views) { relationAdapter.submitList(it) }
            subscribe(vm.isActionButtonEnabled) { binding.btnAction.isEnabled = it }
            subscribe(vm.isDismissed) { isDismissed ->
                if (isDismissed) {
                    // Refact parent dismissing
                    parentFragment?.let { parent ->
                        if (parent is RelationAddBaseFragment) parent.dismiss()
                    }
                    dismiss()
                }
            }
            subscribe(vm.createFromScratchSession) { session ->
                connectWithAdapter.format = session.format
            }
            subscribe(vm.limitObjectTypeValueView) { view ->
                limitObjectTypeAdapter.limitObjectTypeView = view
            }
            subscribe(vm.toasts) { toast(it) }
        }
    }

    abstract fun onCreateRelationClicked()
    abstract fun onConnectWithClicked()
    abstract fun onLimitObjectTypeClicked()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRelationCreateFromScratchBinding = FragmentRelationCreateFromScratchBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val CTX_KEY = "arg.relation-create-from-scratch.ctx"
        const val QUERY_KEY = "arg.relation-create-from-scratch.query"
    }

}

class RelationCreateFromScratchForObjectFragment : RelationCreateFromScratchBaseFragment() {

    @Inject
    lateinit var factory: RelationCreateFromScratchForObjectViewModel.Factory
    override val vm: RelationCreateFromScratchForObjectViewModel by viewModels { factory }

    override fun onCreateRelationClicked() {
        vm.onCreateRelationClicked(ctx)
    }

    override fun onLimitObjectTypeClicked() {
        findNavController().navigate(
            R.id.limitObjectTypeScreen,
            bundleOf(
                LimitObjectTypeFragment.CTX_KEY to ctx,
                LimitObjectTypeFragment.FLOW_TYPE to LimitObjectTypeFragment.FLOW_OBJECT
            )
        )
    }

    override fun onConnectWithClicked() {
        findNavController().navigate(
            R.id.relationFormatPickerScreen,
            bundleOf(
                RelationCreateFromScratchFormatPickerFragment.CTX_KEY to ctx,
                FLOW_TYPE to FLOW_OBJECT
            )
        )
    }

    override fun injectDependencies() {
        componentManager().relationCreateFromScratchForObjectComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().relationCreateFromScratchForObjectComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, query: String) = RelationCreateFromScratchForObjectFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, QUERY_KEY to query)
        }
    }
}

class RelationCreateFromScratchForDataViewFragment : RelationCreateFromScratchBaseFragment() {

    private val dv get() = arg<Id>(DV_KEY)

    @Inject
    lateinit var factory: RelationCreateFromScratchForDataViewViewModel.Factory
    override val vm: RelationCreateFromScratchForDataViewViewModel by viewModels { factory }

    override fun onCreateRelationClicked() {
        vm.onCreateRelationClicked(ctx = ctx, dv = dv)
    }

    override fun onLimitObjectTypeClicked() {
        findNavController().navigate(
            R.id.limitObjectTypeScreen,
            bundleOf(
                LimitObjectTypeFragment.CTX_KEY to ctx,
                LimitObjectTypeFragment.FLOW_TYPE to LimitObjectTypeFragment.FLOW_DV
            )
        )
    }

    override fun onConnectWithClicked() {
        findNavController().navigate(
            R.id.relationFormatPickerScreen,
            bundleOf(
                RelationCreateFromScratchFormatPickerFragment.CTX_KEY to ctx,
                FLOW_TYPE to FLOW_DV
            )
        )
    }

    override fun injectDependencies() {
        componentManager().relationCreateFromScratchForDataViewComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().relationCreateFromScratchForDataViewComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, dv: Id, query: String) =
            RelationCreateFromScratchForDataViewFragment().apply {
                arguments = bundleOf(CTX_KEY to ctx, DV_KEY to dv, QUERY_KEY to query)
            }

        const val DV_KEY = "arg.relation-create-from-scratch-for-data-view.ctx"
    }
}

class RelationCreateFromScratchForObjectBlockFragment : RelationCreateFromScratchBaseFragment() {

    private val target get() = arg<String>(TARGET_KEY)

    @Inject
    lateinit var factory: RelationCreateFromScratchForObjectBlockViewModel.Factory
    override val vm: RelationCreateFromScratchForObjectBlockViewModel by viewModels { factory }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.commands) { observeCommands(it) }
        }
        super.onStart()
    }

    private fun observeCommands(command: RelationCreateFromScratchForObjectBlockViewModel.Command) {
        when (command) {
            is RelationCreateFromScratchForObjectBlockViewModel.Command.OnSuccess -> {
                val result = RelationNewResult(
                    target = target,
                    relation = command.relation
                )
                val editorScreenEntry = findNavController().getBackStackEntry(R.id.pageScreen)
                editorScreenEntry.savedStateHandle.set(RELATION_NEW_RESULT_KEY, result)
                findNavController().popBackStack(R.id.pageScreen, false)
            }
        }
    }

    override fun onCreateRelationClicked() {
        vm.onCreateRelationClicked(ctx)
    }

    override fun onLimitObjectTypeClicked() {
        findNavController().navigate(
            R.id.limitObjectTypeScreen,
            bundleOf(
                LimitObjectTypeFragment.CTX_KEY to ctx,
                LimitObjectTypeFragment.FLOW_TYPE to LimitObjectTypeFragment.FLOW_BLOCK
            )
        )
    }

    override fun onConnectWithClicked() {
        findNavController().navigate(
            R.id.relationFormatPickerScreen,
            bundleOf(
                RelationCreateFromScratchFormatPickerFragment.CTX_KEY to ctx,
                FLOW_TYPE to FLOW_BLOCK
            )
        )
    }

    override fun injectDependencies() {
        componentManager().relationCreateFromScratchForObjectBlockComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().relationCreateFromScratchForObjectBlockComponent.release(ctx)
    }

    companion object {
        const val TARGET_KEY = "arg.rel-create-object-block.target"
        const val RELATION_NEW_RESULT_KEY = "arg.rel-create-object-block.result"
    }
}

data class RelationNewResult(val target: String, val relation: String) : Serializable