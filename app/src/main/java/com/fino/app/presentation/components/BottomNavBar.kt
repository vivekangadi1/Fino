package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.FinoColors

/**
 * Navigation destination for bottom nav bar.
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// Design spec: [Home, Activity, + (FAB), Insights, Cards] — center is the Add trigger.
val finoNavItems = listOf(
    BottomNavItem(
        route = "home",
        label = "Home",
        selectedIcon = Icons.Outlined.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = "activity",
        label = "Activity",
        selectedIcon = Icons.Outlined.ReceiptLong,
        unselectedIcon = Icons.Outlined.ReceiptLong
    ),
    BottomNavItem(
        route = "insights",
        label = "Insights",
        selectedIcon = Icons.Outlined.Insights,
        unselectedIcon = Icons.Outlined.Insights
    ),
    BottomNavItem(
        route = "cards",
        label = "Cards",
        selectedIcon = Icons.Outlined.CreditCard,
        unselectedIcon = Icons.Outlined.CreditCard
    )
)

/**
 * Floating pill bottom nav. 5 slots: Home · Activity · + · Insights · Cards.
 * Center slot is a 38dp ink circle that triggers the Add Transaction flow.
 * Spec: card bg, 1px line border, 100dp radius, 8dp padding, shadow-md,
 * bottom=28dp, horizontal margin=18dp.
 */
@Composable
fun FinoBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(100.dp))
                .clip(RoundedCornerShape(100.dp))
                .background(FinoColors.card())
                .border(1.dp, FinoColors.line(), RoundedCornerShape(100.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PillNavItem(finoNavItems[0], currentRoute == finoNavItems[0].route) {
                onNavigate(finoNavItems[0].route)
            }
            PillNavItem(finoNavItems[1], currentRoute == finoNavItems[1].route) {
                onNavigate(finoNavItems[1].route)
            }
            CenterAddButton(onClick = onAddClick)
            PillNavItem(finoNavItems[2], currentRoute == finoNavItems[2].route) {
                onNavigate(finoNavItems[2].route)
            }
            PillNavItem(finoNavItems[3], currentRoute == finoNavItems[3].route) {
                onNavigate(finoNavItems[3].route)
            }
        }
    }
}

@Composable
private fun PillNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val fg = if (isSelected) FinoColors.ink() else FinoColors.ink3()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CenterAddButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(FinoColors.ink())
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = "Add transaction",
            tint = FinoColors.paper(),
            modifier = Modifier.size(18.dp)
        )
    }
}
