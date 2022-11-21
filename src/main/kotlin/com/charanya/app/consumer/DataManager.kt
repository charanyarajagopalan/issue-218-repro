package com.charanya.app.consumer

import com.charanya.app.AUD_FIELD_IDS
import com.charanya.app.config.ConfigProperties
import com.charanya.app.util.CONSUMER_PREFIX
import com.charanya.app.util.createConsumerConfigMap
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.refinitiv.ema.access.EmaFactory
import com.refinitiv.ema.access.OmmConsumer
import com.refinitiv.ema.access.ReqMsg
import com.refinitiv.ema.rdm.EmaRdm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Instant
import javax.annotation.PreDestroy
import kotlin.concurrent.thread

/**
 * Consumer Manager, used to setup market data consumer & subscription
 */
@Component
class DataManager(
    configProperties: ConfigProperties,
    private val discoveryConsumer: DiscoveryConsumer,
    private val dataConsumer: DataConsumer
) : CoroutineScope {
    override val coroutineContext = Dispatchers.Default + SupervisorJob()

    private val machineId = configProperties.refinitiv.machineId
    private val password = configProperties.refinitiv.password
    private val appKey = configProperties.refinitiv.appKey
    private val service = configProperties.refinitiv.service

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    /**
     * Define our consumer that receives messages via TCP, manages the connection & processes item (RIC data) requests
     */
    private var consumer: OmmConsumer? = null
    private lateinit var initThread: Thread

    private val consumerRequestMutex = Mutex()

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        initThread = startListeners()
    }

    @PreDestroy
    private fun destroy() = removeConsumer()

    fun CoroutineScope.setupSubscriptions() = launch {
        val rics: List<String> =
            jacksonObjectMapper().readValue(javaClass.getResourceAsStream("/rics.json")!!)
        logger.info("Requesting [${rics.size}] rics")
        requestBatchRics(rics)
    }

    /**
     * The thread is LONG RUNNING, therefore the try catch runs indefinitely
     */
    private fun startListeners() = thread(name = "init-thread") {
        while (true) {
            try {
                logger.info("Creating consumer & setting up subscriptions")
                createConsumer()
                setupSubscriptions()
                // We block our running thread (so we can still catch exceptions during dispatch), but the app
                // doesn't switch back to this thread
                logger.info("Sleeping thread indefinitely [${consumer?.consumerName()}]")
                Thread.sleep(Long.MAX_VALUE)
            } catch (e: Exception) {
                logger.error(
                    "Caught exception. Removing consumer [${consumer?.consumerName()}]",
                    e
                )
                removeConsumer()
            }
        }
    }

    /**
     * Process in sublists in case we exceed 64KiB (65536 bytes) as this is Refinitiv's max message size
     */
    private suspend fun requestBatchRics(rics: List<String>) {
        val batchedRics = rics.chunked(RICS_IN_REFINITIV_BATCH_VIEW_REQUEST)
        logger.debug("Requesting [${rics.size}] rics in [${batchedRics.size}] batches")
        batchedRics.forEach {
            logger.debug("Requesting batch RICs: $it")
            val ricArray = EmaFactory.createOmmArray().apply {
                it.forEach { ric -> add(EmaFactory.createOmmArrayEntry().ascii(ric)) }
            }

            val batchList = EmaFactory.createElementList().apply {
                add(EmaFactory.createElementEntry().array(EmaRdm.ENAME_BATCH_ITEM_LIST, ricArray))
                if (REQUEST_SUBSET_MARKET_DATA_FIELDS) {
                    // The view array allows us to customize what data we are interested in (reduces amount of data received)
                    val viewDataArray = EmaFactory.createOmmArray().apply {
                        AUD_FIELD_IDS.values.forEach { fieldValue ->
                            add(EmaFactory.createOmmArrayEntry().intValue(fieldValue))
                        }
                    }

                    add(EmaFactory.createElementEntry().uintValue(EmaRdm.ENAME_VIEW_TYPE, 1))
                    add(EmaFactory.createElementEntry().array(EmaRdm.ENAME_VIEW_DATA, viewDataArray))
                }
            }

            val requestMessage = EmaFactory
                .createReqMsg()
                .serviceName(service)
                .payload(batchList)

            val streamId = addToConsumer(requestMessage)
            streamId?.let { logger.debug("Subscribed for updates on [${rics.size}] rics in [${batchedRics.size}] batches in stream $streamId") }
                ?: logger.error("Unable to subscribe on [${rics.size}] rics, consumer unavailable")
        }
    }

    private suspend fun addToConsumer(reqMsg: ReqMsg): Long? = consumerRequestMutex.withLock {
        consumer?.run {
            registerClient(reqMsg, dataConsumer)
        }
    }

    /**
     * We discover the correct host/port using service discovery, and then populate the consumer configuration with this information.
     */
    private fun createConsumer() {
        val endpoint = discoveryConsumer.discoverEndpoint(machineId, password, appKey)

        val consumerName = "$CONSUMER_PREFIX${Instant.now().epochSecond}"
        val configMap = createConsumerConfigMap(consumerName, endpoint!!.url, endpoint!!.port)
        // The consumer configuration class allows the customization of the consumer (OmmConsumer) interface.
        val consumerConfig = EmaFactory.createOmmConsumerConfig()
            .consumerName(consumerName)
            .username(machineId)
            .password(password)
            .clientId(appKey)
            .config(configMap)

        EmaFactory.createOmmConsumer(consumerConfig).run {
            logger.info("Created new consumer[$consumerName]")

            val loginReq = EmaFactory.Domain.createLoginReq()
            registerClient(loginReq.message(), dataConsumer)
            logger.info("Registered login messages with consumer")
            consumer = this
        }
    }

    /**
     * Uninitialize our consumer to clean up, unregister and disconnect.
     */
    private fun removeConsumer() {
        consumer?.apply {
            try {
                consumer = null
                uninitialize()
                logger.info("Uninitialized consumer[${consumerName()}]")
            } catch (e: Exception) {
                logger.warn("Could not uninitialize consumer[${consumerName()}]. Continuing...", e)
            }
        } ?: return
    }

    companion object {
        private const val REQUEST_SUBSET_MARKET_DATA_FIELDS = true
        private const val RICS_IN_REFINITIV_BATCH_VIEW_REQUEST =
            1 // Set very low on purpose here, to increase chances to hitting deadlock
    }
}
