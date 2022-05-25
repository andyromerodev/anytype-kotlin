package com.anytypeio.anytype.domain.block.repo

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVRecord
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.DocumentInfo
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectInfoWithLinks
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Response
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
import com.anytypeio.anytype.domain.page.Redo
import com.anytypeio.anytype.domain.page.Undo


interface BlockRepository {

    suspend fun uploadFile(command: Command.UploadFile): Hash

    suspend fun move(command: Command.Move): Payload
    suspend fun unlink(command: Command.Unlink): Payload

    suspend fun turnIntoDocument(command: Command.TurnIntoDocument): List<Id>

    /**
     * Duplicates target block
     * @return id of the new block and payload events.
     */
    suspend fun duplicate(command: Command.Duplicate): Pair<List<Id>, Payload>

    /**
     * Creates a new block.
     * @return id of the created block with event payload.
     */
    suspend fun create(command: Command.Create): Pair<Id, Payload>

    /**
     * Creates a new document / page.
     * @return pair of values, where the first one is block id and the second one is target id.
     */
    suspend fun createDocument(command: Command.CreateDocument): Triple<Id, Id, Payload>

    /**
     * Creates a new document / page, without positioning and targets.
     * @return block id of the new document.
     */
    suspend fun createNewDocument(command: Command.CreateNewDocument): Id

    suspend fun merge(command: Command.Merge): Payload

    /**
     * Splits one block into two blocks.
     * @return id of the block, created as a result of splitting.
     */
    suspend fun split(command: Command.Split): Pair<Id, Payload>

    /**
     * Replaces target block by a new block (created from prototype).
     * @see Command.Replace for details
     * @return id of the new block
     */
    suspend fun replace(command: Command.Replace): Pair<Id, Payload>

    suspend fun updateDocumentTitle(command: Command.UpdateTitle)
    suspend fun updateText(command: Command.UpdateText)
    suspend fun updateTextStyle(command: Command.UpdateStyle): Payload

    suspend fun updateTextColor(command: Command.UpdateTextColor): Payload
    suspend fun updateBackgroundColor(command: Command.UpdateBackgroundColor): Payload

    suspend fun updateCheckbox(command: Command.UpdateCheckbox): Payload
    suspend fun updateAlignment(command: Command.UpdateAlignment): Payload

    suspend fun setRelationKey(command: Command.SetRelationKey): Payload

    suspend fun getConfig(): Config

    suspend fun createPage(
        ctx: Id?,
        emoji: String?,
        isDraft: Boolean?,
        type: Id?,
        template: Id?
    ): Id

    suspend fun openObjectPreview(id: Id) : Result<Payload>

    suspend fun openPage(id: String): Result<Payload>

    suspend fun openProfile(id: String): Payload

    suspend fun openObjectSet(id: String): Result<Payload>

    suspend fun closePage(id: String)
    suspend fun openDashboard(contextId: String, id: String): Payload
    suspend fun closeDashboard(id: String)

    /**
     * Upload media or file block by path or url.
     */
    suspend fun uploadBlock(command: Command.UploadBlock): Payload

    suspend fun setDocumentEmojiIcon(command: Command.SetDocumentEmojiIcon): Payload
    suspend fun setDocumentImageIcon(command: Command.SetDocumentImageIcon): Payload
    suspend fun setDocumentCoverColor(ctx: String, color: String): Payload
    suspend fun setDocumentCoverGradient(ctx: String, gradient: String): Payload
    suspend fun setDocumentCoverImage(ctx: String, hash: String): Payload
    suspend fun removeDocumentCover(ctx: String): Payload
    suspend fun removeDocumentIcon(ctx: Id): Payload

    suspend fun setupBookmark(command: Command.SetupBookmark): Payload
    suspend fun createBookmark(command: Command.CreateBookmark): Payload

    suspend fun undo(command: Command.Undo): Undo.Result
    suspend fun redo(command: Command.Redo): Redo.Result

    suspend fun copy(command: Command.Copy): Response.Clipboard.Copy
    suspend fun paste(command: Command.Paste): Response.Clipboard.Paste

    suspend fun getObjectInfoWithLinks(pageId: String): ObjectInfoWithLinks
    suspend fun getListPages(): List<DocumentInfo>

    suspend fun updateDivider(command: Command.UpdateDivider): Payload

    suspend fun setFields(command: Command.SetFields): Payload

