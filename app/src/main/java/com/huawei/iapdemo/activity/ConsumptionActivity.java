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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import android.widget.Toast;
import com.huawei.iapdemo.adapter.ProductListAdapter;
import com.huawei.iapdemo.common.CipherUtil;
import com.huawei.iapdemo.common.Constants;
import com.huawei.iapdemo.common.ExceptionHandle;
import com.huawei.iapdemo.common.IapApiCallback;
import com.huawei.iapdemo.common.IapRequestHelper;
import com.huawei.iapdemo.common.ProductItem;
import com.huawei.iapdemo.common.DeliveryUtils;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.iap.util.IapClientHelper;
import com.huawei.hms.support.api.client.Status;
import com.iapdemo.huawei.R;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for Consumables.
 *
 * @since 2019/12/9
 */
public class ConsumptionActivity extends AppCompatActivity {
    private String TAG = "ConsumptionActivity";

    // Displays the number of gems.
    private TextView countTextView;

    // Consumable products that configured in AppGallery Connect.
    private ListView productsListview;
    // The list of pms products to be purchased.
    private List<ProductInfo> products = new ArrayList<>();
    // The Adapter for productsListview.
    private ProductListAdapter pmsProductsAdapter;

    // Customized consumable products.
    private ListView customizedProductsListview;
    // The list of customized products to be purchased.
    private List<ProductItem> customizedProducts = new ArrayList<>();
    // The Adapter for customizedProductsListview.
    private ProductListAdapter customizedProductsAdapter;

    // Click this button to start the PurchaseHistoryActivity which displays information about purchased products.
    private Button purchaseHisBtn;

    // Use this IapClient instance to call the APIs of IAP.
    private IapClient mClient;

