package com.anytypeio.anytype.core_ui.features.dataview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.sets.model.Viewer
import timber.log.Timber

class ViewerTypeAdapter(
    private var items: List<Viewer> = emptyList(),
    private val gridAdapter: ViewerGridAdapter,
    private val gridHeaderAdapter: ViewerGridHeaderAdapter,
    private val listAdapter: ViewerListAdapter
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val pool = RecyclerView.RecycledViewPool()

    init {
        setHasStableIds(true)
    }

    fun update(viewer: Viewer) {
        Timber.d("Updating adapter")
        items = listOf(viewer)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (val item = items[position]) {
        is Viewer.GridView -> HOLDER_GRID
        is Viewer.ListView -> HOLDER_LIST
        else -> throw IllegalStateException("Unsupported type: $item")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Timber.d("onCreateViewHolder")
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_viewer_container, parent, false)
        return when (viewType) {
            HOLDER_GRID -> GridHolder(view).apply {
                rows.setRecycledViewPool(pool)
            }
            HOLDER_LIST -> ListHolder(view)
            else -> throw RuntimeException("Unknown adapter type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Timber.d("onBindViewHolder")
        when (holder) {
            is GridHolder -> holder.bind(
                viewer = items[position] as Viewer.GridView,
                gridAdapter = gridAdapter,
                headerAdapter = gridHeaderAdapter
            )
            is ListHolder -> holder.bind(listAdapter)
        }
    }

    class GridHolder(view: View) : RecyclerView.ViewHolder(view) {

        val columns: RecyclerView = itemView.findViewById(R.id.rvHeader)
        val rows: RecyclerView = itemView.findViewById(R.id.rvRows)

        private val horizontalDivider =
            ContextCompat.getDrawable(view.context, R.drawable.divider_dv_horizontal)
        private val verticalDivider =
            ContextCompat.getDrawable(view.context, R.drawable.divider_dv_grid)

        init {
            rows.setItemViewCacheSize(20)
            columns.addItemDecoration(
                DividerItemDecoration(
                    itemView.context,
                    DividerItemDecoration.HORIZONTAL
                ).apply {
                    if (horizontalDivider != null) setDrawable(horizontalDivider)
                }
            )
            rows.addItemDecoration(
                DividerItemDecoration(
                    itemView.context,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    if (verticalDivider != null) setDrawable(verticalDivider)
                }
            )
        }

        fun bind(
            viewer: Viewer.GridView,
            gridAdapter: ViewerGridAdapter,
            headerAdapter: ViewerGridHeaderAdapter
        ) {
            Timber.d("Binding grid")

            if (columns.adapter == null) {
                Timber.d("Setting columns adapter")
                columns.adapter = headerAdapter
            }
            if (rows.adapter == null) {
                Timber.d("Setting rows adapter")
                rows.adapter = gridAdapter
            }

            headerAdapter.submitList(viewer.columns)
            gridAdapter.submitList(viewer.rows)

            itemView.findViewById<HorizontalScrollView>(R.id.horizontalScrollView)
                .setOnScrollChangeListener { _, scrollX, _, _, _ ->
                    val translationX = scrollX.toFloat()
                    gridAdapter.recordNamePositionX = translationX
                    rows.children.forEach { view ->
                        view.findViewById<ViewGroup>(R.id.headerContainer).translationX =
                            translationX
                    }
                }
        }
    }

    class ListHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(adapter: ViewerListAdapter) {
            itemView.findViewById<RecyclerView>(R.id.rvRows).adapter = adapter
        }
    }

    companion object {
        const val HOLDER_GRID = 1
        const val HOLDER_LIST = 2
    }
}