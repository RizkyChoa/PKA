package com.example.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.data.model.formatInputNumber

@Composable
fun CurrencyTextField(
    value: String,
    onValueChange: (rawDigits: String, formatted: String) -> Unit,
    label: String,
    placeholder: String = "0",
    modifier: Modifier = Modifier,
    prefix: @Composable (() -> Unit)? = { Text("Rp ") },
    isError: Boolean = false,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = formatInputNumber(value),
        onValueChange = { input ->
            val cleanDigits = input.filter { it.isDigit() }
            val formatted = formatInputNumber(cleanDigits)
            onValueChange(cleanDigits, formatted)
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        prefix = prefix,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        isError = isError,
        singleLine = singleLine,
        enabled = enabled
    )
}
