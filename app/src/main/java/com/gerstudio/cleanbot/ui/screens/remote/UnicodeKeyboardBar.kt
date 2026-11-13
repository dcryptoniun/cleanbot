// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.screens.remote

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.ui.theme.SignalButton
import com.gerstudio.cleanbot.ui.theme.rememberCleanBotHaptics

@Composable
fun UnicodeKeyboardBar(
    onPasteToTv: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberCleanBotHaptics()
    var text by rememberSaveable { mutableStateOf("") }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Type password, Wi-Fi key or Unicode / Emoji", fontSize = 12.sp) },
            leadingIcon = {
                Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (text.isNotEmpty()) {
                    IconButton(onClick = { text = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))

        SignalButton(
            onClick = {
                if (text.isNotEmpty()) {
                    haptics.confirm()
                    onPasteToTv(text)
                    text = ""
                }
            },
            enabled = text.isNotEmpty(),
            modifier = Modifier.height(54.dp),
        ) {
            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Send")
        }
    }
}
