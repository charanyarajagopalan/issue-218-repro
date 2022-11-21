package com.charanya.app.consumer

import com.refinitiv.ema.access.AckMsg
import com.refinitiv.ema.access.EmaFactory
import com.refinitiv.ema.access.GenericMsg
import com.refinitiv.ema.access.Msg
import com.refinitiv.ema.access.OmmConsumerClient
import com.refinitiv.ema.access.OmmConsumerEvent
import com.refinitiv.ema.access.RefreshMsg
import com.refinitiv.ema.access.StatusMsg
import com.refinitiv.ema.access.UpdateMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class DataConsumer : OmmConsumerClient, CoroutineScope {
    override val coroutineContext = Dispatchers.Default + SupervisorJob()
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    /**
     * We clone the refreshMsg as this object is _NOT_ thread safe
     */
    override fun onRefreshMsg(refreshMsg: RefreshMsg, event: OmmConsumerEvent) {
        handleMessage(EmaFactory.createRefreshMsg(refreshMsg))
    }

    /**
     * We clone the updateMsg as this object is _NOT_ thread safe
     */
    override fun onUpdateMsg(updateMsg: UpdateMsg, event: OmmConsumerEvent) {
        handleMessage(EmaFactory.createUpdateMsg(updateMsg))
    }

    private fun CoroutineScope.handleMessage(message: Msg) = launch {
        if (!message.hasName()) {
            logger.warn("${message::class.simpleName} needs to have a name if it is to be processed: message[$message]")
            return@launch
        }

        val itemName = message.name()
        logger.debug("${message::class.simpleName} for [$itemName]: [${message.payload()}]")
    }

    override fun onStatusMsg(statusMsg: StatusMsg, event: OmmConsumerEvent) {
        // Do nothing
    }

    override fun onAllMsg(msg: Msg, consumerEvent: OmmConsumerEvent) {
        // Do nothing
    }

    override fun onAckMsg(ackMsg: AckMsg, consumerEvent: OmmConsumerEvent) {
        // Do nothing
    }

    override fun onGenericMsg(genericMsg: GenericMsg, consumerEvent: OmmConsumerEvent) {
        // Do nothing
    }
}
