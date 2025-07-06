/**
 * @file: AddressTextField.kt
 * @description: Компонент текстового поля с автодополнением адресов через Яндекс.Карты
 * @dependencies: Compose, Address domain entities
 * @created: 2025-01-23
 */
package com.pizzanat.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.pizzanat.app.domain.entities.AddressSuggestion
import kotlinx.coroutines.delay

/**
 * Текстовое поле с автодополнением адресов
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Адрес доставки",
    placeholder: String = "Введите адрес...",
    isError: Boolean = false,
    errorMessage: String? = null,
    suggestions: List<AddressSuggestion> = emptyList(),
    isLoading: Boolean = false,
    onSuggestionSelected: (AddressSuggestion) -> Unit = {},
    onQueryChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var isFocused by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Обновляем запрос с задержкой для оптимизации API вызовов
    LaunchedEffect(value) {
        if (value.length >= 3 && isFocused) {
            delay(300) // Debounce 300ms
            onQueryChanged(value)
        }
    }

    // Показываем подсказки только при фокусе и наличии текста
    LaunchedEffect(isFocused, suggestions, value) {
        showSuggestions = isFocused && value.isNotEmpty() && suggestions.isNotEmpty()
    }

    Column(modifier = modifier) {
        // Основное поле ввода
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                // Сбрасываем подсказки при очистке поля
                if (newValue.isEmpty()) {
                    showSuggestions = false
                }
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error 
                          else MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Индикатор загрузки
                    if (isLoading && value.isNotEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    // Кнопка очистки
                    if (value.isNotEmpty()) {
                        IconButton(
                            onClick = { 
                                onValueChange("")
                                showSuggestions = false
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Очистить",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (!focusState.isFocused) {
                        showSuggestions = false
                    }
                },
            isError = isError,
            supportingText = {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Список подсказок
        AnimatedVisibility(
            visible = showSuggestions,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .shadow(8.dp, RoundedCornerShape(8.dp))
                    .zIndex(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp) // Максимальная высота списка
                        .clip(RoundedCornerShape(8.dp)),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(
                        items = suggestions,
                        key = { it.value }
                    ) { suggestion ->
                        AddressSuggestionItem(
                            suggestion = suggestion,
                            onSelected = {
                                onSuggestionSelected(suggestion)
                                onValueChange(suggestion.value)
                                showSuggestions = false
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Элемент списка подсказок адреса
 */
@Composable
private fun AddressSuggestionItem(
    suggestion: AddressSuggestion,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Основной адрес
        Text(
            text = suggestion.value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        // Дополнительная информация (регион, почтовый индекс)
        if (suggestion.data.region != null || suggestion.data.postalCode != null) {
            Spacer(modifier = Modifier.height(2.dp))
            val additionalInfo = buildString {
                suggestion.data.region?.let { append(it) }
                suggestion.data.postalCode?.let { postalCode ->
                    if (isNotEmpty()) append(" • ")
                    append(postalCode)
                }
            }
            
            if (additionalInfo.isNotEmpty()) {
                Text(
                    text = additionalInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
} 