package com.fino.app.presentation.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.FinoColors

/**
 * Standard layout for Insights drill-in screens.
 * - Paper background
 * - 48dp top bar: back chevron (left) + title + optional trailing slot
 * - Content column with 24dp horizontal padding
 */
@Composable
fun DetailScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = FinoColors.paper()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            DetailTopBar(title = title, onNavigateBack = onNavigateBack, trailing = trailing)
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

@Composable
private fun DetailTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onNavigateBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = FinoColors.ink(),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.size(4.dp))
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = FinoColors.ink(),
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) trailing()
    }
}

object DetailDefaults {
    val ContentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    val SectionSpacing = 20.dp
    val TopSpacing = 8.dp
}
