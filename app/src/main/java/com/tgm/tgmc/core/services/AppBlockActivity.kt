package com.tgm.tgmc.core.services

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tgm.tgmc.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppBlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Navy900
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = ErrorRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = "App Blocked",
                                tint = ErrorRed,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Access Blocked",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val blockReason = intent.getStringExtra("block_reason") 
                        ?: "Your parent has blocked access to this application. Please use other educational features or ask your parent to unlock it."
                        
                    Text(
                        text = blockReason,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = {
                            // Navigate user back to Home Screen
                            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                                addCategory(android.content.Intent.CATEGORY_HOME)
                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                            finish()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Surface800,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Exit Application", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
