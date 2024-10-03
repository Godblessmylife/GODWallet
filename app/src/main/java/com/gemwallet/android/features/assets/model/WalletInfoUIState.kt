package com.gemwallet.android.features.assets.model

import com.wallet.core.primitives.WalletType

data class WalletInfoUIState(
    val name: String = "",
    val icon: String = "",
    val totalValue: String = "0.0",
    val changedValue: String = "0.0",
    val changedPercentages: String = "0.0%",
    val priceState: PriceState = PriceState.Up,
    val type: WalletType = WalletType.view,
)