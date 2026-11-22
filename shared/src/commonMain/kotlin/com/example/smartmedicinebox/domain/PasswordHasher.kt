package com.example.smartmedicinebox.domain

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Password hashing with PBKDF2-HMAC-SHA256 (RFC 8018), written in plain Kotlin
 * so the exact same code runs on Android and iPhone.
 *
 * Checked against the JDK's PBKDF2WithHmacSHA256 and the RFC 7914 test vector
 * in androidApp/src/test/.../PasswordHasherTest.kt.
 */
object PasswordHasher {

    /** Work factor for new passwords (stored per account, so it can be raised later). */
    const val ITERATIONS = 120_000

    private const val KEY_BYTES = 32
    private const val SALT_BYTES = 16

    class Hashed(val hashHex: String, val saltHex: String, val iterations: Int)

    fun hash(password: String, iterations: Int = ITERATIONS): Hashed {
        val salt = newSalt()
        val key = pbkdf2(password.encodeToByteArray(), salt, iterations, KEY_BYTES)
        return Hashed(key.toHex(), salt.toHex(), iterations)
    }

    fun verify(password: String, hashHex: String, saltHex: String, iterations: Int): Boolean {
        val expected = hashHex.hexToBytesOrNull() ?: return false
        val salt = saltHex.hexToBytesOrNull() ?: return false
        if (iterations <= 0) return false
        val actual = pbkdf2(password.encodeToByteArray(), salt, iterations, expected.size)
        return constantTimeEquals(actual, expected)
    }

    /** Cryptographically secure: Uuid.random() uses the platform's secure random generator. */
    @OptIn(ExperimentalUuidApi::class)
    private fun newSalt(): ByteArray = Uuid.random().toByteArray().copyOf(SALT_BYTES)

    fun pbkdf2(password: ByteArray, salt: ByteArray, iterations: Int, keyLength: Int): ByteArray {
        require(iterations > 0 && keyLength > 0)
        val mac = HmacSha256(password)
        val out = ByteArray(keyLength)
        var block = 1
        var offset = 0
        while (offset < keyLength) {
            // U1 = HMAC(P, salt || INT(block))
            var u = mac.compute(salt + byteArrayOf(
                (block ushr 24).toByte(), (block ushr 16).toByte(),
                (block ushr 8).toByte(), block.toByte()
            ))
            val t = u.copyOf()
            for (i in 2..iterations) {
                u = mac.compute(u)
                for (j in t.indices) t[j] = (t[j].toInt() xor u[j].toInt()).toByte()
            }
            val n = minOf(t.size, keyLength - offset)
            t.copyInto(out, offset, 0, n)
            offset += n
            block++
        }
        return out
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
        return diff == 0
    }

    internal fun ByteArray.toHex(): String {
        val digits = "0123456789abcdef"
        val sb = StringBuilder(size * 2)
        for (b in this) {
            val v = b.toInt() and 0xff
            sb.append(digits[v ushr 4]).append(digits[v and 0x0f])
        }
        return sb.toString()
    }

    internal fun String.hexToBytesOrNull(): ByteArray? {
        if (length % 2 != 0) return null
        val out = ByteArray(length / 2)
        for (i in out.indices) {
            val hi = this[2 * i].digitToIntOrNull(16) ?: return null
            val lo = this[2 * i + 1].digitToIntOrNull(16) ?: return null
            out[i] = ((hi shl 4) or lo).toByte()
        }
        return out
    }
}

/** HMAC-SHA256 (RFC 2104) with the key's inner/outer states precomputed for speed. */
internal class HmacSha256(key: ByteArray) {

    private val inner: Sha256
    private val outer: Sha256

    init {
        val k = (if (key.size > 64) Sha256().apply { update(key) }.digest() else key).copyOf(64)
        inner = Sha256().apply { update(ByteArray(64) { (k[it].toInt() xor 0x36).toByte() }) }
        outer = Sha256().apply { update(ByteArray(64) { (k[it].toInt() xor 0x5c).toByte() }) }
    }