    suspend fun getObjectTypes(): List<ObjectType>

    suspend fun createObjectType(prototype: ObjectType.Prototype): ObjectType

    suspend fun createSet(
        context: Id,
        target: Id? = null,
        position: Position? = null,
        objectType: String? = null
    ): CreateObjectSet.Response

    suspend fun setActiveDataViewViewer(
        context: Id,
        block: Id,
        view: Id,
        offset: Int,
        limit: Int
    ): Payload

    suspend fun addNewRelationToDataView(
        context: Id,
        target: Id,
        name: String,
        format: Relation.Format,
        limitObjectTypes: List<Id>
    ): Pair<Id, Payload>

    suspend fun addRelationToDataView(ctx: Id, dv: Id, relation: Id): Payload
    suspend fun deleteRelationFromDataView(ctx: Id, dv: Id, relation: Id): Payload

    suspend fun updateDataViewViewer(
        context: Id,
        target: Id,
        viewer: DVViewer
    ): Payload

    suspend fun duplicateDataViewViewer(
        context: Id,
        target: Id,
        viewer: DVViewer
    ): Payload

    suspend fun createDataViewRecord(
        context: Id,
        target: Id,
        template: Id?
    ): DVRecord

    suspend fun updateDataViewRecord(
        context: Id,
        target: Id,
        record: Id,
        values: Map<String, Any?>
    )

    suspend fun addDataViewViewer(
        ctx: String,
        target: String,
        name: String,
        type: DVViewerType
    ): Payload

    suspend fun removeDataViewViewer(
        ctx: Id,
        dataview: Id,
        viewer: Id
    ): Payload

    suspend fun addDataViewRelationOption(
        ctx: Id,
        dataview: Id,
        relation: Id,
        record: Id,
        name: String,
        color: String
    ): Pair<Payload, Id?>

    suspend fun addObjectRelationOption(
        ctx: Id,
        relation: Id,
        name: Id,
        color: String
    ): Pair<Payload, Id?>

    suspend fun searchObjects(
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        fulltext: String,
        offset: Int,
        limit: Int,
        keys: List<Id> = emptyList()
    ): List<Map<String, Any?>>

    suspend fun searchObjectsWithSubscription(
        subscription: Id,
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        keys: List<String>,
        offset: Long,
        limit: Long,
        beforeId: Id?,
        afterId: Id?,
    ): SearchResult

    suspend fun searchObjectsByIdWithSubscription(
        subscription: Id,
        ids: List<Id>,
        keys: List<String>
    ): SearchResult

    suspend fun cancelObjectSearchSubscription(subscriptions: List<Id>)

    suspend fun relationListAvailable(ctx: Id): List<Relation>

    suspend fun addRelationToObject(ctx: Id, relation: Id): Payload
    suspend fun deleteRelationFromObject(ctx: Id, relation: Id): Payload
    suspend fun addNewRelationToObject(
        ctx: Id,
        name: String,
        format: RelationFormat,
        limitObjectTypes: List<Id>
    ): Pair<Id, Payload>

    suspend fun debugSync(): String
    suspend fun debugLocalStore(path: String): String

    suspend fun turnInto(
        context: Id,
        targets: List<Id>,
        style: Block.Content.Text.Style
    ): Payload

    suspend fun updateDetail(
        ctx: Id,
        key: String,
        value: Any?
    ): Payload

    suspend fun updateBlocksMark(command: Command.UpdateBlocksMark): Payload

    suspend fun addRelationToBlock(command: Command.AddRelationToBlock): Payload

    suspend fun setObjectTypeToObject(ctx: Id, typeId: Id): Payload

    suspend fun addToFeaturedRelations(ctx: Id, relations: List<Id>): Payload
    suspend fun removeFromFeaturedRelations(ctx: Id, relations: List<Id>): Payload

    suspend fun setObjectIsFavorite(ctx: Id, isFavorite: Boolean) : Payload
    suspend fun setObjectIsArchived(ctx: Id, isArchived: Boolean) : Payload
    suspend fun setObjectListIsArchived(targets: List<Id>, isArchived: Boolean)

    suspend fun deleteObjects(targets: List<Id>)

    suspend fun setObjectLayout(ctx: Id, layout: ObjectType.Layout) : Payload

    suspend fun clearFileCache()

    suspend fun duplicateObject(id: Id): Id

    suspend fun applyTemplate(ctx: Id, template: Id)
}