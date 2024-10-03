package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.StakeClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.asset
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigInteger

class SolanaStakeClient(
    private val rpcClient: SolanaRpcClient
): StakeClient {
    override suspend fun getValidators(apr: Double): List<DelegationValidator> {
        return rpcClient.validators(
            JSONRpcRequest.create(
                SolanaMethod.GetValidators,
                listOf(
                    mapOf(
                        "commitment" to "finalized",
                        "keepUnstakedDelinquents" to true
                    )
                )
            )
        ).getOrNull()?.result?.current?.map {
            val isActive = it.epochVoteAccount
            DelegationValidator(
                chain = maintainChain(),
                id = it.votePubkey,
                name = "",
                isActive = isActive,
                commision = it.commission.toDouble(),
                apr = if (isActive) apr - apr * (it.commission.toDouble() / 100) else 0.0,
            )
        } ?: emptyList()
    }

    override suspend fun getStakeDelegations(address: String, apr: Double): List<DelegationBase> = withContext(Dispatchers.IO) {
        val getEpoch = async {
            rpcClient.epoch(JSONRpcRequest.create(SolanaMethod.GetEpoch, emptyList()))
                .getOrNull()?.result
        }
        val getDelegations = async { rpcClient.delegations(address).getOrNull()?.result }
        val epoch = getEpoch.await() ?: return@withContext emptyList()
        val delegations = getDelegations.await() ?: return@withContext emptyList()
        val nextEpoch = System.currentTimeMillis() + ((epoch.slotsInEpoch - epoch.slotIndex) * 420)

        delegations.map { delegation ->
            val info = delegation.account.data.parsed.info
            val deactivateEpoch = try {
                info.stake.delegation.deactivationEpoch.toInt()
            } catch (err: Throwable) {
                null
            }
            val activationEpoch = try {
                info.stake.delegation.activationEpoch.toInt()
            } catch (err: Throwable) {
                null
            }
            val state = fun(): DelegationState {
                if (deactivateEpoch != null) {
                    if (deactivateEpoch == epoch.epoch) {
                        return DelegationState.Deactivating
                    } else if (deactivateEpoch < epoch.epoch) {
                        return DelegationState.AwaitingWithdrawal
                    }
                } else if (activationEpoch != null) {
                    if (activationEpoch == epoch.epoch) {
                        return DelegationState.Activating
                    } else if (activationEpoch <= epoch.epoch) {
                        return DelegationState.Active
                    }
                }
                return DelegationState.Pending
            }()
            val balance = delegation.account.lamports.toString()
            DelegationBase(
                assetId = maintainChain().asset().id,
                state = state,
                balance = balance,
                rewards = (BigInteger(balance) - BigInteger(info.stake.delegation.stake)).toString(),
                delegationId = delegation.pubkey,
                completionDate = nextEpoch,
                validatorId = info.stake.delegation.voter,
                shares = "",
            )
        }
    }

    override fun maintainChain(): Chain = Chain.Solana
}