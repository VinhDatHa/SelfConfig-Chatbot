package io.curri.dictionary.chatbot.presentation.settings.providers_page

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastFilter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.BadgePlus
import com.composables.icons.lucide.Boxes
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pen
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Save
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X
import io.curri.dictionary.chatbot.components.navigation.BackButton
import io.curri.dictionary.chatbot.components.ui.AutoAIIcon
import io.curri.dictionary.chatbot.components.ui.Tag
import io.curri.dictionary.chatbot.components.ui.TagType
import io.curri.dictionary.chatbot.components.ui.ToastType
import io.curri.dictionary.chatbot.components.ui.Toaster
import io.curri.dictionary.chatbot.components.ui.hook.EditState
import io.curri.dictionary.chatbot.components.ui.hook.EditStateContent
import io.curri.dictionary.chatbot.components.ui.hook.useEditState
import io.curri.dictionary.chatbot.components.ui.plus
import io.curri.dictionary.chatbot.components.ui.toaster
import io.curri.dictionary.chatbot.data.models.Modality
import io.curri.dictionary.chatbot.data.models.ModelFromProvider
import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting
import io.curri.dictionary.chatbot.presentation.settings.SettingViewModel
import io.curri.dictionary.chatbot.providers.ProviderManager
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingsProvider(
	viewModel: SettingViewModel = koinViewModel(),
	onBackClick: () -> Unit = {}
) {
	val settings by viewModel.settings.collectAsStateWithLifecycle()

	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(text = "Providers")
				},
				navigationIcon = {
					BackButton { onBackClick() }
				},
				actions = {
					// ToDo add button to navigate the InApp Screen
				}
			)
		},
		snackbarHost = {
			Toaster(
				modifier = Modifier.fillMaxWidth(),
				toastState = toaster
			)
		}
	) { innerPadding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.imePadding(),
			contentPadding = innerPadding + PaddingValues(16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			val providersSorted = settings.providers
				.sortedByDescending { it.enabled }
			items(items = providersSorted, key = { it.id }) { provider ->
				ProviderItem(
					modifier = Modifier.animateItem(),
					provider = provider,
					onDelete = {
						val newSettings = settings.copy(
							providers = settings.providers - provider
						)
						viewModel.updateSettings(newSettings)
					},
					onEdit = { newProvider ->
						val newSettings = settings.copy(
							providers = settings.providers.map {
								if (newProvider.id == it.id) {
									newProvider
								} else {
									it
								}
							}
						)
						viewModel.updateSettings(newSettings)
					}
				)
			}
		}
	}
}

@Composable
private fun ProviderItem(
	provider: ProviderSetting,
	modifier: Modifier = Modifier,
	onEdit: (provider: ProviderSetting) -> Unit,
	onDelete: () -> Unit
) {
	var internalProvider by remember(provider) { mutableStateOf(provider) }
	var expand by remember { mutableStateOf(ProviderExpandState.None) }
	fun setExpand(state: ProviderExpandState) {
		expand = if (expand == state) {
			ProviderExpandState.None
		} else {
			state
		}
	}

	Card(
		modifier = modifier,
		colors = CardDefaults.cardColors(
			containerColor = if (provider.enabled) {
				MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
			} else MaterialTheme.colorScheme.errorContainer,
		),
	) {
		Column(
			modifier = Modifier.padding(16.dp)
				.animateContentSize(),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth(),
			) {
				AutoAIIcon(
					name = provider.name,
					modifier = Modifier.size(32.dp)
				)
				Column(
					modifier = Modifier.weight(1f)
				) {
					Text(
						text = provider.name,
						style = MaterialTheme.typography.titleMedium
					)
					Row(
						horizontalArrangement = Arrangement.spacedBy(4.dp)
					) {
						Tag(type = if (provider.enabled) TagType.SUCCESS else TagType.WARNING) {
							Text(text = if (provider.enabled) "Enabled" else "Disabled", style = MaterialTheme.typography.labelMedium)
						}
						Tag(type = TagType.INFO) {
							Text("${provider.models.size} Models", style = MaterialTheme.typography.labelMedium)
						}
					}
				}
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceAround,
			) {
				/* ToDo implement share later
					val shareSheetState = rememberShareSheetState()
					ShareSheet(shareSheetState)
					TextButton(
						onClick = {
							shareSheetState.show(provider)
						},
					) {
						Icon(
							imageVector = Lucide.Share,
							contentDescription = "Share",
							modifier = Modifier
								.padding(end = 8.dp)
								.size(16.dp)
						)
						Text("分享")
					}
				 */

				TextButton(
					onClick = {
						setExpand(ProviderExpandState.Models)
					},
					enabled = provider.enabled
				) {
					Icon(
						imageVector = Lucide.Boxes,
						contentDescription = "Models",
						modifier = Modifier
							.padding(end = 8.dp)
							.size(16.dp)
					)
					Text("Models", fontSize = 16.sp)
				}
				TextButton(
					onClick = {
						setExpand(ProviderExpandState.Setting)
					},
					enabled = provider.enabled
				) {
					Icon(
						imageVector = Lucide.Settings,
						contentDescription = "Settings",
						modifier = Modifier
							.padding(end = 8.dp)
							.size(16.dp)
					)
					Text("Settings", fontSize = 16.sp)
				}
			}
			when (expand) {
				ProviderExpandState.Setting -> {
					ProviderConfigure(
						providerSetting = internalProvider,
						modifier = Modifier.padding(8.dp),
						onEdit = { provider ->
							internalProvider = provider
						}
					)
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(12.dp)
					) {
						Spacer(Modifier.weight(1f))
						IconButton(
							onClick = {
								onDelete()
							}
						) {
							Icon(imageVector = Lucide.Trash2, "Delete")
						}
						Button(
							onClick = {
								onEdit(internalProvider)
								toaster.show("Save complete", ToastType.SUCCESS)
								expand = ProviderExpandState.None
							},
						) {
							Icon(imageVector = Lucide.Save, "Save")
							Text("Save")
						}
					}
				}

				ProviderExpandState.Models -> {
					ModelList(provider) { provider ->
						onEdit(provider)
					}
				}

				else -> {}
			}
		}
	}

}

