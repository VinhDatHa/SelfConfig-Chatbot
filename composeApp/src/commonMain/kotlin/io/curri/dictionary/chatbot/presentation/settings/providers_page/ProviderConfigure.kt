package io.curri.dictionary.chatbot.presentation.settings.providers_page

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import io.curri.dictionary.chatbot.data.models.providers.ProviderSetting

@Composable
fun ProviderConfigure(
	modifier: Modifier = Modifier,
	providerSetting: ProviderSetting,
	onEdit: (provider: ProviderSetting) -> Unit
) {
	when (providerSetting) {
		is ProviderSetting.TogetherAiProvider -> {
			ProviderConfigure(provider = providerSetting) {
				println("After edit:$it")
				onEdit(it)
			}
		}

		else -> {
			ProviderConfigure(providerSetting as ProviderSetting.TogetherAiProvider) {
				onEdit(it)
			}
		}
	}
}


@Composable
private fun ProviderConfigure(
	provider: ProviderSetting.TogetherAiProvider,
	onConfig: (provider: ProviderSetting) -> Unit
) {

	Row(
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			"Configs", modifier = Modifier.weight(1f)
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
		visualTransformation = PasswordVisualTransformation()
	)
}