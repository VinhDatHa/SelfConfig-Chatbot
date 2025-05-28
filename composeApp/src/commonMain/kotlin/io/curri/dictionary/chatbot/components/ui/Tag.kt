package io.curri.dictionary.chatbot.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.curri.dictionary.chatbot.theme.extendColors

enum class TagType {
	DEFAULT,
	SUCCESS,
	ERROR,
	WARNING,
	INFO
}

@Composable
fun Tag(
	modifier: Modifier = Modifier,
	type: TagType = TagType.DEFAULT,
	children: @Composable RowScope.() -> Unit
) {
	val background = when (type) {
		TagType.SUCCESS -> MaterialTheme.extendColors.green2
		TagType.ERROR -> MaterialTheme.extendColors.red2
		TagType.WARNING -> MaterialTheme.extendColors.orange2
		TagType.INFO -> MaterialTheme.extendColors.blue2
		else -> MaterialTheme.extendColors.gray2
	}
	val textColor = when (type) {
		TagType.SUCCESS -> MaterialTheme.extendColors.gray8
		TagType.ERROR -> MaterialTheme.extendColors.red8
		TagType.WARNING -> MaterialTheme.extendColors.orange8
		TagType.INFO -> MaterialTheme.extendColors.blue8
		else -> MaterialTheme.extendColors.gray8
	}
	ProvideTextStyle(MaterialTheme.typography.labelSmall.copy(color = textColor)) {
		Row(
			modifier = modifier
				.clip(RoundedCornerShape(2.dp))
				.background(background)
				.padding(horizontal = 4.dp, vertical = 1.dp)
		) {
			children()
		}
	}
}