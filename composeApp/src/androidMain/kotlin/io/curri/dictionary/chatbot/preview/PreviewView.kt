package io.curri.dictionary.chatbot.preview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.curri.dictionary.chatbot.components.ui.Toast
import io.curri.dictionary.chatbot.components.ui.ToastItem
import io.curri.dictionary.chatbot.components.ui.ToastType
import io.curri.dictionary.chatbot.components.ui.WavyCircularProgressIndicator
import io.curri.dictionary.chatbot.components.ui.WavyLinearProgressIndicator
import io.curri.dictionary.chatbot.theme.AppTheme
import kotlin.time.ExperimentalTime

@Composable
@Preview(showBackground = true)
fun ProgressIndicatorExample() {
	var progress by remember { mutableFloatStateOf(0.5f) }
	Column(
		modifier = Modifier
			.safeContentPadding()
			.padding(16.dp)
			.clickable {
				progress += 0.1f
				if (progress > 1f) progress = 0f
			},
		verticalArrangement = Arrangement.spacedBy(24.dp)
	) {
		Text(progress.toString())

		LinearProgressIndicator(
			progress = {
				progress
			},
			modifier = Modifier.fillMaxWidth(),
		)

		// 确定进度的波浪进度条
		Text("Wavy Linear - Determinate")
		WavyLinearProgressIndicator(
			progress = progress,
			modifier = Modifier.fillMaxWidth()
		)

		// 不确定进度的波浪进度条
		Text("Wavy Linear - Indeterminate")
		WavyLinearProgressIndicator(
			modifier = Modifier.fillMaxWidth(),
		)

		CircularProgressIndicator()

		// 确定进度的波浪圆形进度条
		Text("Wavy Circular - Determinate")
		WavyCircularProgressIndicator(
			progress = progress,
			modifier = Modifier,
		)

		// 不确定进度的波浪圆形进度条
		Text("Wavy Circular - Indeterminate")
		WavyCircularProgressIndicator(
			modifier = Modifier,
		)
	}
}

@OptIn(ExperimentalTime::class)
@Composable
@Preview(showBackground = true)
private fun ToastPreviewer() {
	Column(
		modifier = Modifier
			.safeContentPadding()
			.padding(16.dp)
			.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		ToastItem(
			Toast(
				message = {
					Text(
						text = "This is a toast",
						style = MaterialTheme.typography.bodyMedium
					)
				},
				type = ToastType.SUCCESS,
			)
		)
		ToastItem(
			Toast(
				message = {
					Text(
						text = "This is a toast",
						style = MaterialTheme.typography.bodyMedium
					)
				},
				type = ToastType.INFO,
			)
		)
		ToastItem(
			Toast(
				message = {
					Text(
						text = "This is a toast",
						style = MaterialTheme.typography.bodyMedium
					)
				},
				type = ToastType.ERROR,
			)
		)
		ToastItem(
			Toast(
				message = {
					Text(
						text = "This is a toast",
						style = MaterialTheme.typography.bodyMedium
					)
				},
				type = ToastType.WARNING,
			)
		)
		ToastItem(
			Toast(
				message = {
					Text(
						text = "This is a toast",
						style = MaterialTheme.typography.bodyMedium
					)
				},
				type = ToastType.DEFAULT,
			)
		)
		ToastItem(
			Toast(
				message = {
					Text(
						text = "This is a toast",
						style = MaterialTheme.typography.bodyMedium
					)
				},
				type = ToastType.DEFAULT,
				action = Toast.Action(
					label = "Action",
					onClick = {}
				)
			))
	}
}