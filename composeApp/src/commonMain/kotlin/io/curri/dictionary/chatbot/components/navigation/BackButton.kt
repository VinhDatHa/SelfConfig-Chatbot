package io.curri.dictionary.chatbot.components.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ChevronLast
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SendToBack

@Composable
fun BackButton(modifier: Modifier = Modifier, action: () -> Unit) {

	IconButton(
		onClick = {
			action()
		}
	) {
		Icon(
			imageVector = Lucide.ChevronLeft,
			contentDescription = "Back"
		)
	}
}