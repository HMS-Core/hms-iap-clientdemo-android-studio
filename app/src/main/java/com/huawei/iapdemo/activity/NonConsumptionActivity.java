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

package com.huawei.iapdemo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.huawei.iapdemo.adapter.ProductListAdapter;
import com.huawei.iapdemo.common.CipherUtil;
import com.huawei.iapdemo.common.Constants;
import com.huawei.iapdemo.common.ExceptionHandle;
import com.huawei.iapdemo.common.IapApiCallback;
import com.huawei.iapdemo.common.IapRequestHelper;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.iapdemo.huawei.R;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for Non-consumables.
 *
 * @since 2019/12/9
 */
public class NonConsumptionActivity extends AppCompatActivity {
    private String TAG = "NonConsumptionActivity";

    // Use this IapClient instance to call the APIs of IAP.
    private IapClient mClient;

    // ListView for displaying non-consumables.
    private ListView nonconsumableProductListview;

    // The list of products to be purchased.
    private List<ProductInfo> nonconsumableProducts = new ArrayList<ProductInfo>();

    // The product ID of the Hidden Level which is a non-consumable product.
    private static final String HIDDEN_LEVEL_PRODUCTID = "NonCProduct01";

    // The Adapter for nonconsumableProductListview.
    private ProductListAdapter productListAdapter;

    // Show this layout when the Hidden Level have been purchased.
    private LinearLayout hasOwnedHiddenLevelLayout;

