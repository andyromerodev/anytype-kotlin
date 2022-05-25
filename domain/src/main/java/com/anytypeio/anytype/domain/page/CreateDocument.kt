package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider

/**
 * Use-case for creating a new document.
 * Should return a pair of ids, where the first one is block id, the second one is target id.
 */
class CreateDocument(
    private val repo: BlockRepository,
    private val documentEmojiProvider: DocumentEmojiIconProvider
) : BaseUseCase<CreateDocument.Result, CreateDocument.Params>() {

    override suspend fun run(params: Params) = try {
        repo.createDocument(
            command = Command.CreateDocument(
                context = params.context,
                target = params.target,
                position = params.position,
                emoji = null,
                type = null,
                layout = null
            )
        ).let { (id, target, payload) ->
            Either.Right(
                Result(
                    id = id,
                    target = target,
                    payload = payload
                )
            )
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * Params for creating a new document
     * @property context id of the context of the block (i.e. page, dashboard or something else)
     * @property target id of the block associated with the block we need to create
     * @property position position of the block that we need to create in relation with the target block
     */
    data class Params(
        val context: Id,
        val target: Id,
        val position: Position
    )

    /**
     * Result for this use-case
     * @property id id of the new block (link)
     * @property target id of the target for this new block
     * @property payload payload of events
     */
    data class Result(
        val id: String,
        val target: String,
        val payload: Payload
    )
}