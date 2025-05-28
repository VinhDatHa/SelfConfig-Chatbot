package io.curri.dictionary.chatbot.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import coil3.Uri
import coil3.compose.AsyncImage
import coil3.toUri
import com.composables.icons.lucide.ArrowUp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import dictionarychatbot.composeapp.generated.resources.Res
import dictionarychatbot.composeapp.generated.resources.cancel_edit
import dictionarychatbot.composeapp.generated.resources.chat_input_placeholder
import dictionarychatbot.composeapp.generated.resources.editing
import dictionarychatbot.composeapp.generated.resources.more_options
import dictionarychatbot.composeapp.generated.resources.send
import dictionarychatbot.composeapp.generated.resources.stop
import io.curri.dictionary.chatbot.data.models.UIMessagePart
import io.curri.dictionary.chatbot.data.models.isEmptyMessage
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class ChatInputState {
	var messageContent by mutableStateOf(listOf<UIMessagePart>())
	var editingMessage by mutableStateOf<String?>(null)
	var loading by mutableStateOf(false)

	fun clearInput() {
		messageContent = emptyList()
		editingMessage = null
	}

	fun isEditing() = editingMessage != null

	fun setMessageText(text: String) {
		val newMessage = messageContent.toMutableList()
		if (newMessage.isEmpty()) {
			newMessage.add(UIMessagePart.Text(text))
			messageContent = newMessage
		} else {
			if (messageContent.filterIsInstance<UIMessagePart.Text>().isEmpty()) {
				newMessage.add(UIMessagePart.Text(text))
			}
			messageContent = newMessage.map {
				if (it is UIMessagePart.Text) {
					it.copy(text)
				} else {
					it
				}
			}
		}
	}

	fun addImages(urisInString: List<String>) {
		val newMessage = messageContent.toMutableList()
		urisInString.forEach { uri ->
			newMessage.add(UIMessagePart.Image(uri))
		}
		messageContent = newMessage
	}
}

@Composable
fun rememberChatInputState(
	message: List<UIMessagePart> = emptyList(),
	loading: Boolean = false,
): ChatInputState {
	return remember(message, loading) {
		ChatInputState().apply {
			this.messageContent = message
			this.loading = loading
		}
	}
}

@Composable
fun ChatInput(
	state: ChatInputState,
	enableSearch: Boolean,
	onToggleSearch: (Boolean) -> Unit,
	modifier: Modifier = Modifier,
	onCancelClick: () -> Unit,
	onSendClick: () -> Unit,
	onImageDelete: (List<Uri>) -> Unit = {},
	actions: @Composable RowScope.() -> Unit = {},
) {
	val text =
		state.messageContent.filterIsInstance<UIMessagePart.Text>().firstOrNull()
			?: UIMessagePart.Text("")

	var expand by remember { mutableStateOf(false) }
	val keyboardController = LocalSoftwareKeyboardController.current

	fun sendMessage() {
		keyboardController?.hide()
		if (state.loading) onCancelClick() else onSendClick()
	}
	Surface {
		Column(
			modifier = modifier
				.imePadding()
				.navigationBarsPadding(),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {
			// Medias
			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 8.dp)
			) {
				state.messageContent.filterIsInstance<UIMessagePart.Image>().forEach { image ->
					Box {
						Surface(
							modifier = Modifier.size(48.dp),
							shape = RoundedCornerShape(8.dp),
							tonalElevation = 4.dp
						) {
							AsyncImage(
								model = image.url,
								contentDescription = null,
								modifier = Modifier.fillMaxSize(),
							)
						}
						Icon(
							imageVector = Lucide.X,
							contentDescription = null,
							modifier = Modifier
								.clip(CircleShape)
								.size(20.dp)
								.clickable {
									// Remove image
									state.messageContent =
										state.messageContent.filterNot { it == image }
									// Delete image
									onImageDelete(listOf(image.url.toUri()))
								}
								.align(Alignment.TopEnd)
								.background(MaterialTheme.colorScheme.secondary),
							tint = MaterialTheme.colorScheme.onSecondary
						)
					}
				}
			}

			// TextField
			Surface(
				shape = RoundedCornerShape(32.dp),
				tonalElevation = 4.dp,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 8.dp)
			) {
				Column {
					if (state.isEditing()) {
						Surface(
							tonalElevation = 8.dp
						) {
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.padding(horizontal = 16.dp, vertical = 4.dp),
								verticalAlignment = Alignment.CenterVertically
							) {
								Text(
									text = stringResource(Res.string.editing),
								)
								Spacer(Modifier.weight(1f))
								Icon(
									Lucide.X, stringResource(Res.string.cancel_edit),
									modifier = Modifier
										.clickable {
											state.clearInput()
										}
								)
							}
						}
					}
					TextField(
						value = text.text,
						onValueChange = { state.setMessageText(it) },
						modifier = Modifier.fillMaxWidth(),
						shape = RoundedCornerShape(32.dp),
						placeholder = {
							Text(stringResource(Res.string.chat_input_placeholder))
						},
						maxLines = 5,
						colors = TextFieldDefaults.colors().copy(
							unfocusedIndicatorColor = Color.Transparent,
							focusedIndicatorColor = Color.Transparent,
							focusedContainerColor = Color.Transparent,
							unfocusedContainerColor = Color.Transparent,
						)
					)
				}
			}

			// Actions Row
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 8.dp)
			) {
				actions()

				IconButton(
					onClick = {
						// ToDo expland later
//						expand = !expand
					}
				) {
					Icon(
						if (expand) Lucide.X else Lucide.Plus,
						stringResource(Res.string.more_options)
					)
				}

				Spacer(Modifier.width(4.dp))

				IconButton(
					onClick = {
						sendMessage()
					},
					colors = IconButtonDefaults.filledIconButtonColors(
						containerColor = if (state.loading) MaterialTheme.colorScheme.errorContainer else Color.Unspecified,
						contentColor = if (state.loading) MaterialTheme.colorScheme.onErrorContainer else Color.Unspecified,
					),
					enabled = state.loading || !state.messageContent.isEmptyMessage()
				) {
					if (state.loading) {
						Icon(Lucide.X, stringResource(Res.string.stop))
					} else {
						Icon(Lucide.ArrowUp, stringResource(Res.string.send))
					}
				}
			}

			// Files
			AnimatedVisibility(expand) {
				Surface(
					tonalElevation = 4.dp
				) {
					FlowRow(
						modifier = Modifier
							.fillMaxWidth()
							.padding(16.dp),
						horizontalArrangement = Arrangement.spacedBy(4.dp),
					) {
						// ToDo pick media later
//						TakePicButton {
//							state.addImages(it)
//							expand = false
//						}
//
//						ImagePickButton {
//							state.addImages(it)
//							expand = false
//						}
//					}
					}
				}
			}
		}
	}
}