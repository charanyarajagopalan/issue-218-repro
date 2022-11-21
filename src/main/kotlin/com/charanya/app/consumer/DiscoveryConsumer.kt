package com.charanya.app.consumer

import com.charanya.app.config.ConfigProperties
import com.refinitiv.ema.access.EmaFactory
import com.refinitiv.ema.access.ServiceEndpointDiscoveryClient
import com.refinitiv.ema.access.ServiceEndpointDiscoveryEvent
import com.refinitiv.ema.access.ServiceEndpointDiscoveryResp
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

@Component
class DiscoveryConsumer(configProperties: ConfigProperties) : ServiceEndpointDiscoveryClient {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    private val location = configProperties.refinitiv.region
    private val pauseTime = 3_000L
    private val maxPauseTime = 30_000L
    private val retryCount = AtomicLong(0)

    var endpointUrl: String? = null
    var endpointPort: String? = null

    override fun onSuccess(serviceEndpointResp: ServiceEndpointDiscoveryResp, event: ServiceEndpointDiscoveryEvent) {
        val discoveryInfo = serviceEndpointResp.serviceEndpointInfoList()
            .find {
                it.locationList().size > 1 && it.endpoint().startsWith(location) && it.transport() == "tcp"
            }!!
        endpointUrl = discoveryInfo.endpoint()
        endpointPort = discoveryInfo.port()
        logger.info("Set endpointUrl[$endpointUrl] and endpointPort[$endpointPort].")
    }

    override fun onError(errorText: String, event: ServiceEndpointDiscoveryEvent) {
        logger.error("Failed to query RDP service discovery. errorText[$errorText]")
    }

    fun discoverEndpoint(username: String, password: String, clientId: String): Endpoint? {
        val serviceDiscovery = EmaFactory.createServiceEndpointDiscovery()
        val options = EmaFactory.createServiceEndpointDiscoveryOption()
            .username(username)
            .password(password)
            .clientId(clientId)

        // This is a synchronous call to the client (waits for onSuccess/onError)
        serviceDiscovery.registerClient(options, this)
        serviceDiscovery.uninitialize()

        if (endpointUrl == null || endpointPort == null) return null
        return Endpoint(endpointUrl!!, endpointPort!!)
    }
}

data class Endpoint(
    val url: String,
    val port: String
)
