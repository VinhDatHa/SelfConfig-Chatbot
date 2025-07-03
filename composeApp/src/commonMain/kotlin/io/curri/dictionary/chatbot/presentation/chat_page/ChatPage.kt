package io.curri.dictionary.chatbot.presentation.chat_page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ListTree
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.MessageCirclePlus
import com.composables.icons.lucide.Settings
import io.curri.dictionary.chatbot.app.Screen
import io.curri.dictionary.chatbot.components.chat.ChatInput
import io.curri.dictionary.chatbot.components.chat.ChatMessage
import io.curri.dictionary.chatbot.components.chat.ModelSelector
import io.curri.dictionary.chatbot.components.chat.rememberChatInputState
import io.curri.dictionary.chatbot.components.ui.Conversation
import io.curri.dictionary.chatbot.components.ui.ToastType
import io.curri.dictionary.chatbot.components.ui.Toaster
import io.curri.dictionary.chatbot.components.ui.WavyCircularProgressIndicator
import io.curri.dictionary.chatbot.components.ui.context.LocalNavController
import io.curri.dictionary.chatbot.components.ui.toaster
import io.curri.dictionary.chatbot.data.models.ModelType
import io.curri.dictionary.chatbot.data.models.UIMessage
import io.curri.dictionary.chatbot.presentation.conversation_list.DateHeader
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ChatPage(
	id: String, viewModel: ChatVM = koinViewModel(),
	onOpenNewChat: () -> Unit
) {
	val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
	val conversation by viewModel.conversation.collectAsStateWithLifecycle()
	val loadingJob by viewModel.conversationJob.collectAsStateWithLifecycle()
	val settings by viewModel.settings.collectAsStateWithLifecycle()
	val chatModel by viewModel.currentModelChat.collectAsStateWithLifecycle()
	val history by viewModel.allConversations.collectAsStateWithLifecycle()

	val navController = LocalNavController.current
	LaunchedEffect(Unit) {
		viewModel.errorFlow.collect { error ->
			toaster.show(error.message ?: "An error occurred", type = ToastType.ERROR)
		}
	}

	LaunchedEffect(id) {
		viewModel.loadConversation(id)
	}

	ModalNavigationDrawer(drawerState = drawerState,
		drawerContent = {
			DrawerContent(
				current = conversation,
				conversations = history
			) { navController.navigate(Screen.SettingsScreen) }
		}) {
		val inputState = rememberChatInputState()
		Scaffold(topBar = {
			TopBar(conversation, drawerState, onClickMenu = {
				// Menu
				viewModel.generateTitle()
			}, onNewChat = {
				onOpenNewChat()
			})
		}, snackbarHost = {
			Toaster(
				modifier = Modifier.fillMaxWidth(), toastState = toaster
			)
		}, bottomBar = {
			ChatInput(state = inputState, enableSearch = false, onToggleSearch = {
				// ToDo toggle search
			}, onCancelClick = {
				loadingJob?.cancel()
			}, onSendClick = {
				if (chatModel == null) {
					toaster.show("Please selected model", ToastType.ERROR)
					return@ChatInput
				}
				if (inputState.isEditing()) {
					//ToDo handling message edit
					viewModel.handleMessageEdit(
						parts = inputState.messageContent, uuid = inputState.editingMessage ?: return@ChatInput
					)
				} else {
					viewModel.handleMessageSend(inputState.messageContent)
				}
				inputState.clearInput()
			}, onImageDelete = {
				viewModel.deleteImage(it)
			}, actions = {
				Box(Modifier.weight(1f)) {
					ModelSelector(
						modelId = settings.chatModelId, providers = settings.providers, onSelect = {
							viewModel.setChatModel(it)
						}, type = ModelType.CHAT
					)
				}
			})
		}) { innerPadding ->
			ChatList(
				innerPaddingValues = innerPadding,
				conversation = conversation,
				loading = loadingJob != null,
				onEdit = {
					inputState.editingMessage = it.id
					inputState.messageContent = it.parts
				},
				onRegenerate = { message ->
					viewModel.regenerateAtMessage(message)
				})
		}
	}
}

