package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusClientError
import com.example.ui.theme.StatusRedirect
import com.example.ui.theme.StatusServerError
import com.example.ui.theme.StatusSuccess
import io.bandroid.simplefetch.HttpMethod

@Composable
fun MethodBadge(
    method: HttpMethod,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val (bg, fg) = when (method) {
        HttpMethod.GET -> Color(0xFF0284C7) to Color.White
        HttpMethod.POST -> Color(0xFF10B981) to Color.White
        HttpMethod.PUT -> Color(0xFFF59E0B) to Color.White
        HttpMethod.DELETE -> Color(0xFFEF4444) to Color.White
        HttpMethod.PATCH -> Color(0xFF8B5CF6) to Color.White
        HttpMethod.HEAD -> Color(0xFF6B7280) to Color.White
    }

    val finalBg = if (onClick != null && !isSelected) bg.copy(alpha = 0.15f) else bg
    val finalFg = if (onClick != null && !isSelected) bg else fg

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(finalBg)
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable(onClick = onClick)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                } else {
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = method.name,
            color = finalFg,
            fontWeight = FontWeight.Bold,
            fontSize = if (onClick != null) 13.sp else 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun StatusBadge(statusCode: Int, durationMs: Long = 0L) {
    val (badgeBg, badgeText) = when {
        statusCode in 200..299 -> StatusSuccess to "$statusCode OK"
        statusCode in 300..399 -> StatusRedirect to "$statusCode Redirect"
        statusCode in 400..499 -> StatusClientError to "$statusCode Error"
        statusCode >= 500 -> StatusServerError to "$statusCode Server Err"
        else -> Color(0xFF6B7280) to "Failed"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeBg.copy(alpha = 0.15f))
                .border(1.dp, badgeBg.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = badgeText,
                color = badgeBg,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        if (durationMs > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${durationMs}ms",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun CodeSnippetCard(
    title: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_code_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = Color(0xFF38BDF8)
                    )
                }
            }

            SelectionContainer {
                Text(
                    text = code,
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
