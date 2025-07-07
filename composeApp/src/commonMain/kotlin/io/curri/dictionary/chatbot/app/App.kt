package io.curri.dictionary.chatbot.app

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import com.mohamedrejeb.calf.picker.coil.KmpFileFetcher
import io.curri.dictionary.chatbot.components.ui.context.LocalNavController
import io.curri.dictionary.chatbot.components.ui.context.LocalSharedTransitionScope
import io.curri.dictionary.chatbot.presentation.chat_page.ChatPage
import io.curri.dictionary.chatbot.presentation.conversation_list.ListConversationScreen
import io.curri.dictionary.chatbot.presentation.search_page.SearchPage
import io.curri.dictionary.chatbot.presentation.settings.SettingsScreen
import io.curri.dictionary.chatbot.presentation.settings.providers_page.SettingsProvider
import io.curri.dictionary.chatbot.theme.AppTheme
import io.curri.dictionary.chatbot.utils.newChat
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
@Preview
fun App() {
	KoinContext {
		AppTheme {
			val navController = rememberNavController()
			Column(
				modifier = Modifier
					.fillMaxSize().background(MaterialTheme.colorScheme.background),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				AppRoute(navController)
			}
			setSingletonImageLoaderFactory { context ->
				ImageLoader.Builder(context)
					.crossfade(true)
					.memoryCache {
						MemoryCache.Builder()
							.maxSizePercent(context, 0.25)
							.build()
					}
					.components {
						add(KmpFileFetcher.Factory())
					}
					.build()
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
					ChatPage(id = id,
						onOpenNewChat = {
							navHostController.newChat(Uuid.random().toString())
						})
				}

				composable<Screen.SettingsScreen> {
					SettingsScreen(
						onBackAction = {
							navHostController.popBackStack()
						},
						navController = navHostController
					)
				}
				composable<Screen.HomeScreen> {
					ListConversationScreen {
						navHostController.navigate(Screen.ChatPage(it.id))
					}
				}

				composable<Screen.ProviderConfigPage> {
					SettingsProvider() {
						navHostController.popBackStack()
					}
				}

				composable<Screen.SearchScreen> {
					SearchPage()
				}
			}
		}
	}
}


@Serializable
sealed interface Screen {
	@Serializable
	data class ChatPage(val id: String) : Screen

	@Serializable
	data object SettingsScreen : Screen

	@Serializable
	data object HomeScreen : Screen

	@Serializable
	data object ProviderConfigPage : Screen

	@Serializable
	data object SearchScreen : Screen
}