package com.anytypeio.anytype.ui.editor.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.objects.ObjectActionAdapter
import com.anytypeio.anytype.core_ui.layout.SpacingItemDecoration
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentObjectMenuBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.ObjectMenuViewModel
import com.anytypeio.anytype.presentation.objects.ObjectMenuViewModelBase
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectFragment
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectSetFragment
import com.anytypeio.anytype.ui.editor.layout.ObjectLayoutFragment
import com.anytypeio.anytype.ui.editor.modals.ObjectIconPickerBaseFragment
import com.anytypeio.anytype.ui.relations.RelationListFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

abstract class ObjectMenuBaseFragment : BaseBottomSheetFragment<FragmentObjectMenuBinding>() {

    protected val ctx get() = arg<String>(CTX_KEY)
    private val isProfile get() = arg<Boolean>(IS_PROFILE_KEY)
    private val isArchived get() = arg<Boolean>(IS_ARCHIVED_KEY)
    private val isFavorite get() = arg<Boolean>(IS_FAVORITE_KEY)

    abstract val vm: ObjectMenuViewModelBase

    private val actionAdapter by lazy {
        ObjectActionAdapter { action ->
            vm.onActionClicked(ctx, action)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.optionHistory
            .clicks()
            .onEach { vm.onHistoryClicked() }
            .launchIn(lifecycleScope)

        binding.optionLayout
            .clicks()
            .onEach { vm.onLayoutClicked(ctx) }
            .launchIn(lifecycleScope)

        binding.optionIcon
            .clicks()
            .onEach { vm.onIconClicked(ctx) }
            .launchIn(lifecycleScope)

        binding.optionRelations
            .clicks()
            .onEach { vm.onRelationsClicked() }
            .launchIn(lifecycleScope)

        binding.optionCover
            .clicks()
            .onEach { vm.onCoverClicked(ctx) }
            .launchIn(lifecycleScope)

        binding.rvActions.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = actionAdapter
            addItemDecoration(
                SpacingItemDecoration(
                    spacingStart = resources.getDimension(R.dimen.dp_20).toInt(),
                    firstItemSpacingStart = resources.getDimension(R.dimen.dp_16).toInt(),
                    lastItemSpacingEnd = resources.getDimension(R.dimen.dp_16).toInt()
                )
            )
        }
    }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.actions) { actionAdapter.submitList(it) }
            jobs += subscribe(vm.toasts) { toast(it) }
            jobs += subscribe(vm.isDismissed) { isDismissed -> if (isDismissed) dismiss() }
            jobs += subscribe(vm.commands) { command -> execute(command) }
        }
        super.onStart()
        vm.onStart(
            ctx = ctx,
            isArchived = isArchived,
            isFavorite = isFavorite,
            isProfile = isProfile
        )
    }

    private fun execute(command: ObjectMenuViewModelBase.Command) {
        when (command) {
            ObjectMenuViewModelBase.Command.OpenObjectCover -> {
                findNavController().navigate(
                    R.id.objectCoverScreen,
                    bundleOf(SelectCoverObjectFragment.CTX_KEY to ctx)
                )
            }
            ObjectMenuViewModelBase.Command.OpenObjectIcons -> {
                findNavController().navigate(
                    R.id.objectIconPickerScreen,
                    bundleOf(
                        ObjectIconPickerBaseFragment.ARG_CONTEXT_ID_KEY to ctx,
                        ObjectIconPickerBaseFragment.ARG_TARGET_ID_KEY to ctx,
                    )
                )
            }
            ObjectMenuViewModelBase.Command.OpenObjectLayout -> {
                val fr = ObjectLayoutFragment.new(ctx)
                fr.show(childFragmentManager, null)
            }
            ObjectMenuViewModelBase.Command.OpenObjectRelations -> {
                findNavController().navigate(
                    R.id.objectRelationListScreen,
                    bundleOf(
                        RelationListFragment.ARG_CTX to ctx,
                        RelationListFragment.ARG_TARGET to null,
                        RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST
                    )
                )
            }
            ObjectMenuViewModelBase.Command.OpenSetCover -> {
                findNavController().navigate(
                    R.id.objectSetCoverScreen,
                    bundleOf(SelectCoverObjectSetFragment.CTX_KEY to ctx)
                )
            }
            ObjectMenuViewModelBase.Command.OpenSetIcons -> {
                findNavController().navigate(
                    R.id.objectSetIconPickerScreen,
                    bundleOf(
                        ObjectIconPickerBaseFragment.ARG_CONTEXT_ID_KEY to ctx,
                        ObjectIconPickerBaseFragment.ARG_TARGET_ID_KEY to ctx,
                    )
                )
            }
            ObjectMenuViewModelBase.Command.OpenSetLayout -> {
                toast(COMING_SOON_MSG)
            }
            ObjectMenuViewModelBase.Command.OpenSetRelations -> {
                toast(COMING_SOON_MSG)
            }
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentObjectMenuBinding = FragmentObjectMenuBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val CTX_KEY = "arg.doc-menu-bottom-sheet.ctx"
        const val IS_ARCHIVED_KEY = "arg.doc-menu-bottom-sheet.is-archived"
        const val IS_PROFILE_KEY = "arg.doc-menu-bottom-sheet.is-profile"
        const val IS_FAVORITE_KEY = "arg.doc-menu-bottom-sheet.is-favorite"
        const val COMING_SOON_MSG = "Coming soon..."
    }

    interface DocumentMenuActionReceiver {
        fun onArchiveClicked()
        fun onRestoreFromArchiveClicked()
        fun onSearchOnPageClicked()
        fun onDocRelationsClicked()
        fun onAddCoverClicked()
        fun onSetIconClicked()
        fun onLayoutClicked()
        fun onUndoRedoClicked()
    }
}

class ObjectMenuFragment : ObjectMenuBaseFragment() {

    @Inject
    lateinit var factory: ObjectMenuViewModel.Factory
    override val vm by viewModels<ObjectMenuViewModel> { factory }

    override fun onStart() {
        super.onStart()
        with(lifecycleScope) {
            subscribe(vm.isObjectArchived) { isArchived ->
                if (isArchived) parentFragment?.findNavController()?.popBackStack()
            }
        }
    }

    override fun injectDependencies() {
        componentManager().objectMenuComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectMenuComponent.release(ctx)
    }

    companion object {
        fun new(
            ctx: Id,
            isProfile: Boolean = false,
            isArchived: Boolean,
            isFavorite: Boolean
        ) = ObjectMenuFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                IS_ARCHIVED_KEY to isArchived,
                IS_PROFILE_KEY to isProfile,
                IS_FAVORITE_KEY to isFavorite
            )
        }
    }
}