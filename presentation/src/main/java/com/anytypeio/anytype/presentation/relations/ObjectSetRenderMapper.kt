package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerCardSize
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.ext.title
import com.anytypeio.anytype.core_utils.ext.EMPTY_TIMESTAMP
import com.anytypeio.anytype.core_utils.ext.EXACT_DAY
import com.anytypeio.anytype.core_utils.ext.MONTH_AGO
import com.anytypeio.anytype.core_utils.ext.MONTH_AHEAD
import com.anytypeio.anytype.core_utils.ext.TODAY
import com.anytypeio.anytype.core_utils.ext.TOMORROW
import com.anytypeio.anytype.core_utils.ext.WEEK_AGO
import com.anytypeio.anytype.core_utils.ext.WEEK_AHEAD
import com.anytypeio.anytype.core_utils.ext.YESTERDAY
import com.anytypeio.anytype.core_utils.ext.getMonthAgoTimeUnit
import com.anytypeio.anytype.core_utils.ext.getMonthAheadTimeUnit
import com.anytypeio.anytype.core_utils.ext.getTodayTimeUnit
import com.anytypeio.anytype.core_utils.ext.getTomorrowTimeUnit
import com.anytypeio.anytype.core_utils.ext.getWeekAgoTimeUnit
import com.anytypeio.anytype.core_utils.ext.getWeekAheadTimeUnit
import com.anytypeio.anytype.core_utils.ext.getYesterdayTimeUnit
import com.anytypeio.anytype.core_utils.ext.isSameDay
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.extension.isValueRequired
import com.anytypeio.anytype.presentation.mapper.toCheckboxView
import com.anytypeio.anytype.presentation.mapper.toGridRecordRows
import com.anytypeio.anytype.presentation.mapper.toNumberView
import com.anytypeio.anytype.presentation.mapper.toSelectedView
import com.anytypeio.anytype.presentation.mapper.toSimpleRelations
import com.anytypeio.anytype.presentation.mapper.toTextView
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.mapper.toViewerColumns
import com.anytypeio.anytype.presentation.number.NumberParser
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetViewState
import com.anytypeio.anytype.presentation.sets.buildGalleryViews
import com.anytypeio.anytype.presentation.sets.buildListViews
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterView
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.FilterValue
import com.anytypeio.anytype.presentation.sets.model.FilterView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.SortingExpression
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.model.ViewerTabView
import timber.log.Timber
import java.util.*


fun ObjectSet.tabs(activeViewerId: String? = null): List<ViewerTabView> {
    val block = blocks.first { it.content is DV }
    val dv = block.content as DV
    return dv.viewers.mapIndexed { index, viewer ->
        ViewerTabView(
            id = viewer.id,
            name = viewer.name,
            isActive = if (activeViewerId != null)
                viewer.id == activeViewerId
            else
                index == 0
        )
    }
}

