package io.curri.dictionary.chatbot.presentation.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Boxes
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Palette
import io.curri.dictionary.chatbot.app.Screen
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingsScreen(
	viewModel: SettingViewModel = koinViewModel(),
	navController: NavController,
	onBackAction: () -> Unit,
) {
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
	val settings by viewModel.settings.collectAsStateWithLifecycle()

	Scaffold(
		topBar = {
			LargeTopAppBar(
				title = {
					Text(text = "Settings")
				},
				navigationIcon = {
					IconButton(onClick = { onBackAction() }) {
						Icon(
							Lucide.ArrowLeft,
							contentDescription = "Back icon"
						)
					}
				},
				scrollBehavior = scrollBehavior
			)
		},
		modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
	) { innerPadding ->
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = innerPadding
		) {
			stickyHeader {
				Text(
					text = "Interface Settings",
					modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.primary
				)
			}
			item {
				ListItem(
					headlineContent = {
						Text("Dynamic Colors")
					},
					supportingContent = {
						Text("Dynamic colors")
					},
					trailingContent = {
						Switch(
							checked = settings.dynamicColor,
							onCheckedChange = {
								viewModel.updateSettings(settings.copy(dynamicColor = it))
								onBackAction.invoke()
							},
						)
					},
					leadingContent = {
						Icon(Lucide.Palette, null)
					}
				)
			}
			stickyHeader {
				Text(
					text = "Providers and Models",
					modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.primary
				)
			}

			item {
				SettingItem(
					title = { Text("Config providers") },
					description = { Text("Config providers") },
					icon = { Icon(Lucide.Boxes, "Models") },
					onClick = {
						// ToDo navigate to screen
						navController.navigate(Screen.ProviderConfigPage)
					}
				)
			}
		}
	}
}

@Composable
fun SettingItem(
	title: @Composable () -> Unit,
	description: @Composable () -> Unit,
	icon: @Composable () -> Unit,
	onClick: () -> Unit = {},
) {
	Surface(
		onClick = {
			onClick()
		}
	) {
		ListItem(
			headlineContent = {
				title()
			},
			supportingContent = {
				description()
			},
			leadingContent = {
				icon()
			}
		)
	}
}