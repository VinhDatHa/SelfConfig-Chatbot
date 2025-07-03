package io.curri.dictionary.chatbot.components.chat

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import io.curri.dictionary.chatbot.network.search.SearchResult
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
import io.ktor.http.parseUrl

@Composable
fun SearchResultList(
	result: SearchResult,
	modifier: Modifier = Modifier
) {
	var showBottomSheet by remember { mutableStateOf(false) }
	val context = LocalUriHandler.current
	Row(
		modifier = Modifier
			.clip(RoundedCornerShape(25))
			.clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = LocalIndication.current,
				onClick = {
					showBottomSheet = true
				}
			)
			.padding(8.dp),
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = "Total: ${result.items.size} found",
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
		)
		Icon(
			Lucide.ChevronRight,
			null,
			tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
			modifier = Modifier.size(16.dp)
		)
	}

	if (showBottomSheet) {
		ModalBottomSheet(
			onDismissRequest = {
				showBottomSheet = false
			},
			sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
		) {
			LazyColumn(
				modifier = modifier
					.fillMaxWidth()
					.fillMaxHeight(fraction = 0.8f),
				verticalArrangement = Arrangement.spacedBy(4.dp),
				contentPadding = PaddingValues(16.dp),
			) {
				items(result.items) { item ->

					Card(
						onClick = {
							context.openUri(item.url)
						}
					) {
						ListItem(
							leadingContent = {
								Favicon(
									url = item.url,
									modifier = Modifier.size(32.dp)
								)
							},
							headlineContent = {
								Text(item.title, maxLines = 1)
							},
							supportingContent = {
								Text(item.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
							},
						)
					}
				}
			}
		}
	}
}

@Composable
fun Favicon(url: String, modifier: Modifier = Modifier) {
	val faviconUrl = remember {
		parseUrl(url)?.let { httpUrl ->
			val protocol = httpUrl.protocol
			val host = httpUrl.host

			URLBuilder().apply {
				this.protocol = protocol
				this.host = host
				encodedPath = "/favicon.ico"
			}.buildString()
		}
	}
	AsyncImage(
		model = faviconUrl,
		modifier = modifier.clip(RoundedCornerShape(25)),
		contentDescription = null
	)
}