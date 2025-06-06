/**
 * @file: PhoneTextField.kt
 * @description: Компонент для ввода номера телефона с форматированием и валидацией
 * @dependencies: Compose Material3
 * @created: 2024-12-20
 */
package com.pizzanat.app.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign

/**
 * Форматированное поле ввода номера телефона
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Номер телефона",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Next
) {
    // Создаем TextFieldValue для управления курсором
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }
    
    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValue ->
            // Извлекаем только введенный текст
            val inputText = newTextFieldValue.text
            
            // Форматируем номер
            val formattedText = formatPhoneInput(inputText)
            
            // Создаем новый TextFieldValue с курсором в конце
            val newFormattedValue = TextFieldValue(
                text = formattedText,
                selection = TextRange(formattedText.length)
            )
            
            // Обновляем локальное состояние
            textFieldValue = newFormattedValue
            
            // Передаем форматированный текст в callback
            onValueChange(formattedText)
        },
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Телефон"
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                val digitCount = extractDigits(value).length
                val color = when {
                    digitCount == 11 -> MaterialTheme.colorScheme.primary
                    digitCount < 11 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.error
                }
                Text(
                    text = "$digitCount/11",
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
        },
        placeholder = { 
            Text(
                text = "+7 (000) 000-00-00",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        },
        modifier = modifier,
        enabled = enabled,
        isError = isError || !isValidPhoneLength(value),
        supportingText = {
            val digitCount = extractDigits(value).length
            when {
                errorMessage != null -> {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                digitCount == 1 -> {
                    // Для "+7" не показываем подсказку
                }
                value.isNotEmpty() && digitCount > 1 && digitCount < 11 -> {
                    Text(
                        text = "Введите ${11 - digitCount} цифр",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                value.isNotEmpty() && digitCount > 11 -> {
                    Text(
                        text = "Слишком много цифр",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                value.isNotEmpty() && digitCount == 11 -> {
                    Text(
                        text = "✓ Номер корректный",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        singleLine = true
    )
}

/**
 * Форматирует ввод номера телефона в формат +7 (XXX) XXX-XX-XX
 */
private fun formatPhoneInput(input: String): String {
    // Извлекаем только цифры из введенного текста
    val digits = extractDigits(input)
    
    // Если пользователь полностью очистил поле, возвращаем "+7"
    if (digits.isEmpty()) {
        return "+7"
    }
    
    // Обрабатываем случай, когда пользователь начинает с 8
    val normalizedDigits = if (digits.startsWith("8") && digits.length > 1) {
        "7" + digits.substring(1)
    } else if (digits.startsWith("7")) {
        digits
    } else {
        // Если не начинается с 7 или 8, добавляем 7 в начало
        "7$digits"
    }
    
    // Ограничиваем до 11 цифр (7 + 10 цифр номера)
    val limitedDigits = normalizedDigits.take(11)
    
    // Форматируем в +7 (XXX) XXX-XX-XX
    return when (limitedDigits.length) {
        0, 1 -> "+7"
        2, 3, 4 -> "+7 (${limitedDigits.substring(1)}"
        5, 6, 7 -> "+7 (${limitedDigits.substring(1, 4)}) ${limitedDigits.substring(4)}"
        8, 9 -> "+7 (${limitedDigits.substring(1, 4)}) ${limitedDigits.substring(4, 7)}-${limitedDigits.substring(7)}"
        10 -> "+7 (${limitedDigits.substring(1, 4)}) ${limitedDigits.substring(4, 7)}-${limitedDigits.substring(7, 9)}-${limitedDigits.substring(9)}"
        11 -> "+7 (${limitedDigits.substring(1, 4)}) ${limitedDigits.substring(4, 7)}-${limitedDigits.substring(7, 9)}-${limitedDigits.substring(9, 11)}"
        else -> "+7 (${limitedDigits.substring(1, 4)}) ${limitedDigits.substring(4, 7)}-${limitedDigits.substring(7, 9)}-${limitedDigits.substring(9, 11)}"
    }
}

/**
 * Извлекает только цифры из строки
 */
private fun extractDigits(input: String): String {
    return input.filter { it.isDigit() }
}

/**
 * Проверяет корректность длины номера телефона
 */
private fun isValidPhoneLength(formattedPhone: String): Boolean {
    val digits = extractDigits(formattedPhone)
    // "+7" или пустое поле не должны подсвечиваться красным
    return digits.isEmpty() || digits.length == 1 || digits.length == 11
}

/**
 * Проверяет полную валидность номера телефона
 */
fun isValidPhoneNumber(formattedPhone: String): Boolean {
    val digits = extractDigits(formattedPhone)
    return digits.length == 11 && digits.startsWith("7")
}

/**
 * Нормализует номер телефона для отправки в API
 */
fun normalizePhoneForApi(formattedPhone: String): String {
    val digits = extractDigits(formattedPhone)
    return if (digits.length == 11 && digits.startsWith("7")) {
        "+$digits"
    } else {
        formattedPhone
    }
} 