package io.curri.dictionary.chatbot.components.ui.hook

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.curri.dictionary.chatbot.components.ui.context.LocalAnimatedVisibilityScope
import io.curri.dictionary.chatbot.components.ui.context.LocalSharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.heroAnimation(
	key: Any,
): Modifier {
	val sharedTransitionScope = LocalSharedTransitionScope.current
	val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
	return with(sharedTransitionScope) {
		this@heroAnimation.sharedElement(
			sharedContentState = rememberSharedContentState(key),
			animatedVisibilityScope = animatedVisibilityScope
		)
	}
}