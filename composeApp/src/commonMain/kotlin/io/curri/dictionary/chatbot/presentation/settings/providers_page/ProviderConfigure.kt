package io.curri.dictionary.chatbot.presentation.settings.providers_page

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lucide
import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting

@Composable
fun ProviderConfiguration(
	modifier: Modifier = Modifier,
	providerSetting: ProviderSetting,
	onEdit: (provider: ProviderSetting) -> Unit
) {
	if (providerSetting is ProviderSetting.TogetherAiProvider) {
		TogetherAiConfigure(provider = providerSetting) {
			onEdit(it)
		}
	}

	if (providerSetting is ProviderSetting.OpenAiProvider) {
		ProviderConfigureOpenAI(provider = providerSetting) {
			onEdit(it)
		}
	}
}


@Composable
private fun TogetherAiConfigure(
	provider: ProviderSetting.TogetherAiProvider,
	onConfig: (provider: ProviderSetting) -> Unit
) {
	var isShowingKey by remember { mutableStateOf(false) }
	val visualTransformation by remember(isShowingKey) {
		derivedStateOf {
			if (isShowingKey) VisualTransformation.None else PasswordVisualTransformation()
		}
	}
	Row(
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			"Configs", modifier = Modifier.weight(1f)
		)
		Checkbox(
			checked = provider.enabled,
			onCheckedChange = {
				onConfig(provider.copy(enabled = it))
			}
		)
	}
	OutlinedTextField(
		value = provider.name,
		readOnly = true,
		onValueChange = {},
		label = {
			Text("Name")
		},
		modifier = Modifier.fillMaxWidth()
	)

	OutlinedTextField(
		value = provider.baseUrl,
		onValueChange = {
			onConfig(provider.copy(baseUrl = it))
		},
		label = { Text("Base URL") },
		modifier = Modifier.fillMaxWidth()
	)

	OutlinedTextField(
		value = provider.apiKey,
		onValueChange = {
			onConfig(provider.copy(apiKey = it))
		},
		label = { Text("API Key") },
		modifier = Modifier.fillMaxWidth(),
		visualTransformation = visualTransformation,
		trailingIcon = {
			IconButton(
				onClick = {
					isShowingKey = !isShowingKey
				}
			) {
				Icon(
					imageVector = if (isShowingKey) Lucide.EyeOff else Lucide.Eye,
					contentDescription = "Showing key"
				)
			}
		}
	)
}

@Composable
private fun ProviderConfigureOpenAI(
	provider: ProviderSetting.OpenAiProvider,
	onConfig: (provider: ProviderSetting.OpenAiProvider) -> Unit
) {
	var isShowingKey by remember { mutableStateOf(false) }
	val visualTransformation by remember(isShowingKey) {
		derivedStateOf {
			if (isShowingKey) VisualTransformation.None else PasswordVisualTransformation()
		}
	}
	Row(
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			"Configs", modifier = Modifier.weight(1f)
		)
		Checkbox(
			checked = provider.enabled,
			onCheckedChange = {
				onConfig(provider.copy(enabled = it))
			}
		)
	}
	OutlinedTextField(
		value = provider.name,
		readOnly = true,
		onValueChange = {},
		label = {
			Text("Name")
		},
		modifier = Modifier.fillMaxWidth()
	)

	OutlinedTextField(
		value = provider.baseUrl,
		onValueChange = {
			onConfig(provider.copy(baseUrl = it))
		},
		label = { Text("Base URL") },
		modifier = Modifier.fillMaxWidth()
	)

	OutlinedTextField(
		value = provider.apiKey,
		onValueChange = {
			onConfig(provider.copy(apiKey = it))
		},
		label = { Text("API Key") },
		modifier = Modifier.fillMaxWidth(),
		visualTransformation = visualTransformation,
		trailingIcon = {
			IconButton(
				onClick = {
					isShowingKey = !isShowingKey
				}
			) {
				Icon(
					imageVector = if (isShowingKey) Lucide.EyeOff else Lucide.Eye,
					contentDescription = "Showing key"
				)
			}
		}
	)
}