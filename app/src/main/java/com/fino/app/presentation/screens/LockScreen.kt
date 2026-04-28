package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.Newsreader

@Composable
fun LockScreen(
    onUnlockClick: () -> Unit,
    subtitle: String = "Unlock to continue"
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FinoColors.paper())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Eyebrow(text = "Fino · Locked")

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Welcome back",
                fontFamily = Newsreader,
                fontSize = 44.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-1.32).sp,
                color = FinoColors.ink(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = subtitle,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = FinoColors.ink3(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(56.dp))

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(FinoColors.accentSoft())
                    .border(1.dp, FinoColors.line(), CircleShape)
                    .clickable(onClick = onUnlockClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = "Unlock with biometric",
                    tint = FinoColors.accentInk(),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "Tap to unlock",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = FinoColors.ink3(),
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(FinoColors.ink())
                .clickable(onClick = onUnlockClick)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Unlock Fino",
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = FinoColors.paper()
            )
        }
    }
}
