package com.example.smartmedicinebox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmedicinebox.domain.AccountRules
import com.example.smartmedicinebox.ui.AuthFormState
import com.example.smartmedicinebox.ui.components.FormLabel
import com.example.smartmedicinebox.ui.components.modernTextFieldColors
import com.example.smartmedicinebox.ui.theme.Bleu
import com.example.smartmedicinebox.ui.theme.BleuClair
import com.example.smartmedicinebox.ui.theme.Fond
import com.example.smartmedicinebox.ui.theme.Gris
import com.example.smartmedicinebox.ui.theme.Ivoire
import com.example.smartmedicinebox.ui.theme.Rouge
import com.example.smartmedicinebox.ui.theme.RougeClair
import com.example.smartmedicinebox.ui.theme.Texte
import com.example.smartmedicinebox.ui.theme.Vert
import com.example.smartmedicinebox.domain.ThemeMode
import com.example.smartmedicinebox.ui.components.ThemeModeSelector

private enum class AuthMode { Connexion, Inscription }

/** Login / sign-up, shown while nobody is logged in. */
@Composable
internal fun AuthScreen(
    form: AuthFormState,
    onLogIn: (identifier: String, password: String) -> Unit,
    onSignUp: (email: String, username: String, password: String, confirm: String) -> Unit,
    onClearError: () -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    var mode by rememberSaveable { mutableStateOf(AuthMode.Connexion) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fond)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Light / dark choice
        ThemeModeSelector(
            selected = themeMode,
            onSelect = onThemeModeChange,
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(Modifier.height(18.dp))

        // Brand
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Ivoire, shadowElevation = 3.dp) {
                Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                    Text("✚", color = Bleu, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("SmartMed", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Bleu)
                Text("Votre santé, notre priorité", fontSize = 12.sp, color = Gris)
            }
        }

        Spacer(Modifier.height(30.dp))

        Text(
            if (mode == AuthMode.Connexion) "Bon retour ! 👋" else "Créer un compte",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Texte
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (mode == AuthMode.Connexion)
                "Connectez-vous pour retrouver vos médicaments."
            else
                "Quelques informations pour sécuriser votre espace.",
            fontSize = 13.sp,
            color = Gris
        )

        Spacer(Modifier.height(20.dp))

        ModeSwitch(
            mode = mode,
            onChange = {
                if (!form.busy) {
                    mode = it
                    onClearError()
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(23.dp),
            colors = CardDefaults.cardColors(containerColor = Ivoire),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(Modifier.padding(19.dp)) {
                when (mode) {
                    AuthMode.Connexion -> LoginForm(form, onLogIn)
                    AuthMode.Inscription -> SignUpForm(form, onSignUp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "Votre compte est enregistré uniquement sur ce téléphone. " +
                "Votre mot de passe est chiffré et n'est jamais stocké en clair.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = Gris
        )
    }
}

@Composable
private fun ModeSwitch(mode: AuthMode, onChange: (AuthMode) -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = BleuClair) {
        Row(Modifier.fillMaxWidth().padding(4.dp)) {
            listOf(AuthMode.Connexion to "Connexion", AuthMode.Inscription to "Créer un compte")
                .forEach { (m, label) ->
                    val selected = m == mode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onChange(m) },
                        shape = RoundedCornerShape(13.dp),
                        color = if (selected) Ivoire else Color.Transparent,
                        shadowElevation = if (selected) 2.dp else 0.dp
                    ) {
                        Text(
                            label,
                            modifier = Modifier.padding(vertical = 11.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (selected) Bleu else Gris
                        )
                    }
                }
        }
    }
}

@Composable
private fun LoginForm(
    form: AuthFormState,
    onLogIn: (String, String) -> Unit
) {
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    FormLabel("E-mail ou nom d'utilisateur")
    Spacer(Modifier.height(7.dp))
    AuthTextField(
        value = identifier,
        onValueChange = { identifier = it },
        placeholder = "nom@exemple.com",
        keyboardType = KeyboardType.Email
    )

    Spacer(Modifier.height(15.dp))

    FormLabel("Mot de passe")
    Spacer(Modifier.height(7.dp))
    PasswordField(
        value = password,
        onValueChange = { password = it },
        imeAction = ImeAction.Done
    )

    ErrorBanner(form.error)

    Spacer(Modifier.height(20.dp))

    SubmitButton(
        text = "Se connecter",
        busy = form.busy,
        enabled = identifier.isNotBlank() && password.isNotEmpty(),
        onClick = { onLogIn(identifier, password) }
    )
}

@Composable
private fun SignUpForm(
    form: AuthFormState,
    onSignUp: (String, String, String, String) -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    val emailError = if (email.isBlank()) null else AccountRules.emailError(email)
    val usernameError = if (username.isBlank()) null else AccountRules.usernameError(username)
    val rulesMet = AccountRules.passwordRulesMet(password)
    val passwordOk = AccountRules.passwordError(password) == null
    val confirmOk = confirm.isNotEmpty() && confirm == password

    FormLabel("Adresse e-mail")
    Spacer(Modifier.height(7.dp))
    AuthTextField(
        value = email,
        onValueChange = { email = it },
        placeholder = "nom@exemple.com",
        keyboardType = KeyboardType.Email,
        isError = emailError != null
    )
    FieldHint(emailError)

    Spacer(Modifier.height(15.dp))

    FormLabel("Nom d'utilisateur")
    Spacer(Modifier.height(7.dp))
    AuthTextField(
        value = username,
        onValueChange = { username = it },
        placeholder = "Ex : sara_b",
        keyboardType = KeyboardType.Text,
        isError = usernameError != null
    )
    FieldHint(
        usernameError,
        fallback = "${AccountRules.USERNAME_MIN_LENGTH} à ${AccountRules.USERNAME_MAX_LENGTH} caractères : lettres, chiffres, « . » ou « _ »."
    )

    Spacer(Modifier.height(15.dp))

    FormLabel("Mot de passe")
    Spacer(Modifier.height(7.dp))
    PasswordField(
        value = password,
        onValueChange = { password = it },
        imeAction = ImeAction.Next
    )

    Spacer(Modifier.height(10.dp))

    Surface(shape = RoundedCornerShape(14.dp), color = BleuClair) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp)) {
            Text(
                "Votre mot de passe doit contenir :",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Texte
            )
            Spacer(Modifier.height(6.dp))
            AccountRules.PasswordRule.entries.forEach { rule ->
                RuleRow(rule.label, met = rule in rulesMet)
            }
        }
    }

    Spacer(Modifier.height(15.dp))

    FormLabel("Confirmer le mot de passe")
    Spacer(Modifier.height(7.dp))
    PasswordField(
        value = confirm,
        onValueChange = { confirm = it },
        imeAction = ImeAction.Done,
        isError = confirm.isNotEmpty() && !confirmOk
    )
    if (confirm.isNotEmpty()) {
        Spacer(Modifier.height(6.dp))
        RuleRow(
            if (confirmOk) "Les mots de passe correspondent" else "Les mots de passe ne correspondent pas",
            met = confirmOk
        )
    }

    ErrorBanner(form.error)

    Spacer(Modifier.height(20.dp))

    SubmitButton(
        text = "Créer mon compte",
        busy = form.busy,
        enabled = email.isNotBlank() && emailError == null &&
            username.isNotBlank() && usernameError == null &&
            passwordOk && confirmOk,
        onClick = { onSignUp(email, username, password, confirm) }
    )
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        singleLine = true,
        isError = isError,
        shape = RoundedCornerShape(14.dp),
        colors = modernTextFieldColors(),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction,
    isError: Boolean = false
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("••••••••") },
        singleLine = true,
        isError = isError,
        shape = RoundedCornerShape(14.dp),
        colors = modernTextFieldColors(),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false,
            imeAction = imeAction
        ),
        trailingIcon = {
            TextButton(onClick = { visible = !visible }) {
                Text(
                    if (visible) "Masquer" else "Afficher",
                    color = Bleu,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

@Composable
private fun RuleRow(text: String, met: Boolean) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (met) "✓" else "○",
            color = if (met) Vert else Gris,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(20.dp)
        )
        Text(
            text,
            color = if (met) Vert else Gris,
            fontSize = 12.sp,
            fontWeight = if (met) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun FieldHint(error: String?, fallback: String? = null) {
    val text = error ?: fallback ?: return
    Spacer(Modifier.height(5.dp))
    Text(
        text,
        color = if (error != null) Rouge else Gris,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )
}

@Composable
private fun ErrorBanner(error: String?) {
    if (error == null) return
    Spacer(Modifier.height(14.dp))
    Surface(shape = RoundedCornerShape(12.dp), color = RougeClair) {
        Text(
            error,
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            color = Rouge,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SubmitButton(
    text: String,
    busy: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !busy,
        modifier = Modifier
            .fillMaxWidth()
            .height(53.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Bleu)
    ) {
        if (busy) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
}
