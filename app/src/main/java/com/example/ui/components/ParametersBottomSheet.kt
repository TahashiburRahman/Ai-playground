package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvailableModels
import com.example.data.model.PromptConfig
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametersBottomSheet(
    config: PromptConfig,
    onConfigChange: (PromptConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF111624),
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(
                    modifier = Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .background(Color(0xFF333E56), RoundedCornerShape(2.dp))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color(0xFF4285F4),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Run Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        onConfigChange(
                            PromptConfig(
                                modelId = config.modelId,
                                systemInstruction = "",
                                temperature = 0.7f,
                                topP = 0.95f,
                                topK = 40,
                                maxOutputTokens = 2048,
                                isJsonOutput = false,
                                thinkingLevel = "none"
                            )
                        )
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("reset_parameters_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Model Selector chips
            Text(
                text = "Model",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AvailableModels.ALL.forEach { model ->
                    val isSelected = config.modelId == model.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange(config.copy(modelId = model.id)) },
                        label = {
                            Text(
                                text = model.badge,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E3A8A),
                            selectedLabelColor = Color(0xFF93C5FD),
                            containerColor = Color(0xFF1A2132),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("model_chip_${model.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // System Instruction
            Text(
                text = "System Instructions",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Optional steering instructions given to the model before any input.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = config.systemInstruction,
                onValueChange = { onConfigChange(config.copy(systemInstruction = it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("system_instruction_input"),
                placeholder = {
                    Text(
                        "e.g. You are an expert code tutor. Always explain reasoning concisely.",
                        color = Color(0xFF64748B)
                    )
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4285F4),
                    unfocusedBorderColor = Color(0xFF2B364E),
                    focusedContainerColor = Color(0xFF161C2C),
                    unfocusedContainerColor = Color(0xFF161C2C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFE2E8F0)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Temperature Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Temperature",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = String.format("%.2f", config.temperature),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF60A5FA),
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = config.temperature,
                onValueChange = { onConfigChange(config.copy(temperature = it)) },
                valueRange = 0.0f..2.0f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF4285F4),
                    activeTrackColor = Color(0xFF4285F4),
                    inactiveTrackColor = Color(0xFF252D42)
                ),
                modifier = Modifier.testTag("temperature_slider")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Top-P Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top-P (Nucleus Sampling)",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = String.format("%.2f", config.topP),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF60A5FA),
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = config.topP,
                onValueChange = { onConfigChange(config.copy(topP = it)) },
                valueRange = 0.0f..1.0f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF4285F4),
                    activeTrackColor = Color(0xFF4285F4),
                    inactiveTrackColor = Color(0xFF252D42)
                ),
                modifier = Modifier.testTag("top_p_slider")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Top-K Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top-K",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${config.topK}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF60A5FA),
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = config.topK.toFloat(),
                onValueChange = { onConfigChange(config.copy(topK = it.roundToInt())) },
                valueRange = 1f..100f,
                steps = 98,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF4285F4),
                    activeTrackColor = Color(0xFF4285F4),
                    inactiveTrackColor = Color(0xFF252D42)
                ),
                modifier = Modifier.testTag("top_k_slider")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Max Output Tokens Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Max Output Tokens",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${config.maxOutputTokens}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF60A5FA),
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = config.maxOutputTokens.toFloat(),
                onValueChange = { onConfigChange(config.copy(maxOutputTokens = it.roundToInt())) },
                valueRange = 256f..8192f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF4285F4),
                    activeTrackColor = Color(0xFF4285F4),
                    inactiveTrackColor = Color(0xFF252D42)
                ),
                modifier = Modifier.testTag("max_tokens_slider")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Structured Output / JSON Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "JSON Output Mode",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Constrains response to valid application/json format",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
                Switch(
                    checked = config.isJsonOutput,
                    onCheckedChange = { onConfigChange(config.copy(isJsonOutput = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4285F4),
                        uncheckedTrackColor = Color(0xFF252D42)
                    ),
                    modifier = Modifier.testTag("json_mode_switch")
                )
            }

            // Thinking Level (For Gemini 3.1 Pro)
            if (config.modelId.contains("pro")) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Reasoning & Thinking Level",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("none", "low", "high").forEach { level ->
                        val isSelected = config.thinkingLevel == level
                        FilterChip(
                            selected = isSelected,
                            onClick = { onConfigChange(config.copy(thinkingLevel = level)) },
                            label = { Text(level.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4C1D95),
                                selectedLabelColor = Color(0xFFE9D5FF),
                                containerColor = Color(0xFF1A2132),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }
        }
    }
}
