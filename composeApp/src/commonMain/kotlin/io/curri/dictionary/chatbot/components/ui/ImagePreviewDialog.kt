package io.curri.dictionary.chatbot.components.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X

@Composable
internal fun ImagePreviewDialog(
	imageUrl: String,
	onDismissRequest: () -> Unit
) {
	BasicAlertDialog(
		modifier = Modifier.heightIn(400.dp),
		onDismissRequest = onDismissRequest,
		properties = DialogProperties(
			dismissOnClickOutside = false,
			usePlatformDefaultWidth = false
		),
	) {
		Column(
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			IconButton(
				modifier = Modifier.align(
					Alignment.End
				),
				onClick = {
					onDismissRequest()
				},
				colors = IconButtonDefaults.iconButtonColors(
					containerColor = MaterialTheme.colorScheme.errorContainer,
					contentColor = MaterialTheme.colorScheme.error,
				)
			) {
				Icon(
					imageVector = Lucide.X,
					contentDescription = "Close"
				)
			}
			AsyncImage(
				model = imageUrl,
				contentDescription = "Image detail",
				modifier = Modifier.fillMaxSize().aspectRatio(1f),
				contentScale = ContentScale.Fit
			)
		}
	}
}