// TODO rework the function to exclude index == -1 scenario
fun ObjectSet.render(
    index: Int = 0,
    ctx: Id,
    builder: UrlBuilder,
    useFallbackView: Boolean = false
): ObjectSetViewState {

    val block = blocks.first { it.content is DV }

    val dv = block.content as DV

    val viewer = if (index >= 0) dv.viewers[index] else dv.viewers.first()

    if (viewer.type == DVViewerType.GALLERY) {
        val records = viewerDb[viewer.id]?.records ?: emptyList()
        val view = Viewer.GalleryView(
            id = viewer.id,
            items = viewer.buildGalleryViews(
                objects = records.map { ObjectWrapper.Basic(it) },
                details = details,
                relations = dv.relations,
                urlBuilder = builder
            ),
            title = viewer.name,
            largeCards = viewer.cardSize != DVViewerCardSize.SMALL
        )
        return ObjectSetViewState(
            title = title(ctx = ctx, urlBuilder = builder),
            viewer = view
        )
    }

    val vmap = viewer.viewerRelations.associateBy { it.key }

    val relations = dv.relations.filter { relation ->
        val vr = vmap[relation.key]
        vr?.isVisible ?: false
    }

    val columns = viewer.viewerRelations.toViewerColumns(
        relations = relations,
        filterBy = listOf(ObjectSetConfig.NAME_KEY)
    )

    val rows = mutableListOf<Viewer.GridView.Row>()

    viewerDb[viewer.id]?.let { data ->
        rows.addAll(
            data.records.toGridRecordRows(
                columns = columns,
                relations = relations,
                details = details,
                builder = builder,
                types = objectTypes
            )
        )
    }

    val dvview = when (viewer.type) {
        Block.Content.DataView.Viewer.Type.GRID -> {
            Viewer.GridView(
                id = viewer.id,
                source = dv.sources.firstOrNull().orEmpty(),
                name = viewer.name,
                columns = columns,
                rows = rows
            )
        }
        Block.Content.DataView.Viewer.Type.GALLERY -> {
            Viewer.GalleryView(
                id = viewer.id,
                items = emptyList(),
                title = viewer.name
            )
        }
        Block.Content.DataView.Viewer.Type.LIST -> {
            Viewer.ListView(
                id = viewer.id,
                items = viewer.buildListViews(
                    objects = (viewerDb[viewer.id]?.records ?: emptyList()).map { ObjectWrapper.Basic(it) },
                    details = details,
                    relations = relations,
                    urlBuilder = builder
                ),
                title = viewer.name
            )
        }
        else -> {
            if (useFallbackView) {
                Viewer.GridView(
                    id = viewer.id,
                    source = dv.sources.firstOrNull().orEmpty(),
                    name = viewer.name,
                    columns = columns,
                    rows = rows
                )
            } else {
                Viewer.Unsupported(
                    id = viewer.id,
                    title = viewer.name,
                    error = "This view type (${viewer.type.name.lowercase()}) is not supported on Android yet. See it as grid view?"
                )
            }
        }
    }

    return ObjectSetViewState(
        title = title(ctx = ctx, urlBuilder = builder),
        viewer = dvview
    )
}

fun ObjectSet.title(
    ctx: Id,
    urlBuilder: UrlBuilder
): BlockView.Title.Basic? {
    val title = blocks.title() ?: return null

    val objectDetails = details[ctx]

    var coverColor: CoverColor? = null
    var coverImage: Url? = null
    var coverGradient: String? = null

    when (val type = objectDetails?.coverType?.toInt()) {
        CoverType.UPLOADED_IMAGE.code -> {
            coverImage = objectDetails.coverId?.let { id ->
                urlBuilder.image(id)
            }
        }
        CoverType.COLOR.code -> {
            coverColor = objectDetails.coverId?.let { id ->
                CoverColor.values().find { it.code == id }
            }
        }
        CoverType.GRADIENT.code -> {
            coverGradient = objectDetails.coverId
        }
        else -> Timber.d("Missing cover type: $type")
    }


    return BlockView.Title.Basic(
        id = title.id,
        text = title.content<Block.Content.Text>().text,
        emoji = objectDetails?.iconEmoji?.ifEmpty { null },
        image = objectDetails?.iconImage?.let { hash ->
            if (hash.isNotEmpty())
                urlBuilder.thumbnail(hash = hash)
            else
                null
        },
        coverImage = coverImage,
        coverColor = coverColor,
        coverGradient = coverGradient
    )
}

fun ObjectSet.simpleRelations(viewerId: Id?): ArrayList<SimpleRelationView> {
    return if (isInitialized) {
        val block = blocks.first { it.content is DV }
        val dv = block.content as DV
        val viewer = dv.viewers.find { it.id == viewerId } ?: dv.viewers.first()
        viewer.viewerRelations.toSimpleRelations(dv.relations)
    } else {
        arrayListOf()
    }
}

