package com.anytypeio.anytype.data.auth.status

import com.anytypeio.anytype.data.auth.model.SyncStatusEntity
import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.flow.Flow

interface ThreadStatusRemoteChannel {
    fun observe(ctx: Id): Flow<SyncStatusEntity>
}