@Composable
internal fun ChatList(
	innerPaddingValues: PaddingValues,
	conversation: Conversation,
	loading: Boolean,
	onRegenerate: (UIMessage) -> Unit = {},
	onEdit: (UIMessage) -> Unit = {}
) {
	val state = rememberLazyListState()

	val scrollToBottom = { state.requestScrollToItem(0) }

	Box(modifier = Modifier.padding(innerPaddingValues)) {
		LazyColumn(
			state = state, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), reverseLayout = true
		) {
			item(ScrollBottomKey) {
				Spacer(modifier = Modifier.fillMaxWidth().height(5.dp))
			}

			if (loading) {
				item(LoadingIndicatorKey) {
					WavyCircularProgressIndicator(
						modifier = Modifier.padding(start = 4.dp).size(24.dp), strokeWidth = 2.dp, waveCount = 8
					)
				}
			}

			items(items = conversation.messages.reversed(), key = { it.id }) { message ->
				ChatMessage(message = message, onRegenerate = {
					onRegenerate(message)
				}, onEdit = {
					onEdit(message)
				})
			}
		}
		AnimatedVisibility(
			state.canScrollBackward, modifier = Modifier.align(Alignment.BottomCenter)
		) {
			Surface(
				shape = RoundedCornerShape(50), modifier = Modifier.padding(8.dp), onClick = {
					scrollToBottom()
				}, border = BorderStroke(
					width = 1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
				)
			) {
				Row(
					modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						Lucide.ChevronDown, contentDescription = "Scroll to bottom", modifier = Modifier.size(16.dp)
					)
					Text("Scroll to bottom", style = MaterialTheme.typography.bodySmall)
				}
			}
		}
	}
}

private const val LoadingIndicatorKey = "LoadingIndicator"
private const val ScrollBottomKey = "ScrollBottomKey"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
	conversation: Conversation, drawerState: DrawerState, onClickMenu: () -> Unit, onNewChat: () -> Unit
) {
	val scope = rememberCoroutineScope()

	TopAppBar(navigationIcon = {
		IconButton(onClick = {
			scope.launch { drawerState.open() }
		}) {
			Icon(Lucide.ListTree, "Messages")
		}
	}, title = {
		Text(
			text = conversation.title.ifBlank { "New chat" },
			maxLines = 1,
			fontSize = MaterialTheme.typography.titleMedium.fontSize,
			overflow = TextOverflow.Ellipsis,
			fontWeight = FontWeight.Normal
		)
	}, actions = {
		IconButton(onClick = {
			onClickMenu()
		}) {
			Icon(Lucide.Menu, "Menu")
		}

		IconButton(onClick = {
			onNewChat()
		}) {
			Icon(Lucide.MessageCirclePlus, "New Message")
		}
	})
}

@Composable
private fun DrawerContent(
	current: Conversation, conversations: List<Conversation> = emptyList(), openSetting: () -> Unit
) {
	val navController = LocalNavController.current
	ModalDrawerSheet(
		modifier = Modifier.width(270.dp)
	) {
		Column(
			modifier = Modifier.padding(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			// ToDo card to check update available
			Spacer(modifier = Modifier.size(16.dp))
			Text("History", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
			ConversationHistory(
				modifier = Modifier.fillMaxWidth().weight(1f),
				current = current,
				conversations = conversations,
				onClick = {
					navController.navigate(Screen.ChatPage(it.id)) {
						launchSingleTop = true
						popUpTo<Screen.ChatPage> {
							inclusive = true
						}
					}
				}
			)
			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				TextButton(
					onClick = {
						openSetting()
					}, modifier = Modifier.weight(1f)
				) {
					Icon(Lucide.Settings, "Settings")
					Text("Settings", modifier = Modifier.padding(start = 4.dp))
				}
			}
		}
	}
}

@Composable
private fun ConversationHistory(
	modifier: Modifier = Modifier, current: Conversation, conversations: List<Conversation>, onClick: (Conversation) -> Unit = { }
) {

	val groupConversation by remember(conversations) {
		derivedStateOf {
			conversations.sortedByDescending { it.createAt }.groupBy { conversation ->
				val instant = conversation.createAt
				instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
			}.toImmutableMap()
		}
	}
	LazyColumn(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		groupConversation.forEach { (date, conversationsOfDate) ->
			stickyHeader {
				DateHeader(date)
			}
			items(conversationsOfDate, key = { conversation -> conversation.id }) { item ->
				Box(
					modifier = modifier
						.clip(RoundedCornerShape(50f))
						.background(if (item.id == current.id) MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp) else Color.Transparent).clickable {
							onClick(item)
						},
				) {
					Text(
						modifier = Modifier.padding(8.dp),
						text = item.title, maxLines = 1, overflow = TextOverflow.Ellipsis
					)
				}
			}
		}
	}
}