package com.example.smartmedicinebox

import com.example.smartmedicinebox.domain.AccountRules
import com.example.smartmedicinebox.domain.AccountRules.PasswordRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AccountRulesTest {

    @Test
    fun strongPasswordMeetsEveryRule() {
        assertEquals(PasswordRule.entries.toSet(), AccountRules.passwordRulesMet("Sm@rtMed2026"))
        assertNull(AccountRules.passwordError("Sm@rtMed2026"))
        assertNull(AccountRules.passwordError("É1é-abcd")) // accented letters count
    }

    @Test
    fun eachMissingRuleIsDetected() {
        assertEquals(PasswordRule.entries.toSet() - PasswordRule.Length, AccountRules.passwordRulesMet("Ab1!"))
        assertEquals(PasswordRule.entries.toSet() - PasswordRule.Uppercase, AccountRules.passwordRulesMet("abcdef1!"))
        assertEquals(PasswordRule.entries.toSet() - PasswordRule.Lowercase, AccountRules.passwordRulesMet("ABCDEF1!"))
        assertEquals(PasswordRule.entries.toSet() - PasswordRule.Digit, AccountRules.passwordRulesMet("Abcdefg!"))
        assertEquals(PasswordRule.entries.toSet() - PasswordRule.Symbol, AccountRules.passwordRulesMet("Abcdefg1"))
        // A space is not a symbol.
        assertEquals(PasswordRule.entries.toSet() - PasswordRule.Symbol, AccountRules.passwordRulesMet("Abcd efg1"))
        assertEquals(emptySet<PasswordRule>(), AccountRules.passwordRulesMet(""))
        assertNotNull(AccountRules.passwordError("abcdefg1"))
    }

    @Test
    fun tooLongPasswordIsRejected() {
        assertNotNull(AccountRules.passwordError("Aa1!" + "x".repeat(AccountRules.PASSWORD_MAX_LENGTH)))
    }

    @Test
    fun emails() {
        listOf("sara@mail.com", "  Sara.B+meds@Example.co.uk ", "a_b-c@sub.domain.fr").forEach {
            assertNull(it, AccountRules.emailError(it))
        }
        listOf("", "sara", "sara@", "@mail.com", "sara@mail", "sara@mail.c", "sa ra@mail.com", "sara..b@mail.com", "sara@@mail.com")
            .forEach { assertNotNull(it, AccountRules.emailError(it)) }
        assertEquals("sara.b@example.com", AccountRules.normalizeEmail("  Sara.B@Example.COM "))
    }

    @Test
    fun usernames() {
        listOf("sara", "Sara_B", "s.b2026", "abc", "a".repeat(20)).forEach {
            assertNull(it, AccountRules.usernameError(it))
        }
        listOf("", "ab", "a".repeat(21), "1sara", "_sara", "sara b", "sara!", "sára@x").forEach {
            assertNotNull(it, AccountRules.usernameError(it))
        }
    }
}
