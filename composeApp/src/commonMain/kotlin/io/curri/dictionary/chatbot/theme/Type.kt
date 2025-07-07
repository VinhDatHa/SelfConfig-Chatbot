package io.curri.dictionary.chatbot.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dictionarychatbot.composeapp.generated.resources.Res
import dictionarychatbot.composeapp.generated.resources.sf_pro_display_black
import dictionarychatbot.composeapp.generated.resources.sf_pro_display_bold
import dictionarychatbot.composeapp.generated.resources.sf_pro_display_light
import dictionarychatbot.composeapp.generated.resources.sf_pro_display_semibold
import dictionarychatbot.composeapp.generated.resources.sf_prodisplay_medium
import dictionarychatbot.composeapp.generated.resources.sf_prodisplay_regular
import org.jetbrains.compose.resources.Font

// Set of Material typography styles to start with
val Typography = Typography()

@Composable
fun SfProDisplayTypography(): Typography {
	val customFontFamily = FontFamily(
		Font(
			Res.font.sf_pro_display_bold,
			weight = FontWeight.Bold
		),
		Font(
			Res.font.sf_pro_display_black,
			weight = FontWeight.Black
		),
		Font(
			Res.font.sf_pro_display_semibold,
			weight = FontWeight.SemiBold
		),
		Font(
			Res.font.sf_prodisplay_regular,
			weight = FontWeight.Normal
		), Font(
			Res.font.sf_prodisplay_medium,
			weight = FontWeight.Medium
		),
		Font(
			Res.font.sf_pro_display_light,
			weight = FontWeight.Light
		)
	)
	return Typography.copy(
		displayLarge = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.Black,
			fontSize = 57.sp,
			lineHeight = 64.sp,
			letterSpacing = (-0.25).sp,
		),
		displayMedium = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.Bold,
			fontSize = 45.sp,
			lineHeight = 52.sp,
			letterSpacing = 0.sp,
		),
		displaySmall = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.Bold,
			fontSize = 36.sp,
			lineHeight = 44.sp,
			letterSpacing = 0.sp,
		),
		headlineLarge = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.SemiBold,
			fontSize = 32.sp,
			lineHeight = 40.sp,
			letterSpacing = 0.sp,
		),
		headlineMedium = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.SemiBold,
			fontSize = 28.sp,
			lineHeight = 36.sp,
			letterSpacing = 0.sp,
		),
		headlineSmall = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.SemiBold,
			fontSize = 24.sp,
			lineHeight = 32.sp,
			letterSpacing = 0.sp,
		),
		titleLarge = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.Medium,
			fontSize = 22.sp,
			lineHeight = 28.sp,
			letterSpacing = 0.sp,
		),
		titleMedium = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.Medium,
			fontSize = 16.sp,
			lineHeight = 24.sp,
			letterSpacing = 0.15.sp,
		),
		titleSmall = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.Medium,
			fontSize = 14.sp,
			lineHeight = 20.sp,
			letterSpacing = 0.1.sp,
		),
		bodyLarge = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.Normal,
			fontSize = 16.sp,
			lineHeight = 24.sp,
			letterSpacing = 0.15.sp,
		),
		bodyMedium = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.Normal,
			fontSize = 14.sp,
			lineHeight = 20.sp,
			letterSpacing = 0.25.sp,
		),
		bodySmall = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.Normal,
			fontSize = 12.sp,
			lineHeight = 16.sp,
			letterSpacing = 0.4.sp,
		),
		labelLarge = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.Medium,
			fontSize = 14.sp,
			lineHeight = 20.sp,
			letterSpacing = 0.1.sp,
		),
		labelMedium = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.Medium,
			fontSize = 12.sp,
			lineHeight = 16.sp,
			letterSpacing = 0.5.sp,
		),
		labelSmall = TextStyle(
			fontFamily = customFontFamily,
			fontWeight = FontWeight.Medium,
			fontSize = 11.sp,
			lineHeight = 16.sp,
			letterSpacing = 0.5.sp,
		),
	)
}