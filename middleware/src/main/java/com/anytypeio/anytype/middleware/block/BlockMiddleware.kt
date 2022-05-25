package com.anytypeio.anytype.middleware.block

import com.anytypeio.anytype.core_models.CBTextStyle
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.DocumentInfo
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectInfoWithLinks
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Response
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.data.auth.repo.block.BlockRemote
import com.anytypeio.anytype.middleware.interactor.Middleware
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import com.anytypeio.anytype.middleware.mappers.toCoreModels
import com.anytypeio.anytype.middleware.mappers.toMiddlewareModel

class BlockMiddleware(
    private val middleware: Middleware
) : BlockRemote {

    override suspend fun getConfig(): Config = middleware.getConfig()

    override suspend fun openDashboard(
        contextId: String,
        id: String
    ): Payload = middleware.openDashboard(contextId, id)

    override suspend fun closeDashboard(id: String) {
        middleware.closeDashboard(id)
    }

    override suspend fun createPage(
        ctx: Id?,
        emoji: String?,
        isDraft: Boolean?,
        type: String?,
        template: Id?
    ): String = middleware.createPage(
        ctx = ctx,
        emoji = emoji,
        isDraft = isDraft,
        type = type,
        template = template
    )

    override suspend fun createPage(
        command: Command.CreateNewDocument
    ): String = middleware.createPage(command)

    override suspend fun openPage(id: String): Payload = middleware.openBlock(id)
    override suspend fun openProfile(id: String): Payload = middleware.openBlock(id)
    override suspend fun openObjectSet(id: String): Payload = middleware.openBlock(id)
    override suspend fun openObjectPreview(id: Id): Payload = middleware.showBlock(id)

    override suspend fun closePage(id: String) {
        middleware.closePage(id)
    }

    override suspend fun updateDocumentTitle(command: Command.UpdateTitle) {
        middleware.updateDocumentTitle(command)
    }

    override suspend fun updateText(command: Command.UpdateText) {
        middleware.updateText(
            command.contextId,
            command.blockId,
            command.text,
            command.marks.map { it.toMiddlewareModel() }
        )
    }

    override suspend fun uploadBlock(command: Command.UploadBlock): Payload =
        middleware.uploadBlock(command)

    override suspend fun updateTextStyle(
        command: Command.UpdateStyle
    ): Payload = middleware.updateTextStyle(command)

    override suspend fun updateTextColor(
        command: Command.UpdateTextColor
    ): Payload = middleware.updateTextColor(command)

    override suspend fun updateBackgroundColor(
        command: Command.UpdateBackgroundColor
    ): Payload = middleware.updateBackgroundColor(command)

    override suspend fun updateAlignment(
        command: Command.UpdateAlignment
    ): Payload = middleware.updateAlignment(command)

    override suspend fun updateCheckbox(
        command: Command.UpdateCheckbox
    ): Payload = middleware.updateCheckbox(
        command.context,
        command.target,
        command.isChecked
    )

    override suspend fun create(
        command: Command.Create
    ): Pair<String, Payload> = middleware.createBlock(
        command.context,
        command.target,
        command.position,
        command.prototype
    )

    override suspend fun createDocument(
        command: Command.CreateDocument
    ): Triple<String, String, Payload> = middleware.createDocument(command)

    override suspend fun duplicate(
        command: Command.Duplicate
    ): Pair<List<Id>, Payload> = middleware.duplicate(command)

    override suspend fun duplicateObject(id: Id) = middleware.objectDuplicate(id)

    override suspend fun move(command: Command.Move): Payload {
        return middleware.move(command)
    }

    override suspend fun unlink(
        command: Command.Unlink
    ): Payload = middleware.unlink(command)

    override suspend fun merge(
        command: Command.Merge
    ): Payload = middleware.merge(command)

    override suspend fun split(
        command: Command.Split
    ): Pair<String, Payload> = middleware.split(command)

    override suspend fun setDocumentEmojiIcon(
        command: Command.SetDocumentEmojiIcon
    ): Payload = middleware.setDocumentEmojiIcon(command)

    override suspend fun setDocumentImageIcon(
        command: Command.SetDocumentImageIcon
    ): Payload = middleware.setDocumentImageIcon(command)

    override suspend fun setDocumentCoverColor(
        ctx: String,
        color: String
    ): Payload = middleware.setDocumentCoverColor(ctx = ctx, color = color)

    override suspend fun setDocumentCoverGradient(
        ctx: String,
        gradient: String
    ): Payload = middleware.setDocumentCoverGradient(ctx = ctx, gradient = gradient)

    override suspend fun setDocumentCoverImage(
        ctx: String,
        hash: String
    ): Payload = middleware.setDocumentCoverImage(ctx = ctx, hash = hash)

    override suspend fun removeDocumentCover(
        ctx: String
    ): Payload = middleware.removeDocumentCover(ctx)

    override suspend fun removeDocumentIcon(
        ctx: Id
    ): Payload = middleware.removeDocumentIcon(ctx)

    override suspend fun setupBookmark(
        command: Command.SetupBookmark
    ): Payload = middleware.setupBookmark(command)

    override suspend fun createBookmark(
        command: Command.CreateBookmark
    ): Payload = middleware.createAndSetupBookmark(command)

    override suspend fun undo(
        command: Command.Undo
    ): Payload = middleware.undo(command)

    override suspend fun redo(
        command: Command.Redo
    ): Payload = middleware.redo(command)

    override suspend fun turnIntoDocument(
        command: Command.TurnIntoDocument
    ): List<String> = middleware.turnIntoDocument(command)

    override suspend fun replace(
        command: Command.Replace
    ): Pair<String, Payload> = middleware.replace(command)

    override suspend fun paste(
        command: Command.Paste
    ): Response.Clipboard.Paste = middleware.paste(command)

    override suspend fun copy(
        command: Command.Copy
    ): Response.Clipboard.Copy = middleware.copy(command)

    override suspend fun uploadFile(
        command: Command.UploadFile
    ): String = middleware.uploadFile(command).hash

    override suspend fun getObjectInfoWithLinks(pageId: String): ObjectInfoWithLinks {
        return middleware.getObjectInfoWithLinks(pageId).toCoreModel()
    }

    override suspend fun getListPages(): List<DocumentInfo> {
        return middleware.listObjects().map { it.toCoreModel() }
    }

    override suspend fun setRelationKey(command: Command.SetRelationKey): Payload {
        return middleware.setRelationKey(command)
    }

    override suspend fun updateDivider(
        command: Command.UpdateDivider
    ): Payload = middleware.updateDividerStyle(command)

    override suspend fun setFields(
        command: Command.SetFields
    ): Payload = middleware.setFields(command)

    override suspend fun getObjectTypes(): List<ObjectType> {
        return middleware.getObjectTypes().map { it.toCoreModels() }
    }

    override suspend fun createObjectType(
        prototype: ObjectType.Prototype
    ): ObjectType = middleware.objectTypeCreate(prototype).toCoreModels()

    override suspend fun createSet(
        contextId: String,
        targetId: String?,
        position: Position?,
        objectType: String?
    ): Response.Set.Create = middleware.createSet(
        contextId = contextId,
        targetId = targetId,
        position = position,
        objectType = objectType
    )

    override suspend fun setActiveDataViewViewer(
        context: String,
        block: String,
        view: String,
        offset: Int,
        limit: Int
    ): Payload = middleware.setActiveDataViewViewer(
        contextId = context,
        blockId = block,
        viewId = view,
        offset = offset,
        limit = limit
    )

    override suspend fun addNewRelationToDataView(
        context: String,
        target: String,
        name: String,
        format: Relation.Format,
        limitObjectTypes: List<Id>
    ): Pair<Id, Payload> = middleware.addNewRelationToDataView(
        context = context,
        target = target,
        format = format,
        name = name,
        limitObjectTypes = limitObjectTypes
    )

    override suspend fun addRelationToDataView(
        ctx: Id,
        dv: Id,
        relation: Id
    ): Payload = middleware.addRelationToDataView(
        ctx = ctx,
        dv = dv,
        relation = relation
    )

    override suspend fun deleteRelationFromDataView(
        ctx: Id,
        dv: Id,
        relation: Id
    ): Payload = middleware.deleteRelationFromDataView(
        ctx = ctx,
        dv = dv,
        relation = relation
    )

    override suspend fun updateDataViewViewer(
        context: String,
        target: String,
        viewer: DVViewer
    ): Payload = middleware.updateDataViewViewer(
        context = context,
        target = target,
        viewer = viewer
    )

    override suspend fun duplicateDataViewViewer(
        context: String,
        target: String,
        viewer: DVViewer
    ): Payload = middleware.duplicateDataViewViewer(
        context = context,
        target = target,
        viewer = viewer
    )

    override suspend fun createDataViewRecord(
        context: String,
        target: String,
        template: Id?
    ): Map<String, Any?> = middleware.createDataViewRecord(
        context = context,
        target = target,
        template = template
    )

    override suspend fun updateDataViewRecord(
        context: String,
        target: String,
        record: String,
        values: Map<String, Any?>
    ) = middleware.updateDataViewRecord(
        context = context,
        target = target,
        record = record,
        values = values
    )

    override suspend fun addDataViewViewer(
        ctx: String,
        target: String,
        name: String,
        type: DVViewerType
    ): Payload = middleware.addDataViewViewer(
        ctx = ctx,
        target = target,
        name = name,
        type = type
    )

    override suspend fun removeDataViewViewer(
        ctx: String,
        dataview: String,
        viewer: String
    ): Payload = middleware.removeDataViewViewer(
        ctx = ctx,
        dataview = dataview,
        viewer = viewer
    )

    override suspend fun addDataViewRelationOption(
        ctx: Id,
        dataview: Id,
        relation: Id,
        record: Id,
        name: String,
        color: String
    ): Pair<Payload, Id?> = middleware.addRecordRelationOption(
        ctx = ctx,
        dataview = dataview,
        relation = relation,
        record = record,
        name = name,
        color = color
    )

    override suspend fun addObjectRelationOption(
        ctx: Id,
        relation: Id,
        name: Id,
        color: String
    ): Pair<Payload, Id?> = middleware.addObjectRelationOption(
        ctx = ctx,
        relation = relation,
        name = name,
        color = color
    )

    override suspend fun searchObjects(
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        fulltext: String,
        offset: Int,
        limit: Int,
        keys: List<Id>
    ): List<Map<String, Any?>> = middleware.searchObjects(
        sorts = sorts,
        filters = filters,
        fulltext = fulltext,
        offset = offset,
        limit = limit,
        keys = keys
    )

    override suspend fun searchObjectsWithSubscription(
        subscription: Id,
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        keys: List<String>,
        offset: Long,
        limit: Long,
        beforeId: Id?,
        afterId: Id?
    ): SearchResult = middleware.searchObjectsWithSubscription(
        subscription = subscription,
        sorts = sorts,
        filters = filters,
        keys = keys,
        offset = offset,
        limit = limit,
        afterId = afterId,
        beforeId = beforeId
    )

    override suspend fun searchObjectsByIdWithSubscription(
        subscription: Id,
        ids: List<Id>,
        keys: List<String>
    ): SearchResult = middleware.searchObjectsByIdWithSubscription(
        subscription = subscription,
        ids = ids,
        keys = keys
    )

    override suspend fun cancelObjectSearchSubscription(
        subscriptions: List<Id>
    ) = middleware.cancelObjectSearchSubscription(subscriptions = subscriptions)

    override suspend fun relationListAvailable(
        ctx: Id
    ): List<Relation> = middleware.relationListAvailable(ctx).map { it.toCoreModels() }

    override suspend fun addRelationToObject(
        ctx: Id, relation: Id
    ) : Payload = middleware.addRelationToObject(ctx, relation)

    override suspend fun addNewRelationToObject(
        ctx: Id,
        name: String,
        format: RelationFormat,
        limitObjectTypes: List<Id>
    ): Pair<Id, Payload> = middleware.addNewRelationToObject(
        ctx = ctx,
        format = format,
        name = name,
        limitObjectTypes = limitObjectTypes
    )

    override suspend fun deleteRelationFromObject(
        ctx: Id,
        relation: Id
    ): Payload = middleware.deleteRelationFromObject(
        ctx = ctx,
        relation = relation
    )

    override suspend fun debugSync(): String = middleware.debugSync()
    override suspend fun debugLocalStore(path: String): String = middleware.exportLocalStore(path)

    override suspend fun turnInto(
        context: String,
        targets: List<String>,
        style: CBTextStyle
    ): Payload = middleware.blockListTurnInto(
        context = context,
        targets = targets,
        style = style
    )

    override suspend fun updateDetail(
        ctx: Id,
        key: String,
        value: Any?
    ): Payload = middleware.updateDetail(
        ctx = ctx,
        key = key,
        value = value
    )

    override suspend fun updateBlocksMark(command: Command.UpdateBlocksMark): Payload =
        middleware.blockListSetTextMarkup(command)

    override suspend fun addRelationToBlock(command: Command.AddRelationToBlock): Payload =
        middleware.addRelationToBlock(command)

    override suspend fun setObjectTypeToObject(ctx: Id, typeId: Id): Payload =
        middleware.setObjectType(ctx = ctx, typeId = typeId)

    override suspend fun addToFeaturedRelations(
        ctx: Id,
        relations: List<Id>
    ): Payload = middleware.addToFeaturedRelations(ctx, relations)

    override suspend fun removeFromFeaturedRelations(
        ctx: Id,
        relations: List<Id>
    ): Payload = middleware.removeFromFeaturedRelations(ctx, relations)

    override suspend fun setObjectIsFavorite(
        ctx: Id,
        isFavorite: Boolean
    ): Payload = middleware.setObjectIsFavorite(ctx = ctx, isFavorite = isFavorite)

    override suspend fun setObjectIsArchived(
        ctx: Id,
        isArchived: Boolean
    ): Payload = middleware.setObjectIsArchived(ctx = ctx, isArchived = isArchived)

    override suspend fun deleteObjects(targets: List<Id>) = middleware.deleteObjects(
        targets = targets
    )

    override suspend fun setObjectListIsArchived(
        targets: List<Id>,
        isArchived: Boolean
    ) = middleware.setObjectListIsArchived(
        targets = targets,
        isArchived = isArchived
    )

    override suspend fun setObjectLayout(ctx: Id, layout: ObjectType.Layout) : Payload =
        middleware.setObjectLayout(ctx, layout)

    override suspend fun clearFileCache() = middleware.fileListOffload()

    override suspend fun applyTemplate(ctx: Id, template: Id) = middleware.applyTemplate(
        ctx = ctx,
        template = template
    )
}