package io.curri.dictionary.chatbot.presentation.search_page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import io.curri.dictionary.chatbot.components.navigation.BackButton
import io.curri.dictionary.chatbot.components.ui.context.LocalNavController
import io.curri.dictionary.chatbot.extensions.openChatPage
import io.curri.dictionary.chatbot.presentation.conversation_list.ConversationItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SearchPage(
	modifier: Modifier = Modifier,
	viewModel: SearchVM = koinViewModel()
) {

	val navController = LocalNavController.current

	val query = viewModel.searchValue.collectAsStateWithLifecycle()

	val searchResult = viewModel.searchResult.collectAsStateWithLifecycle()

	Scaffold(
		modifier = Modifier.fillMaxSize()
			.background(MaterialTheme.colorScheme.background),
		topBar = {
			TopAppBar(
				title = {
					Text("Search")
				},
				navigationIcon = {
					BackButton {
						navController.popBackStack()
					}
				}
			)
		}, bottomBar = {
			SearchBar(
				modifier = Modifier.imePadding().navigationBarsPadding(),
				input = query.value
			) { input ->
				viewModel.performSearch(input)
			}
		}
	) { innerPadding ->
		Column(
			modifier = Modifier.padding(innerPadding),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			LazyColumn(
				contentPadding = PaddingValues(16.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				items(searchResult.value, key = { it.id }) { conversation ->
					ConversationItem(
						modifier = Modifier.fillMaxWidth().animateItem(),
						conversation = conversation,
						onClick = {
							navController.openChatPage(it.id)
						}, onDelete = {

						}, onRegenerateTitle = {

						}, loading = false, selected = false
					)
				}
			}
		}
	}
}


@Composable
private fun SearchBar(
	modifier: Modifier = Modifier,
	input: String,
	onQuerySearch: (String) -> Unit
) {
	OutlinedTextField(
		modifier = modifier.fillMaxWidth(),
		value = input,
		onValueChange = onQuerySearch,
		placeholder = {
			Text("Enter keywords to search chat")
		},
		singleLine = true,
		trailingIcon = {
			IconButton(
				onClick = { onQuerySearch("") },
				modifier = Modifier
			) {
				Icon(Lucide.X, "Clear")
			}
		}
	)
}