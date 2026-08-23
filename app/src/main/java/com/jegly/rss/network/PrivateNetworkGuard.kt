package com.jegly.rss.network

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

/**
 * Rejects URLs that resolve to loopback, link-local, site-local, or otherwise non-routable
 * addresses. Used by feed discovery to prevent the user-controlled URL field from being abused
 * as an SSRF / port-scan vector against the device's own LAN.
 */
object PrivateNetworkGuard {

    /** Allowed remote ports. Anything outside this set is rejected to block port-scan use. */
    private val ALLOWED_PORTS = setOf(80, 443, 8080, 8443)

    /**
     * Fast pre-filter only: returns true if every resolved address for [host] is routable and
     * the port is acceptable. This performs its own DNS resolution, independent of and earlier
     * than the resolution the real connection will use — it must NOT be relied on as the sole
     * enforcement point, since a second, later resolution (e.g. via DNS rebinding, where the
     * attacker's nameserver answers differently on each query) can return different addresses.
     * The authoritative check lives in [GuardedDns], which validates the exact addresses the
     * connection is about to use.
     */
    fun isAllowed(host: String, port: Int): Boolean {
        if (port != -1 && port !in ALLOWED_PORTS) return false
        return try {
            val addrs = InetAddress.getAllByName(host)
            addrs.isNotEmpty() && addrs.all { isPublicAddress(it) }
        } catch (_: Exception) {
            false
        }
    }

    /** Filters [addrs] down to the routable subset, preserving order. */
    fun filterPublic(addrs: List<InetAddress>): List<InetAddress> = addrs.filter { isPublicAddress(it) }

    private fun isPublicAddress(addr: InetAddress): Boolean {
        if (addr.isAnyLocalAddress) return false        // 0.0.0.0 / ::
        if (addr.isLoopbackAddress) return false        // 127.0.0.0/8, ::1
        if (addr.isLinkLocalAddress) return false       // 169.254/16, fe80::/10
        if (addr.isSiteLocalAddress) return false       // 10/8, 172.16/12, 192.168/16
        if (addr.isMulticastAddress) return false
        if (addr is Inet4Address) {
            val b = addr.address.map { it.toInt() and 0xff }
            // 100.64.0.0/10 — carrier-grade NAT
            if (b[0] == 100 && b[1] in 64..127) return false
            // 169.254.169.254 — AWS/GCP metadata service (sometimes routable)
            if (b[0] == 169 && b[1] == 254) return false
        }
        if (addr is Inet6Address) {
            val b = addr.address.map { it.toInt() and 0xff }
            // fc00::/7 — unique local addresses
            if ((b[0] and 0xfe) == 0xfc) return false
            // ::ffff:0:0/96 — IPv4-mapped (re-check the embedded IPv4)
            val isV4Mapped = b.take(10).all { it == 0 } && b[10] == 0xff && b[11] == 0xff
            if (isV4Mapped) {
                val mapped = Inet4Address.getByAddress(addr.address.copyOfRange(12, 16))
                return isPublicAddress(mapped)
            }
        }
        return true
    }
}