fun DVViewer.toViewRelation(relation: Relation): SimpleRelationView {
    val viewerRelation = viewerRelations.firstOrNull { it.key == relation.key }
    if (viewerRelation == null) {
        Timber.e("ViewerRelations is not containing relation:$relation")
    }
    return SimpleRelationView(
        key = relation.key,
        isHidden = relation.isHidden,
        isVisible = viewerRelation?.isVisible ?: false,
        title = relation.name,
        format = relation.format.toView()
    )
}

fun Relation.toCreateFilterCheckboxView(isSelected: Boolean? = null): List<CreateFilterView.Checkbox> {
    return listOf(
        CreateFilterView.Checkbox(
            isChecked = true,
            isSelected = isSelected == true
        ),
        CreateFilterView.Checkbox(
            isChecked = false,
            isSelected = isSelected != true
        )
    )
}

fun Relation.toCreateFilterTagView(ids: List<*>? = null): List<CreateFilterView.Tag> =
    selections
        .filter { it.scope == Relation.OptionScope.LOCAL }
        .map { option ->
            CreateFilterView.Tag(
                id = option.id,
                name = option.text,
                color = option.color,
                isSelected = ids?.contains(option.id) ?: false
            )
        }

fun Relation.toCreateFilterStatusView(ids: List<*>? = null): List<CreateFilterView.Status> =
    selections
        .filter { it.scope == Relation.OptionScope.LOCAL }
        .map { option ->
            CreateFilterView.Status(
                id = option.id,
                name = option.text,
                color = option.color,
                isSelected = ids?.contains(option.id) ?: false
            )
        }

fun Relation.toCreateFilterDateView(exactDayTimestamp: Long): List<CreateFilterView.Date> {
    val filterTime = Calendar.getInstance()
    if (exactDayTimestamp != EMPTY_TIMESTAMP) {
        filterTime.timeInMillis = exactDayTimestamp * 1000
    }
    val today = getTodayTimeUnit()
    val tomorrow = getTomorrowTimeUnit()
    val yesterday = getYesterdayTimeUnit()
    val weekAgo = getWeekAgoTimeUnit()
    val weekForward = getWeekAheadTimeUnit()
    val monthAgo = getMonthAgoTimeUnit()
    val monthForward = getMonthAheadTimeUnit()

    val isToday = filterTime.isSameDay(today)
    val isTomorrow = filterTime.isSameDay(tomorrow)
    val isYesterday = filterTime.isSameDay(yesterday)
    val isWeekAgo = filterTime.isSameDay(weekAgo)
    val isWeekAhead = filterTime.isSameDay(weekForward)
    val isMonthAgo = filterTime.isSameDay(monthAgo)
    val isMonthAhead = filterTime.isSameDay(monthForward)
    val isExactDay = !isToday && !isTomorrow && !isYesterday && !isWeekAgo && !isWeekAhead
            && !isMonthAgo && !isMonthAhead

    return listOf(
        CreateFilterView.Date(
            id = key,
            description = TODAY,
            type = DateDescription.TODAY,
            timeInMillis = today.timeInMillis,
            isSelected = isToday
        ),
        CreateFilterView.Date(
            id = key,
            description = TOMORROW,
            type = DateDescription.TOMORROW,
            timeInMillis = tomorrow.timeInMillis,
            isSelected = isTomorrow
        ),
        CreateFilterView.Date(
            id = key,
            description = YESTERDAY,
            type = DateDescription.YESTERDAY,
            timeInMillis = yesterday.timeInMillis,
            isSelected = isYesterday
        ),
        CreateFilterView.Date(
            id = key,
            description = WEEK_AGO,
            type = DateDescription.ONE_WEEK_AGO,
            timeInMillis = weekAgo.timeInMillis,
            isSelected = isWeekAgo
        ),
        CreateFilterView.Date(
            id = key,
            description = WEEK_AHEAD,
            type = DateDescription.ONE_WEEK_FORWARD,
            timeInMillis = weekForward.timeInMillis,
            isSelected = isWeekAhead
        ),
        CreateFilterView.Date(
            id = key,
            description = MONTH_AGO,
            type = DateDescription.ONE_MONTH_AGO,
            timeInMillis = monthAgo.timeInMillis,
            isSelected = isMonthAgo
        ),
        CreateFilterView.Date(
            id = key,
            description = MONTH_AHEAD,
            type = DateDescription.ONE_MONTH_FORWARD,
            timeInMillis = monthForward.timeInMillis,
            isSelected = isMonthAhead
        ),
        CreateFilterView.Date(
            id = key,
            description = EXACT_DAY,
            type = DateDescription.EXACT_DAY,
            timeInMillis = filterTime.timeInMillis,
            isSelected = isExactDay
        )
    )
}

