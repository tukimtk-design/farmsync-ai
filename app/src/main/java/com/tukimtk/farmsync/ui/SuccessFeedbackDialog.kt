package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tukimtk.farmsync.i18n.Strings

@Composable
fun SuccessFeedbackDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    val isError = message.contains("❌") || 
                  message.contains("Error", ignoreCase = true) || 
                  message.contains("ล้มเหลว") || 
                  message.contains("failed", ignoreCase = true) ||
                  message.contains("not granted", ignoreCase = true)

    val iconColor = if (isError) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
    val containerBg = if (isError) MaterialTheme.colorScheme.errorContainer else Color(0xFF2E7D32).copy(alpha = 0.2f)
    val symbol = if (isError) "✕" else "✓"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = containerBg,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = symbol,
                            color = iconColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
                Text(
                    text = title, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Text(
                text = message, 
                style = MaterialTheme.typography.bodyLarge, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    contentColor = if (isError) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = Strings.get("เข้าใจแล้ว", "OK"),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
