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

import android.text.TextUtils
import android.util.Log
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.OrderStatusCode
import com.huawei.hms.iap.entity.OwnedPurchasesResult
import com.huawei.hms.iap.entity.ProductInfoResult
import com.huawei.hms.iap.entity.PurchaseIntentResult
import com.huawei.iapdemo.common.Constants
import com.huawei.iapdemo.common.ExceptionHandle
import com.huawei.iapdemo.common.ExceptionHandle.handle
import com.huawei.iapdemo.common.IapApiCallback
import com.huawei.iapdemo.common.IapRequestHelper.createPurchaseIntent
import com.huawei.iapdemo.common.IapRequestHelper.obtainOwnedPurchases
import com.huawei.iapdemo.common.IapRequestHelper.obtainProductInfo
import com.huawei.iapdemo.common.IapRequestHelper.showSubscription
import com.huawei.iapdemo.common.IapRequestHelper.startResolutionForResult
import com.huawei.iapdemo.subscription.SubscriptionContract.Presenter

/**
 * Presenter for Subscription function.
 *
 * @since 2019/12/9
 */
class SubscriptionPresenter internal constructor(view: SubscriptionContract.View?) : Presenter {
    private var view: SubscriptionContract.View? = null
    private var cacheOwnedPurchasesResult: OwnedPurchasesResult? = null
    override fun setView(view: SubscriptionContract.View?) {
        if (null == view) {
            throw NullPointerException("can not set null view")
        }
        this.view = view
    }

    override fun load(productIds: List<String>) {
        queryProducts(productIds)
        refreshSubscription()
    }

    override fun refreshSubscription() {
        querySubscriptions(object: SubscriptionContract.ResultCallback<Boolean> {
            override fun onResult(result: Boolean) {
                view!!.updateProductStatus(cacheOwnedPurchasesResult)
            }
        }, null)
    }

    private fun queryProducts(productIds: List<String>) {
        obtainProductInfo(Iap.getIapClient(view!!.activity), productIds, IapClient.PriceType.IN_APP_SUBSCRIPTION, object : IapApiCallback<ProductInfoResult?> {
            override fun onSuccess(result: ProductInfoResult?) {
                if (null == result) {
                    Log.e(TAG, "ProductInfoResult is null")
                    return
                }
                val productInfos = result.productInfoList
                view!!.showProducts(productInfos)
            }

            override fun onFail(e: Exception?) {
                val errorCode = handle(view!!.activity, e!!)
                if (ExceptionHandle.SOLVED != errorCode) {
                    Log.e(TAG, "unknown error")
                }
                view!!.showProducts(null)
            }
        })
    }

    private fun querySubscriptions(callback: SubscriptionContract.ResultCallback<Boolean>, continuationToken: String?) {
        obtainOwnedPurchases(Iap.getIapClient(view!!.activity), IapClient.PriceType.IN_APP_SUBSCRIPTION, continuationToken, object : IapApiCallback<OwnedPurchasesResult?> {
            override fun onSuccess(result: OwnedPurchasesResult?) {
                cacheOwnedPurchasesResult = result
                callback.onResult(true)
            }

            override fun onFail(e: Exception?) {
                Log.e(TAG, "querySubscriptions exception", e)
                handle(view!!.activity, e!!)
                callback.onResult(false)
            }
        })
    }

    override fun buy(productId: String?) {
        // clear local cache
        cacheOwnedPurchasesResult = null
        val iapClient = Iap.getIapClient(view!!.activity)
        createPurchaseIntent(iapClient, productId!!, IapClient.PriceType.IN_APP_SUBSCRIPTION, object : IapApiCallback<PurchaseIntentResult?> {
            override fun onSuccess(result: PurchaseIntentResult?) {
                if (result == null) {
                    Log.e(TAG, "GetBuyIntentResult is null")
                    return
                }

                // you should pull up the page to complete the payment process
                startResolutionForResult(view!!.activity, result.status, Constants.REQ_CODE_BUY)
            }

            override fun onFail(e: Exception?) {
                val errorCode = handle(view!!.activity, e!!)
                if (ExceptionHandle.SOLVED != errorCode) {
                    Log.w(TAG, "createPurchaseIntent, returnCode: $errorCode")
                    if (OrderStatusCode.ORDER_PRODUCT_OWNED == errorCode) {
                        Log.w(TAG, "already own this product")
                        showSubscription(productId)
                    } else {
                        Log.e(TAG, "unknown error")
                    }
                }
            }
        })
    }

    override fun showSubscription(productId: String?) {
        showSubscription(view!!.activity, productId)
    }

    override fun shouldOfferService(productId: String, callback: SubscriptionContract.ResultCallback<Boolean?>?) {
        if (null == callback || TextUtils.isEmpty(productId)) {
            Log.e(TAG, "ResultCallback is null or productId is empty")
            return
        }
        if (null != cacheOwnedPurchasesResult) {
            Log.i(TAG, "using cache data")
            val shouldOffer = SubscriptionUtils.shouldOfferService(cacheOwnedPurchasesResult, productId)
            callback.onResult(shouldOffer)
        } else {
            querySubscriptions(object : SubscriptionContract.ResultCallback<Boolean> {
                override fun onResult(result: Boolean) {
                    val shouldOffer = SubscriptionUtils.shouldOfferService(cacheOwnedPurchasesResult, productId)
                    callback.onResult(shouldOffer)
                }
            }, null)
        }
    }

    companion object {
        private const val TAG = "IapPresenter"
    }

    /**
     * Init SubscriptionPresenter
     * @param view the view which the data place in
     */
    init {
        setView(view)
    }
}