enum class DateDescription {
    TODAY, TOMORROW, YESTERDAY, ONE_WEEK_AGO,
    ONE_WEEK_FORWARD, ONE_MONTH_AGO, ONE_MONTH_FORWARD, EXACT_DAY
}

fun ObjectSet.columns(viewerId: Id): ArrayList<ColumnView> {

    val block = blocks.first { it.content is DV }

    val dv = block.content as DV

    val viewer = dv.viewers.first { it.id == viewerId }

    val columns = viewer.viewerRelations.toViewerColumns(
        dv.relations, listOf()
    )
    return ArrayList(columns)
}

fun ObjectSet.sortingExpression(viewerId: Id): ArrayList<SortingExpression> {

    val block = blocks.first { it.content is DV }

    val dv = block.content as DV

    val viewer = dv.viewers.first { it.id == viewerId }

    val list = arrayListOf<SortingExpression>()
    viewer.sorts.forEach { sort ->
        list.add(
            SortingExpression(key = sort.relationKey, type = sort.type.toView())
        )
    }
    return list
}

fun ObjectSet.filterExpression(viewerId: Id?): List<DVFilter> {

    val block = blocks.first { it.content is DV }

    val dv = block.content as DV

    val viewer = dv.viewers.find { it.id == viewerId } ?: dv.viewers.first()
    return viewer.filters
}