private enum class ProviderExpandState {
	Setting,
	Models,
	None
}

// Model List
@Composable
private fun ModelList(
	providerSetting: ProviderSetting,
	onUpdateProvider: (ProviderSetting) -> Unit
) {
	val modelList by produceState(emptyList(), providerSetting) {
		runCatching {
			println("loading models...")
			value = ProviderManager.getProviderByType(providerSetting)
				.listModels(providerSetting)
				.sortedBy { it.modelId }
				.toImmutableList()
			println(value)
		}.onFailure {
			it.printStackTrace()
		}
	}

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(4.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		// 模型列表
		if (providerSetting.models.isEmpty()) {
			Column(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth(),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center
			) {
				Text(
					text = "No model was selected",
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				Text(
					text = "Click to Add model",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
				)
			}
		} else {
			providerSetting.models.forEach { model ->
				key(model.modelId) {
					ModelCard(
						model = model,
						onDelete = {
							onUpdateProvider(providerSetting.delModel(model))
						},
						onEdit = { editedModel ->
							onUpdateProvider(providerSetting.editModel(editedModel))
						},
					)
				}
			}
		}
		AddModelButton(modelList) {
			onUpdateProvider(providerSetting.addModel(it))
		}
	}
}

// Model Card
@Composable
private fun ModelCard(
	modifier: Modifier = Modifier,
	model: ModelFromProvider,
	onDelete: () -> Unit,
	onEdit: (provider: ModelFromProvider) -> Unit
) {
	val dialogState = useEditState<ModelFromProvider> {
		onEdit(it)
	}
	val swipeToDismissBoxState = rememberSwipeToDismissBoxState()
	val scope = rememberCoroutineScope()

	dialogState.EditStateContent { editingModel, updateEditingModel ->
		BasicAlertDialog(
			onDismissRequest = {
				dialogState.dismiss()
			}
		) {
			Text(
				"Edit Models"
			)
			Column(
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				OutlinedTextField(
					value = editingModel.modelId,
					onValueChange = {},
					label = {
						Text("Model ID")
					},
					modifier = Modifier.fillMaxWidth(),
					enabled = false
				)
				OutlinedTextField(
					value = editingModel.displayName,
					onValueChange = {
						updateEditingModel(editingModel.copy(displayName = it.trim()))
					},
					label = { Text("Model name") },
					modifier = Modifier.fillMaxWidth()
				)
				/*
				ModelTypeSelector(
										selectedType = editingModel.type,
										onTypeSelected = {
											updateEditingModel(editingModel.copy(type = it))
										}
									)
									ModelModalitySelector(
										inputModalities = editingModel.inputModalities,
										onUpdateInputModalities = {
											updateEditingModel(editingModel.copy(inputModalities = it))
										},
										outputModalities = editingModel.outputModalities,
										onUpdateOutputModalities = {
											updateEditingModel(editingModel.copy(outputModalities = it))
										}
									)
									ModalAbilitySelector(
										abilities = editingModel.abilities,
										onUpdateAbilities = {
											updateEditingModel(editingModel.copy(abilities = it))
										}
									)ModelTypeSelector(
										selectedType = editingModel.type,
										onTypeSelected = {
											updateEditingModel(editingModel.copy(type = it))
										}
									)
									ModelModalitySelector(
										inputModalities = editingModel.inputModalities,
										onUpdateInputModalities = {
											updateEditingModel(editingModel.copy(inputModalities = it))
										},
										outputModalities = editingModel.outputModalities,
										onUpdateOutputModalities = {
											updateEditingModel(editingModel.copy(outputModalities = it))
										}
									)
									ModalAbilitySelector(
										abilities = editingModel.abilities,
										onUpdateAbilities = {
											updateEditingModel(editingModel.copy(abilities = it))
										}
									)
				 */
				TextButton(
					onClick = {
						if (editingModel.displayName.isNotBlank()) {
							dialogState.confirm()
						}
					}
				) {
					Text("Confirm")
				}
				TextButton(
					onClick = {
						dialogState.dismiss()
					}
				) {
					Text("Cancel")
				}
			}
		}
		SwipeToDismissBox(
			state = swipeToDismissBoxState,
			backgroundContent = {
				Row(
					modifier = Modifier
						.fillMaxSize()
						.padding(8.dp),
					horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
					verticalAlignment = Alignment.CenterVertically
				) {
					IconButton(
						onClick = {
							scope.launch {
								swipeToDismissBoxState.reset()
							}
						}
					) {
						Icon(Lucide.X, null)
					}
					FilledIconButton(
						onClick = {
							scope.launch {
								onDelete()
								swipeToDismissBoxState.reset()
							}
						}
					) {
						Icon(Lucide.Trash2, contentDescription = "删除")
					}
				}
			},
			enableDismissFromStartToEnd = false,
			gesturesEnabled = true,
			modifier = modifier
		) {
			OutlinedCard {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 12.dp, vertical = 4.dp),
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					AutoAIIcon(
						name = model.modelId,
						modifier = Modifier.size(28.dp),
					)
					Column(modifier = Modifier.weight(1f)) {
						Text(
							text = model.modelId,
							style = MaterialTheme.typography.labelSmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
						)
						Text(
							text = model.displayName,
							style = MaterialTheme.typography.titleSmall,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
						/*
						Row(
							horizontalArrangement = Arrangement.spacedBy(4.dp),
						) {
							Tag(
								type = TagType.INFO
							) {
								Text(
									text = when (model.type) {
										ModelType.CHAT -> "Chat Model"
										ModelType.EMBEDDING -> "Embedding"
										ModelType.AUDIO -> "Audio"
										ModelType.IMAGE -> "Image"
									}
								)
							}
							Tag(
								type = TagType.SUCCESS
							) {
								Text(
									text = buildString {
										append(model.inputModalities.joinToString(",") { it.name.lowercase() })
										append("->")
										append(model.outputModalities.joinToString(",") { it.name.lowercase() })
									},
									maxLines = 1,
								)
							}
							if (model.abilities.contains(ModelAbility.TOOL)) {
								Tag(
									type = TagType.WARNING
								) {
									Icon(Lucide.Hammer, null, modifier = Modifier.size(14.dp))
								}
							}
						}
						 */
					}

					// Edit button
					IconButton(
						onClick = {
							dialogState.open(model.copy())
						}
					) {
						Icon(imageVector = Lucide.Pen, "Edit")
					}
				}
			}
		}
	}
}

