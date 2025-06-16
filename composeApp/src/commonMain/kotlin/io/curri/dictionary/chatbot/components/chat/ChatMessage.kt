package io.curri.dictionary.chatbot.components.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import coil3.compose.LocalPlatformContext
import com.composables.icons.lucide.BookDashed
import com.composables.icons.lucide.BookHeart
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Volume2
import com.composables.icons.lucide.Wrench
import io.curri.dictionary.chatbot.components.ui.FormItem
import io.curri.dictionary.chatbot.data.models.MessageRole
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.data.models.UIMessagePart

@Composable
fun ChatMessage(
	modifier: Modifier = Modifier,
	message: UIMessage,
	onRegenerate: () -> Unit,
	onEdit: () -> Unit,
) {
	Column(
		modifier = modifier.fillMaxWidth(),
		horizontalAlignment = if (message.role == MessageRole.USER) Alignment.End else Alignment.Start,
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		MessagePartsBlock(
			role = message.role,
			parts = message.parts
		)
		if (message.isValidToShowActions()) {
			Actions(
				message,
				onRegenerate = onRegenerate,
				onEdit = onEdit
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessagePartsBlock(
	role: MessageRole,
	parts: List<UIMessagePart>,
//	annotations: List<UIMessageAnnotation>,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
	val context = LocalPlatformContext.current

	// Raw Text
	parts.filterIsInstance<UIMessagePart.Text>().fastForEach { part ->
		SelectionContainer {
			if (role == MessageRole.USER) {
				Card(
					modifier = Modifier.animateContentSize(),
					shape = RoundedCornerShape(8.dp)
				) {
					Column(modifier = Modifier.padding(8.dp)) {
						// ToDo display markdown here
						Text(part.text)
					}
				}
			} else {
				// ToDo Markdown Block define here
				Text(part.text)
			}
		}
	}

	// Tool calls
	parts.filterIsInstance<UIMessagePart.ToolResult>().fastForEachIndexed { index, tool ->
		key(index) {
			var showResult by remember { mutableStateOf(false) }

			Surface(
				shape = RoundedCornerShape(25),
				tonalElevation = 4.dp,
				onClick = {
					showResult = true
				}
			) {
				Row(
					modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
						.height(IntrinsicSize.Min),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Icon(
						imageVector = when (tool.toolCallId) {
							"create_memory", "edit_memory" -> Lucide.BookHeart
							"delete_memory" -> Lucide.BookDashed
							else -> Lucide.Wrench
						},
						contentDescription = null,
						modifier = Modifier.fillMaxHeight()
					)
					Column {
						Text(
							text = when (tool.toolName) {
								"create_memory" -> "Created memory"
								"edit_memory" -> "Edited memory"
								"delete_memory" -> "Deleted memory"
								else -> "Tool: ${tool.toolName}"
							},
							style = MaterialTheme.typography.labelMedium
						)
					}
				}
			}

			if (showResult) {
				BasicAlertDialog(
					onDismissRequest = {
						showResult = false
					},
					modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())
				) {
					Column(
						modifier = Modifier.wrapContentSize(),
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						FormItem(
							label = {
								Text("")
							}
						) {

						}

						Text("Improve later")
					}
				}
			}
		}
	}

	// ToDo Implement the image and annotation
}


@Composable
private fun Actions(
	message: UIMessage,
	onRegenerate: () -> Unit,
	onEdit: () -> Unit
) {
	val context = LocalPlatformContext.current
	val reuseIconModifier = Modifier.clip(CircleShape).padding(8.dp).size(16.dp)
	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Icon(
			Lucide.Copy, contentDescription = "Copy",
			modifier = reuseIconModifier
				.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = LocalIndication.current,
					onClick = {
						// ToDo copy message to clipboard
					}
				)
		)

		Icon(
			Lucide.RefreshCw, contentDescription = "Regenerate",
			modifier = reuseIconModifier.clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = LocalIndication.current,
				onClick = {
					onRegenerate()
				}
			)
		)

		if (message.role == MessageRole.USER) {
			Icon(
				Lucide.Pencil, "Edit", modifier = reuseIconModifier
					.clickable(
						interactionSource = remember { MutableInteractionSource() },
						indication = LocalIndication.current,
						onClick = {
							onEdit()
						}
					)
			)
		}

		if (message.role == MessageRole.ASSISTANT) {
			// ToDo Implement tts service
//			val tts = LocalTTSService.current
			Icon(
				Lucide.Volume2, "TTS", modifier = reuseIconModifier
					.clickable(
						interactionSource = remember { MutableInteractionSource() },
						indication = LocalIndication.current,
						onClick = {
//							tts?.speak(message.toText(), TextToSpeech.QUEUE_FLUSH, null, null)
							// ToDo tts?.speak
						}
					)
			)
		}
	}
}