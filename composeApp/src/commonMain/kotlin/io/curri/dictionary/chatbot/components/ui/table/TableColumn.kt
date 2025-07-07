package io.curri.dictionary.chatbot.components.ui.table

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.curri.dictionary.chatbot.components.ui.richtext.MarkdownBlock
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

@Composable
fun TableNode(node: ASTNode, content: String, modifier: Modifier = Modifier) {
	val headerNode = node.children.find { it.type == GFMElementTypes.HEADER }
	val rowNodes = node.children.filter { it.type == GFMElementTypes.ROW }

	val columnCount = headerNode?.children?.count { it.type == GFMTokenTypes.CELL } ?: 0
	if (columnCount == 0) return

	val headerCells = headerNode?.children
		?.filter { it.type == GFMTokenTypes.CELL }
		?.map { it.getTextInNode(content).trim() }
		?: emptyList()

	val rows = rowNodes.map { rowNode ->
		rowNode.children
			.filter { it.type == GFMTokenTypes.CELL }
			.map { it.getTextInNode(content).trim().toString() }
	}

	val columns = List(columnCount) { columnIndex ->
		ColumnDefinition<List<String>>(
			header = {
				MarkdownBlock(
					content = (if (columnIndex < headerCells.size) headerCells[columnIndex] else "").toString(),
				)
			},
			cell = { rowData ->
				MarkdownBlock(
					content = (if (columnIndex < rowData.size) rowData[columnIndex] else "").toString(),
				)
			},
			width = ColumnWidth.Adaptive(min = 80.dp)
		)
	}

	// 渲染表格
	DataTable(
		columns = columns,
		data = rows,
		modifier = modifier
			.padding(vertical = 8.dp)
			.fillMaxWidth()

	)
}