package com.gemwallet.android.data.database.mappers

import com.gemwallet.android.data.database.entities.DbTransactionExtended
import com.gemwallet.android.ext.toAssetId
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Price
import com.wallet.core.primitives.Transaction
import com.wallet.core.primitives.TransactionExtended

class ExtendedTransactionMapper : Mapper<TransactionExtended?, DbTransactionExtended> {

    override fun asDomain(entity: TransactionExtended?): DbTransactionExtended {
        throw IllegalAccessException()
    }

    override fun asEntity(domain: DbTransactionExtended): TransactionExtended? {
        return TransactionExtended(
            transaction = Transaction(
                id = domain.id,
                hash = domain.hash,
                assetId = domain.assetId.toAssetId() ?: return null,
                from = domain.owner,
                to = domain.recipient,
                contract = domain.contract,
                type = domain.type,
                state = domain.state,
                blockNumber = domain.blockNumber,
                sequence = domain.sequence,
                fee = domain.fee,
                feeAssetId = domain.feeAssetId.toAssetId() ?: return null,
                value = domain.value,
                memo = domain.payload,
                direction = domain.direction,
                utxoInputs = emptyList(),
                utxoOutputs = emptyList(),
                createdAt = domain.createdAt,
                metadata = domain.metadata,
            ),
            asset = Asset(
                id = domain.assetId.toAssetId() ?: return null,
                name = domain.assetName,
                symbol = domain.assetSymbol,
                decimals = domain.assetDecimals,
                type = domain.assetType,
            ),
            feeAsset = Asset(
                id = domain.feeAssetId.toAssetId() ?: return null,
                name = domain.feeName,
                symbol = domain.feeSymbol,
                decimals = domain.feeDecimals,
                type = domain.feeType,
            ),
            price = if (domain.assetPrice == null)
                null
            else
                Price(domain.assetPrice, domain.assetPriceChanged ?: 0.0),
            feePrice = if (domain.feePrice == null)
                null
            else
                Price(domain.feePrice, domain.feePriceChanged ?: 0.0),
            assets = emptyList(),
        )
    }
}