package com.jegly.rss.network

import okhttp3.Dns
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Wraps [delegate] and rejects non-routable addresses on the exact lookup OkHttp uses to open
 * the connection. A separate, earlier pre-check (e.g. [PrivateNetworkGuard.isAllowed] called
 * before issuing a request) cannot close a DNS-rebinding gap by itself, because it resolves the
 * host independently of — and before — the resolution the connection actually uses; an attacker
 * controlling authoritative DNS with a short TTL can answer the two lookups differently. Making
 * the guard part of the Dns implementation that OkHttp actually connects with means there is only
 * ever one resolution in the loop, closing that gap.
 */
class GuardedDns(private val delegate: Dns) : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val addrs = delegate.lookup(hostname)
        val safe = PrivateNetworkGuard.filterPublic(addrs)
        if (safe.isEmpty()) {
            throw UnknownHostException("$hostname resolves to a non-routable address; blocked by PrivateNetworkGuard")
        }
        return safe
    }
}
