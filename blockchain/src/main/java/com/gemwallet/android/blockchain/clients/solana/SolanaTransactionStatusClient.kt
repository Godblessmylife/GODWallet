package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class SolanaTransactionStatusClient(
    private val rpcClient: SolanaRpcClient,
) : TransactionStatusClient {

    override suspend fun getStatus(owner: String, txId: String): Result<TransactionChages> {
        val request = JSONRpcRequest(
            SolanaMethod.GetTransaction.value,
            listOf(
                txId,
                mapOf(
                    "encoding" to "jsonParsed",
                    "maxSupportedTransactionVersion" to 0,
                ),
            )
        )
        return rpcClient.transaction(request).mapCatching {
            if (it.error != null) return@mapCatching TransactionChages(TransactionState.Failed)
            val state = if (it.result.slot > 0) {
                if (it.result.meta.err != null) {
                    TransactionState.Failed
                } else {
                    TransactionState.Confirmed
                }
            } else {
                TransactionState.Pending
            }
            TransactionChages(state)
        }
    }

    override fun maintainChain(): Chain = Chain.Solana
}