fun Relation.toText(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

fun Relation.toUrl(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

fun Relation.toPhone(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

fun Relation.toEmail(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

fun Relation.toCheckbox(value: Any?): Boolean? =
    if (value is Boolean?) {
        value
    } else {
        throw IllegalArgumentException("Relation format $format value should be Boolean, actual:$value")
    }

fun Relation.toTags(value: Any?): List<TagView> = if (value is List<*>?) {
    val views = arrayListOf<TagView>()
    value?.filterIsInstance<Id>()?.forEach { id ->
        val option = selections.find { it.id == id }
        if (option != null) {
            views.add(
                TagView(
                    id = option.id,
                    tag = option.text,
                    color = option.color
                )
            )
        } else {
            Timber.e("Failed to find corresponding tag option")
        }
    }
    views.toList()
} else {
    throw IllegalArgumentException("Relation format $format value should be List<String>, actual:$value")
}

fun Relation.toStatus(value: Any?): StatusView? =
    if (value is List<*>?) {
        var status: StatusView? = null
        val filter = value?.filterIsInstance<String>()?.firstOrNull()
        val option = selections.firstOrNull { it.id == filter }
        if (option != null) {
            status = StatusView(
                id = option.id,
                status = option.text,
                color = option.color
            )
        }
        status
    } else {
        throw IllegalArgumentException("Relation format $format value should be List<String>, actual:$value")
    }

fun Relation.toObjects(
    value: Any?,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder
) = if (value is List<*>?) {
    val ids = value?.filterIsInstance<String>() ?: emptyList()
    val list = arrayListOf<ObjectView>()
    ids.forEach { id ->
        val wrapper = ObjectWrapper.Basic(details[id]?.map ?: emptyMap())
        if (wrapper.isDeleted == true) {
            list.add(ObjectView.Deleted(id = id))
        } else {
            list.add(
                ObjectView.Default(
                    id = id,
                    name = wrapper.getProperName(),
                    icon = ObjectIcon.from(
                        obj = wrapper,
                        layout = wrapper.layout,
                        builder = urlBuilder
                    ),
                    types = wrapper.type
                )
            )
        }
    }
    list
} else {
    throw IllegalArgumentException("Relation format $format value should be List<String>, actual:$value")
}

fun DVFilter.toView(
    relation: Relation,
    details: Map<Id, Block.Fields>,
    isInEditMode: Boolean,
    urlBuilder: UrlBuilder
): FilterView.Expression = when (relation.format) {
    Relation.Format.SHORT_TEXT -> {
        FilterView.Expression.TextShort(
            key = relationKey,
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toTextView(),
            filterValue = FilterValue.TextShort(relation.toText(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.LONG_TEXT -> {
        FilterView.Expression.Text(
            key = relationKey,
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toTextView(),
            filterValue = FilterValue.Text(relation.toText(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.URL -> {
        FilterView.Expression.Url(
            key = relationKey,
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toTextView(),
            filterValue = FilterValue.Url(relation.toUrl(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.EMAIL -> {
        FilterView.Expression.Email(
            key = relationKey,
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toTextView(),
            filterValue = FilterValue.Email(relation.toEmail(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.PHONE -> {
        FilterView.Expression.Phone(
            key = relationKey,
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toTextView(),
            filterValue = FilterValue.Phone(relation.toPhone(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.NUMBER -> {
        FilterView.Expression.Number(
            key = relationKey,
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toNumberView(),
            filterValue = FilterValue.Number(NumberParser.parse(value)),
            format = relation.format.toView(),
            isValueRequired = true,
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.DATE -> {
        FilterView.Expression.Date(
            key = relationKey,
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toNumberView(),
            filterValue = FilterValue.Date(DateParser.parse(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.STATUS -> {
        FilterView.Expression.Status(
            key = relationKey,
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toSelectedView(),
            filterValue = FilterValue.Status(relation.toStatus(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.TAG -> {
        FilterView.Expression.Tag(
            key = relationKey,
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toSelectedView(),
            filterValue = FilterValue.Tag(relation.toTags(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.OBJECT -> {
        FilterView.Expression.Object(
            key = relationKey,
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toSelectedView(),
            filterValue = FilterValue.Object(relation.toObjects(value, details, urlBuilder)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.CHECKBOX -> {
        FilterView.Expression.Checkbox(
            key = relationKey,
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toCheckboxView(),
            filterValue = FilterValue.Check(relation.toCheckbox(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    else -> throw UnsupportedOperationException("Unsupported relation format:${relation.format}")
}

fun Relation.toFilterValue(
    value: Any?,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder
): FilterValue =
    when (this.format) {
        Relation.Format.SHORT_TEXT -> FilterValue.TextShort(toText(value))
        Relation.Format.LONG_TEXT -> FilterValue.Text(toText(value))
        Relation.Format.NUMBER -> FilterValue.Number(NumberParser.parse(value))
        Relation.Format.STATUS -> FilterValue.Status(toStatus(value))
        Relation.Format.TAG -> FilterValue.Tag(toTags(value))
        Relation.Format.DATE -> FilterValue.Date(DateParser.parse(value))
        Relation.Format.URL -> FilterValue.Url(toText(value))
        Relation.Format.EMAIL -> FilterValue.Email(toText(value))
        Relation.Format.PHONE -> FilterValue.Phone(toText(value))
        Relation.Format.OBJECT -> FilterValue.Object(toObjects(value, details, urlBuilder))
        Relation.Format.CHECKBOX -> FilterValue.Check(toCheckbox(value))
        else -> throw UnsupportedOperationException("Unsupported relation format:${format}")
    }

fun List<ObjectType>.getTypePrettyName(type: String?): String? =
    firstOrNull { it.url == type }?.name

fun List<ObjectType>.getObjectTypeById(types: List<String>?): ObjectType? {
    types?.forEach { type ->
        val objectType = firstOrNull { it.url == type }
        if (objectType != null) {
            return objectType
        }
    }
    return null
}