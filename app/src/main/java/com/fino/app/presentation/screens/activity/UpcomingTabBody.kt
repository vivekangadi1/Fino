package com.fino.app.presentation.screens.activity

import androidx.compose.runtime.Composable
import com.fino.app.presentation.screens.UpcomingBillsBody

@Composable
fun UpcomingTabBody(
    onAddBill: () -> Unit,
    onEditBill: (Long) -> Unit,
    onEditCreditCardBill: (Long) -> Unit
) {
    UpcomingBillsBody(
        onAddBill = onAddBill,
        onEditBill = onEditBill,
        onEditCreditCardBill = onEditCreditCardBill
    )
}
