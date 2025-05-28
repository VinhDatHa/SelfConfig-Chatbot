package io.curri.dictionary.chatbot.components.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FormItem(
	modifier: Modifier = Modifier,
	label: @Composable () -> Unit,
	content: @Composable ColumnScope.() -> Unit
) {
	Column(
		modifier = modifier.padding(4.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp),
	) {
		ProvideTextStyle(
			MaterialTheme.typography.labelMedium.copy(
				color = MaterialTheme.colorScheme.primary
			)
		) {
			label()
		}
		content()
	}
}