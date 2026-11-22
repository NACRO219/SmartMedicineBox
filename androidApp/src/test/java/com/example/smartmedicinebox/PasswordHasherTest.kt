package com.example.smartmedicinebox

import com.example.smartmedicinebox.domain.PasswordHasher
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Checks the shared (plain Kotlin) PBKDF2-HMAC-SHA256 against the JDK's implementation. */
class PasswordHasherTest {

    private fun jdkPbkdf2(password: String, salt: ByteArray, iterations: Int, bytes: Int): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(password.toCharArray(), salt, iterations, bytes * 8))
            .encoded

    private fun hex(s: String) = s.replace(" ", "").chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    @Test
    fun rfc7914TestVector() {
        val expected = hex(
            "55 ac 04 6e 56 e3 08 9f ec 16 91 c2 25 44 b6 05 f9 41 85 21 6d de 04 65 e6 8b 9d 57 c2 0d ac bc " +
                "49 ca 9c cc f1 79 b6 45 99 16 64 b3 9d 77 ef 31 7c 71 b8 45 b1 e3 0b d5 09 11 20 41 d3 a1 97 83"
        )
        val actual = PasswordHasher.pbkdf2("passwd".encodeToByteArray(), "salt".encodeToByteArray(), 1, 64)
        assertArrayEquals(expected, actual)
    }

    @Test
    fun matchesJdkForManyPasswordAndSaltLengths() {
        // Covers SHA-256 padding edge cases (55/56/63/64/65-byte inputs),
        // keys longer than the 64-byte HMAC block, and multi-block output.
        val passwords = listOf("a", "Password1!", "x".repeat(63), "y".repeat(64), "z".repeat(65), "P@ss".repeat(40))
        val saltLengths = listOf(1, 16, 51, 52, 55, 56, 60, 64, 65, 130)
        for (pw in passwords) for (len in saltLengths) {
            val salt = ByteArray(len) { (it * 31 + 7).toByte() }
            for ((iterations, bytes) in listOf(1 to 32, 3 to 32, 2 to 80)) {
                assertArrayEquals(
                    "pw=${pw.length} salt=$len it=$iterations bytes=$bytes",
                    jdkPbkdf2(pw, salt, iterations, bytes),
                    PasswordHasher.pbkdf2(pw.encodeToByteArray(), salt, iterations, bytes)
                )
            }
        }
    }

    @Test
    fun matchesJdkWithRealIterationCount() {
        val salt = ByteArray(16) { it.toByte() }
        assertArrayEquals(
            jdkPbkdf2("Sm@rtMed2026", salt, PasswordHasher.ITERATIONS, 32),
            PasswordHasher.pbkdf2("Sm@rtMed2026".encodeToByteArray(), salt, PasswordHasher.ITERATIONS, 32)
        )
    }

    @Test
    fun hashThenVerify() {
        val h = PasswordHasher.hash("Sm@rtMed2026", iterations = 1_000)
        assertEquals(64, h.hashHex.length)   // 32 bytes
        assertEquals(32, h.saltHex.length)   // 16 bytes
        assertTrue(PasswordHasher.verify("Sm@rtMed2026", h.hashHex, h.saltHex, h.iterations))
        assertFalse(PasswordHasher.verify("sm@rtmed2026", h.hashHex, h.saltHex, h.iterations))
        assertFalse(PasswordHasher.verify("Sm@rtMed2026 ", h.hashHex, h.saltHex, h.iterations))
        assertFalse(PasswordHasher.verify("", h.hashHex, h.saltHex, h.iterations))
    }

    @Test
    fun samePasswordGetsDifferentSaltAndHash() {
        val a = PasswordHasher.hash("Sm@rtMed2026", iterations = 1_000)
        val b = PasswordHasher.hash("Sm@rtMed2026", iterations = 1_000)
        assertNotEquals(a.saltHex, b.saltHex)
        assertNotEquals(a.hashHex, b.hashHex)
    }

    @Test
    fun corruptStoredValuesNeverVerify() {
        assertFalse(PasswordHasher.verify("x", "zz", "00", 1_000))
        assertFalse(PasswordHasher.verify("x", "abc", "00", 1_000))
        assertFalse(PasswordHasher.verify("x", "00", "00", 0))
    }
}
