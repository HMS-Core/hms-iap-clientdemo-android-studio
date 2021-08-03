/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.iapdemo.common

import android.app.Activity
import android.content.IntentSender.SendIntentException
import android.text.TextUtils
import android.util.Log
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.*
import com.huawei.hms.support.api.client.Status

/**
 * The tool class of Iap interface.
 *
 * @since 2019/12/9
 */
object IapRequestHelper {
    private const val TAG = "IapRequestHelper"

    /**
     * Create a PurchaseIntentReq object.
     *
     * @param type In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param productId ID of the in-app product to be paid.
     * The in-app product ID is the product ID you set during in-app product configuration in AppGallery Connect.
     * @return PurchaseIntentReq
     */
    private fun createPurchaseIntentReq(type: Int, productId: String): PurchaseIntentReq {
        val req = PurchaseIntentReq()
        req.priceType = type
        req.productId = productId
        req.developerPayload = "testPurchase"
        return req
    }


    /**
     * Create a ConsumeOwnedPurchaseReq object.
     *
     * @param purchaseToken which is generated by the Huawei payment server during product payment and returned to the app through InAppPurchaseData.
     * The app transfers this parameter for the Huawei payment server to update the order status and then deliver the in-app product.
     * @return ConsumeOwnedPurchaseReq
     */
    private fun createConsumeOwnedPurchaseReq(purchaseToken: String): ConsumeOwnedPurchaseReq {
        val req = ConsumeOwnedPurchaseReq()
        req.purchaseToken = purchaseToken
        req.developerChallenge = "testConsume"
        return req
    }

    /**
     * Create a OwnedPurchasesReq object.
     *
     * @param type type In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param continuationToken A data location flag which returns from obtainOwnedPurchases api or obtainOwnedPurchaseRecord api.
     * @return OwnedPurchasesReq
     */
    private fun createOwnedPurchasesReq(type: Int, continuationToken: String?): OwnedPurchasesReq {
        val req = OwnedPurchasesReq()
        req.priceType = type
        req.continuationToken = continuationToken
        return req
    }

    /**
     * Create a ProductInfoReq object.
     *
     * @param type In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param productIds ID list of products to be queried. Each product ID must exist and be unique in the current app.
     * @return ProductInfoReq
     */
    private fun createProductInfoReq(type: Int, productIds: List<String>): ProductInfoReq {
        val req = ProductInfoReq()
        req.priceType = type
        req.productIds = productIds
        return req
    }

    /**
     * To check whether the country or region of the logged in HUAWEI ID is included in the countries or regions supported by HUAWEI IAP.
     *
     * @param mClient IapClient instance to call the isEnvReady API.
     * @param callback IapApiCallback.
     */
    fun isEnvReady(mClient: IapClient, callback: IapApiCallback<IsEnvReadyResult?>) {
        Log.i(TAG, "call isEnvReady")
        val task = mClient.isEnvReady
        task.addOnSuccessListener { result ->
            Log.i(TAG, "isEnvReady, success")
            callback.onSuccess(result)
        }.addOnFailureListener { e ->
            Log.e(TAG, "isEnvReady, fail")
            callback.onFail(e)
        }
    }

    /**
     * Obtain in-app product details configured in AppGallery Connect.
     *
     * @param iapClient IapClient instance to call the obtainProductInfo API.
     * @param productIds ID list of products to be queried. Each product ID must exist and be unique in the current app.
     * @param type In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param callback IapApiCallback
     */
    @JvmStatic
    fun obtainProductInfo(iapClient: IapClient?, productIds: List<String>, type: Int, callback: IapApiCallback<ProductInfoResult?>) {
        Log.i(TAG, "call obtainProductInfo")
        val task = iapClient!!.obtainProductInfo(createProductInfoReq(type, productIds))
        task.addOnSuccessListener { result ->
            Log.i(TAG, "obtainProductInfo, success")
            callback.onSuccess(result)
        }.addOnFailureListener { e ->
            Log.e(TAG, "obtainProductInfo, fail")
            callback.onFail(e)
        }
    }

    /**
     * Create orders for in-app products in the PMS.
     *
     * @param iapClient IapClient instance to call the createPurchaseIntent API.
     * @param productId ID of the in-app product to be paid.
     * The in-app product ID is the product ID you set during in-app product configuration in AppGallery Connect.
     * @param type  In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param callback IapApiCallback
     */
    @JvmStatic
    fun createPurchaseIntent(iapClient: IapClient?, productId: String, type: Int, callback: IapApiCallback<PurchaseIntentResult?>) {
        Log.i(TAG, "call createPurchaseIntent")
        val task = iapClient!!.createPurchaseIntent(createPurchaseIntentReq(type, productId))
        task.addOnSuccessListener { result ->
            Log.i(TAG, "createPurchaseIntent, success")
            callback.onSuccess(result)
        }.addOnFailureListener { e ->
            Log.e(TAG, "createPurchaseIntent, fail")
            callback.onFail(e)
        }
    }


