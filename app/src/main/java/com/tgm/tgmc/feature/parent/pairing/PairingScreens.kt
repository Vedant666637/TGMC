package com.tgm.tgmc.feature.parent.pairing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingStartScreen(onGenerateQr: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = { Text("Add Child Device", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Box(
                modifier = Modifier.size(120.dp).background(Brush.radialGradient(listOf(Cyan400.copy(alpha = 0.2f), Navy900)), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PhonelinkSetup, contentDescription = null, tint = Cyan400, modifier = Modifier.size(64.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Pair a Child Device", style = MaterialTheme.typography.headlineSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Generate a QR code or invite link. Have the child install TGM-C and scan/enter the code.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Steps
            listOf(
                Triple(Icons.Default.Download, "Step 1", "Child installs TGM-C app"),
                Triple(Icons.Default.QrCode, "Step 2", "Tap 'Generate Code' below"),
                Triple(Icons.Default.QrCodeScanner, "Step 3", "Child scans the QR code"),
                Triple(Icons.Default.CheckCircle, "Step 4", "Devices are linked instantly")
            ).forEachIndexed { i, (icon, step, desc) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp).background(Surface800, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = if (i == 3) SuccessGreen else Cyan400, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(step, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                        Text(desc, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onGenerateQr,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = Navy900)
            ) {
                Icon(Icons.Default.QrCode2, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Pairing Code", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingQrScreen(
    onBack: () -> Unit,
    viewModel: PairingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = { Text("Pairing Code", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // QR placeholder
            Surface(color = Surface050, shape = RoundedCornerShape(16.dp), modifier = Modifier.size(200.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.QrCode2, contentDescription = "QR Code", tint = Navy900, modifier = Modifier.size(160.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Cyan400)
            } else if (uiState.error != null) {
                Text(text = uiState.error ?: "An error occurred", color = MaterialTheme.colorScheme.error)
                TextButton(onClick = viewModel::generateCode) { Text("Retry", color = Cyan400) }
            } else {
                Text(
                    text = uiState.code ?: "XXXX-XXXX", 
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Cyan400, 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 4.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("This code expires in 10 minutes", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
            Spacer(modifier = Modifier.height(24.dp))
            Surface(color = Surface800, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text("⚙️  Enter this 8-digit code on the Child's device.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.labelMedium.copy(color = TextMuted))
            }
        }
    }
}
