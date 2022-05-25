package com.anytypeio.anytype.middleware.config

import anytype.Rpc.Config
import com.anytypeio.anytype.data.auth.repo.config.Configurator
import service.Service

typealias CoreConfig = com.anytypeio.anytype.core_models.Config
/**
 * Obtains middleware configuration data.
 */
class DefaultConfigurator : Configurator {

    override fun configure() = get()

    private val builder: () -> CoreConfig = {
        fetchConfig().let { response ->
            CoreConfig(
                home = response.homeBlockId,
                gateway = response.gatewayUrl,
                profile = response.profileBlockId
            )
        }
    }

    private var instance: CoreConfig? = null

    fun get() = instance ?: builder().also { instance = it }

    fun new() = builder().also { instance = it }

    override fun release() {
        instance = null
    }

    private fun fetchConfig(): Config.Get.Response {
        val request = Config.Get.Request()
        val encoded = Service.configGet(Config.Get.Request.ADAPTER.encode(request))
        val response = Config.Get.Response.ADAPTER.decode(encoded)
        return parseResponse(response)
    }

    private fun parseResponse(response: Config.Get.Response): Config.Get.Response {
        val error = response.error
        return if (error != null && error.code != Config.Get.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            response
        }
    }
}