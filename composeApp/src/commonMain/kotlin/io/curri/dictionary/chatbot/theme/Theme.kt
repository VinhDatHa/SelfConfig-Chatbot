package io.curri.dictionary.chatbot.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

private val LightColorScheme = lightColorScheme()
private val DarkColorScheme = darkColorScheme()

private val ExtendLightColors = lightExtendColors()
private val ExtendDarkColors = darkExtendColors()
val LocalExtendColors = compositionLocalOf { ExtendLightColors }

val LocalDarkMode = compositionLocalOf { false }

@Composable
fun AppTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	dynamicColor: Boolean = true,
	content: @Composable () -> Unit
) {
	val colorScheme = when {
//		dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//			val context = LocalContext.current
//			if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//		}

		darkTheme -> DarkColorScheme
		else -> LightColorScheme
	}
	val extendColors = if (darkTheme) ExtendDarkColors else ExtendLightColors

	CompositionLocalProvider(
		LocalDarkMode provides darkTheme,
		LocalExtendColors provides extendColors
	) {
		MaterialTheme(
			colorScheme = colorScheme,
			typography = SfProDisplayTypography(),
			content = content
		)
	}
}

val MaterialTheme.extendColors
	@Composable
	@ReadOnlyComposable
	get() = LocalExtendColors.current