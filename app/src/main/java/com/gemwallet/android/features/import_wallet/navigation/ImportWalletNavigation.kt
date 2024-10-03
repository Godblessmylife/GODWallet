package com.gemwallet.android.features.import_wallet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.gemwallet.android.features.import_wallet.viewmodels.ImportType
import com.gemwallet.android.features.import_wallet.views.ImportScreen
import com.gemwallet.android.features.import_wallet.views.SelectImportTypeScreen
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType

internal const val walletTypeArg = "wallet_type"
internal const val chainArg = "chain"

const val importSelectType = "import_select_type"
const val importWalletRoute = "import_wallet"

fun NavController.navigateToImportWalletScreen(importType: ImportType? = null, navOptions: NavOptions? = null) {
    if (importType == null) {
        navigate(route = importSelectType, navOptions)
    } else {
        navigate(
            route = "$importWalletRoute/${importType.walletType.string}/${importType.chain?.string}",
            navOptions = navOptions
        )
    }
}

fun NavGraphBuilder.importWalletScreen(
    onCancel: () -> Unit,
    onImported: () -> Unit,
    onSelectType: (ImportType?) -> Unit,
) {
    navigation(startDestination = importSelectType, route = importWalletRoute) {
        composable(route = importSelectType) {
            SelectImportTypeScreen(onClose = onCancel, onSelect = onSelectType)
        }
        composable(
            route = "$importWalletRoute/{$walletTypeArg}/{$chainArg}",
            arguments = listOf(
                navArgument(walletTypeArg) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(chainArg) {
                    type = NavType.StringType
                    nullable = true
                },
            )
        ) { entry ->
            val walletTypeString = entry.arguments?.getString(walletTypeArg)
            val chainString = entry.arguments?.getString(chainArg)
            val walletType = WalletType.values().firstOrNull { it.string == walletTypeString }
            val chain = Chain.values().firstOrNull { it.string == chainString }
            if (walletType == null || (walletType != WalletType.multicoin && chain == null)) {
                onSelectType(null)
            } else {
                ImportScreen(
                    importType = ImportType(walletType, chain),
                    onCancel = onCancel,
                    onImported = onImported,
                )
            }
        }
    }
}