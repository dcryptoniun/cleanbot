// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.domain.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Tip / Support item metadata representing in-app consumable purchases.
 */
data class TipOption(
    val productId: String,
    val title: String,
    val description: String,
    val emoji: String,
    val defaultPrice: String,
    val formattedPrice: String? = null,
    val productDetails: ProductDetails? = null,
)

sealed interface PurchaseStatus {
    data object Idle : PurchaseStatus
    data class Processing(val productId: String) : PurchaseStatus
    data class Success(val message: String) : PurchaseStatus
    data class Error(val message: String) : PurchaseStatus
}

/**
 * Manages Google Play Billing for in-app tips and donations.
 * All tip options are consumable so they can be purchased multiple times.
 */
class PlayBillingManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "PlayBillingManager"

        // In-App Product IDs registered in Google Play Console
        const val PRODUCT_TIP_COFFEE = "tip_coffee_1"
        const val PRODUCT_TIP_PIZZA = "tip_pizza_5"
        const val PRODUCT_TIP_SUPPORTER = "tip_supporter_10"
        const val PRODUCT_TIP_SUPERFAN = "tip_superfan_25"

        val DEFAULT_TIP_OPTIONS = listOf(
            TipOption(
                productId = PRODUCT_TIP_COFFEE,
                title = "Buy a Coffee",
                description = "Fuel late-night coding sessions and maintenance",
                emoji = "☕",
                defaultPrice = "$0.99"
            ),
            TipOption(
                productId = PRODUCT_TIP_PIZZA,
                title = "Pizza Slice",
                description = "Support ongoing TV compatibility research & themes",
                emoji = "🍕",
                defaultPrice = "$4.99"
            ),
            TipOption(
                productId = PRODUCT_TIP_SUPPORTER,
                title = "Living Room Hero",
                description = "Accelerate wireless ADB toolkits & custom launchers",
                emoji = "🌟",
                defaultPrice = "$9.99"
            ),
            TipOption(
                productId = PRODUCT_TIP_SUPERFAN,
                title = "cLeanBot Super Fan",
                description = "Ultimate token of appreciation for keeping TVs bloat-free",
                emoji = "👑",
                defaultPrice = "$24.99"
            )
        )
    }

    private val _tipOptions = MutableStateFlow<List<TipOption>>(DEFAULT_TIP_OPTIONS)
    val tipOptions: StateFlow<List<TipOption>> = _tipOptions.asStateFlow()

    private val _purchaseStatus = MutableStateFlow<PurchaseStatus>(PurchaseStatus.Idle)
    val purchaseStatus: StateFlow<PurchaseStatus> = _purchaseStatus.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val billingClient: BillingClient by lazy {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()

        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()
    }

    fun startBillingConnection() {
        if (billingClient.isReady) {
            _isConnected.value = true
            queryAvailableProducts()
            consumePendingPurchases()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "BillingClient connected successfully.")
                    _isConnected.value = true
                    queryAvailableProducts()
                    consumePendingPurchases()
                } else {
                    Log.w(TAG, "BillingClient setup failed: ${billingResult.debugMessage}")
                    _isConnected.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "BillingClient disconnected.")
                _isConnected.value = false
            }
        })
    }

    /**
     * Query product details from Google Play for all tip options.
     */
    private fun queryAvailableProducts() {
        val productList = DEFAULT_TIP_OPTIONS.map { tip ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(tip.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, queryResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val detailsMap = queryResult.associateBy { it.productId }
                _tipOptions.value = DEFAULT_TIP_OPTIONS.map { tip ->
                    val details = detailsMap[tip.productId]
                    val formatted = details?.oneTimePurchaseOfferDetails?.formattedPrice
                    tip.copy(
                        formattedPrice = formatted ?: tip.defaultPrice,
                        productDetails = details
                    )
                }
                Log.d(TAG, "Loaded ${queryResult.size} product details from Google Play.")
            } else {
                Log.w(TAG, "Failed to query product details: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Check for unconsumed purchases and consume them immediately so they can be bought again.
     */
    private fun consumePendingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        consumePurchase(purchase, isInitialSync = true)
                    }
                }
            }
        }
    }

    /**
     * Launch Google Play Billing Purchase Flow for a tip option.
     */
    fun launchPurchaseFlow(activity: Activity, tipOption: TipOption) {
        val details = tipOption.productDetails
        if (details == null) {
            _purchaseStatus.value = PurchaseStatus.Error(
                "Tip option '${tipOption.title}' is currently connecting to Google Play. Please verify network and try again."
            )
            return
        }

        _purchaseStatus.value = PurchaseStatus.Processing(tipOption.productId)

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Failed to launch billing flow: ${result.debugMessage}")
            _purchaseStatus.value = PurchaseStatus.Error(result.debugMessage.ifBlank { "Could not start purchase flow." })
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isNullOrEmpty()) {
                    _purchaseStatus.value = PurchaseStatus.Idle
                } else {
                    purchases.forEach { purchase ->
                        handlePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.i(TAG, "User canceled purchase flow.")
                _purchaseStatus.value = PurchaseStatus.Idle
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
                _purchaseStatus.value = PurchaseStatus.Error(
                    billingResult.debugMessage.ifBlank { "Purchase could not be completed." }
                )
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            consumePurchase(purchase, isInitialSync = false)
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _purchaseStatus.value = PurchaseStatus.Processing("Pending Google Play confirmation...")
        }
    }

    /**
     * Consumes the purchase immediately so the user can purchase this item multiple times.
     */
    private fun consumePurchase(purchase: Purchase, isInitialSync: Boolean) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { result, _ ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Successfully consumed purchase token: ${purchase.purchaseToken}")
                if (!isInitialSync) {
                    _purchaseStatus.value = PurchaseStatus.Success(
                        "Thank you so much for supporting cLeanBot! Your tip has been received ❤️"
                    )
                }
            } else {
                Log.w(TAG, "Failed to consume purchase token: ${result.debugMessage}")
                if (!isInitialSync) {
                    _purchaseStatus.value = PurchaseStatus.Error(
                        "Purchase succeeded but consumption failed: ${result.debugMessage}"
                    )
                }
            }
        }
    }

    fun dismissPurchaseStatus() {
        _purchaseStatus.value = PurchaseStatus.Idle
    }

    fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
