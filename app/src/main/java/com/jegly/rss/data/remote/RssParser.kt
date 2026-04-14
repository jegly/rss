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
            
            var title = ""; var link = ""; var description = ""; var pubDate = ""
            var isInsideItem = false
            
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val name = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (name.equals("item", true)) isInsideItem = true
                        else if (isInsideItem) {
                            when (name.lowercase()) {
                                "title" -> title = sanitize(parser.nextText())
                                "link" -> link = sanitize(parser.nextText())
                                "description" -> description = sanitize(parser.nextText())
                                "pubdate" -> pubDate = sanitize(parser.nextText())
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (name.equals("item", true)) {
                            articles.add(Article(title, link, pubDate, description))
                            isInsideItem = false
                            title = ""; link = ""; description = ""; pubDate = ""
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) { e.printStackTrace() }
        return articles
    }

    /**
     * Input Validation: Sanitize RSS content to prevent XSS or injection attacks.
     * Removes HTML tags and converts entities back to plain text.
     */
    private fun sanitize(input: String): String {
        return HtmlCompat.fromHtml(input, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim()
    }
}
