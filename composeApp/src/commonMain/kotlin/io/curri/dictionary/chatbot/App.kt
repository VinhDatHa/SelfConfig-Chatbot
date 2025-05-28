package io.curri.dictionary.chatbot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.curri.dictionary.chatbot.components.ui.context.LocalNavController
import io.curri.dictionary.chatbot.components.ui.context.LocalSharedTransitionScope
import io.curri.dictionary.chatbot.presentation.chat_page.ChatPage
import io.curri.dictionary.chatbot.presentation.conversation_list.ListConversationScreen
import io.curri.dictionary.chatbot.presentation.settings.SettingsScreen
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
@Preview
fun App() {
	MaterialTheme {
		var showContent by remember { mutableStateOf(false) }
		var navController = rememberNavController()
		Column(
			modifier = Modifier
				.safeContentPadding()
				.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Button(onClick = { showContent = !showContent }) {
				Text("Click me!")
			}
			AnimatedVisibility(showContent) {
				val greeting = remember { Greeting().greet() }
//                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//                    Image(painterResource(Res.drawable.compose_multiplatform), null)
//                    Text("Compose: $greeting")
//                    ChatPage("Test")
//                }
//				ChatPage("Test")
				AppRoute(navController)
			}
		}
	}
}


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalUuidApi::class)
@Composable
private fun AppRoute(navHostController: NavHostController) {
	SharedTransitionLayout {
		CompositionLocalProvider(
			LocalNavController provides navHostController,
			LocalSharedTransitionScope provides this
		) {
			val enterTransition = scaleIn(initialScale = 0.35f) + fadeIn(animationSpec = tween(300))
			val exitTransition = fadeOut(animationSpec = tween(300))
			val popEnterTransition = fadeIn(animationSpec = tween(300))
			val popExitTransition = scaleOut(targetScale = 0.35f) + fadeOut(animationSpec = tween(300))
			NavHost(
				modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
				navController = navHostController,
				startDestination = Screen.HomeScreen,
				enterTransition = {
					scaleIn(initialScale = 0.35f) + fadeIn(animationSpec = tween(300))
				},
				exitTransition = {
					fadeOut(animationSpec = tween(300))
				},
				popEnterTransition = {
					fadeIn(animationSpec = tween(300))
				},
				popExitTransition = {
					scaleOut(targetScale = 0.35f) + fadeOut(animationSpec = tween(300))
				}
			) {
				composable<Screen.ChatPage>(
					enterTransition = {
						enterTransition
					},
					exitTransition = { exitTransition },
					popEnterTransition = { popEnterTransition },
					popExitTransition = { popExitTransition }
				) {
					val id = it.toRoute<Screen.ChatPage>().id
					ChatPage(id)
				}

				composable<Screen.SettingsScreen> {
					SettingsScreen {
						navHostController.navigate(Screen.ChatPage("TOOL"))
					}
				}
				composable<Screen.HomeScreen> {
					ListConversationScreen {
						navHostController.navigate(Screen.ChatPage(it.id))
					}
				}
			}
		}
	}
}

//}

@Serializable
sealed interface Screen {
	@Serializable
	data class ChatPage(val id: String) : Screen {

	}

	@Serializable
	data object SettingsScreen : Screen

	@Serializable
	data object HomeScreen : Screen
}