package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.AvailableModels
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.GetCodeDialog
import com.example.ui.components.ParametersBottomSheet
import com.example.ui.viewmodel.PlaygroundViewModel

@Composable
fun PlaygroundScreen(
    viewModel: PlaygroundViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    var inputPrompt by remember { mutableStateOf("") }
    var showParametersSheet by remember { mutableStateOf(false) }
    var showGetCodeDialog by remember { mutableStateOf(false) }
    var showSavePromptDialog by remember { mutableStateOf(false) }
    var isSystemInstructionExpanded by remember { mutableStateOf(false) }

    // Save prompt dialog fields
    var saveTitle by remember { mutableStateOf("") }
    var saveCategory by remember { mutableStateOf("Development") }

    // Photo picker launcher (Zero-permission Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.attachImage(uri)
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size, uiState.isGenerating) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C101A))
    ) {
        // Top Model & Parameter Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141926))
                .border(1.dp, Color(0xFF20283C))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Model Selector Pill
            val currentModel = AvailableModels.getById(uiState.config.modelId)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E2840))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                    .clickable { showParametersSheet = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("active_model_pill"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF34D399))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentModel.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Change model",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Quick Actions: Get Code, Parameters, Save
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showGetCodeDialog = true },
                    modifier = Modifier.size(36.dp).testTag("get_code_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Get Code",
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { showSavePromptDialog = true },
                    modifier = Modifier.size(36.dp).testTag("save_prompt_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Save Prompt",
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { showParametersSheet = true },
                    modifier = Modifier.size(36.dp).testTag("tune_parameters_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Tune Parameters",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Optional System Instruction collapsible card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { isSystemInstructionExpanded = !isSystemInstructionExpanded },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131826)),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222B3E))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFA172F8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "System Instruction",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFCBD5E1),
                            fontWeight = FontWeight.Medium
                        )
                        if (uiState.config.systemInstruction.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF2E244A))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Active",
                                    color = Color(0xFFC084FC),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = if (isSystemInstructionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isSystemInstructionExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = uiState.config.systemInstruction,
                            onValueChange = {
                                viewModel.updateConfig(uiState.config.copy(systemInstruction = it))
                            },
                            placeholder = {
                                Text("Set role, tone, formatting rules, or boundaries...", color = Color(0xFF64748B), fontSize = 12.sp)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFA172F8),
                                unfocusedBorderColor = Color(0xFF2E3852),
                                focusedContainerColor = Color(0xFF0F1422),
                                unfocusedContainerColor = Color(0xFF0F1422),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color(0xFFE2E8F0)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Error Banner (e.g., API key missing)
        if (uiState.errorBanner != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3F161E)),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF882434))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.errorBanner ?: "",
                        color = Color(0xFFFECACA),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onNavigateToSettings) {
                        Text("Settings", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = { viewModel.dismissError() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Conversation / Playground Stream
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (uiState.messages.isEmpty()) {
                // Empty state with Google AI Studio hero card & prompt ideas
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
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
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Google AI Studio",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Experiment with Google's fastest, multimodal Gemini models. Tune parameters, test vision, and export production code.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Quick Starter Prompts",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFCBD5E1),
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val starterPrompts = listOf(
                            "Design a high-performance Kotlin cache with coroutines and Flow.",
                            "Explain the architectural trade-offs between GraphQL and REST.",
                            "Extract entities, invoice amount, and dates into structured JSON.",
                            "Write a creative product release announcement for a developer tool."
                        )

                        starterPrompts.forEach { prompt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        inputPrompt = prompt
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF141926)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232C42)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        ChatMessageItem(message = message)
                    }

                    if (uiState.isGenerating) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color(0xFF60A5FA),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Gemini is generating response...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Image Attachment Preview Pill (if user attached an image)
        if (uiState.attachedImageUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141926))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uiState.attachedImageUri),
                    contentDescription = "Attached thumbnail",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF384666), RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Image attached",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ready to send with prompt",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                IconButton(
                    onClick = { viewModel.clearAttachedImage() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove image",
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Bottom Input Field & Controls Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111624))
                .border(1.dp, Color(0xFF21293D))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Attach image button
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("attach_image_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach image",
                        tint = Color(0xFF60A5FA)
                    )
                }

                // If chat is active, allow clearing conversation
                if (uiState.messages.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearConversation() },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("clear_conversation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear chat",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                // Main Prompt Text Input
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = {
                        Text(
                            text = "Enter a prompt or question...",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .testTag("prompt_input_field"),
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4285F4),
                        unfocusedBorderColor = Color(0xFF28324A),
                        focusedContainerColor = Color(0xFF161C2C),
                        unfocusedContainerColor = Color(0xFF161C2C),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0)
                    )
                )

                // Send / Run Prompt Button
                IconButton(
                    onClick = {
                        val text = inputPrompt
                        inputPrompt = ""
                        viewModel.sendMessage(text)
                    },
                    enabled = !uiState.isGenerating && (inputPrompt.isNotBlank() || uiState.attachedImageBase64 != null),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (!uiState.isGenerating && (inputPrompt.isNotBlank() || uiState.attachedImageBase64 != null))
                                Color(0xFF2563EB)
                            else Color(0xFF1E2638)
                        )
                        .testTag("run_prompt_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Run Prompt",
                        tint = if (!uiState.isGenerating && (inputPrompt.isNotBlank() || uiState.attachedImageBase64 != null))
                            Color.White
                        else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Parameters Bottom Sheet
    if (showParametersSheet) {
        ParametersBottomSheet(
            config = uiState.config,
            onConfigChange = { viewModel.updateConfig(it) },
            onDismiss = { showParametersSheet = false }
        )
    }

    // Get Code Dialog
    if (showGetCodeDialog) {
        val promptForExport = inputPrompt.ifBlank {
            uiState.messages.lastOrNull { it.isUser }?.text ?: "Explain the Google Gemini architecture."
        }
        GetCodeDialog(
            prompt = promptForExport,
            config = uiState.config,
            onDismiss = { showGetCodeDialog = false }
        )
    }

    // Save Prompt Dialog
    if (showSavePromptDialog) {
        AlertDialog(
            onDismissRequest = { showSavePromptDialog = false },
            title = {
                Text(
                    text = "Save to Prompt Library",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Save your current prompt, system instructions, and tuned model parameters to your library.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    OutlinedTextField(
                        value = saveTitle,
                        onValueChange = { saveTitle = it },
                        label = { Text("Prompt Title") },
                        modifier = Modifier.fillMaxWidth().testTag("save_prompt_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4285F4),
                            unfocusedBorderColor = Color(0xFF2B364E),
                            focusedContainerColor = Color(0xFF161C2C),
                            unfocusedContainerColor = Color(0xFF161C2C),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        )
                    )
                    OutlinedTextField(
                        value = saveCategory,
                        onValueChange = { saveCategory = it },
                        label = { Text("Category (e.g. Development, Content, Vision)") },
                        modifier = Modifier.fillMaxWidth().testTag("save_prompt_category_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4285F4),
                            unfocusedBorderColor = Color(0xFF2B364E),
                            focusedContainerColor = Color(0xFF161C2C),
                            unfocusedContainerColor = Color(0xFF161C2C),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentAsPrompt(
                            title = saveTitle,
                            description = "Custom prompt using ${uiState.config.modelId}",
                            category = saveCategory
                        )
                        showSavePromptDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.testTag("confirm_save_prompt_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePromptDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF111624)
        )
    }
}
