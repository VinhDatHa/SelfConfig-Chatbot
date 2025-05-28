package io.curri.dictionary.chatbot.components.ui.context

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf


@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
	error("No SharedTransitionScope provided")
}

val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope> {
	error("No AnimatedVisibilityScope provided")
}