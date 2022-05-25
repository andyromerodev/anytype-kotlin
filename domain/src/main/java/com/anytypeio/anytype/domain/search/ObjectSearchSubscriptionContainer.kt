package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.`object`.move
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.ObjectStore
import kotlinx.coroutines.flow.*

class ObjectSearchSubscriptionContainer(
    private val repo: BlockRepository,
    private val channel: SubscriptionEventChannel,
    private val store: ObjectStore,
    private val dispatchers: AppCoroutineDispatchers
) {

    fun subscribe(subscriptions: List<Id>) = channel.subscribe(subscriptions)

    // TODO refact: replace parameters with one param class
    fun observe(
        subscription: Id,
        sorts: List<DVSort> = emptyList(),
        filters: List<DVFilter> = emptyList(),
        offset: Long,
        limit: Long,
        keys: List<String>
    ): Flow<Subscription> {
        return flow {

            val initial = repo.searchObjectsWithSubscription(
                subscription = subscription,
                sorts = sorts,
                filters = filters,
                offset = offset,
                limit = limit,
                keys = keys,
                afterId = null,
                beforeId = null
            )

            store.merge(
                objects = initial.results,
                dependencies = initial.dependencies,
                subscriptions = listOf(subscription)
            )

            val sub = Subscription(
                objects = initial.results.map { it.id }
            )

            emitAll(
                subscribe(listOf(subscription)).scan(sub) { s, payload ->
                    var result = s
                    payload.forEach { event ->
                        when(event) {
                            is SubscriptionEvent.Add -> {
                                val afterId = event.afterId
                                if (afterId != null) {
                                    val afterIdx = result.objects.indexOfFirst { id ->
                                        afterId == id
                                    }
                                    val updated = result.objects.toMutableList().apply {
                                        if (afterIdx != -1) {
                                            add(afterIdx.inc(), event.target)
                                        } else {
                                            add(0, event.target)
                                        }
                                    }
                                    result = result.copy(
                                        objects = updated
                                    )
                                } else {
                                    result = result.copy(
                                        objects = result.objects.toMutableList().apply {
                                            add(0, event.target)
                                        }
                                    )
                                }
                                store.subscribe(
                                    subscription = event.subscription,
                                    target = event.target
                                )
                            }
                            is SubscriptionEvent.Amend -> {
                                store.amend(
                                    target = event.target,
                                    diff = event.diff,
                                    subscriptions = event.subscriptions
                                )
                            }
                            is SubscriptionEvent.Position -> {
                                result = result.copy(
                                    objects = result.objects.move(
                                        target = event.target,
                                        afterId = event.afterId
                                    )
                                )
                            }
                            is SubscriptionEvent.Remove -> {
                                result = result.copy(
                                    objects = result.objects.filter { id ->
                                        id != event.target
                                    }
                                )
                                store.unsubscribe(
                                    target = event.target,
                                    subscription = event.subscription
                                )
                            }
                            is SubscriptionEvent.Set -> {
                                store.set(
                                    target = event.target,
                                    data = event.data,
                                    subscriptions = event.subscriptions
                                )
                            }
                            is SubscriptionEvent.Unset -> {
                                store.unset(
                                    target = event.target,
                                    keys = event.keys,
                                    subscriptions = event.subscriptions
                                )
                            }
                        }
                    }
                    result
                }
            )
        }
            .flowOn(dispatchers.io)
    }
}