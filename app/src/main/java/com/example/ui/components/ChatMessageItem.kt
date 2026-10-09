package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (message.isUser) {
        // User Message (Right aligned)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                modifier = Modifier.widthIn(max = 320.dp),
                horizontalAlignment = Alignment.End
            ) {
                // If has image attachment
                if (!message.imageBase64.isNullOrBlank()) {
                    val bitmap = remember(message.imageBase64) {
                        try {
                            val decoded = Base64.decode(message.imageBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Attached Image",
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF3B4868), RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E3A8A)
                    ),
                    modifier = Modifier.testTag("user_message_bubble")
                ) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // User Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    } else {
        // Assistant Message (Left aligned)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            // Gemini Avatar with gradient
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF4285F4), Color(0xFF9C6FFF), Color(0xFF00E5FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Gemini",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Metadata strip (Model name, latency, token count)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = message.modelUsed ?: "Gemini",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA172F8),
                        fontWeight = FontWeight.Bold
                    )

                    if (message.latencyMs != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${message.latencyMs}ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (message.tokenCount != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Token,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${message.tokenCount} tok",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (message.isError) {
                    Card(
                        shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF3B151E)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF7F1D1D), RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                            .testTag("assistant_error_bubble")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.text,
                                color = Color(0xFFFECACA),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF141926)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF263048), RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                            .testTag("assistant_message_bubble")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Render message text with embedded code blocks parsing
                            RenderFormattedText(text = message.text)

                            // Action footer (Copy message)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Gemini Response", message.text))
                                        Toast.makeText(context, "Response copied", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("copy_message_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy whole message",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Splits text into markdown code fences and regular text paragraphs
 */
@Composable
private fun RenderFormattedText(text: String) {
    val parts = remember(text) { parseCodeBlocks(text) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (part in parts) {
            when (part) {
                is TextSegment.Normal -> {
                    if (part.content.isNotBlank()) {
                        Text(
                            text = part.content,
                            color = Color(0xFFF1F5F9),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
                is TextSegment.Code -> {
                    CodeBlockView(code = part.code, language = part.language)
                }
            }
        }
    }
}

private sealed class TextSegment {
    data class Normal(val content: String) : TextSegment()
    data class Code(val code: String, val language: String) : TextSegment()
}

private fun parseCodeBlocks(raw: String): List<TextSegment> {
    val result = mutableListOf<TextSegment>()
    val codeFenceRegex = Regex("```([a-zA-Z0-9_-]*)\n?([\\s\\S]*?)```")

    var lastIndex = 0
    val matches = codeFenceRegex.findAll(raw)

    for (match in matches) {
        val start = match.range.first
        val end = match.range.last + 1

        if (start > lastIndex) {
            val normalText = raw.substring(lastIndex, start).trim()
            if (normalText.isNotEmpty()) {
                result.add(TextSegment.Normal(normalText))
            }
        }

        val lang = match.groupValues[1].ifBlank { "code" }
        val code = match.groupValues[2].trimEnd()
        result.add(TextSegment.Code(code = code, language = lang))

        lastIndex = end
    }

    if (lastIndex < raw.length) {
        val remaining = raw.substring(lastIndex).trim()
        if (remaining.isNotEmpty()) {
            result.add(TextSegment.Normal(remaining))
        }
    }

    if (result.isEmpty()) {
        result.add(TextSegment.Normal(raw))
    }

    return result
}
