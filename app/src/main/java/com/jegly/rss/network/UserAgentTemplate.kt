package com.jegly.rss.network

enum class UserAgentTemplate(val key: String, val displayName: String, val uaString: String) {
    DEFAULT(
        "default", "Default (WebView)", ""
    ),
    // Mobile
    CHROME_ANDROID(
        "chrome_android", "Chrome Android",
        "Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Mobile Safari/537.36"
    ),
    FIREFOX_ANDROID(
        "firefox_android", "Firefox Android",
        "Mozilla/5.0 (Android 15; Mobile; rv:127.0) Gecko/127.0 Firefox/127.0"
    ),
    SAMSUNG_INTERNET(
        "samsung_internet", "Samsung Internet",
        "Mozilla/5.0 (Linux; Android 15; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/27.0 Chrome/130.0.0.0 Mobile Safari/537.36"
    ),
    SAFARI_IOS(
        "safari_ios", "Safari iOS",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1"
    ),
    // Desktop
    DESKTOP_CHROME(
        "desktop_chrome", "Desktop Chrome",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36"
    ),
    DESKTOP_FIREFOX(
        "desktop_firefox", "Desktop Firefox",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:127.0) Gecko/20100101 Firefox/127.0"
    ),
    DESKTOP_SAFARI(
        "desktop_safari", "Desktop Safari",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Safari/605.1.15"
    ),
    DESKTOP_EDGE(
        "desktop_edge", "Desktop Edge",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Edg/136.0.0.0"
    ),
    // Special
    GOOGLEBOT(
        "googlebot", "Googlebot",
        "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
    ),
    STEALTH(
        "stealth", "Stealth (Minimal)",
        "Mozilla/5.0 (compatible; MSIE 9.0)"
    );

    companion object {
        fun fromKey(key: String?): UserAgentTemplate =
            entries.find { it.key == key } ?: DEFAULT
    }
}