@Composable
private fun AddModelButton(
	models: List<ModelFromProvider>,
	onAddModel: (ModelFromProvider) -> Unit
) {
	val dialogState = useEditState<ModelFromProvider> { onAddModel(it) }

	fun setModelId(id: String) {
//		val modality = guessModalityFromModelId(id)
//		val abilities = guessModelAbilityFromModelId(id)
		dialogState.currentState = dialogState.currentState?.copy(
			modelId = id,
			displayName = id.uppercase(),
			inputModalities = immutableListOf(Modality.TEXT),
//			outputModalities = modality.second,
//			abilities = abilities
		)
	}

	val modelPickerState = useEditState<ModelFromProvider?> { model ->
		model?.let {
			setModelId(it.modelId)
		}
	}

	Card(
		modifier = Modifier.fillMaxWidth(),
		onClick = {
			dialogState.open(ModelFromProvider())
		}
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(imageVector = Lucide.Plus, contentDescription = "Add Model")
			Spacer(modifier = Modifier.size(8.dp))
			Text("Adding a New Model", style = MaterialTheme.typography.bodyLarge)
		}
	}

	if (dialogState.isEditing) {
		dialogState.currentState?.let { modelState ->
			AlertDialog(
				onDismissRequest = {
					dialogState.dismiss()
				},
				title = {
					Text("Add Model")
				},
				text = {
					Column(
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						OutlinedTextField(
							value = modelState.modelId,
							onValueChange = {
								setModelId(it.trim())
							},
							label = { Text("Model ID") },
							modifier = Modifier.fillMaxWidth(),
							placeholder = {
								Text("For Example：gpt-3.5-turbo", modifier = Modifier.fillMaxWidth())
							},
							trailingIcon = {
								println("All models 1: ${models.size}")
								ModelPicker(modelPickerState, models)
							}
						)

						OutlinedTextField(
							value = modelState.displayName,
							onValueChange = { id ->
								dialogState.currentState = dialogState.currentState?.copy(
									displayName = id.replace(Regex("[-/]"), " ")
										.split(" ")
										.joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
								)
							},
							label = { Text("Model Display Name") },
							modifier = Modifier.fillMaxWidth(),
							placeholder = {
								Text("For example: GPT-3.5, used for UI display")
							}
						)


//						ModelTypeSelector(
//							selectedType = modelState.type,
//							onTypeSelected = {
//								dialogState.currentState = dialogState.currentState?.copy(
//									type = it
//								)
//							}
//						)
//
//						ModelModalitySelector(
//							inputModalities = modelState.inputModalities,
//							onUpdateInputModalities = {
//								dialogState.currentState = dialogState.currentState?.copy(
//									inputModalities = it
//								)
//							},
//							outputModalities = modelState.outputModalities,
//							onUpdateOutputModalities = {
//								dialogState.currentState = dialogState.currentState?.copy(
//									outputModalities = it
//								)
//							}
//						)
//
//						ModalAbilitySelector(
//							abilities = modelState.abilities,
//							onUpdateAbilities = {
//								dialogState.currentState = dialogState.currentState?.copy(
//									abilities = it
//								)
//							}
//						)
					}
				},
				confirmButton = {
					TextButton(
						onClick = {
							if (modelState.modelId.isNotBlank() && modelState.displayName.isNotBlank()) {
								dialogState.confirm()
							}
						},
					) {
						Text("Add")
					}
				},
				dismissButton = {
					TextButton(
						onClick = {
							dialogState.dismiss()
						},
					) {
						Text("Cancel")
					}
				}
			)
		}
	}
}

