package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.diff.DefaultObjectDiffIdentifier
import com.anytypeio.anytype.presentation.sets.model.FileView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView

sealed class DocumentRelationView : DefaultObjectDiffIdentifier {

    abstract val relationId: Id
    abstract val name: String
    abstract val value: String?
    abstract val isFeatured: Boolean

    override val identifier: String get() = relationId

    data class Default(
        override val relationId: Id,
        override val name: String,
        override val value: String? = null,
        override val isFeatured: Boolean = false,
        val format: Relation.Format
    ) : DocumentRelationView()

    data class Checkbox(
        override val relationId: Id,
        override val name: String,
        override val isFeatured: Boolean = false,
        val isChecked: Boolean
    ): DocumentRelationView() {
        override val value: String? = null
    }

    data class Status(
        override val relationId: Id,
        override val name: String,
        override val value: String? = null,
        override val isFeatured: Boolean = false,
        val status: List<StatusView>,
    ) : DocumentRelationView()

    data class Tags(
        override val relationId: Id,
        override val name: String,
        override val value: String? = null,
        override val isFeatured: Boolean = false,
        val tags: List<TagView>,
    ) : DocumentRelationView()

    data class Object(
        override val relationId: Id,
        override val name: String,
        override val value: String? = null,
        override val isFeatured: Boolean = false,
        val objects: List<ObjectView>
    ) : DocumentRelationView()

    data class File(
        override val relationId: Id,
        override val name: String,
        override val value: String? = null,
        override val isFeatured: Boolean = false,
        val files: List<FileView>
    ) : DocumentRelationView()

    /**
     * @property [type] object type id
     * @property [relationId] id of the relation
     */
    data class ObjectType(
        override val relationId: Id,
        override val name: String,
        override val value: String? = null,
        override val isFeatured: Boolean = false,
        val type: Id
    ) : DocumentRelationView()
}