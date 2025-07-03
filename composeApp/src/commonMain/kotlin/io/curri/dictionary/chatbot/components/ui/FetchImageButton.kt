package io.curri.dictionary.chatbot.components.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Delete
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Lucide
import io.ktor.http.parseUrl

@Composable
internal fun FetchImageButton(onLoadImage: (String) -> Unit) {
	var isShowDialog by remember { mutableStateOf(false) }
	var url by remember { mutableStateOf("") }
	val focusRequester = remember { FocusRequester() }
	val keyboardController = LocalSoftwareKeyboardController.current

	var isError by remember { mutableStateOf(false) }

	IconTextButton(icon = {
		Icon(Lucide.Globe, null)
	}, text = {
		Text("Take picture")
	}, onClick = {
		isShowDialog = true
	})

	if (isShowDialog) {
		Dialog(
			onDismissRequest = {
				isShowDialog = false
			},
			properties = DialogProperties(
				dismissOnBackPress = true,
				dismissOnClickOutside = true
			)
		) {
			Card(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp),
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.surface
				),
				elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(24.dp),
					verticalArrangement = Arrangement.spacedBy(16.dp)
				) {
					// Title
					Text(
						text = "Fetch Image",
						style = MaterialTheme.typography.headlineSmall,
						color = MaterialTheme.colorScheme.onSurface
					)

					// URL Input Field
					OutlinedTextField(
						value = url,
						onValueChange = {
							url = it
						},
						label = { Text("URL") },
						placeholder = { Text("https://example.com") },
						isError = isError,
						supportingText = if (isError) {
							{ Text("Please enter valid URL") }
						} else null,
						keyboardOptions = KeyboardOptions(
							keyboardType = KeyboardType.Uri,
							imeAction = ImeAction.Done
						),
						keyboardActions = KeyboardActions(
							onDone = { keyboardController?.hide() },
						),
						modifier = Modifier
							.fillMaxWidth()
							.focusRequester(focusRequester),
						trailingIcon = {
							if (url.isBlank()) return@OutlinedTextField
							IconButton(
								onClick = {
									url = ""
								}
							) {
								Icon(
									imageVector = Lucide.Delete,
									contentDescription = "Clear"
								)
							}
						}
					)

					// Buttons Row
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.End,
						verticalAlignment = Alignment.CenterVertically
					) {
						TextButton(
							onClick = { isShowDialog = false },
							colors = ButtonDefaults.textButtonColors(
								contentColor = MaterialTheme.colorScheme.primary
							)
						) {
							Text("Cancel")
						}

						Spacer(modifier = Modifier.width(8.dp))

						TextButton(
							onClick = {
								isError = !isUrlValid(url)
								if (!isError) {
									onLoadImage(url)
//									url = ""
								}
							},
							colors = ButtonDefaults.buttonColors(
								containerColor = MaterialTheme.colorScheme.primary
							)
						) {
							Text("Confirm")
						}
					}
				}
			}
		}
	}
}

private fun isUrlValid(input: String): Boolean {
	return parseUrl(input) != null
}