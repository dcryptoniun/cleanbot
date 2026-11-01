package com.gerstudio.cleanbot.kadb.core

import com.gerstudio.cleanbot.kadb.DelayedAckMode

internal fun shouldAdvertiseDelayedAck(mode: DelayedAckMode): Boolean = when (mode) {
    DelayedAckMode.AOSP_DEFAULT -> aospDefaultDelayedAckEnabled()
    DelayedAckMode.ENABLED -> true
    DelayedAckMode.DISABLED -> false
}

internal fun aospDefaultDelayedAckEnabled(): Boolean = false
