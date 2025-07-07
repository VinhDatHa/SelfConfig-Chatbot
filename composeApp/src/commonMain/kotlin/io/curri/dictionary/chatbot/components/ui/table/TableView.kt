package io.curri.dictionary.chatbot.components.ui.table

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.max

private const val DEFAULT_SAMPLE_SIZE = 8 // Number of rows to measure for adaptive width
private val DEFAULT_CELL_PADDING = 8.dp

@Composable
fun <T> DataTable(
	columns: List<ColumnDefinition<T>>,
	data: List<T>,
	modifier: Modifier = Modifier,
	cellPadding: PaddingValues = PaddingValues(DEFAULT_CELL_PADDING),
	border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.5f)),
	adaptiveWidthSampleSize: Int = DEFAULT_SAMPLE_SIZE // Number of rows to sample for adaptive width calculation
) {
	var calculatedColumnWidths by remember(
		columns,
		data.size
	) { // Recalculate if columns or data size change significantly
		mutableStateOf<List<Dp>?>(null)
	}

	val density = LocalDensity.current

	// Phase 1: Calculate Column Widths using SubcomposeLayout
	// This composable doesn't render anything visible itself, it just measures.
	SubcomposeColumnWidthCalculator(
		columns = columns,
		data = data,
		adaptiveWidthSampleSize = adaptiveWidthSampleSize,
		density = density,
		onWidthsCalculated = { widthsPx ->
			// Convert pixel widths back to Dp and store them
			calculatedColumnWidths = widthsPx.map { with(density) { it.toDp() } }
		}
	)

	// Phase 2: Render the Table using the calculated widths
	val horizontalScrollState = rememberScrollState()

	// Only render when widths are calculated
	if (calculatedColumnWidths != null) {
		Column(
			modifier = modifier
				.wrapContentSize()
				.clip(MaterialTheme.shapes.small)
				.border(border, MaterialTheme.shapes.small)
		) {
			// Use HorizontalScroll for tables wider than the screen
			Column(
				modifier = Modifier.horizontalScroll(horizontalScrollState)
			) {
				// --- Header Row ---
				TableHeaderRow(
					columns = columns,
					columnWidths = calculatedColumnWidths!!, // Not null here
					cellPadding = cellPadding,
					border = border
				)

				// --- Data Rows ---
				data.fastForEachIndexed { rowIndex, rowData ->
					key(rowIndex) {
						TableRow(
							rowData = rowData,
							columns = columns,
							columnWidths = calculatedColumnWidths!!,
							cellPadding = cellPadding,
							border = border,
						)
					}
				}
			} // End Inner Column
		} // End Outer Column
	}
}


// --- Helper Composables ---

/**
 * Calculates column widths using SubcomposeLayout. Does not render anything itself.
 */
@Composable
private fun <T> SubcomposeColumnWidthCalculator(
	columns: List<ColumnDefinition<T>>,
	data: List<T>,
	adaptiveWidthSampleSize: Int,
	density: Density,
	onWidthsCalculated: (List<Int>) -> Unit
) {
	SubcomposeLayout { constraints ->
		val measuredWidths = IntArray(columns.size)
		val sampleData = data.take(adaptiveWidthSampleSize.coerceAtLeast(0)) // Take a sample

		columns.fastForEachIndexed { colIndex, column ->
			when (val widthDef = column.width) {
				is ColumnWidth.Fixed -> {
					// Use fixed width directly
					measuredWidths[colIndex] = with(density) { widthDef.width.roundToPx() }
				}

				is ColumnWidth.Adaptive -> {
					var maxContentWidth = 0

					// Measure Header
					val headerPlaceables = subcompose("h_$colIndex") { column.header() }
					headerPlaceables.forEach {
						val measured = it.measure(Constraints())
						maxContentWidth = max(maxContentWidth, measured.width)
					}

					// Measure Sample Data Cells
					sampleData.forEachIndexed { sampleRowIndex, rowData ->
						val cellSlotId = "c_${colIndex}_${sampleRowIndex}"
						val cellPlaceables = subcompose(cellSlotId) { column.cell(rowData) }
						cellPlaceables.forEach { placeable ->
							// Measure with unlimited width to get natural size
							val measured = placeable.measure(Constraints())
							maxContentWidth = max(maxContentWidth, measured.width)
						}
					}

					// Apply constraints
					val minPx = with(density) { widthDef.min.roundToPx() }
					val maxPx =
						with(density) { if (widthDef.max == Dp.Infinity) Int.MAX_VALUE else widthDef.max.roundToPx() }
					measuredWidths[colIndex] = maxContentWidth.coerceIn(minPx, maxPx)
				}
				// Add other width types (like Weighted) here if needed
			}
		}

		// Report calculated widths (in pixels)
		onWidthsCalculated(measuredWidths.toList())

		// Layout phase - we don't actually place anything here
		layout(0, 0) {}
	}
}

/**
 * Renders the Header Row.
 */
@Composable
private fun <T> TableHeaderRow(
	columns: List<ColumnDefinition<T>>,
	columnWidths: List<Dp>,
	cellPadding: PaddingValues,
	border: BorderStroke
) {
	Row(
		modifier = Modifier
			.border(border)
			.background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)),
		verticalAlignment = Alignment.CenterVertically
	) {
		columns.fastForEachIndexed { index, column ->
			Box(
				modifier = Modifier
					.width(columnWidths[index])
					.padding(cellPadding)
			) {
				column.header()
			}
		}
	}
}

/**
 * Renders a single Data Row.
 */
@Composable
private fun <T> TableRow(
	rowData: T,
	columns: List<ColumnDefinition<T>>,
	columnWidths: List<Dp>,
	cellPadding: PaddingValues,
	border: BorderStroke,
) {
	Row(
		modifier = Modifier.border(border),
		verticalAlignment = Alignment.Top
	) {
		columns.fastForEachIndexed { index, column ->
			key(index) {
				Box(
					modifier = Modifier
						.width(columnWidths[index])
						.padding(cellPadding)
				) {
					column.cell(rowData)
				}
			}
		}
	}
}

sealed class ColumnWidth {
	// Automatically determines width based on content, with optional constraints
	data class Adaptive(
		val min: Dp = 0.dp, // Minimum width
		val max: Dp = Dp.Infinity // Maximum width (Dp.Infinity means no upper limit)
	) : ColumnWidth()

	// Fixed width for the column
	data class Fixed(val width: Dp) : ColumnWidth()
}

// Defines a single column in the DataTable
data class ColumnDefinition<T>(
	// Composable function to render the header cell for this column
	val header: @Composable () -> Unit,
	// Composable function to render a data cell for this column, given the row data
	val cell: @Composable (row: T) -> Unit,
	// How the width of this column is determined (defaults to Adaptive)
	val width: ColumnWidth = ColumnWidth.Adaptive()
)