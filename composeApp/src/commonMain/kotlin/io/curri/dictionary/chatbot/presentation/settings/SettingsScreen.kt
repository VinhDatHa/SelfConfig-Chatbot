package io.curri.dictionary.chatbot.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide

@Composable
internal fun SettingsScreen(
	onBackAction: () -> Unit,
) {
	Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
		Text("Setting screen", style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
		Button(
			onClick = {
				onBackAction()
			}
		) {
			Icon(Lucide.ArrowLeft, "Back")
			Text("Get back")
		}
	}
}