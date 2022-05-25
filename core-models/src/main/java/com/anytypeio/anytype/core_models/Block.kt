package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.Block.Content.Text.Mark
import com.anytypeio.anytype.core_models.Block.Content.Text.Style
import com.anytypeio.anytype.core_models.ext.typeOf

/**
 * Represents block as basic data structure.
 * @property id block's id
 * @property children block's children ids
 * @property fields block's fields
 * @property content block's content
 * @property backgroundColor background color for the whole block
 */
data class Block(
    val id: String,
    val children: List<String>,
    val content: Content,
    val fields: Fields,
    val backgroundColor: String? = null
) {

    /**
     * Block fields containing useful block properties.
     * @property map map containing fields
     */
    data class Fields(val map: Map<String, Any?>) {

        private val default = map.withDefault { null }

        val featuredRelations: List<String>? by default
        val name: String? by default
        val iconEmoji: String? by default
        val coverId: String? by default
        val coverType: Double? by default
        val iconImage: String? by default
        val isArchived: Boolean? by default
        val isLocked: Boolean? by default
        val isDeleted: Boolean? by default
        val isFavorite: Boolean? by default
        val done: Boolean? by default
        val lang: String? by default
        val fileExt: String? by default
        val fileMimeType: String? by default
        val type: List<String>
            get() = when (val value = map[TYPE_KEY]) {
                is String -> listOf(value)
                is List<*> -> value.typeOf()
                else -> emptyList()
            }

        val id: Id? by default
        val isDraft: Boolean? by default
        val snippet: String? by default

        val layout: Double?
            get() = when (val value = map[Relations.LAYOUT]) {
                is Double -> value
                is Int -> value.toDouble()
                else -> null
            }

        val withName: Boolean? by default
        val withDescription: Boolean? by default
        val withIcon: Boolean? by default
        val withCover: Boolean? by default

        /**
         * 0.0 - text, 1.0 - card
         */
        val style: Double? by default

        /**
         *  1.0 - small, 2.0 - medium, 3.0 - large
         */
        val iconSize: Double? by default

        val analyticsContext: String? by default

        companion object {
            fun empty(): Fields = Fields(emptyMap())
            const val NAME_KEY = "name"
            const val TYPE_KEY = "type"
            const val ICON_SIZE_KEY = "iconSize"
            const val ICON_WITH_KEY = "withIcon"
            const val IS_LOCKED_KEY = "isLocked"
            const val COVER_WITH_KEY = "withCover"
            const val STYLE_KEY = "style"
            const val WITH_NAME_KEY = "withName"
            const val WITH_DESCRIPTION_KEY = "withDescription"
        }
    }

    /**
     * Document metadata
     * @property details maps id of the block to its details (contained as fields)
     */
    data class Details(val details: Map<Id, Fields> = emptyMap())

    /**
     * Block's content.
     */
    sealed class Content {

        fun asText() = this as Text
        fun asLink() = this as Link

        /**
         * Smart block.
         */
        data class Smart(val type: SmartBlockType = SmartBlockType.PAGE) : Content()

        /**
         * Textual block.
         * @property text content text
         * @property marks markup related to [text],
         * @property isChecked whether this block is checked or not (see [Style.CHECKBOX])
         * @property color text color, which should be applied to the whole block (as opposed to [Mark.Type.TEXT_COLOR])
         */
        data class Text(
            val text: String,
            val style: Style,
            val marks: List<Mark>,
            val isChecked: Boolean? = null,
            val color: String? = null,
            val align: Align? = null
        ) : Content() {

            /**
             * Toggles checked/unchecked state.
             * Does not modify this instance's checked/unchecked state (preserves immutability)
             * @return new checked/unchecked state without modifying
             */
            fun toggleCheck(): Boolean = isChecked == null || isChecked == false

            /**
             * @return true if this is a title block.
             */
            fun isTitle() = style == Style.TITLE

            /**
             * @return true if this is a toggle block.
             */
            fun isToggle() = style == Style.TOGGLE

            /**
             * @return true if this text block is a list item.
             */
            fun isList(): Boolean {
                return style == Style.BULLET || style == Style.CHECKBOX || style == Style.NUMBERED
            }

            fun isHeader(): Boolean {
                return style == Style.H1 || style == Style.H2 || style == Style.H3 || style == Style.H4
            }

            /**
             * Mark as a part of markup.
             * @property type markup type
             * @property param optional parameter (i.e. text color, url, etc)
             * @property range text range for markup (start == start char index, end == end char index + 1).
             */
            data class Mark(
                val range: IntRange,
                val type: Type,
                val param: String? = null
            ) {
                enum class Type {
                    STRIKETHROUGH,
                    KEYBOARD,
                    ITALIC,
                    BOLD,
                    UNDERSCORED,
                    LINK,
                    TEXT_COLOR,
                    BACKGROUND_COLOR,
                    MENTION,
                    EMOJI,
                    OBJECT
                }

                fun isClickableMark(): Boolean =
                    type == Type.LINK || type == Type.MENTION || type == Type.OBJECT
            }

            /**
             * Style H4 is depricated
             */
            enum class Style {
                P, H1, H2, H3, H4, TITLE, QUOTE, CODE_SNIPPET, BULLET, NUMBERED, TOGGLE, CHECKBOX, DESCRIPTION, CALLOUT
            }
        }

        data class Layout(val type: Type) : Content() {
            enum class Type { ROW, COLUMN, DIV, HEADER }
        }

        @Deprecated("Legacy class")
        data class Page(val style: Style) : Content() {
            enum class Style { EMPTY, TASK, SET }
        }

        /**
         * A link to some other block.
         * @property target id of the target block
         * @property type type of the link
         * @property fields fields storing additional properties
         */
        data class Link(
            val target: Id,
            val type: Type,
            val fields: Fields
        ) : Content() {
            enum class Type { PAGE, DATA_VIEW, DASHBOARD, ARCHIVE }
        }

        /**
         * Page icon.
         * @property name conventional emoji short name.
         */
        @Deprecated("To be deleted")
        data class Icon(
            val name: String
        ) : Content()

        /**
         * File block.
         * @property hash file hash
         * @property name filename
         * @property mime mime type
         * @property size file size (in bytes)
         * @property type file type
         * @property state file state
         */
        data class File(
            val hash: String? = null,
            val name: String? = null,
            val mime: String? = null,
            val size: Long? = null,
            val type: Type? = null,
            val state: State? = null
        ) : Content() {
            enum class Type { NONE, FILE, IMAGE, VIDEO, AUDIO, PDF }
            enum class State { EMPTY, UPLOADING, DONE, ERROR }
        }

        /**
         * @property url url associated with this bookmark
         * @property title optional bookmark title
         * @property description optional bookmark's content description
         * @property image optional hash of bookmark's image
         * @property favicon optional hash of bookmark's favicon
         */
        data class Bookmark(
            val url: Url?,
            val title: String?,
            val description: String?,
            val image: Hash?,
            val favicon: Hash?
        ) : Content()

        data class Divider(val style: Style) : Content() {
            enum class Style { LINE, DOTS }
        }

        object FeaturedRelations : Content()

        data class RelationBlock(val key: Id?) : Content()

        data class DataView(
            val sources: List<String>,
            val viewers: List<Viewer>,
            val relations: List<Relation>
        ) : Content() {

            data class Viewer(
                val id: String,
                val name: String,
                val type: Type,
                val sorts: List<Sort>,
                val filters: List<Filter>,
                val viewerRelations: List<ViewerRelation>,
                val cardSize: Size = Size.SMALL,
                val hideIcon: Boolean = false,
                val coverFit: Boolean = false,
                val coverRelationKey: String? = null
            ) {

                enum class Type { GRID, LIST, GALLERY, BOARD }

                enum class Size { SMALL, MEDIUM, LARGE }

                //relations fields/columns options, also used to provide the order
                data class ViewerRelation(
                    val key: String,
                    val isVisible: Boolean,
                    val width: Int? = null,
                    val dateFormat: DateFormat? = null,
                    val timeFormat: TimeFormat? = null,
                    val isDateIncludeTime: Boolean? = null
                )
            }

            enum class DateFormat(val format: String) {
                MONTH_ABBR_BEFORE_DAY("MMM dd, yyyy"),  // Jul 30, 2020
                MONTH_ABBR_AFTER_DAY("dd MMM yyyy"),    // 30 Jul 2020
                SHORT("dd/MM/yyyy"),                    // 30/07/2020
                SHORTUS("MM/dd/yyyy"),                  // 07/30/2020
                ISO("yyyy-MM-dd")                       // 2020-07-30
            }

            enum class TimeFormat { H12, H24 }

            data class Sort(
                val relationKey: String,
                val type: Type
            ) {
                enum class Type { ASC, DESC }
            }

            data class Filter(
                val relationKey: String,
                val operator: Operator = Operator.AND,
                val condition: Condition,
                val value: Any?
            ) {
                enum class Operator { AND, OR }
                enum class Condition {
                    EQUAL, NOT_EQUAL, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL,
                    LIKE, NOT_LIKE, IN, NOT_IN, EMPTY, NOT_EMPTY, ALL_IN, NOT_ALL_IN, NONE
                }

                enum class ConditionType { TEXT, NUMBER, SELECT, CHECKBOX }
            }
        }

        data class Latex(val latex: String) : Content()
        object TableOfContents : Content()
        object Unsupported : Content()
    }

    /**
     * Block prototype used as a model or a blueprint for a block to create.
     */
    sealed class Prototype {
        /**
         * Prototype of the textual block.
         * @param style style for a block to create
         */
        data class Text(
            val style: Content.Text.Style
        ) : Prototype()

        data class Page(
            val style: Content.Page.Style
        ) : Prototype()

        data class File(
            val type: Content.File.Type,
            val state: Content.File.State
        ) : Prototype()

        data class Link(
            val target: Id
        ) : Prototype()

        object DividerLine : Prototype()
        object DividerDots : Prototype()
        object Bookmark : Prototype()
        object Latex : Prototype()
        data class Relation(
            val key: Id
        ) : Prototype()
        object TableOfContents : Prototype()
    }

    /**
     * Block alignment property
     */
    sealed class Align {
        object AlignLeft : Align()
        object AlignCenter : Align()
        object AlignRight : Align()
    }
}