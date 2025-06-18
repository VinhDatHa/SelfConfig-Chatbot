package io.curri.dictionary.chatbot.components.ui.richtext

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import coil3.compose.AsyncImage
import io.curri.dictionary.chatbot.components.ui.Table
import io.curri.dictionary.chatbot.components.ui.TableCell
import io.curri.dictionary.chatbot.components.ui.TableHeader
import io.curri.dictionary.chatbot.components.ui.TableRow
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.MarkdownParser

private val flavour by lazy {
	GFMFlavourDescriptor()
}

private val parser by lazy {
	MarkdownParser(flavour)
}

private val INLINE_LATEX_REGEX = Regex("\\\\\\((.+?)\\\\\\)")
private val BLOCK_LATEX_REGEX = Regex("\\\\\\[(.+?)\\\\\\]", RegexOption.MULTILINE)
private val CITATION_REGEX = Regex("\\[citation:(\\w+)\\]")

// Preprocess markdown content and convert inline formulas and block-level formulas to LaTeX format
// Replace inline formulas \( ... \) to $ ... $
// Replace block-level formulas \[ ... \] to $$ ... $$
private fun preProcess(content: String): String {
	var result = content.replace(INLINE_LATEX_REGEX) { matchResult ->
		"$" + matchResult.groupValues[1] + "$"
	}

	result = result.replace(CITATION_REGEX) { matchResult ->
		"<citation>${matchResult.groupValues[1]}</citation>"
	}
	return result
}

object HeaderStyle {
	val H1 = TextStyle(
		fontStyle = FontStyle.Normal,
		fontWeight = FontWeight.Bold,
		fontSize = 24.sp
	)

	val H2 = TextStyle(
		fontStyle = FontStyle.Normal,
		fontWeight = FontWeight.Bold,
		fontSize = 20.sp
	)

	val H3 = TextStyle(
		fontStyle = FontStyle.Normal,
		fontWeight = FontWeight.Bold,
		fontSize = 18.sp
	)

	val H4 = TextStyle(
		fontStyle = FontStyle.Normal,
		fontWeight = FontWeight.Bold,
		fontSize = 16.sp
	)

	val H5 = TextStyle(
		fontStyle = FontStyle.Normal,
		fontWeight = FontWeight.Bold,
		fontSize = 14.sp
	)

	val H6 = TextStyle(
		fontStyle = FontStyle.Normal,
		fontWeight = FontWeight.Bold,
		fontSize = 12.sp
	)
}

@Composable
internal fun MarkdownBlock(
	modifier: Modifier = Modifier,
	style: TextStyle = LocalTextStyle.current,
	content: String
) {
	val preProcessed = remember(content) { preProcess(content) }
	val astTree = remember(preProcessed) {
		parser.buildMarkdownTreeFromString(preProcessed)
	}
	ProvideTextStyle(style) {
		MarkdownAst(astNode = astTree, content = preProcessed, modifier = modifier)
	}
}

@Composable
private fun MarkdownAst(modifier: Modifier = Modifier, astNode: ASTNode, content: String) {
	Column(
		modifier
	) {
		astNode.children.fastForEach { child ->
			// ToDo markdown node
			MarkdownNode(child, content)
		}
	}
}

@Composable
private fun MarkdownParagraph(modifier: Modifier = Modifier, node: ASTNode, content: String) {
	val colorScheme = MaterialTheme.colorScheme
	val inlineContents = remember {
		mutableStateMapOf<String, InlineTextContent>()
	}

	BoxWithConstraints {
		val maxWidth = this.maxWidth
		val annotatedString = remember(content) {
			buildAnnotatedString {
				node.children.fastForEach { child ->
					appendMarkdownNodeContent(child, content, inlineContents, colorScheme, maxWidth)
				}
			}
		}
		Text(
			text = annotatedString,
			modifier = modifier.padding(horizontal = 4.dp),
			style = LocalTextStyle.current,
			overflow = TextOverflow.Visible,
			softWrap = true,
			inlineContent = inlineContents,
		)
	}
}

