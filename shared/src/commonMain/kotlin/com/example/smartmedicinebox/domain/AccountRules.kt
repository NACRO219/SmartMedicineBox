package com.example.smartmedicinebox.domain

/** Rules for the sign-up form (shared by Android and iPhone). */
object AccountRules {

    const val PASSWORD_MIN_LENGTH = 8
    const val PASSWORD_MAX_LENGTH = 128
    const val USERNAME_MIN_LENGTH = 3
    const val USERNAME_MAX_LENGTH = 20

    /** The strong-password checklist shown under the password field. */
    enum class PasswordRule(val label: String) {
        Length("Au moins $PASSWORD_MIN_LENGTH caractères"),
        Uppercase("Une lettre majuscule"),
        Lowercase("Une lettre minuscule"),
        Digit("Un chiffre"),
        Symbol("Un symbole (ex : ! @ # ? -)")
    }

    fun passwordRulesMet(password: String): Set<PasswordRule> = buildSet {
        if (password.length >= PASSWORD_MIN_LENGTH) add(PasswordRule.Length)
        if (password.any { it.isUpperCase() }) add(PasswordRule.Uppercase)
        if (password.any { it.isLowerCase() }) add(PasswordRule.Lowercase)
        if (password.any { it.isDigit() }) add(PasswordRule.Digit)
        if (password.any { !it.isLetterOrDigit() && !it.isWhitespace() }) add(PasswordRule.Symbol)
    }

    fun passwordError(password: String): String? = when {
        password.length > PASSWORD_MAX_LENGTH ->
            "Le mot de passe ne doit pas dépasser $PASSWORD_MAX_LENGTH caractères."
        passwordRulesMet(password).size < PasswordRule.entries.size ->
            "Le mot de passe ne respecte pas toutes les règles."
        else -> null
    }

    fun normalizeEmail(email: String): String = email.trim().lowercase()

    private val emailPattern =
        Regex("^[a-z0-9._%+-]+@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]{2,}$")

    fun emailError(email: String): String? {
        val e = normalizeEmail(email)
        return when {
            e.isEmpty() -> "Veuillez saisir votre adresse e-mail."
            e.length > 254 || !emailPattern.matches(e) || ".." in e ->
                "Adresse e-mail invalide. Exemple : nom@exemple.com"
            else -> null
        }
    }

    private val usernamePattern = Regex("^[A-Za-z][A-Za-z0-9._]*$")

    fun usernameError(username: String): String? {
        val u = username.trim()
        return when {
            u.isEmpty() -> "Veuillez choisir un nom d'utilisateur."
            u.length < USERNAME_MIN_LENGTH || u.length > USERNAME_MAX_LENGTH ->
                "Le nom d'utilisateur doit contenir entre $USERNAME_MIN_LENGTH et $USERNAME_MAX_LENGTH caractères."
            !usernamePattern.matches(u) ->
                "Le nom d'utilisateur doit commencer par une lettre et ne contenir que des lettres, chiffres, « . » ou « _ »."
            else -> null
        }
    }
}
