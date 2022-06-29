package com.example.autofilltest

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalArrangement = remember { Arrangement.spacedBy(20.dp) }
            ) {
                var emailFirstAutofillValue by remember { mutableStateOf("") }
                val emailFirstInteractionSource = remember { MutableInteractionSource() }
                val emailFirstAutofillTypes = remember {
                    listOf(AutofillType.EmailAddress, AutofillType.PersonFirstName)
                }
                OutlinedTextField(
                    value = emailFirstAutofillValue,
                    onValueChange = { emailFirstAutofillValue = it},
                    interactionSource = emailFirstInteractionSource,
                    placeholder = { Text(text = "Email or person first name") },
                    maxLines = 1,
                    singleLine = true,
                    modifier = Modifier
                        .autofill(
                            value = emailFirstAutofillValue,
                            autofillTypes = emailFirstAutofillTypes,
                            interactionSource = emailFirstInteractionSource
                        ) { emailFirstAutofillValue = it }
                )
                var emailSecondAutofillValue by remember { mutableStateOf("") }
                val emailSecondInteractionSource = remember { MutableInteractionSource() }
                val emailSecondAutofillTypes = remember {
                    listOf(AutofillType.PersonFirstName, AutofillType.EmailAddress)
                }
                OutlinedTextField(
                    value = emailSecondAutofillValue,
                    onValueChange = { emailSecondAutofillValue = it},
                    interactionSource = emailSecondInteractionSource,
                    placeholder = { Text(text = "Person first name or email") },
                    maxLines = 1,
                    singleLine = true,
                    modifier = Modifier
                        .autofill(
                            value = emailSecondAutofillValue,
                            autofillTypes = emailSecondAutofillTypes,
                            interactionSource = emailSecondInteractionSource
                        ) { emailSecondAutofillValue = it }
                )

                var phoneFirstAutofillValue by remember { mutableStateOf("") }
                val multiInteractionSource = remember { MutableInteractionSource() }
                val phoneFirstAutofillTypes = remember {
                    listOf(AutofillType.PhoneNumber, AutofillType.EmailAddress)
                }
                OutlinedTextField(
                    value = phoneFirstAutofillValue,
                    onValueChange = { phoneFirstAutofillValue = it},
                    interactionSource = multiInteractionSource,
                    placeholder = { Text(text = "Phone or email") },
                    maxLines = 1,
                    singleLine = true,
                    modifier = Modifier
                        .autofill(
                            value = phoneFirstAutofillValue,
                            autofillTypes = phoneFirstAutofillTypes,
                            interactionSource = multiInteractionSource
                        ) { phoneFirstAutofillValue = it }
                )

                var phoneAutoFillValue by remember { mutableStateOf("") }
                val interactionSource = remember { MutableInteractionSource() }
                val phoneAutoFillTypes = remember {
                    listOf(AutofillType.PhoneNumber)
                }
                OutlinedTextField(
                    value = phoneAutoFillValue,
                    onValueChange = { phoneAutoFillValue = it},
                    interactionSource = interactionSource,
                    placeholder = { Text(text = "Phone") },
                    maxLines = 1,
                    singleLine = true,
                    modifier = Modifier
                        .autofill(
                            value = phoneAutoFillValue,
                            autofillTypes = phoneAutoFillTypes,
                            interactionSource = interactionSource
                        ) { phoneAutoFillValue = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.autofill(
    value: String,
    autofillTypes: List<AutofillType>,
    interactionSource: MutableInteractionSource,
    onFill: (String) -> Unit
) = composed {
    val autofill = LocalAutofill.current
    val localAutofillTree = LocalAutofillTree.current
    var viewBounds by remember { mutableStateOf(Rect.Zero) }
    var hasFocus by remember { mutableStateOf(false) }

    val autofillNode by remember(viewBounds, autofillTypes) {
        mutableStateOf(
            AutofillNode(
                onFill = {
                    onFill.invoke(it)
                },
                autofillTypes = autofillTypes
            ).apply {
                boundingBox = viewBounds
                Log.e("Oops", this.autofillTypes.size.toString())
                localAutofillTree.plusAssign(this)
            }
        )
    }

    LaunchedEffect(hasFocus, value, autofillNode) {
        when {
            hasFocus && value.isEmpty() -> autofill?.requestAutofillForNode(autofillNode)
            else -> {
                Log.e("Oops", "cancel from has focus: ${System.currentTimeMillis()}, ${autofillNode.hashCode()}")
                autofill?.cancelAutofillForNode(autofillNode)
            }
        }
    }

    LaunchedEffect(interactionSource, autofillNode) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Release) {
                Log.e("Oops", "request from interaction source: ${System.currentTimeMillis()}, ${autofillNode.hashCode()}")
                autofill?.requestAutofillForNode(autofillNode)
            }
        }
    }
    onGloballyPositioned {
        viewBounds = it.boundsInRoot()
        autofillNode.boundingBox = viewBounds
        Log.e("Oops", autofillNode.autofillTypes.size.toString())
        localAutofillTree.plusAssign(autofillNode)
    }.onFocusChanged { focusState ->
        hasFocus = focusState.isFocused
    }
}