/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.iapdemo.subscription;

import android.app.Activity;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfo;
import java.util.List;

/**
 * Define the contract between view and presenter
 *
 * @since 2019/12/9
 */
public interface SubscriptionContract {
    interface View {
        /**
         * Show subscription products
         * @param productInfoList Product list
         */
        void showProducts(List<ProductInfo> productInfoList);

        /**
         * Update product purchase status
         * @param ownedPurchasesResult Purchases result
         */
        void updateProductStatus(OwnedPurchasesResult ownedPurchasesResult);

        /**
         * Get Activity
         * @return Activity
         */
        Activity getActivity();
    }

    interface Presenter {
        /**
         * Set the view for presenting data
         * @param view The view for presenting data
         */
        void setView(View view);

        /**
         * Load product data according product Ids
         * @param productIds Product Ids
         */
        void load(List<String> productIds);

        /**
         * Refresh owned subscriptions
         */
        void refreshSubscription();

        /**
         * Buy a subscription product according to productId
         * @param productId Subscription product id
         */
        void buy(String productId);

        /**
         * Show subscription detail
         * @param productId Owned subscription product id
         */
        void showSubscription(String productId);

        /**
         * Decide whether to offer subscription service
         * @param productId Subscription product id
         * @param callback Result callback
         */
        void shouldOfferService(String productId, ResultCallback<Boolean> callback);

    }

    interface ResultCallback<T> {
        /**
         * Result callback
         * @param result Result
         */
        void onResult(T result);
    }
}
