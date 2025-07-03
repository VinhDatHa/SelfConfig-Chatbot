package io.curri.dictionary.chatbot.network.search

import kotlinx.serialization.Serializable

interface SearchService {
	val name: String
	suspend fun search(
		query: String,
		options: SearchCommonOptions
	): Result<SearchResult>
}

@Serializable
data class SearchCommonOptions(
	val resultSize: Int = 10
)

@Serializable
data class SearchResult(
	val items: List<SearchResultItem>,
) {
	@Serializable
	data class SearchResultItem(
		val title: String,
		val url: String,
		val text: String,
	)
}