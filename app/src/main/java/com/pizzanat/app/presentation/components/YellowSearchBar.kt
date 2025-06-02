/**
 * @file: FoxSearchBar.kt
 * @description: Строка поиска в стиле Fox Whiskers (серая)
 * @dependencies: Compose, Material3
 * @created: 2024-12-19
 * @updated: 2024-12-20 - Изменен на серый стиль Fox Whiskers
 */
package com.pizzanat.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pizzanat.app.presentation.theme.SearchBarGray
import com.pizzanat.app.presentation.theme.Gray600

/**
 * Строка поиска в стиле Fox Whiskers с серым фоном
 */
@Composable
fun FoxSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Искать",
    enabled: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = SearchBarGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Поиск",
                tint = Gray600,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { 
                        onSearch(query)
                        keyboardController?.hide()
                    }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Gray600
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

/**
 * Упрощенная строка поиска для совместимости (deprecated)
 */
@Deprecated("Use FoxSearchBar instead", ReplaceWith("FoxSearchBar"))
@Composable
fun YellowSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Искать",
    enabled: Boolean = true
) {
    FoxSearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        modifier = modifier,
        placeholder = placeholder,
        enabled = enabled
    )
} 