    /**
     * To start an activity.
     *
     * @param activity the activity to launch a new page.
     * @param status This parameter contains the pendingIntent object of the payment page.
     * @param reqCode Result code.
     */
    @JvmStatic
    fun startResolutionForResult(activity: Activity?, status: Status?, reqCode: Int) {
        if (status == null) {
            Log.e(TAG, "status is null")
            return
        }
        if (status.hasResolution()) {
            try {
                status.startResolutionForResult(activity, reqCode)
            } catch (exp: SendIntentException) {
                Log.e(TAG, exp.message)
            }
        } else {
            Log.e(TAG, "intent is null")
        }
    }

    /**
     * Query information about all subscribed in-app products, including consumables, non-consumables, and auto-renewable subscriptions.
     * If consumables are returned, the system needs to deliver them and calls the consumeOwnedPurchase API to consume the products.
     * If non-consumables are returned, the in-app products do not need to be consumed.
     * If subscriptions are returned, all existing subscription relationships of the user under the app are returned.
     *
     * @param mClient IapClient instance to call the obtainOwnedPurchases API.
     * @param type In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param callback IapApiCallback
     */
    @JvmStatic
    fun obtainOwnedPurchases(mClient: IapClient?, type: Int, continuationToken: String?, callback: IapApiCallback<OwnedPurchasesResult?>) {
        Log.i(TAG, "call obtainOwnedPurchases")
        val task = mClient!!.obtainOwnedPurchases(createOwnedPurchasesReq(type, continuationToken))
        task.addOnSuccessListener { result ->
            Log.i(TAG, "obtainOwnedPurchases, success")
            callback.onSuccess(result)
        }.addOnFailureListener { e ->
            Log.e(TAG, "obtainOwnedPurchases, fail")
            callback.onFail(e)
        }
    }

    /**
     * Obtain the historical consumption information about a consumable in-app product or all subscription receipts of a subscription.
     *
     * @param iapClient IapClient instance to call the obtainOwnedPurchaseRecord API.
     * @param priceType In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription.
     * @param continuationToken Data locating flag for supporting query in pagination mode.
     * @param callback IapApiCallback
     */
    fun obtainOwnedPurchaseRecord(iapClient: IapClient, priceType: Int, continuationToken: String?, callback: IapApiCallback<OwnedPurchasesResult?>) {
        Log.i(TAG, "call obtainOwnedPurchaseRecord")
        val task = iapClient.obtainOwnedPurchaseRecord(createOwnedPurchasesReq(priceType, continuationToken))
        task.addOnSuccessListener { result ->
            Log.i(TAG, "obtainOwnedPurchaseRecord, success")
            callback.onSuccess(result)
        }.addOnFailureListener { e ->
            Log.e(TAG, "obtainOwnedPurchaseRecord, fail")
            callback.onFail(e)
        }
    }

    /**
     * Consume all the unconsumed purchases with priceType 0.
     *
     * @param iapClient IapClient instance to call the consumeOwnedPurchase API.
     * @param purchaseToken which is generated by the Huawei payment server during product payment and returned to the app through InAppPurchaseData.
     */
    fun consumeOwnedPurchase(iapClient: IapClient?, purchaseToken: String) {
        Log.i(TAG, "call consumeOwnedPurchase")
        val task = iapClient!!.consumeOwnedPurchase(createConsumeOwnedPurchaseReq(purchaseToken))
        task.addOnSuccessListener { // Consume success.
            Log.i(TAG, "consumeOwnedPurchase success")
        }.addOnFailureListener { e ->
            if (e is IapApiException) {
                val returnCode = e.statusCode
                Log.e(TAG, "consumeOwnedPurchase fail, IapApiException returnCode: $returnCode")
            } else {
                // Other external errors
                Log.e(TAG, e.message)
            }
        }
    }

    /**
     * Displays the subscription editing page or subscription management page of HUAWEI IAP.
     *
     * @param activity The activity to launch a new page.
     * @param productId The productId of the subscription product.
     */
    @JvmStatic
    fun showSubscription(activity: Activity?, productId: String?) {
        val req = StartIapActivityReq()
        if (TextUtils.isEmpty(productId)) {
            req.type = StartIapActivityReq.TYPE_SUBSCRIBE_MANAGER_ACTIVITY
        } else {
            req.type = StartIapActivityReq.TYPE_SUBSCRIBE_EDIT_ACTIVITY
            req.subscribeProductId = productId
        }
        val iapClient = Iap.getIapClient(activity)
        val task = iapClient.startIapActivity(req)
        task.addOnSuccessListener { result -> result?.startActivity(activity) }.addOnFailureListener { e -> ExceptionHandle.handle(activity, e) }
    }
}