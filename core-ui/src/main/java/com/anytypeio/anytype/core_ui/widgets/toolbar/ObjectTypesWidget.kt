package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.databinding.WidgetObjectTypesListBinding
import com.anytypeio.anytype.core_ui.features.objects.ObjectTypeHorizontalListAdapter
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectTypeView

class ObjectTypesWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var isOpenState = true
    private var onItemClick: ((Id) -> Unit)? = null
    private var onSearchClick: (() -> Unit)? = null
    private var onDoneClick: (() -> Unit)? = null

    private val typesAdapter by lazy {
        ObjectTypeHorizontalListAdapter(
            data = arrayListOf(),
            onItemClick = this::onItemClicked,
            onSearchClick = { onSearchClick?.invoke() }
        )
    }

    val binding = WidgetObjectTypesListBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        setup()
    }

    private fun setup() = with(binding) {
        with(objectTypesRecycler) {
            adapter = typesAdapter
        }
        tvChangeType.setOnClickListener {
            if (isOpenState) {
                setHiddenState()
            } else {
                setOpenState()
            }
        }
        iconArrowUp.setOnClickListener {
            setHiddenState()
        }
        iconArrowDown.setOnClickListener {
            setOpenState()
        }
        tvDone.setOnClickListener {
            onDoneClick?.invoke()
        }
    }

    fun setupClicks(onItemClick: (Id) -> Unit, onSearchClick: () -> Unit, onDoneClick: () -> Unit) {
        this.onItemClick = onItemClick
        this.onSearchClick = onSearchClick
        this.onDoneClick = onDoneClick
    }

    fun update(data: List<ObjectTypeView>) {
        setOpenState()
        typesAdapter.update(data)
    }

    fun clear() {
        typesAdapter.update(listOf())
    }

    private fun setOpenState() = with(binding) {
        isOpenState = true
        iconArrowUp.visible()
        iconArrowDown.gone()
        objectTypesRecycler.visible()
    }

    private fun setHiddenState() = with(binding) {
        isOpenState = false
        objectTypesRecycler.gone()
        iconArrowUp.gone()
        iconArrowDown.visible()
    }

    private fun onItemClicked(id: Id, name: String) {
        onItemClick?.invoke(id)
    }
}