    // Whether the Hidden Level has been purchased.
    private boolean isHiddenLevelPurchased = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_consumption);
        mClient = Iap.getIapClient(this);
        initView();
        // Call the obtainOwnedPurchases API during startup to obtain the data about non-consumable products that a user has purchased.
        queryPurchases(null);
    }

    /**
     * Initialize the UI.
     */
    private void initView() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.content).setVisibility(View.INVISIBLE);
        nonconsumableProductListview = (ListView) findViewById(R.id.nonconsumable_product_list);
        hasOwnedHiddenLevelLayout = (LinearLayout) findViewById(R.id.layout_hasOwnedHiddenLevel);
    }

    /**
     * Call the obtainOwnedPurchases API to obtain the data about non-consumable products that the user has purchased.
     *
     * @param continuationToken A data location flag for a query in pagination mode.
     */
    private void queryPurchases(final String continuationToken) {
        // Query users' purchased non-consumable products.
        IapRequestHelper.obtainOwnedPurchases(mClient, IapClient.PriceType.IN_APP_NONCONSUMABLE, continuationToken, new IapApiCallback<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                Log.i(TAG, "obtainOwnedPurchases, success");
                checkHiddenLevelPurchaseState(result);
                if (result != null && !TextUtils.isEmpty(result.getContinuationToken())) {
                    queryPurchases(result.getContinuationToken());
                }
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "obtainOwnedPurchases, type=" + IapClient.PriceType.IN_APP_NONCONSUMABLE + ", " + e.getMessage());
                Toast.makeText(NonConsumptionActivity.this, "get Purchases fail, " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * Checking the Purchase Status of Hidden Level.
     *
     * @param result OwnedPurchasesResults instance obtained from IAP, which contains information about purchased products.
     */
    private void checkHiddenLevelPurchaseState(OwnedPurchasesResult result) {
        if (result == null || result.getInAppPurchaseDataList() == null) {
            Log.i(TAG, "result is null");
            queryProducts();
            return;
        }

        List<String> inAppPurchaseDataList = result.getInAppPurchaseDataList();
        List<String> inAppSignature= result.getInAppSignature();
        for (int i = 0; i < inAppPurchaseDataList.size(); i++) {
            // Check whether the signature of the purchase data is valid.
            if (CipherUtil.doCheck(inAppPurchaseDataList.get(i), inAppSignature.get(i), CipherUtil.getPublicKey())) {
                try {
                    InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseDataList.get(i));
                    if (inAppPurchaseDataBean.getPurchaseState() == InAppPurchaseData.PurchaseState.PURCHASED) {
                        // Check whether the purchased product is Hidden Level.
                        if (HIDDEN_LEVEL_PRODUCTID.equals(inAppPurchaseDataBean.getProductId())) {
                            isHiddenLevelPurchased = true;
                        }
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "delivery:" + e.getMessage());
                }
            } else {
                Log.e(TAG, "delivery:" + ", verify signature error");
            }
        }
        if (isHiddenLevelPurchased) {
            // Deliver the product after the Hidden Level was purchased.
            deliverProduct();
        } else {
            // The user has not purchased the hidden level.
            // Obtain the product details and show the purchase entry to the user.
            queryProducts();
        }
    }

    /**
     * Deliver the product.
     */
    private void deliverProduct() {
        // User has purchased hidden level.
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.content).setVisibility(View.VISIBLE);

        nonconsumableProductListview.setVisibility(View.GONE);
        hasOwnedHiddenLevelLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Obtains product details of products and show the products.
     */
    private void queryProducts() {
        List<String> productIds = new ArrayList<String> ();
        productIds.add(HIDDEN_LEVEL_PRODUCTID);
        IapRequestHelper.obtainProductInfo(mClient, productIds, IapClient.PriceType.IN_APP_NONCONSUMABLE, new IapApiCallback<ProductInfoResult>() {
            @Override
            public void onSuccess(ProductInfoResult result) {
                Log.i(TAG, "obtainProductInfo, success");
                if (result == null || result.getProductInfoList() == null) {
                    Toast.makeText(NonConsumptionActivity.this, "error", Toast.LENGTH_SHORT).show();
                    return;
                }
                nonconsumableProducts = result.getProductInfoList();
                // To show product information.
                showProducts();
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "obtainProductInfo: " + e.getMessage());
                Toast.makeText(NonConsumptionActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show products on the page.
     */
    private void showProducts() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.content).setVisibility(View.VISIBLE);
        nonconsumableProductListview.setVisibility(View.VISIBLE);
        hasOwnedHiddenLevelLayout.setVisibility(View.GONE);

        productListAdapter = new ProductListAdapter(this, nonconsumableProducts);
        nonconsumableProductListview.setAdapter(productListAdapter);
        productListAdapter.notifyDataSetChanged();
        nonconsumableProductListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                buy(position);
            }
        });
    }

    /**
     * Initiate a purchase.
     *
     * @param index Item to be purchased.
     */
    private void buy(int index) {
        ProductInfo productInfo = nonconsumableProducts.get(index);
        IapRequestHelper.createPurchaseIntent(mClient, productInfo.getProductId(), IapClient.PriceType.IN_APP_NONCONSUMABLE, new IapApiCallback<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                if (result == null) {
                    Log.e(TAG, "result is null");
                    return;
                }
                // You should pull up the page to complete the payment process.
                IapRequestHelper.startResolutionForResult(NonConsumptionActivity.this, result.getStatus(), Constants.REQ_CODE_BUY);
            }

            @Override
            public void onFail(Exception e) {
                int errorCode = ExceptionHandle.handle(NonConsumptionActivity.this, e);
                if (errorCode != ExceptionHandle.SOLVED) {
                    Log.i(TAG, "createPurchaseIntent, returnCode: " + errorCode);
                    // Handle error scenarios.
                    switch (errorCode) {
                        case OrderStatusCode.ORDER_PRODUCT_OWNED:
                            queryPurchases(null);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isHiddenLevelPurchased = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQ_CODE_BUY) {
            if (data == null) {
                Log.e(TAG, "data is null");
                return;
            }
            // Parses payment result data.
            PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data);
            switch(purchaseResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    Toast.makeText(this,"Order has been canceled!", Toast.LENGTH_SHORT).show();
                    break;
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    // Obtains the order information of all purchased products to check whether the product has been purchased.
                    queryPurchases(null);
                    break;
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    // Check whether the signature of the purchase data is valid.
                    if (CipherUtil.doCheck(purchaseResultInfo.getInAppPurchaseData(), purchaseResultInfo.getInAppDataSignature(), CipherUtil.getPublicKey())) {
                        isHiddenLevelPurchased = true;
                        deliverProduct();
                    } else {
                        Toast.makeText(this,getString(R.string.pay_success_signfail), Toast.LENGTH_SHORT).show();
                    }

                    break;
                default:
                    break;
            }
        }
    }
}
