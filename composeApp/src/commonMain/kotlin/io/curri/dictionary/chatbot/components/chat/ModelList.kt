package io.curri.dictionary.chatbot.components.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import io.curri.dictionary.chatbot.components.ui.AutoAIIcon
import io.curri.dictionary.chatbot.data.data_store.findModelById
import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.ModelType
import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting
import io.curri.dictionary.chatbot.theme.extendColors
import io.curri.dictionary.chatbot.utils.MockData

// ToDo find the model from data store

@Composable
fun ModelSelector(
	modifier: Modifier = Modifier,
	providers: List<ProviderSetting>,
	modelId: String,
	type: ModelType = ModelType.CHAT,
	onSelect: (ModelFromProvider) -> Unit = {}
) {

	val models = providers.findModelById(modelId)
	var popup by remember { mutableStateOf(false) }
	val mockModel = MockData.mockModelProvider
	TextButton(
		onClick = {
			popup = true
		},
		modifier = modifier
	) {
		(models)?.let {
			println("name: from Model List $it")
			AutoAIIcon(
				name = it.displayName,
				modifier = Modifier
					.padding(end = 4.dp)
					.size(24.dp)
			)
		}
		Text(
			text = models?.displayName ?: "Select Model",
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			style = MaterialTheme.typography.bodySmall
		)
	}

	if (popup) {
		ModalBottomSheet(
			onDismissRequest = {
				popup = false
			},
			sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
		) {
			Column(
				modifier = Modifier.padding(8.dp),
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
				val filteredModels = providers.fastFilter {
					it.enabled && it.models.fastAny { model -> model.type == type }
				}
				ModelList(
					providers = filteredModels,
					modelType = type
				) {
					popup = false
					onSelect(it)
				}
			}
		}
	}
}

@Composable
internal fun ModelList(
	providers: List<ProviderSetting> = emptyList(),
	modelType: ModelType,
	onSelect: (ModelFromProvider) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxWidth().height(500.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		if (providers.isEmpty()) {
			item {
				Text(
					"No AI provider available, please add one in settings",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Normal,
					color = MaterialTheme.extendColors.gray6,
					modifier = Modifier.padding(8.dp)
				)
			}
//			MockData.mockListModel.let { provider ->
//				stickyHeader {
//					Text(
//						text = "Together model",
//						style = MaterialTheme.typography.labelMedium,
//						color = MaterialTheme.colorScheme.primary,
//						modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
//					)
//				}
//				items(
//					items = provider,
//					key = { it.modelId }
//				) { model ->
//					ModelItem(
//						model = model,
//						onSelect = onSelect,
//					)
//				}
//			}
		}

		providers.fastForEach { provider ->
			stickyHeader {
				Text(
					text = provider.name,
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.primary,
					modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
				)
			}
			items(
				items = provider.models.fastFilter { it.type == modelType },
				key = { it.modelId }
			) { model ->
				ModelItem(
					model = model,
					onSelect = onSelect,
				)
			}
		}
	}
}

@Composable
internal fun ModelItem(
	modifier: Modifier = Modifier,
	model: ModelFromProvider,
	onSelect: (ModelFromProvider) -> Unit,
	tail: @Composable RowScope.() -> Unit = {}
) {
	OutlinedCard(
		modifier = modifier,
		onClick = { onSelect(model) }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier.fillMaxWidth().padding(12.dp)
		) {
			AutoAIIcon(model.modelId, modifier = Modifier.size(32.dp))
			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					model.modelId,
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.extendColors.gray4
				)
				Text(
					text = model.displayName,
					style = MaterialTheme.typography.labelMedium,
				)
			}
			tail()
		}
	}
}