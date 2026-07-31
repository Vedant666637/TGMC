package com.tgm.tgmc.feature.auth.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.tgm.tgmc.R
import com.tgm.tgmc.core.domain.model.UserRole
import com.tgm.tgmc.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    onForgotPassword: () -> Unit,
    onChildPairClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isLoggedIn, uiState.userRole) {
        if (uiState.isLoggedIn && uiState.userRole != UserRole.NONE) {
            onLoginSuccess(uiState.userRole)
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClayBackground)
    ) {
        // Decorative 3D Orbs (Claymorphism hallmark)
        Box(
            modifier = Modifier
                .offset(x = (-40).dp, y = (-40).dp)
                .size(200.dp)
                .clay(
                    backgroundColor = ClayBackground,
                    cornerRadius = 100.dp,
                    elevation = 20.dp,
                    lightShadowColor = ClayShadowLight,
                    darkShadowColor = ClayShadowDark
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 80.dp)
                .size(250.dp)
                .clay(
                    backgroundColor = ClayBackground,
                    cornerRadius = 125.dp,
                    elevation = 30.dp,
                    lightShadowColor = ClayShadowLight,
                    darkShadowColor = ClayShadowDark
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Language Selector
            LanguageSelector(modifier = Modifier.align(Alignment.End))

            Spacer(modifier = Modifier.height(16.dp))

            // Logo Header
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clay(
                        backgroundColor = ClayCard,
                        cornerRadius = 35.dp,
                        elevation = 16.dp,
                        lightShadowColor = ClayShadowLight,
                        darkShadowColor = ClayShadowDark
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = ClayPrimary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.login_welcome),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = ClayTextTitle,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge.copy(color = ClayTextBody)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Professional Inset Email Field
            ClayInputField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                hint = stringResource(id = R.string.email_hint),
                icon = Icons.Default.Email,
                error = uiState.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Professional Inset Password Field
            var passwordVisible by remember { mutableStateOf(false) }
            ClayInputField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                hint = stringResource(id = R.string.password_hint),
                icon = Icons.Default.Lock,
                isPassword = !passwordVisible,
                error = uiState.passwordError,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = ClayTextBody
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.login()
                })
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Forgot password
            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(stringResource(id = R.string.forgot_password), color = ClayPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Error banner
            AnimatedVisibility(visible = uiState.error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Login button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clay(
                        backgroundColor = ClayPrimary,
                        cornerRadius = 24.dp,
                        elevation = 12.dp,
                        lightShadowColor = ClayPrimary.copy(alpha = 0.6f),
                        darkShadowColor = ClayShadowDark
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(enabled = !uiState.isLoading, onClick = viewModel::login),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = ClayWhite, strokeWidth = 3.dp)
                } else {
                    Text(
                        text = stringResource(id = R.string.sign_in_button),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = ClayWhite,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }



            // Sign Up Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(id = R.string.no_account_text), style = MaterialTheme.typography.bodyMedium.copy(color = ClayTextBody))
                Text(
                    text = stringResource(id = R.string.sign_up_button),
                    modifier = Modifier
                        .clickable(onClick = onNavigateToRegister)
                        .padding(8.dp),
                    color = ClayPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }


            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun ClayInputField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    error: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .insetClay(
                    backgroundColor = ClayCard,
                    cornerRadius = 24.dp,
                    elevation = 8.dp,
                    lightShadowColor = ClayShadowLight,
                    darkShadowColor = ClayShadowDark
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = ClayPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = ClayTextTitle, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    cursorBrush = SolidColor(ClayPrimary),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(hint, style = MaterialTheme.typography.bodyLarge.copy(color = ClayTextBody, fontSize = 16.sp))
                        }
                        innerTextField()
                    },
                    modifier = Modifier.weight(1f)
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 24.dp, top = 8.dp)
            )
        }
    }
}

@Composable
fun tgmcTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
    focusedLabelColor = ClayPrimary,
    unfocusedLabelColor = ClayTextBody,
    cursorColor = ClayPrimary,
    focusedLeadingIconColor = ClayPrimary,
    unfocusedLeadingIconColor = ClayTextBody,
    focusedTextColor = ClayTextTitle,
    unfocusedTextColor = ClayTextBody,
    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
)

@Composable
fun LanguageSelector(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Box(modifier = modifier) {
        // Pill Button
        Box(
            modifier = Modifier
                .height(48.dp)
                .clay(
                    backgroundColor = ClayCard,
                    cornerRadius = 24.dp,
                    elevation = 4.dp,
                    lightShadowColor = ClayShadowLight,
                    darkShadowColor = ClayShadowDark
                )
                .clip(RoundedCornerShape(24.dp))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = null, tint = ClayPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.language_select),
                    style = MaterialTheme.typography.labelLarge.copy(color = ClayTextTitle, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = ClayTextBody, modifier = Modifier.size(20.dp))
            }
        }
        
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(surface = ClayCard)
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(ClayCard)
            ) {
                DropdownMenuItem(
                    text = { Text("English", color = ClayTextTitle, fontWeight = FontWeight.Bold) }, 
                    onClick = { changeLanguage(context, "en"); expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("हिंदी (Hindi)", color = ClayTextTitle, fontWeight = FontWeight.Bold) }, 
                    onClick = { changeLanguage(context, "hi"); expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("मराठी (Marathi)", color = ClayTextTitle, fontWeight = FontWeight.Bold) }, 
                    onClick = { changeLanguage(context, "mr"); expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Русский (Russian)", color = ClayTextTitle, fontWeight = FontWeight.Bold) }, 
                    onClick = { changeLanguage(context, "ru"); expanded = false }
                )
            }
        }
    }
}

fun changeLanguage(context: Context, languageCode: String) {
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
            localeManager.applicationLocales = android.os.LocaleList.forLanguageTags(languageCode)
        } else {
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                androidx.core.os.LocaleListCompat.forLanguageTags(languageCode)
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
