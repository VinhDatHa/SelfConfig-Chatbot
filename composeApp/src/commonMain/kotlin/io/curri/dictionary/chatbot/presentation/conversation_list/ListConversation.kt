package io.curri.dictionary.chatbot.presentation.conversation_list

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.NotebookPen
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.SquarePen
import com.composables.icons.lucide.Trash2
import io.curri.dictionary.chatbot.app.Screen
import io.curri.dictionary.chatbot.components.ui.Conversation
import io.curri.dictionary.chatbot.components.ui.context.LocalNavController
import io.curri.dictionary.chatbot.utils.newChat
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
	val navController = LocalNavController.current

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
			IconButton(
				onClick = {
					navController.navigate(Screen.SearchScreen)
				}
			) {
				Icon(
					imageVector = Lucide.Search,
					contentDescription = "Search content"
				)
			}
			IconButton(onClick = {
				openConversation(Conversation.empty())
			}) {
				Icon(
					imageVector = Lucide.NotebookPen, contentDescription = "Create new chat"
				)
			}
		})
	}) { innerPadding ->
		Column(
			modifier = Modifier.padding(innerPadding)
		) {
			ListOfConversation(
				modifier = Modifier.fillMaxWidth(),
				onDelete = { viewModel.delete(it) },
				onNewConversation = {
					navController.newChat(Uuid.random().toString())
				},
				conversations = groupConversation,
				onOpen = {
					openConversation(it)
				},
				onRegenerateTitle = {

				}
			)
		}

	}
}

@Composable
private fun EmptyListScreen(
	modifier: Modifier = Modifier, onClick: () -> Unit
) {
	Column(
		modifier
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
private fun ListOfConversation(
	modifier: Modifier = Modifier,
	conversations: Map<LocalDate, List<Conversation>>,
	onOpen: (Conversation) -> Unit,
	onDelete: (Conversation) -> Unit,
	onRegenerateTitle: (Conversation) -> Unit,
	onNewConversation: () -> Unit
) {
	LazyColumn(
		modifier,
		contentPadding = PaddingValues(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		if (conversations.values.isEmpty()) {
			item {
				EmptyListScreen {
					onNewConversation()
				}
			}
		} else {
			conversations.forEach { (date, conversationOfDate) ->
				stickyHeader {
					DateHeader(date)
				}
				items(conversationOfDate, key = { it.id }) { item ->
					ConversationItem(
						modifier = Modifier.animateItem(),
						conversation = item,
						onClick = onOpen,
						onDelete = onDelete,
						onRegenerateTitle = onRegenerateTitle
					)
				}
			}
		}
	}
}

@Composable
internal fun ConversationItem(
	conversation: Conversation,
	modifier: Modifier = Modifier,
	onDelete: (Conversation) -> Unit = {},
	onRegenerateTitle: (Conversation) -> Unit = {},
	onClick: (Conversation) -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }
	var showDropdownMenu by remember {
		mutableStateOf(false)
	}
	Box(
		modifier = modifier.clip(RoundedCornerShape(10f))
			.combinedClickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = { onClick(conversation) }, onLongClick = {
				showDropdownMenu = true
			}),
	) {
		Card(
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surfaceContainer,
				contentColor = MaterialTheme.colorScheme.onSurface
			),
			shape = RoundedCornerShape(12.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically
			) {
				Column(
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					Text(
						text = conversation.title.ifBlank { "News" },
						maxLines = 1,
						style = MaterialTheme.typography.titleMedium,
						overflow = TextOverflow.Ellipsis
					)
					Text(
						text = conversation.messages.last().summaryAsText(),
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Light,
						overflow = TextOverflow.Ellipsis,
						maxLines = 1,
					)
				}
				Spacer(Modifier.weight(1f))
//				AnimatedVisibility(loading) {
//					Box(modifier = Modifier.clip(CircleShape).background(MaterialTheme.extendColors.green6).size(4.dp).semantics {
//						contentDescription = "Loading"
//					})
//				}
				DropdownMenu(
					expanded = showDropdownMenu,
					onDismissRequest = { showDropdownMenu = false },
				) {
					DropdownMenuItem(text = {
						Text("Rename conversation")
					}, onClick = {
						onRegenerateTitle(conversation)
						showDropdownMenu = false
					}, leadingIcon = {
						Icon(Lucide.SquarePen, null)
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