package litun.uxinnovator.ui.feed

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import litun.uxinnovator.components.AddUserComponent
import litun.uxinnovator.components.AddUserState
import litun.uxinnovator.domain.model.Gender
import litun.uxinnovator.domain.model.UserStatus
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.ic_check
import org.jetbrains.compose.resources.painterResource

// Computed once at class load — enum entries never change at runtime
private val GENDER_OPTIONS = Gender.entries.map {
    it.name.lowercase().replaceFirstChar(Char::uppercaseChar) to it
}
private val STATUS_OPTIONS = UserStatus.entries.map {
    it.name.lowercase().replaceFirstChar(Char::uppercaseChar) to it
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserSheet(component: AddUserComponent) {
    val state by component.state.subscribeAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = component.onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null,
    ) {
        SheetContent(
            state = state,
            onNameChanged = component::onNameChanged,
            onEmailChanged = component::onEmailChanged,
            onGenderChanged = component::onGenderChanged,
            onStatusChanged = component::onStatusChanged,
            onSubmit = component::onSubmit,
            onDismiss = component.onDismiss,
        )
    }
}

@Composable
private fun SheetContent(
    state: AddUserState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onGenderChanged: (Gender) -> Unit,
    onStatusChanged: (UserStatus) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Drag handle
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Text(
            text = "Create New User",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.5).sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Complete the profile to add a new member.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(28.dp))

        // Full Name
        FormField(
            label = "FULL NAME",
            value = state.name,
            onValueChange = onNameChanged,
            placeholder = "e.g. Julian Casablancas",
            error = state.nameError,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Email
        FormField(
            label = "EMAIL ADDRESS",
            value = state.email,
            onValueChange = onEmailChanged,
            placeholder = "name@domain.com",
            error = state.emailError,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Gender + Status row — DropdownField fills its weighted slot directly
        Row(modifier = Modifier.fillMaxWidth()) {
            DropdownField(
                modifier = Modifier.weight(1f),
                label = "GENDER",
                selectedLabel = state.gender.name.lowercase().replaceFirstChar(Char::uppercaseChar),
                options = GENDER_OPTIONS,
                onSelect = onGenderChanged,
            )
            Spacer(modifier = Modifier.width(12.dp))
            DropdownField(
                modifier = Modifier.weight(1f),
                label = "STATUS",
                selectedLabel = state.status.name.lowercase().replaceFirstChar(Char::uppercaseChar),
                options = STATUS_OPTIONS,
                onSelect = onStatusChanged,
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Save button
        SaveButton(
            isSubmitting = state.isSubmitting,
            enabled = !state.isSubmitting && state.name.isNotBlank() && state.email.isNotBlank(),
            onClick = onSubmit,
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Server error
        if (state.submitError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.submitError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
            )
        }

        // Cancel
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Cancel",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
) {
    val hasError = error != null
    val fieldBg = if (hasError) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.surfaceContainerHighest

    Column(modifier = Modifier.fillMaxWidth()) {
        FieldLabel(label)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(fieldBg)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                singleLine = true,
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = 16.sp,
                        )
                    }
                    inner()
                },
            )
        }
        if (hasError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownField(
    modifier: Modifier = Modifier,
    label: String,
    selectedLabel: String,
    options: List<Pair<String, T>>,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(label)
        Spacer(modifier = Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .menuAnchor(type = androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selectedLabel,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                options.forEach { (optLabel, value) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = optLabel,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        onClick = {
                            onSelect(value)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveButton(isSubmitting: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    // Memoized per color values — recomputes only when theme colors change
    val gradient = remember(primary, primaryContainer) {
        Brush.linearGradient(colors = listOf(primary, primaryContainer))
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (enabled) Modifier.background(gradient) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Save User",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check),
                        contentDescription = null,
                        tint = if (enabled) onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
