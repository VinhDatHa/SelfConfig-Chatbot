package io.curri.dictionary.chatbot.theme

import androidx.compose.ui.graphics.Color

// Atom One Dark Theme Colors
val AtomOneDarkPalette = HighlightTextColorPalette(
	keyword = Color(0xFFc678dd),    // Purple
	string = Color(0xFF98c379),     // Green
	number = Color(0xFFd19a66),     // Orange
	comment = Color(0xFF5c6370),    // Gray
	function = Color(0xFF61afef),   // Blue
	operator = Color(0xFF56b6c2),   // Cyan
	punctuation = Color(0xFFabb2bf), // Light Gray
	className = Color(0xFFe5c07b),  // Yellow
	property = Color(0xFFe06c75),   // Red
	boolean = Color(0xFFd19a66),    // Orange
	variable = Color(0xFFe06c75),   // Red
	tag = Color(0xFFe06c75),        // Red
	attrName = Color(0xFFd19a66),   // Orange
	attrValue = Color(0xFF98c379),  // Green
	fallback = Color(0xFFabb2bf)    // Light Gray
)

// Atom One Light Theme Colors
val AtomOneLightPalette = HighlightTextColorPalette(
	keyword = Color(0xFFa626a4),    // Purple
	string = Color(0xFF50a14f),     // Green
	number = Color(0xFFc18401),     // Orange
	comment = Color(0xFF9ca0a4),    // Gray
	function = Color(0xFF4078f2),   // Blue
	operator = Color(0xFF0184bc),   // Cyan
	punctuation = Color(0xFF383a42), // Dark Gray
	className = Color(0xFFc18401),  // Yellow
	property = Color(0xFFe45649),   // Red
	boolean = Color(0xFFc18401),    // Orange
	variable = Color(0xFFe45649),   // Red
	tag = Color(0xFFe45649),        // Red
	attrName = Color(0xFFc18401),   // Orange
	attrValue = Color(0xFF50a14f),  // Green
	fallback = Color(0xFF383a42)    // Dark Gray
)

data class HighlightTextColorPalette(
	val keyword: Color,
	val string: Color,
	val number: Color,
	val comment: Color,
	val function: Color,
	val operator: Color,
	val punctuation: Color,
	val className: Color,
	val property: Color,
	val boolean: Color,
	val variable: Color,
	val tag: Color,
	val attrName: Color,
	val attrValue: Color,
	val fallback: Color
) {
	companion object {
		val Default = HighlightTextColorPalette(
			keyword = Color(0xFFCC7832),
			string = Color(0xFF6A8759),
			number = Color(0xFF6897BB),
			comment = Color(0xFF808080),
			function = Color(0xFFFFC66D),
			operator = Color(0xFFCC7832),
			punctuation = Color(0xFFCC7832),
			className = Color(0xFFCB772F),
			property = Color(0xFFCB772F),
			boolean = Color(0xFF6897BB),
			variable = Color(0xFF6A8759),
			tag = Color(0xFFE8BF6A),
			attrName = Color(0xFFBABABA),
			attrValue = Color(0xFF6A8759),
			fallback = Color(0xFF808080),
		)
	}
}