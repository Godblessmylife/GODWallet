package com.gemwallet.android.features.settings.networks.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.clients.NodeStatusClientsProxy
import com.gemwallet.android.data.chains.ChainInfoRepository
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.config.NodesRepository
import com.gemwallet.android.ext.filter
import com.gemwallet.android.features.settings.networks.models.AddSourceType
import com.gemwallet.android.features.settings.networks.models.NetworksUIState
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Node
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NetworksViewModel @Inject constructor(
    private val chainInfoRepository: ChainInfoRepository,
    private val configRepository: ConfigRepository,
    private val nodesRepository: NodesRepository,
    private val nodeStatusClientsProxy: NodeStatusClientsProxy,
    private val nodeStatusClients: NodeStatusClientsProxy,
) : ViewModel() {

    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, NetworksUIState())
    val chainFilter = TextFieldState()
    var nodes: StateFlow<List<Node>> = state.flatMapLatest {
        if (it.chain == null) {
            return@flatMapLatest emptyFlow()
        }
        nodesRepository.getNodes(it.chain)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    var nodeStates = nodes.map { nodes ->
        val chain = state.value.chain ?: return@map emptyMap()
        withContext(Dispatchers.IO) {
            nodes.map { node ->
                    async {
                        Pair(node.url, nodeStatusClients(chain, node.url))
                    }
                }.awaitAll()
                .groupBy { it.first }
                .mapValues { it.value.first().second }
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    init {
        viewModelScope.launch {
            state.update { it.copy(availableChains = chainInfoRepository.getAll()) }
            snapshotFlow { chainFilter.text }.collectLatest { query ->
                state.update { it.copy(availableChains = chainInfoRepository.getAll().filter(query.toString().lowercase())) }
            }
        }
    }

    fun onSelectedChain(chain: Chain) {
        state.update {
            it.copy(
                chain = chain,
                selectChain = false,
                explorers = configRepository.getBlockExplorers(chain),
                currentNode = configRepository.getCurrentNode(chain),
                currentExplorer = configRepository.getCurrentBlockExplorer(chain),
                availableAddNode = nodeStatusClientsProxy.isMaintained(chain)
            )
        }
    }

    fun onSelectNode(node: Node) {
        val chain = state.value.chain ?: return
        configRepository.setCurrentNode(chain, node)
        onSelectedChain(chain)
    }

    fun onSelectBlockExplorer(name: String) {
        val chain = state.value.chain ?: return
        configRepository.setCurrentBlockExplorer(chain, name)
        onSelectedChain(chain)
    }

    fun onSelectChain() {
        state.update {it.copy(selectChain = true) }
    }

    private data class State(
        val chain: Chain? = null,
        val explorers: List<String> = emptyList(),
        val currentNode: Node? = null,
        val currentExplorer: String? = null,
        val availableChains: List<Chain> = emptyList(),
        val selectChain: Boolean = true,
        val availableAddNode: Boolean = true,
        val addSourceType: AddSourceType = AddSourceType.None,
    ) {
        fun toUIState(): NetworksUIState {
            return NetworksUIState(
                chain = chain,
                chains = availableChains,
                selectChain = selectChain,
                blockExplorers = explorers,
                currentNode = currentNode,
                currentExplorer = currentExplorer,
                availableAddNode = availableAddNode,
            )
        }
    }
}