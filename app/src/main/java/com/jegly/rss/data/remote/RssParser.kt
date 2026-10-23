package com.jegly.rss.data.remote

import android.util.Xml
import com.jegly.rss.domain.model.Article
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import javax.inject.Inject
import androidx.core.text.HtmlCompat

class RssParser @Inject constructor() {

    fun parse(inputStream: InputStream): List<Article> {
        val articles = mutableListOf<Article>()
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)

            var title = ""; var link = ""; var description = ""; var content = ""; var pubDate = ""
            var isInsideItem = false

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT && articles.size < MAX_ITEMS) {
                val name = parser.name
                when (eventType) {
                    // RSS wraps each article in <item>; Atom (e.g. Reddit's .rss) uses <entry>.
                    XmlPullParser.START_TAG -> {
                        if (name.equals("item", true) || name.equals("entry", true)) {
                            isInsideItem = true
                        } else if (isInsideItem) {
                            when (name.lowercase()) {
                                "title" -> title = sanitize(parser.nextText(), MAX_TITLE)
                                "link" -> {
                                    // Atom: <link href="..." rel="alternate"/> (link is an attribute).
                                    // RSS:  <link>https://...</link> (link is element text).
                                    val href = parser.getAttributeValue(null, "href")
                                    if (href != null) {
                                        val rel = parser.getAttributeValue(null, "rel") ?: "alternate"
                                        if (rel.equals("alternate", true)) link = sanitize(href, MAX_LINK)
                                    } else {
                                        link = sanitize(parser.nextText(), MAX_LINK)
                                    }
                                }
                                // RSS <description>; Atom <summary>.
                                "description", "summary" -> description = sanitizeBody(parser.nextText())
                                // Full article body: RSS <content:encoded> (namespaces are off, so the
                                // prefix stays in the name); Atom <content>. Atom type="xhtml" holds child
                                // elements, which nextText() can't read, so it's skipped.
                                "content:encoded" -> content = sanitizeBody(parser.nextText())
                                "content" -> if (!"xhtml".equals(parser.getAttributeValue(null, "type"), true)) {
                                    content = sanitizeBody(parser.nextText())
                                }
                                // RSS <pubDate>; Atom <published>/<updated>.
                                "pubdate", "published" -> pubDate = sanitize(parser.nextText(), MAX_DATE)
                                "updated" -> if (pubDate.isEmpty()) pubDate = sanitize(parser.nextText(), MAX_DATE)
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (name.equals("item", true) || name.equals("entry", true)) {
                            // Prefer the full body so the text-only reader shows the whole article.
                            articles.add(Article(title, link, pubDate, content.ifEmpty { description }))
                            isInsideItem = false
                            title = ""; link = ""; description = ""; content = ""; pubDate = ""
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) { /* malformed feed: return whatever we parsed so far */ }
        return articles
    }

    /**
     * Strip HTML to plain text, then truncate. The cap defends against billion-laughs entity
     * expansion (KXmlParser expands internal entities even though it doesn't fetch external ones).
     */
    private fun sanitize(input: String, maxLen: Int): String {
        val clipped = if (input.length > maxLen * 2) input.substring(0, maxLen * 2) else input
        return HtmlCompat.fromHtml(clipped, HtmlCompat.FROM_HTML_MODE_COMPACT)
            .toString()
            .trim()
            .take(maxLen)
    }

    /**
     * Like [sanitize] but keeps paragraph breaks (LEGACY mode puts a blank line between block
     * elements) and drops the U+FFFC placeholders left where <img> tags were.
     */
    private fun sanitizeBody(input: String): String {
        val clipped = if (input.length > MAX_DESCRIPTION * 2) input.substring(0, MAX_DESCRIPTION * 2) else input
        return HtmlCompat.fromHtml(clipped, HtmlCompat.FROM_HTML_MODE_LEGACY)
            .toString()
            .replace("\uFFFC", "")
            .replace(Regex("[ \\t]+\n"), "\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
            .take(MAX_DESCRIPTION)
    }

    private companion object {
        const val MAX_ITEMS = 500
        const val MAX_TITLE = 500
        const val MAX_LINK = 2048
        const val MAX_DESCRIPTION = 50_000
        const val MAX_DATE = 64
    }
}
