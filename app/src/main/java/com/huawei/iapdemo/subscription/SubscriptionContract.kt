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
import com.huawei.hms.iap.entity.OwnedPurchasesResult
import com.huawei.hms.iap.entity.ProductInfo

/**
 * Define the contract between view and presenter
 *
 * @since 2019/12/9
 */
interface SubscriptionContract {
    interface View {
        /**
         * Show subscription products
         * @param productInfoList Product list
         */
        fun showProducts(productInfoList: List<ProductInfo>?)

        /**
         * Update product purchase status
         * @param ownedPurchasesResult Purchases result
         */
        fun updateProductStatus(ownedPurchasesResult: OwnedPurchasesResult?)

        /**
         * Get Activity
         * @return Activity
         */
        val activity: Activity
    }

    interface Presenter {
        /**
         * Set the view for presenting data
         * @param view The view for presenting data
         */
        fun setView(view: View?)

        /**
         * Load product data according product Ids
         * @param productIds Product Ids
         */
        fun load(productIds: List<String>)

        /**
         * Refresh owned subscriptions
         */
        fun refreshSubscription()

        /**
         * Buy a subscription product according to productId
         * @param productId Subscription product id
         */
        fun buy(productId: String?)

        /**
         * Show subscription detail
         * @param productId Owned subscription product id
         */
        fun showSubscription(productId: String?)

        /**
         * Decide whether to offer subscription service
         * @param productId Subscription product id
         * @param callback Result callback
         */
        fun shouldOfferService(productId: String, callback: ResultCallback<Boolean?>?)
    }

    interface ResultCallback<T> {
        /**
         * Result callback
         * @param result Result
         */
        fun onResult(result: T)
    }
}