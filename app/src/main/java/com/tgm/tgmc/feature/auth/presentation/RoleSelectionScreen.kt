package com.tgm.tgmc.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tgm.tgmc.core.domain.model.UserRole
import com.tgm.tgmc.ui.theme.*

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (UserRole) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClayBackground)
    ) {
        // Decorative 3D Orbs (Claymorphism hallmark)
        Box(
            modifier = Modifier
                .offset(x = 200.dp, y = (-50).dp)
                .size(250.dp)
                .clay(
                    backgroundColor = ClayBackground,
                    cornerRadius = 125.dp,
                    elevation = 20.dp,
                    lightShadowColor = ClayShadowLight,
                    darkShadowColor = ClayShadowDark
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "Choose Role",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = ClayTextTitle,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Who is using this device?",
                style = MaterialTheme.typography.bodyLarge.copy(color = ClayTextBody)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Parent Role Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clay(
                        backgroundColor = ClayCard,
                        cornerRadius = 32.dp,
                        elevation = 12.dp,
                        lightShadowColor = ClayShadowLight,
                        darkShadowColor = ClayShadowDark
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .clickable { onRoleSelected(UserRole.PARENT) }
                    .padding(24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clay(
                                backgroundColor = ClayBackground,
                                cornerRadius = 24.dp,
                                elevation = 8.dp,
                                lightShadowColor = ClayShadowLight,
                                darkShadowColor = ClayShadowDark
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = ClayPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    Column {
                        Text(
                            text = "Parent",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = ClayTextTitle,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Manage & Monitor",
                            style = MaterialTheme.typography.bodyMedium.copy(color = ClayTextBody)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Child Role Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clay(
                        backgroundColor = ClayCard,
                        cornerRadius = 32.dp,
                        elevation = 12.dp,
                        lightShadowColor = ClayShadowLight,
                        darkShadowColor = ClayShadowDark
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .clickable { onRoleSelected(UserRole.CHILD) }
                    .padding(24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clay(
                                backgroundColor = ClayBackground,
                                cornerRadius = 24.dp,
                                elevation = 8.dp,
                                lightShadowColor = ClayShadowLight,
                                darkShadowColor = ClayShadowDark
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChildCare,
                            contentDescription = null,
                            tint = ClaySecondary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    Column {
                        Text(
                            text = "Child",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = ClayTextTitle,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Learn & Play",
                            style = MaterialTheme.typography.bodyMedium.copy(color = ClayTextBody)
                        )
                    }
                }
            }
        }
    }
}
