package io.curri.dictionary.chatbot.presentation.conversation_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.NotebookPen
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Trash2
import io.curri.dictionary.chatbot.components.ui.Conversation
import io.curri.dictionary.chatbot.presentation.common_state.ScreenState
import io.curri.dictionary.chatbot.theme.extendColors
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Composable
internal fun ListConversationScreen(
	viewModel: ListConversationVM = koinViewModel(), openConversation: (Conversation) -> Unit
) {

	val conversations by viewModel.conversation.collectAsStateWithLifecycle()
	val screenState by viewModel.screenState.collectAsStateWithLifecycle()

	LaunchedEffect(Unit) {
		viewModel.init()
	}

	val groupConversation by remember(conversations) {
		derivedStateOf {
			conversations.sortedByDescending { it.createAt }.groupBy { conversation ->
				val instant = conversation.createAt
				instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
			}
		}
	}

	Scaffold(modifier = Modifier.background(MaterialTheme.colorScheme.background), topBar = {
		TopAppBar(title = {
			Text(
				"Recent chat",
				style = MaterialTheme.typography.titleLarge,
			)
		}, actions = {
			IconButton(onClick = {
				openConversation(Conversation.empty())
			}) {
				Icon(
					imageVector = Lucide.NotebookPen, contentDescription = "Create new chat"
				)
			}
		})
	}) {
		Column(
			modifier = Modifier.padding(it)
		) {
			LazyColumn(
				modifier = Modifier.fillMaxWidth(),
				contentPadding = PaddingValues(16.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp),
			) {
				if (conversations.isEmpty()) {
					item {
						EmptyListScreen {
							openConversation(
								Conversation(
									id = Uuid.random().toString(), messages = emptyList()
								)
							)
						}
					}
				} else {
					groupConversation.forEach { (date, conversationsOfDate) ->
						stickyHeader {
							DateHeader(date)
						}
						items(conversationsOfDate, key = { conversation -> conversation.id }) { item ->
							ConversationItem(conversation = item, selected = false, loading = screenState is ScreenState.Loading, onClick = {
								openConversation(it)
							}, onDelete = {

							}, onRegenerateTitle = {

							}, modifier = Modifier.animateItem()
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun LazyItemScope.EmptyListScreen(
	modifier: Modifier = Modifier, onClick: () -> Unit
) {
	Surface(
		modifier, shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerLow
	) {
		Button(onClick = {
			onClick()
		}) {
			Text("Open mock conversation")
		}

		Text(
			text = "Conversation history is empty", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface
		)
	}
}

@Composable
private fun ConversationItem(
	conversation: Conversation,
	selected: Boolean,
	loading: Boolean,
	modifier: Modifier = Modifier,
	onDelete: (Conversation) -> Unit = {},
	onRegenerateTitle: (Conversation) -> Unit = {},
	onClick: (Conversation) -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }
	val backgroundColor = if (selected) {
		MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
	} else {
		MaterialTheme.colorScheme.surfaceContainer
	}
	var showDropdownMenu by remember {
		mutableStateOf(false)
	}
	Box(
		modifier = modifier.clip(RoundedCornerShape(10f))
			.combinedClickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = { onClick(conversation) }, onLongClick = {
				showDropdownMenu = true
			}).background(backgroundColor),
	) {
		Row(
			modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = conversation.title.ifBlank { "News" }, maxLines = 1, overflow = TextOverflow.Ellipsis
			)
			Spacer(Modifier.weight(1f))
			AnimatedVisibility(loading) {
				Box(modifier = Modifier.clip(CircleShape).background(MaterialTheme.extendColors.green6).size(4.dp).semantics {
					contentDescription = "Loading"
				})
			}
			DropdownMenu(
				expanded = showDropdownMenu,
				onDismissRequest = { showDropdownMenu = false },
			) {
				DropdownMenuItem(text = {
					Text("Regenerate Title")
				}, onClick = {
					onRegenerateTitle(conversation)
					showDropdownMenu = false
				}, leadingIcon = {
					Icon(Lucide.RefreshCw, null)
				})

				DropdownMenuItem(text = {
					Text("Delete")
				}, onClick = {
					onDelete(conversation)
					showDropdownMenu = false
				}, leadingIcon = {
					Icon(Lucide.Trash2, null)
				})
			}
		}
	}
}

@Composable
internal fun DateHeader(date: LocalDate) {
	val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
	val yesterday = today.minus(1, DateTimeUnit.DAY)

	val displayText = when {
		date == today -> "Today"
		date == yesterday -> "Yesterday"
		date.year == today.year -> "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.day}"
		else -> "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}"
	}

	Row(
		modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = displayText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary
		)
	}
}