private fun AnnotatedString.Builder.appendMarkdownNodeContent(
	node: ASTNode,
	content: String,
	inlineContents: MutableMap<String, InlineTextContent>,
	colorScheme: ColorScheme,
	maxWidth: Dp
) {
	when (node.type) {
		MarkdownTokenTypes.TEXT,
		MarkdownTokenTypes.LPAREN,
		MarkdownTokenTypes.RPAREN,
		MarkdownTokenTypes.WHITE_SPACE,
		MarkdownTokenTypes.COLON -> {
			append(node.getTextInNode(content))
		}

		MarkdownTokenTypes.EMPH -> {
			val text = node.getTextInNode(content)
			if (text != "*") append(text)
		}

		MarkdownTokenTypes.HTML_TAG -> {
			val text = node.getTextInNode(content)
			if (text == "<citation>") {
				val id = node.nextSibling()?.getTextInNode(content)
				if (id != null) {
					pushStyle(
						SpanStyle(
							background = colorScheme.secondaryContainer,
							fontSize = 0.85.em
						)
					)
					append(" ")
				}
			} else if (text == "</citation>") {
				append(" ")
				pop()
				append(" ")
			} else {
				append(text)
			}
		}

		MarkdownElementTypes.EMPH -> {
			withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
				node.children.fastForEach {
					appendMarkdownNodeContent(
						it,
						content,
						inlineContents,
						colorScheme,
						maxWidth
					)
				}
			}
		}

		MarkdownElementTypes.STRONG -> {
			withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
				node.children.fastForEach {
					appendMarkdownNodeContent(
						it,
						content,
						inlineContents,
						colorScheme,
						maxWidth
					)
				}
			}
		}

		MarkdownElementTypes.INLINE_LINK -> {
			val linkText =
				node.findChildOfType(MarkdownTokenTypes.TEXT)?.getTextInNode(content).toString() ?: ""
			val linkDest =
				node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(content).toString()
					?: ""
			withLink(LinkAnnotation.Url(linkDest)) {
				withStyle(
					SpanStyle(
						color = colorScheme.primary,
						textDecoration = TextDecoration.Underline
					)
				) {
					append(linkText)
				}
			}
		}

		MarkdownElementTypes.CODE_SPAN -> {
			val code = node.getTextInNode(content).trim('`')
			withStyle(
				SpanStyle(
					fontFamily = FontFamily.Monospace,
					fontSize = 0.95.em,
				)
			) {
				append(code)
			}
		}

//		GFMElementTypes.INLINE_MATH -> {
//			// formula as id
//			val formula = node.getTextInNode(content)
//			appendInlineContent(formula, "[Latex]")
//			inlineContents.putIfAbsent(
//				formula, InlineTextContent(
//					placeholder = Placeholder(
//						width = 1.em,
//						height = 1.em,
//						placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
//					),
//					children = {
//						val density = LocalDensity.current
//						MathInline(
//							formula,
//							modifier = Modifier
//								.onGloballyPositioned { coord ->
//									val width = coord.size.width
//									val height = coord.size.height
//									with(density) {
//										val widthInSp = width.toDp().toSp()
//										val heightInSp = (height.toDp() + 4.dp).toSp()
//										val inlineContent = inlineContents[formula]
//										if (inlineContent != null && inlineContent.placeholder.width != widthInSp) {
//											inlineContents[formula] = InlineTextContent(
//												placeholder = Placeholder(
//													width = widthInSp,
//													height = heightInSp,
//													placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
//												),
//												children = inlineContent.children
//											)
//										}
//									}
//								}
//						)
//					}
//				))
//		}

//		GFMElementTypes.BLOCK_MATH -> {
//			// formula as id
//			val formula = node.getTextInNode(content)
//			appendInlineContent(formula, "[Latex]")
//			inlineContents.putIfAbsent(
//				formula, InlineTextContent(
//					placeholder = Placeholder(
//						width = 1.em,
//						height = 1.em,
//						placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
//					),
//					children = {
//						val density = LocalDensity.current
//						MathBlock(
//							formula,
//							modifier = Modifier
//								.width(maxWidth)
//								.onGloballyPositioned { coord ->
//									val height = coord.size.height
//									with(density) {
//										val widthInSp = maxWidth.toSp()
//										val heightInSp = (height.toDp() + 24.dp).toSp()
//										val inlineContent = inlineContents[formula]
//										if (inlineContent != null && inlineContent.placeholder.width != widthInSp) {
//											inlineContents[formula] = InlineTextContent(
//												placeholder = Placeholder(
//													width = widthInSp,
//													height = heightInSp,
//													placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
//												),
//												children = inlineContent.children
//											)
//										}
//									}
//								}
//						)
//					}
//				))
//		}

		// 其他类型继续递归处理
		else -> {
			node.children.fastForEach {
				appendMarkdownNodeContent(
					it,
					content,
					inlineContents,
					colorScheme,
					maxWidth
				)
			}
		}
	}
}

private fun ASTNode.nextSibling(): ASTNode? {
	val brother = this.parent?.children ?: return null
	for (i in brother.indices) {
		if (brother[i] == this) {
			if (i + 1 < brother.size) {
				return brother[i + 1]
			}
		}
	}
	return null
}


