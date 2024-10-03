package com.gemwallet.android.model

import com.gemwallet.android.ext.same
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetLinks
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.WalletType

data class AssetInfo(
    val owner: Account,
    val asset: Asset,
    val balances: Balances = Balances(),
    val walletType: WalletType = WalletType.view,
    val walletName: String = "",
    val price: AssetPriceInfo? = null,
    val metadata: AssetMetaData? = null,
    val links: AssetLinks? = null,
    val market: AssetMarket? = null,
    val rank: Int = 0,
    val stakeApr: Double? = null,
    val position: Int = 0,
) {
    fun id() = asset.id

    override fun equals(other: Any?): Boolean {
        return (other as? AssetInfo)?.let { info ->
            asset.same(info.asset)
        } ?: false
    }

    override fun hashCode(): Int {
        var result = owner.hashCode()
        result = 31 * result + asset.hashCode()
        result = 31 * result + balances.hashCode()
        result = 31 * result + walletType.hashCode()
        result = 31 * result + walletName.hashCode()
        result = 31 * result + (price?.hashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + (links?.hashCode() ?: 0)
        result = 31 * result + (market?.hashCode() ?: 0)
        result = 31 * result + rank
        result = 31 * result + (stakeApr?.hashCode() ?: 0)
        result = 31 * result + position
        return result
    }
}
