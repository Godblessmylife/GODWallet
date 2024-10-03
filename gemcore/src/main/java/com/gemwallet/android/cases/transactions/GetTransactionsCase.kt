package com.gemwallet.android.cases.transactions

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.TransactionExtended
import com.wallet.core.primitives.TransactionState
import kotlinx.coroutines.flow.Flow

interface GetTransactionsCase {
    fun getTransactions(assetId: AssetId? = null, state: TransactionState? = null): Flow<List<TransactionExtended>>

    fun getChangedTransactions(): Flow<List<TransactionExtended>>

    fun getPendingTransactions(): Flow<List<TransactionExtended>> {
        return getTransactions(state = TransactionState.Pending)
    }
}