    fun compute(message: ByteArray): ByteArray {
        val innerHash = inner.copy().apply { update(message) }.digest()
        return outer.copy().apply { update(innerHash) }.digest()
    }
}

/** SHA-256 (FIPS 180-4). */
internal class Sha256 private constructor(
    private val h: IntArray,
    private val buffer: ByteArray,
    private var bufferLength: Int,
    private var totalBytes: Long
) {
    constructor() : this(H0.copyOf(), ByteArray(64), 0, 0L)

    private val w = IntArray(64)

    fun copy(): Sha256 = Sha256(h.copyOf(), buffer.copyOf(), bufferLength, totalBytes)

    fun update(data: ByteArray) {
        totalBytes += data.size
        var i = 0
        while (i < data.size) {
            val n = minOf(64 - bufferLength, data.size - i)
            data.copyInto(buffer, bufferLength, i, i + n)
            bufferLength += n
            i += n
            if (bufferLength == 64) {
                compress(buffer)
                bufferLength = 0
            }
        }
    }

    /** Finishes the hash. The instance must not be used afterwards. */
    fun digest(): ByteArray {
        val bitLength = totalBytes * 8
        buffer[bufferLength++] = 0x80.toByte()
        if (bufferLength > 56) {
            buffer.fill(0, bufferLength, 64)
            compress(buffer)
            bufferLength = 0
        }
        buffer.fill(0, bufferLength, 56)
        for (i in 0 until 8) buffer[56 + i] = (bitLength ushr (56 - 8 * i)).toByte()
        compress(buffer)

        val out = ByteArray(32)
        for (i in 0 until 8) {
            out[4 * i] = (h[i] ushr 24).toByte()
            out[4 * i + 1] = (h[i] ushr 16).toByte()
            out[4 * i + 2] = (h[i] ushr 8).toByte()
            out[4 * i + 3] = h[i].toByte()
        }
        return out
    }

    private fun compress(block: ByteArray) {
        for (t in 0 until 16) {
            w[t] = ((block[4 * t].toInt() and 0xff) shl 24) or
                ((block[4 * t + 1].toInt() and 0xff) shl 16) or
                ((block[4 * t + 2].toInt() and 0xff) shl 8) or
                (block[4 * t + 3].toInt() and 0xff)
        }
        for (t in 16 until 64) {
            val s0 = w[t - 15].rotateRight(7) xor w[t - 15].rotateRight(18) xor (w[t - 15] ushr 3)
            val s1 = w[t - 2].rotateRight(17) xor w[t - 2].rotateRight(19) xor (w[t - 2] ushr 10)
            w[t] = w[t - 16] + s0 + w[t - 7] + s1
        }

        var a = h[0]; var b = h[1]; var c = h[2]; var d = h[3]
        var e = h[4]; var f = h[5]; var g = h[6]; var hh = h[7]

        for (t in 0 until 64) {
            val s1 = e.rotateRight(6) xor e.rotateRight(11) xor e.rotateRight(25)
            val ch = (e and f) xor (e.inv() and g)
            val temp1 = hh + s1 + ch + K[t] + w[t]
            val s0 = a.rotateRight(2) xor a.rotateRight(13) xor a.rotateRight(22)
            val maj = (a and b) xor (a and c) xor (b and c)
            val temp2 = s0 + maj
            hh = g; g = f; f = e; e = d + temp1
            d = c; c = b; b = a; a = temp1 + temp2
        }

        h[0] += a; h[1] += b; h[2] += c; h[3] += d
        h[4] += e; h[5] += f; h[6] += g; h[7] += hh
    }

    private companion object {
        val H0 = longArrayOf(
            0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
            0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
        ).map { it.toInt() }.toIntArray()

        val K = longArrayOf(
            0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
            0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
            0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
            0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
            0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
            0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
            0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
            0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
        ).map { it.toInt() }.toIntArray()
    }
}
