package com.jegly.rss.network

import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress

enum class DohProvider(val key: String, val displayName: String, val dohUrl: String, val bootstrap: List<String>) {
    CLOUDFLARE(
        key = "cloudflare",
        displayName = "Cloudflare (1.1.1.1)",
        dohUrl = "https://cloudflare-dns.com/dns-query",
        bootstrap = listOf("1.1.1.1", "1.0.0.1", "2606:4700:4700::1111", "2606:4700:4700::1001")
    ),
    QUAD9(
        key = "quad9",
        displayName = "Quad9 (9.9.9.9)",
        dohUrl = "https://dns.quad9.net/dns-query",
        bootstrap = listOf("9.9.9.9", "149.112.112.112", "2620:fe::fe", "2620:fe::9")
    ),
    GOOGLE(
        key = "google",
        displayName = "Google (8.8.8.8)",
        dohUrl = "https://dns.google/dns-query",
        bootstrap = listOf("8.8.8.8", "8.8.4.4", "2001:4860:4860::8888", "2001:4860:4860::8844")
    );

    companion object {
        fun fromKey(key: String?): DohProvider =
            values().firstOrNull { it.key == key } ?: CLOUDFLARE
    }
}

/**
 * Single OkHttp Dns implementation backed by user-selectable DoH provider. The choice is read
 * from EncryptedSharedPreferences on every lookup, so flipping the setting takes effect
 * immediately without recreating the main OkHttp client.
 *
 * A bootstrap OkHttpClient (no DoH, system DNS) is required to make the DoH POST/GET request
 * itself — chicken-and-egg avoidance.
 */
class SwitchableDohDns(
    bootstrapClient: OkHttpClient,
    private val providerKeyLookup: () -> String?
) : Dns {

    private val resolvers: Map<DohProvider, Dns> = DohProvider.values().associateWith { p ->
        DnsOverHttps.Builder()
            .client(bootstrapClient)
            .url(p.dohUrl.toHttpUrl())
            .bootstrapDnsHosts(p.bootstrap.map { InetAddress.getByName(it) })
            .includeIPv6(true)
            .post(true)        // POST avoids URL caching of DNS queries by intermediates
            .build()
    }

    override fun lookup(hostname: String): List<InetAddress> {
        val provider = DohProvider.fromKey(providerKeyLookup())
        val resolver = resolvers[provider] ?: return Dns.SYSTEM.lookup(hostname)
        return try {
            resolver.lookup(hostname)
        } catch (e: Exception) {
            // If DoH provider is unreachable (captive portal, blocked) fall back to system DNS
            // rather than break all networking. The cleartext block at network-security-config
            // still prevents leaking sensitive content over plain HTTP.
            Dns.SYSTEM.lookup(hostname)
        }
    }
}
