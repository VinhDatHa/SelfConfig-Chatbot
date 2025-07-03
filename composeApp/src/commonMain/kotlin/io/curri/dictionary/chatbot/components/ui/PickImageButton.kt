package io.curri.dictionary.chatbot.components.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.composables.icons.lucide.BookImage
import com.composables.icons.lucide.Lucide
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import kotlinx.coroutines.launch

@Composable
internal fun PickImageButton(
	modifier: Modifier = Modifier,
	onImageInByteArray: (String) -> Unit
) {
	val scope = rememberCoroutineScope()
	val context = LocalPlatformContext.current
	val singleImagePicker = rememberFilePickerLauncher(
		type = FilePickerFileType.Image,
		selectionMode = FilePickerSelectionMode.Single,
		onResult = { files ->
			scope.launch {
				files.firstOrNull()?.let { file ->
					file.getPath(context = context)?.let {
						onImageInByteArray(it)
					}
				}
			}
		}
	)

	IconTextButton(
		modifier = modifier,
		onClick = {
			singleImagePicker.launch()
		},
		text = {
			Text("Open Gallery")
		},
		icon = {
			Icon(
				imageVector = Lucide.BookImage,
				contentDescription = "Gallery"
			)
		}
	)

}