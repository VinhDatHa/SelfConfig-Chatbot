package io.curri.dictionary.chatbot.network.search

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.encodeURLParameter
import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.headers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object BingSearchService : SearchService, KoinComponent {
	override val name: String
		get() = "Bing Search"

	private val client: HttpClient by inject()

	override suspend fun search(query: String, options: SearchCommonOptions): Result<SearchResult> = withContext(Dispatchers.IO) {
		runCatching {
			val encodedQuery = query.encodeURLParameter()
			val url = "https://www.bing.com/search?q=$encodedQuery"


			// Fetch HTML using Ktor
			val html: String = client.get(url) {
				headers {
					append(HttpHeaders.UserAgent, "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.15 (KHTML, like Gecko) Chrome/24.0.1295.0 Safari/537.15")
					append(HttpHeaders.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
					append(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")
					append(HttpHeaders.AcceptEncoding, "gzip, deflate, sdch")
					append(HttpHeaders.Connection, "keep-alive")
					append(HttpHeaders.Referrer, "https://www.bing.com/")
					append(HttpHeaders.AcceptCharset, "utf-8")
					append(HttpHeaders.Cookie, "SRCHHPGUSR=LD=en-US")
				}
				timeout {
					requestTimeoutMillis = 30000
				}
			}.bodyAsText()

			// Parse HTML with Jsoup
			val doc= Ksoup.parse(html)

			val results = doc.select("li.b_algo").map { element ->
				val title = element.select("h2").text()
				val link = element.select("h2 > a").attr("href")
				val snippet = element.select(".b_caption p").text()

				SearchResult.SearchResultItem(
					title = title,
					url = link,
					text = snippet
				)
			}

			require(results.isNotEmpty()) {
				"Search failed: no results found"
			}

			SearchResult(results)
		}
	}
}