@Composable
private fun MarkdownNode(node: ASTNode, content: String, modifier: Modifier = Modifier) {
	when (node.type) {
		MarkdownElementTypes.MARKDOWN_FILE -> {
			node.children.fastForEach { child ->
				MarkdownNode(modifier = modifier, node = child, content = content)
			}
		}

		MarkdownElementTypes.PARAGRAPH -> {
			MarkdownParagraph(
				modifier,
				node,
				content
			)
		}

		MarkdownElementTypes.ATX_1,
		MarkdownElementTypes.ATX_2,
		MarkdownElementTypes.ATX_3,
		MarkdownElementTypes.ATX_4,
		MarkdownElementTypes.ATX_5,
		MarkdownElementTypes.ATX_6 -> {
			val style = when (node.type) {
				MarkdownElementTypes.ATX_1 -> HeaderStyle.H1
				MarkdownElementTypes.ATX_2 -> HeaderStyle.H2
				MarkdownElementTypes.ATX_3 -> HeaderStyle.H3
				MarkdownElementTypes.ATX_4 -> HeaderStyle.H4
				MarkdownElementTypes.ATX_5 -> HeaderStyle.H5
				MarkdownElementTypes.ATX_6 -> HeaderStyle.H6
				else -> throw IllegalArgumentException("Unknown header type")
			}
			ProvideTextStyle(style) {
				FlowRow(modifier = modifier.padding(vertical = 8.dp)) {
					node.children.forEach { child ->
						MarkdownNode(
							node = child,
							content = content,
							modifier = Modifier.align(Alignment.CenterVertically)
						)
					}
				}
			}
		}

		// Unordered List
		MarkdownElementTypes.UNORDERED_LIST -> {
			Column(
				modifier = modifier.padding(start = 4.dp)
			) {
				node.children.fastForEach { child ->
					if (child.type == MarkdownElementTypes.LIST_ITEM) {
						Row {
							Text(
								text = "• ",
								modifier = Modifier.alignByBaseline()
							)
							FlowRow {
								child.children.fastForEach { listItemChild ->
									MarkdownNode(node = listItemChild, content = content)
								}
							}
						}
					}
				}
			}
		}

		MarkdownElementTypes.ORDERED_LIST -> {
			Column(
				modifier = modifier.padding(start = 4.dp)
			) {
				var index = 1
				node.children.fastForEach { child ->
					if (child.type == MarkdownElementTypes.LIST_ITEM) {
						Row {
							Text(
								text = child.findChildOfType(MarkdownTokenTypes.LIST_NUMBER)
									?.getTextInNode(content).toString().takeIf { it.isNotBlank() } ?: "-",
							)
							FlowRow {
								child.children.fastForEach { listItemChild ->
									MarkdownNode(
										node = listItemChild,
										content = content
									)
								}
							}
						}
						index++
					}
				}
			}
		}

		// Block quote
		MarkdownElementTypes.BLOCK_QUOTE -> {
			ProvideTextStyle(LocalTextStyle.current.copy(fontStyle = FontStyle.Italic)) {
				val borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
				val bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
				FlowRow(
					modifier = Modifier
						.drawWithContent {
							drawContent()
							drawRect(
								color = bgColor,
								size = size
							)
							drawRect(
								color = borderColor,
								size = Size(10f, size.height)
							)
						}
						.padding(8.dp)
				) {
					node.children.fastForEach { child ->
						MarkdownNode(node = child, content = content)
					}
				}
			}
		}

		// Inline link
		MarkdownElementTypes.INLINE_LINK -> {
			val linkText =
				node.findChildOfType(MarkdownTokenTypes.TEXT)?.getTextInNode(content)?.toString() ?: ""
			val linkDest =
				node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(content)?.toString()
					?: ""
			val uriHandler = LocalUriHandler.current
			Text(
				text = linkText,
				color = MaterialTheme.colorScheme.primary,
				textDecoration = TextDecoration.Underline,
				modifier = modifier.clickable {
					uriHandler.openUri(linkDest)
				}
			)
		}

		MarkdownElementTypes.EMPH -> {
			ProvideTextStyle(TextStyle(fontStyle = FontStyle.Italic)) {
				node.children.fastForEach { child ->
					MarkdownNode(child, content, modifier)
				}
			}
		}

		MarkdownElementTypes.STRONG -> {
			ProvideTextStyle(TextStyle(fontWeight = FontWeight.Bold)) {
				node.children.fastForEach { child ->
					MarkdownNode(child, content, modifier)
				}
			}
		}

		GFMElementTypes.STRIKETHROUGH -> {
			Text(
				text = node.getTextInNode(content).toString(),
				textDecoration = TextDecoration.LineThrough,
				modifier = modifier
			)
		}
		GFMElementTypes.TABLE -> {
			Table(modifier = modifier) {
				node.children.fastForEach {
					MarkdownNode(it, content)
				}
			}
		}
		GFMElementTypes.HEADER -> {
			TableHeader(modifier = modifier) {
				node.children.fastForEach {
					if (it.type == GFMTokenTypes.CELL) {
						TableCell {
							MarkdownNode(it, content)
						}
					}
				}
			}
		}

		GFMElementTypes.ROW -> {
			TableRow(modifier = modifier) {
				node.children.fastForEach {
					if (it.type == GFMTokenTypes.CELL) {
						TableCell {
							MarkdownNode(it, content)
						}
					}
				}
			}
		}

		MarkdownElementTypes.IMAGE -> {
			val altText =
				node.findChildOfType(MarkdownElementTypes.LINK_TEXT)?.getTextInNode(content) ?: ""
			val imageUrl =
				node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(content)
					?: ""
			Column(
				modifier = modifier,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				AsyncImage(model = imageUrl, contentDescription = altText.toString())
			}
		}
	}
}