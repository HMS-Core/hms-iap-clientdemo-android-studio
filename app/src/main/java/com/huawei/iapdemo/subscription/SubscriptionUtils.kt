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

package com.huawei.iapdemo.subscription

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.entity.InAppPurchaseData
import com.huawei.hms.iap.entity.OrderStatusCode
import com.huawei.hms.iap.entity.OwnedPurchasesResult
import com.huawei.iapdemo.common.CipherUtil
import com.huawei.iapdemo.common.CipherUtil.doCheck
import org.json.JSONException

/**
 * Util for Subscription function.
 *
 * @since 2019/12/9
 */
object SubscriptionUtils {
    private const val TAG = "SubscriptionUtils"

    /**
     * Decide whether to offer subscription service.
     *
     * @param result the OwnedPurchasesResult from IapClient.obtainOwnedPurchases
     * @param productId subscription product id
     * @return decision result
     */
    fun shouldOfferService(result: OwnedPurchasesResult?, productId: String): Boolean {
        if (null == result) {
            Log.e(TAG, "OwnedPurchasesResult is null")
            return false
        }
        val inAppPurchaseDataList = result.inAppPurchaseDataList
        for (data in inAppPurchaseDataList) {
            try {
                val inAppPurchaseData = InAppPurchaseData(data)
                if (productId == inAppPurchaseData.productId) {
                    val index = inAppPurchaseDataList.indexOf(data)
                    val signature = result.inAppSignature[index]
                    val credible = doCheck(data!!, signature, CipherUtil.publicKey)
                    return if (credible) {
                        inAppPurchaseData.isSubValid
                    } else {
                        Log.e(TAG, "check the data signature fail")
                        false
                    }
                }
            } catch (e: JSONException) {
                Log.e(TAG, "parse InAppPurchaseData JSONException", e)
                return false
            }
        }
        return false
    }

    /**
     * Parse PurchaseResult data from intent.
     *
     * @param activity Activity
     * @param data the intent from onActivityResult
     * @return result status
     */
    fun getPurchaseResult(activity: Activity?, data: Intent?): Int {
        // Parse PurchaseResult data from intent.
        val purchaseResultInfo = Iap.getIapClient(activity).parsePurchaseResultInfoFromIntent(data)
        if (null == purchaseResultInfo) {
            Log.e(TAG, "PurchaseResultInfo is null")
            return OrderStatusCode.ORDER_STATE_FAILED
        }
        val returnCode = purchaseResultInfo.returnCode
        val errMsg = purchaseResultInfo.errMsg
        return when (returnCode) {
            OrderStatusCode.ORDER_PRODUCT_OWNED -> {
                Log.w(TAG, "you have owned this product")
                OrderStatusCode.ORDER_PRODUCT_OWNED
            }
            OrderStatusCode.ORDER_STATE_SUCCESS -> {
                // Check whether the signature of the purchase data is valid.
                val credible = doCheck(purchaseResultInfo.inAppPurchaseData, purchaseResultInfo.inAppDataSignature, CipherUtil.publicKey)
                if (credible) {
                    try {
                        val inAppPurchaseData = InAppPurchaseData(purchaseResultInfo.inAppPurchaseData)
                        if (inAppPurchaseData.isSubValid) {
                            return OrderStatusCode.ORDER_STATE_SUCCESS
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "parse InAppPurchaseData JSONException", e)
                        return OrderStatusCode.ORDER_STATE_FAILED
                    }
                } else {
                    Log.e(TAG, "check the data signature fail")
                }
                OrderStatusCode.ORDER_STATE_FAILED
            }
            else -> {
                Log.e(TAG, "returnCode: $returnCode , errMsg: $errMsg")
                returnCode
            }
        }
    }
}