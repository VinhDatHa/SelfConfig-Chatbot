package io.curri.dictionary.chatbot.presentation.conversation_list

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.curri.dictionary.chatbot.components.ui.Conversation
import io.curri.dictionary.chatbot.utils.MockData

@Composable
internal fun ListConversationScreen(
	openConversation: (Conversation) -> Unit
) {
	Scaffold {
		Column {
			Text("All conversation here")
			Button(
				onClick = {
					openConversation(MockData.mockConversation)
				}
			){
				Text("Open mock conversation")
			}
		}
	}
}