@Composable
private fun ModelPicker(
	modelListState: EditState<ModelFromProvider?>,
	models: List<ModelFromProvider>
) {
	println("All models: ${models.size}")
	if (modelListState.isEditing) {
		ModalBottomSheet(
			onDismissRequest = { modelListState.dismiss() },
			sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
		) {
			var filterText by remember { mutableStateOf("") }
			val filterKeywords = filterText.trimIndent().split(" ").filter { it.isNotBlank() }
			val filteredModels = models.fastFilter {
				if (filterKeywords.isEmpty()) {
					true
				} else {
					filterKeywords.all { keyword ->
						it.modelId.contains(keyword, ignoreCase = true) ||
								it.displayName.contains(keyword, ignoreCase = true)
					}
				}
			}
			Column(
				modifier = Modifier.fillMaxWidth()
					.height(500.dp)
					.padding(8.dp)
					.imePadding(),
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
				LazyColumn(
					modifier = Modifier.fillMaxWidth().weight(1f),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					items(filteredModels.toImmutableList(), key = { it.modelId }) { model ->
						Card(
							onClick = {
								modelListState.currentState = model.copy()
								modelListState.confirm()
							}
						) {
							Row(
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.spacedBy(8.dp),
								modifier = Modifier.fillMaxWidth().padding(8.dp)
							) {
								AutoAIIcon(
									model.modelId,
									Modifier.size(32.dp)
								)
								Column(
									verticalArrangement = Arrangement.spacedBy(
										4.dp
									),
								) {
									Text(
										text = model.modelId,
										style = MaterialTheme.typography.titleSmall,
									)
								}
							}
						}
					}
				}
				OutlinedTextField(
					value = filterText,
					onValueChange = {
						filterText = it
					},
					label = { Text("Search model") },
					modifier = Modifier.fillMaxWidth(),
					placeholder = {
						Text("Ex：GPT-3.5")
					},
				)
			}
		}
	}
	BadgedBox(
		modifier = Modifier.padding(4.dp),
		badge = {
			if (models.isNotEmpty()) {
				Badge {
					Text("${models.size}")
				}
			}
		}
	) {
		IconButton(
			onClick = {
				modelListState.open(null)
			}
		) {
			Icon(imageVector = Lucide.Boxes, null)
		}
	}
}