package com.agileburo.anytype.domain.event.model

import com.agileburo.anytype.domain.common.Id

/**
 * Represent events, as response to some command.
 * @param context id of the context
 * @param events new events, contained in response.
 */
data class Payload(
    val context: Id,
    val events: List<Event>
)