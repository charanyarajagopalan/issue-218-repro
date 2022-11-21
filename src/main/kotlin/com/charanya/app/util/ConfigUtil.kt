package com.charanya.app.util

import com.refinitiv.ema.access.EmaFactory
import com.refinitiv.ema.access.Map
import com.refinitiv.ema.access.MapEntry

const val CHANNEL_NAME = "Main_Channel"
const val CONSUMER_PREFIX = "Consumer_"

fun createConsumerConfigMap(consumerName: String, endpointUrl: String, endpointPort: String): Map {
    val consumerGroupElements = EmaFactory.createElementList().apply {
        val map = EmaFactory.createMap().apply {
            val list = EmaFactory.createElementList().apply {
                add(EmaFactory.createElementEntry().ascii("Channel", CHANNEL_NAME))
                add(EmaFactory.createElementEntry().intValue("RequestTimeout", 45000))
            }
            add(EmaFactory.createMapEntry().keyAscii(consumerName, MapEntry.MapAction.ADD, list))
        }

        add(EmaFactory.createElementEntry().map("ConsumerList", map))
    }

    val channelGroupElements = EmaFactory.createElementList().apply {
        val map = EmaFactory.createMap().apply {
            val list = EmaFactory.createElementList().apply {
                add(EmaFactory.createElementEntry().ascii("ChannelType", "ChannelType::RSSL_ENCRYPTED"))
                add(EmaFactory.createElementEntry().ascii("Host", endpointUrl))
                add(EmaFactory.createElementEntry().ascii("Port", endpointPort))
                add(EmaFactory.createElementEntry().intValue("EnableSessionManagement", 1))
            }
            add(EmaFactory.createMapEntry().keyAscii(CHANNEL_NAME, MapEntry.MapAction.ADD, list))
        }

        add(EmaFactory.createElementEntry().map("ChannelList", map))
    }

    return EmaFactory.createMap().apply {
        add(EmaFactory.createMapEntry().keyAscii("ConsumerGroup", MapEntry.MapAction.ADD, consumerGroupElements))
        add(EmaFactory.createMapEntry().keyAscii("ChannelGroup", MapEntry.MapAction.ADD, channelGroupElements))
    }
}
