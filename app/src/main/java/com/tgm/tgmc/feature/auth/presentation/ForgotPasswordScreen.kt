package com.tgm.tgmc.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import com.tgm.tgmc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var emailSent by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClayBackground)
    ) {
        // Decorative orbs
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
                .padding(horizontal = 28.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Clay back button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clay(
                        backgroundColor = ClayCard,
                        cornerRadius = 22.dp,
                        elevation = 6.dp,
                        lightShadowColor = ClayShadowLight,
                        darkShadowColor = ClayShadowDark
                    )
                    .clip(CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ClayTextTitle)
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (!emailSent) {
                Text(
                    text = "Reset your password",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = ClayTextTitle,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter your account email and we'll send you a reset link.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = ClayTextBody)
                )
                Spacer(modifier = Modifier.height(36.dp))

                // Inset clay email input
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
                            Icon(Icons.Default.Email, contentDescription = null, tint = ClayPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            BasicTextField(
                                value = email,
                                onValueChange = { email = it; emailError = null },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ClayTextTitle, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                                cursorBrush = SolidColor(ClayPrimary),
                                decorationBox = { innerTextField ->
                                    if (email.isEmpty()) {
                                        Text("Email address", style = MaterialTheme.typography.bodyLarge.copy(color = ClayTextBody, fontSize = 16.sp))
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (emailError != null) {
                        Text(
                            text = emailError!!,
                            color = ClayAccent,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(start = 24.dp, top = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Send button
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
                        .clickable {
                            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Enter a valid email address"
                            } else {
                                isLoading = true
                                emailSent = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Send Reset Link",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = ClayWhite,
                            fontSize = 18.sp
                        )
                    )
                }
            } else {
                // Success state
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp))

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clay(
                                backgroundColor = ClayCard,
                                cornerRadius = 50.dp,
                                elevation = 14.dp,
                                lightShadowColor = ClayShadowLight,
                                darkShadowColor = ClayShadowDark
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MarkEmailRead,
                            contentDescription = null,
                            tint = ClayPrimary,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Check your email",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = ClayTextTitle,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "We've sent a password reset link to $email",
                        style = MaterialTheme.typography.bodyMedium.copy(color = ClayTextBody),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Box(
                        modifier = Modifier
                            .clay(
                                backgroundColor = ClayCard,
                                cornerRadius = 20.dp,
                                elevation = 8.dp,
                                lightShadowColor = ClayShadowLight,
                                darkShadowColor = ClayShadowDark
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onBack() }
                            .padding(horizontal = 28.dp, vertical = 14.dp)
                    ) {
                        Text(
                            "Back to Login",
                            color = ClayPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
