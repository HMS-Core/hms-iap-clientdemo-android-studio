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
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.iapdemo.common.Constants;
import com.iapdemo.huawei.R;
import java.util.Arrays;
import java.util.List;

/**
 * Activity for auto-renewable subscriptions.
 *
 * @since 2019/12/9
 */
public class SubscriptionActivity extends AppCompatActivity implements SubscriptionContract.View {

    private static final String TAG = "SubscriptionActivity";

    // The product ID array of products to be purchased.
    private static final String[] SUBSCRIPTION_PRODUCT = new String[]{"demosub101", "demosub102", "demosub201", "demosub202"};

    // Presenter of this page, which is used to process data interactions.
    private SubscriptionContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.content).setVisibility(View.INVISIBLE);

        List<String> list = Arrays.asList(SUBSCRIPTION_PRODUCT);
        presenter = new SubscriptionPresenter(this);
        presenter.load(list);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQ_CODE_BUY) {
            if (resultCode == Activity.RESULT_OK) {
                int purchaseResult = SubscriptionUtils.getPurchaseResult(this, data);
                if (OrderStatusCode.ORDER_STATE_SUCCESS == purchaseResult) {
                    Toast.makeText(this, R.string.pay_success, Toast.LENGTH_SHORT).show();
                    presenter.refreshSubscription();
                    return;
                }
                if (OrderStatusCode.ORDER_STATE_CANCEL == purchaseResult) {
                    Toast.makeText(this, R.string.cancel, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, R.string.pay_fail, Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "cancel subscribe");
                Toast.makeText(this, R.string.cancel, Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Show subscription products.
     *
     * @param productInfos Product list.
     */
    @Override
    public void showProducts(List<ProductInfo> productInfos) {
        if (null == productInfos) {
            Toast.makeText(this, R.string.external_error, Toast.LENGTH_SHORT).show();
            return;
        }

        for (ProductInfo productInfo : productInfos) {
            showProduct(productInfo);
        }

        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.content).setVisibility(View.VISIBLE);
    }

    /**
     * Update product purchase status.
     *
     * @param ownedPurchasesResult Purchases result.
     */
    @Override
    public void updateProductStatus(OwnedPurchasesResult ownedPurchasesResult) {
        for (String productId : SUBSCRIPTION_PRODUCT) {
            View view = getView(productId);
            Button button = view.findViewById(R.id.action);
            button.setTag(productId);
            // Check whether to offer subscription service.
            if (SubscriptionUtils.shouldOfferService(ownedPurchasesResult, productId)) {
                // Offer subscription service.
                button.setText(R.string.active);
                button.setOnClickListener(getDetailActionListener());
            } else {
                // Show the purchase Entry.
                button.setText(R.string.buy);
                button.setOnClickListener(getBuyActionListener());
            }
        }
    }

    /**
     * Jump to manage subscription page.
     *
     * @param view The view which has been clicked.
     */
    public void manageSubscription(View view) {
        presenter.showSubscription("");
    }

    /**
     * Show products on the page.
     *
     * @param productInfo Contains details of a product.
     */
    private void showProduct(ProductInfo productInfo) {
        View view = getView(productInfo.getProductId());

        if (view != null) {
            TextView productName = view.findViewById(R.id.product_name);
            TextView productDesc = view.findViewById(R.id.product_desc);
            TextView price = view.findViewById(R.id.price);

            productName.setText(productInfo.getProductName());
            productDesc.setText(productInfo.getProductDesc());
            price.setText(productInfo.getPrice());

        }
    }

    /**
     * Obtains the layout of the product.
     *
     * @param productId ProductId of the product.
     *
     * @return View
     */
    private View getView(String productId) {
        View view = null;
        if (SUBSCRIPTION_PRODUCT[0].equals(productId)) {
            view = findViewById(R.id.service_one_product_one);
        }
        if (SUBSCRIPTION_PRODUCT[1].equals(productId)) {
            view = findViewById(R.id.service_one_product_two);
        }
        if (SUBSCRIPTION_PRODUCT[2].equals(productId)) {
            view = findViewById(R.id.service_two_product_one);
        }
        if (SUBSCRIPTION_PRODUCT[3].equals(productId)) {
            view = findViewById(R.id.service_two_product_two);
        }
        return view;
    }


    @Override
    public Activity getActivity() {
        return this;
    }

    /**
     * Obtains the listener of the BUY button.
     *
     * @return View.OnClickListener
     */
    private View.OnClickListener getBuyActionListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object data = v.getTag();
                if (data instanceof String) {
                    String productId = (String) data;
                    presenter.buy(productId);
                }
            }
        };
    }

    /**
     * Obtains the listener of the ACTIVE button.
     *
     * @return View.OnClickListener
     */
    private View.OnClickListener getDetailActionListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object data = v.getTag();
                if (data instanceof String) {
                    String productId = (String) data;
                    // Open the subscription page of HUAWEI IAP.
                    presenter.showSubscription(productId);
                }
            }
        };
    }
}
