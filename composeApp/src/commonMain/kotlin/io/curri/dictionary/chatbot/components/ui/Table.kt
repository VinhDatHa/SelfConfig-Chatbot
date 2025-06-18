package io.curri.dictionary.chatbot.components.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun RowScope.TableCell(
	modifier: Modifier = Modifier,
	weight: Float = 1f,
	content: @Composable () -> Unit
) {
	Box(
		modifier.weight(weight).padding(8.dp),
		contentAlignment = Alignment.Center
	) {
		Row { content() }
	}
}

@Composable
internal fun TableRow(
	modifier: Modifier = Modifier,
	content: @Composable RowScope.() -> Unit
) {
	Row(
		modifier = modifier.fillMaxWidth().padding(1.dp),
		verticalAlignment = Alignment.CenterVertically,
		content = content
	)
}

@Composable
internal fun TableHeader(
	modifier: Modifier = Modifier,
	content: @Composable RowScope.() -> Unit
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.surfaceVariant
	) {
		Row(
			modifier = Modifier.fillMaxWidth().padding(1.dp),
			verticalAlignment = Alignment.CenterVertically,
			content = content
		)
	}
}

@Composable
fun Table(
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit
) {
	Surface(
		modifier = modifier
			.border(
				width = 1.dp,
				color = MaterialTheme.colorScheme.outlineVariant,
				shape = MaterialTheme.shapes.small
			),
		shape = MaterialTheme.shapes.small
	) {
		ProvideTextStyle(MaterialTheme.typography.bodySmall) {
			Column(
				modifier = Modifier.fillMaxWidth()
			) {
				content()
			}
		}
	}
}