    // Record the customized product that the user is purchasing.
    private ProductItem productItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumption);
        mClient = Iap.getIapClient(this);
        initView();
        // To check if there exists consumable products that a user has purchased but has not been delivered.
        queryPurchases(null);
    }

    /**
     * Initialize the UI.
     */
    private void initView() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.content).setVisibility(View.GONE);
        countTextView = (TextView) findViewById(R.id.gems_count);
        countTextView.setText(String.valueOf(DeliveryUtils.getCountOfGems(this)));
        productsListview = (ListView) findViewById(R.id.consumable_product_list1);
        productsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProductInfo productInfo = products.get(position);
                buy(productInfo.getProductId());

            }
        });

        customizedProductsListview = (ListView) findViewById(R.id.consumable_product_list2);
        customizedProductsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProductItem productItem = customizedProducts.get(position);
                buyWithPrice(productItem);

            }
        });

        purchaseHisBtn = (Button) findViewById(R.id.enter_purchase_his);
        purchaseHisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConsumptionActivity.this, PurchaseHistoryActivity.class);
                startActivity(intent);
            }
        });

        queryProducts();
    }

    /**
     * Obtains product details of products and show the products.
     */
    private void queryProducts() {
        // Add non-managed products.
        String productId = "CustomizedCProduct01";
        ProductItem item = new ProductItem();
        item.setProductId(productId);
        item.setProductName("10宝石");
        item.setPrice("0.30");
        item.setCurrency("CNY");
        item.setCountry("CN");
        item.setCurrencySymbol("¥");
        customizedProducts.add(item);

        // Obtain the products managed in AppGallery Connect.
        List<String> productIds = new ArrayList<String>();
        productIds.add("CProduct01");
        IapRequestHelper.obtainProductInfo(mClient, productIds, IapClient.PriceType.IN_APP_CONSUMABLE, new IapApiCallback<ProductInfoResult>() {
            @Override
            public void onSuccess(ProductInfoResult result) {
                Log.i(TAG, "obtainProductInfo, success");
                if (result == null) {
                    return;
                }
                if (result.getProductInfoList() != null) {
                    // to show product information
                    products = result.getProductInfoList();
                }
                showProducts();
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "obtainProductInfo: " + e.getMessage());
                ExceptionHandle.handle(ConsumptionActivity.this, e);
                showProducts();
            }
        });
    }

    /**
     * Show products on the page.
     */
    private void showProducts() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.content).setVisibility(View.VISIBLE);
        pmsProductsAdapter = new ProductListAdapter(ConsumptionActivity.this, products);
        productsListview.setAdapter(pmsProductsAdapter);
        pmsProductsAdapter.notifyDataSetChanged();

        customizedProductsAdapter = new ProductListAdapter(ConsumptionActivity.this, customizedProducts);
        customizedProductsListview.setAdapter(customizedProductsAdapter);
        customizedProductsAdapter.notifyDataSetChanged();
    }

    /**
     * Call the obtainOwnedPurchases API to obtain the data about consumable products that a user has purchased but has not been delivered.
     */
    private void queryPurchases(final String continuationToken) {
        final String tag = "obtainOwnedPurchases";
        IapRequestHelper.obtainOwnedPurchases(mClient, IapClient.PriceType.IN_APP_CONSUMABLE, continuationToken, new IapApiCallback<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                if (result == null) {
                    Log.e(TAG, tag + " result is null");
                    return;
                }
                Log.i(TAG, "obtainOwnedPurchases, success");
                if (result.getInAppPurchaseDataList() != null) {
                    List<String> inAppPurchaseDataList = result.getInAppPurchaseDataList();
                    List<String> inAppSignature= result.getInAppSignature();
                    for (int i = 0; i < inAppPurchaseDataList.size(); i++) {
                        final String inAppPurchaseData = inAppPurchaseDataList.get(i);
                        final String inAppPurchaseDataSignature = inAppSignature.get(i);
                        deliverProduct(inAppPurchaseData, inAppPurchaseDataSignature);
                    }
                }
                if (!TextUtils.isEmpty(result.getContinuationToken())) {
                    queryPurchases(result.getContinuationToken());
                }
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "obtainOwnedPurchases, type=" + IapClient.PriceType.IN_APP_CONSUMABLE + ", " + e.getMessage());
                ExceptionHandle.handle(ConsumptionActivity.this, e);
            }
        });

    }

    /**
     * Deliver the product.
     *
     * @param inAppPurchaseDataStr Includes the purchase details.
     * @param inAppPurchaseDataSignature The signature String of inAppPurchaseDataStr.
     */
    private void deliverProduct(final String inAppPurchaseDataStr, final String inAppPurchaseDataSignature) {
        // Check whether the signature of the purchase data is valid.
        if (CipherUtil.doCheck(inAppPurchaseDataStr, inAppPurchaseDataSignature, CipherUtil.getPublicKey())) {
            try {
                InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseDataStr);
                if (inAppPurchaseDataBean.getPurchaseState() != InAppPurchaseData.PurchaseState.PURCHASED) {
                    return;
                }
                String purchaseToken = inAppPurchaseDataBean.getPurchaseToken();
                String tmpProductId = inAppPurchaseDataBean.getProductId();
                if (DeliveryUtils.isDelivered(ConsumptionActivity.this, purchaseToken)) {
                    Toast.makeText(ConsumptionActivity.this, tmpProductId + " has been delivered", Toast.LENGTH_SHORT).show();
                    IapRequestHelper.consumeOwnedPurchase(mClient, purchaseToken);
                } else {
                    if (DeliveryUtils.deliverProduct(this, tmpProductId, purchaseToken)) {
                        Log.i(TAG, "delivery success");
                        Toast.makeText(ConsumptionActivity.this, tmpProductId + " delivery success", Toast.LENGTH_SHORT).show();
                        updateNumberOfGems();
                        // To consume the product after successfully delivering.
                        IapRequestHelper.consumeOwnedPurchase(mClient, purchaseToken);
                    } else {
                        Log.e(TAG, tmpProductId + " delivery fail");
                        Toast.makeText(ConsumptionActivity.this, tmpProductId + " delivery fail", Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (JSONException e) {
                Log.e(TAG, "delivery:" + e.getMessage());
            }
        } else {
            Log.e(TAG, "delivery:" + getString(R.string.verify_signature_fail));
            Toast.makeText(ConsumptionActivity.this, getString(R.string.verify_signature_fail), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update the number of gems on the page.
     */
    private void updateNumberOfGems() {
        // Update the number of gems.
        String countOfGems = String.valueOf(DeliveryUtils.getCountOfGems(ConsumptionActivity.this));
        countTextView.setText(countOfGems);
    }

    /**
     * Initiate a purchase.
     *
     * @param productId Item to be purchased.
     */
    private void buy(String productId) {
        IapRequestHelper.createPurchaseIntent(mClient, productId, IapClient.PriceType.IN_APP_CONSUMABLE, new IapApiCallback<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                if (result == null) {
                    Log.d(TAG, "result is null");
                    return;
                }
                Status status = result.getStatus();
                if (status == null) {
                    Log.d(TAG, "status is null");
                    return;
                }
                // you should pull up the page to complete the payment process.
                IapRequestHelper.startResolutionForResult(ConsumptionActivity.this, status, Constants.REQ_CODE_BUY);
            }

            @Override
            public void onFail(Exception e) {
                int errorCode = ExceptionHandle.handle(ConsumptionActivity.this, e);
                if (errorCode != ExceptionHandle.SOLVED) {
                    Log.i(TAG, "createPurchaseIntent, returnCode: " + errorCode);
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

    /**
     * Purchase the customized product.
     *
     * @param productItem
     */
    private void buyWithPrice(ProductItem productItem) {
        final String tag = "createPurchaseIntentWithPrice";
        this.productItem = productItem;
        IapRequestHelper.createPurchaseIntentWithPrice(mClient, productItem, new IapApiCallback<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                if (result == null) {
                    Toast.makeText(ConsumptionActivity.this, tag + getString(R.string.fail), Toast.LENGTH_SHORT).show();
                    return;
                }
                Status status = result.getStatus();
                if (status == null) {
                    Toast.makeText(ConsumptionActivity.this, tag + getString(R.string.fail), Toast.LENGTH_SHORT).show();
                    return;
                }
                // Check whether the signature of the order data is valid.
                boolean isSuccess = CipherUtil.doCheck(result.getPaymentData(), result.getPaymentSignature(), CipherUtil.getPublicKey());
                if (isSuccess) {
                    // If success, pull up cashier page.
                    IapRequestHelper.startResolutionForResult(ConsumptionActivity.this, status, Constants.REQ_CODE_BUYWITHPRICE);
                } else {
                    // Verify signature fail.
                    Toast.makeText(ConsumptionActivity.this, getString(R.string.verify_signature_fail), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFail(Exception e) {
                Toast.makeText(ConsumptionActivity.this, "createPurchaseIntentWithPrice, " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "createPurchaseIntentWithPrice, " + e.getMessage());
                int errorCode = ExceptionHandle.handle(ConsumptionActivity.this, e);
                if (errorCode != ExceptionHandle.SOLVED) {
                    // Handle the unresolved error scenarios.
                    switch(errorCode) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQ_CODE_BUY || requestCode == Constants.REQ_CODE_BUYWITHPRICE) {
            if (data == null) {
                Log.e(TAG, "data is null");
                return;
            }
            PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data);
            switch(purchaseResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    Toast.makeText(ConsumptionActivity.this, "Order has been canceled!", Toast.LENGTH_SHORT).show();
                    break;
                case OrderStatusCode.ORDER_STATE_FAILED:
                case OrderStatusCode.ORDER_STATE_DEFAULT_CODE:
                    // Default value returned by parsePurchaseResultInfoFromIntent when no return code is received from the IAP.
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    queryPurchases(null);
                    break;
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    deliverProduct(purchaseResultInfo.getInAppPurchaseData(), purchaseResultInfo.getInAppDataSignature());
                    break;
                default:
                    break;
            }
            return;
        }

        if (requestCode == Constants.REQ_CODE_LOGIN || requestCode == Constants.REQ_CODE_BUYWITHPRICE_CONTINUE) {
            int returnCode = IapClientHelper.parseRespCodeFromIntent(data);
            if (data != null) {
                returnCode = data.getIntExtra("returnCode", -1);
            }
            if (returnCode == OrderStatusCode.ORDER_STATE_SUCCESS) {
                // If success, you can call createPurchaseIntentWithPrice API again.
                buyWithPrice(productItem);
            } else {
                Log.e(TAG, getString(R.string.cancel));
            }
            return;
        }
    }

}
