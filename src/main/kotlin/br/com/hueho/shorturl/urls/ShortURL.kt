package br.com.hueho.shorturl.urls

/**
 * Represents a stored URL
 *
 * @property id the stored ID, auto-generated
 * @property url the stored URL
 * @property visits the number of times the URL was visited
 */
data class ShortURL(val id: Long, val url: String, val visits: Long)
