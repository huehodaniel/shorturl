package br.com.hueho.shorturl.urls

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Repository for URL retrieval
 */
interface ShortURLRepository {
    /**
     * Find a ShortURL by its id. May return null.
     *
     * @param id the id for the URL
     * @return the [ShortURL] object
     */
    fun findById(id: Long): ShortURL?

    /**
     * Saves a URL in the database. Does nothing if the URL was already shortened.
     *
     * @param url the URL to shorten
     * @return the newly created or already existing [ShortURL]
     */
    fun saveURL(url: String): ShortURL

    /**
     * Mark a visit for a ShortURL. Does nothing if the URL doesn't exist.
     *
     * @param id the id for the URL
     * @return true if the URL exists, false otherwise
     */
    fun countVisitById(id: Long): Boolean
}


/**
 * Default database-backed implementation for ShortURLRepository
 */
@Repository
@Qualifier("default")
class ShortURLRepositoryImpl(val jdbc: JdbcTemplate) : ShortURLRepository {
    override fun findById(id: Long): ShortURL? {
        val resultSet = jdbc.query("SELECT url, visits FROM shorturls WHERE id = ? LIMIT 1", arrayOf(id)) { rs, _ ->
            ShortURL(id, rs.getString(1), rs.getLong(2))
        }

        return if(resultSet.isNotEmpty()) {
            resultSet.first()
        } else null
    }

    @Retryable(DataIntegrityViolationException::class)
    @Transactional
    override fun saveURL(url: String): ShortURL {
        val resultSet = jdbc.query("SELECT id, visits FROM shorturls WHERE url = ? LIMIT 1", arrayOf<Any>(url)) { rs, _ ->
            ShortURL(rs.getLong(1), url, rs.getLong(2))
        }

        if(resultSet.isNotEmpty()) {
            return resultSet.first()
        }

        val keyHolder = GeneratedKeyHolder()
        jdbc.update({ con ->
            val stmt = con.prepareStatement("INSERT INTO shorturls(url) VALUES (?)", arrayOf("id"))
            stmt.setString(1, url)
            stmt
        }, keyHolder)

        val key = keyHolder.key
        if(key != null) {
            return ShortURL(key.toLong(), url, 0)
        } else {
            throw RuntimeException("Unknown error")
        }
    }

    override fun countVisitById(id: Long): Boolean {
        val affected = jdbc.update("UPDATE shorturls SET visits = visits + 1 WHERE id = ?", id)
        return affected > 0
    }
}

/**
 * In-memory test implementation for ShortURLRepository
 */
@Repository
@Qualifier("inmemory")
class ShortURLRepositoryInMemoryImpl : ShortURLRepository {
    private val backingMap: MutableMap<Long, ShortURL> = mutableMapOf()
    private val urlIndex: MutableMap<String, Long> = mutableMapOf()
    private val counter: AtomicLong = AtomicLong(0)
    private val lock: Lock = ReentrantLock()

    override fun saveURL(url: String): ShortURL {
        lock.withLock {
            val existing = urlIndex[url]
            if(existing != null) {
                return backingMap[existing]!!
            }

            val new = ShortURL(counter.incrementAndGet(), url, 0)
            backingMap[new.id] = new
            urlIndex[new.url] = new.id
            return new
        }
    }

    override fun countVisitById(id: Long): Boolean {
        lock.withLock {
            backingMap.compute(id) { _, shortUrl ->
                shortUrl?.copy(visits = shortUrl.visits + 1)
            }

            return backingMap.containsKey(id)
        }
    }

    override fun findById(id: Long): ShortURL? = lock.withLock { backingMap[id] }
}
