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

package com.example.iapdemo.subscription;

import android.app.Activity;

import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfo;

import java.util.List;

/**
 * 文 件 名: IapContract.java
 * 版    权: Copyright Huawei Tech.Co.Ltd. All Rights Reserved.
 * 描    述:
 *
 * @author z00455615
 * @since 2019/12/14, 16:24
 */
public interface SubscriptionContract {
    interface View {
        void showProducts(List<ProductInfo> productInfoList);

        void updateProductStatus(OwnedPurchasesResult ownedPurchasesResult);

        Activity getActivity();
    }

    interface Presenter {
        void setView(View view);

        void load(List<String> productIds);

        void refreshSubscription();

        void buy(String productId);

        void showSubscription(String productId);

        void shouldOfferService(String productId, ResultCallback<Boolean> callback);

    }

    interface ResultCallback<T> {
        void onResult(T result);
    }
}
