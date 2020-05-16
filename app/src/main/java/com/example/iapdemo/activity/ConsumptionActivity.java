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

package com.example.iapdemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.iapdemo.adapter.ProductListAdapter;
import com.example.iapdemo.callback.ProductInfoCallback;
import com.example.iapdemo.callback.PurchaseIntentResultCallback;
import com.example.iapdemo.callback.QueryPurchasesCallback;
import com.example.iapdemo.common.CipherUtil;
import com.example.iapdemo.common.Constants;
import com.example.iapdemo.common.ExceptionHandle;
import com.example.iapdemo.common.IapRequestHelper;
import com.example.iapdemo.common.ProductItem;
import com.example.iapdemo.common.DeliveryUtils;
import com.example.iapdemo.common.Utils;
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

public class ConsumptionActivity extends Activity {
    private String TAG = "ConsumptionActivity";
    private TextView countTextView;

    // consumable product.
    private ListView consumableProductsListview;
    private List<ProductItem> consumableProducts = new ArrayList<ProductItem>();
    private ProductListAdapter adapter;
    private Button purchaseHisBtn;

    private IapClient mClient;

    // Record the product that the user is purchasing.
    private static ProductItem productItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumption);
        mClient = Iap.getIapClient(this);
        initView();
        // To check if there exists consumable products that a user has purchased but has not been delivered.
        queryPurchases();
    }

    private void initView() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.content).setVisibility(View.GONE);
        countTextView = (TextView) findViewById(R.id.gems_count);
        countTextView.setText(String.valueOf(DeliveryUtils.getCountOfGems(this)));
        consumableProductsListview = (ListView) findViewById(R.id.consumable_product_list1);
        consumableProductsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gotoBuy(position);

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

    private void queryProducts() {
        // Add non-managed products.
        String productId = "CustomizedCProduct01";
        ProductItem item = new ProductItem(productId);
        item.setNumOfGems(10);
        item.setProductName("10宝石");
        item.setIsCustomized(true);
        item.setPrice("0.30");
        item.setCurrency("CNY");
        item.setCountry("CN");
        item.setCurrencySymbol("¥");
        consumableProducts.add(item);

        // Obtain the products managed in AppGallery Connect.
        List<String> productIds = new ArrayList<String>();
        productIds.add("CProduct01");
        IapRequestHelper.obtainProductInfo(mClient, productIds, IapClient.PriceType.IN_APP_CONSUMABLE, new ProductInfoCallback() {
            @Override
            public void onSuccess(ProductInfoResult result) {
                Log.i(TAG, "obtainProductInfo, success");
                if (result == null) {
                    return;
                }
                if (result.getProductInfoList() != null) {
                    // to show product information
                    for (ProductInfo productInfo : result.getProductInfoList()) {
                        ProductItem productItem = new ProductItem(productInfo);
                        consumableProducts.add(productItem);
                    }
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

    private void showProducts() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.content).setVisibility(View.VISIBLE);
        adapter = new ProductListAdapter(ConsumptionActivity.this, consumableProducts);
        consumableProductsListview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * Call the obtainOwnedPurchases API to obtain the data about consumable products that a user has purchased but has not been delivered.
     */
    private void queryPurchases() {
        final String tag = "obtainOwnedPurchases";
        IapRequestHelper.obtainOwnedPurchases(mClient, IapClient.PriceType.IN_APP_CONSUMABLE, new QueryPurchasesCallback() {
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
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "obtainOwnedPurchases, type=" + IapClient.PriceType.IN_APP_CONSUMABLE + ", " + e.getMessage());
                int returnCode = ExceptionHandle.handle(ConsumptionActivity.this, e);
            }
        });

    }

    private void deliverProduct(final String inAppPurchaseDataStr, final String inAppPurchaseDataSignature) {
        if (CipherUtil.doCheck(inAppPurchaseDataStr, inAppPurchaseDataSignature, CipherUtil.getPublicKey())) {
            try {
                InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseDataStr);
                if (inAppPurchaseDataBean.getPurchaseState() != InAppPurchaseData.PurchaseState.PURCHASED) {
                    return;
                }
                String purchaseToken = inAppPurchaseDataBean.getPurchaseToken();
                String tmpProductId = inAppPurchaseDataBean.getProductId();
                if (DeliveryUtils.isDelivered(ConsumptionActivity.this, purchaseToken)) {
                    Utils.showMessage(this, tmpProductId + " has been delivered");
                    IapRequestHelper.consumeOwnedPurchase(mClient, purchaseToken);
                } else {
                    if (DeliveryUtils.deliverProduct(this, tmpProductId, purchaseToken)) {
                        Log.i(TAG, "delivery success");
                        Utils.showMessage(this, tmpProductId + " delivery success");
                        updateNumberOfGems();
                        // To consume the product after successfully delivering.
                        IapRequestHelper.consumeOwnedPurchase(mClient, purchaseToken);
                    } else {
                        Log.e(TAG, tmpProductId + " delivery fail");
                        Utils.showMessage(this, tmpProductId + " delivery fail");
                    }
                }

            } catch (JSONException e) {
                Log.e(TAG, "delivery:" + e.getMessage());
            }
        } else {
            Log.e(TAG, "delivery:" + getString(R.string.verify_signature_fail));
            Utils.showMessage(this, getString(R.string.verify_signature_fail));
        }
    }

    private void updateNumberOfGems() {
        // Update the number of gems.
        String countOfGems = String.valueOf(DeliveryUtils.getCountOfGems(ConsumptionActivity.this));
        countTextView.setText(countOfGems);
    }

    private void gotoBuy(int index) {
        ProductItem productItem = consumableProducts.get(index);
        if (productItem.getIsCustomized()) {
            buyWithPrice(productItem);
        } else {
            buy(productItem.getProductInfo().getProductId());
        }
    }

    private void buy(String productId) {
        IapRequestHelper.createPurchaseIntent(mClient, productId, IapClient.PriceType.IN_APP_CONSUMABLE, new PurchaseIntentResultCallback() {
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
                            queryPurchases();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private void buyWithPrice(ProductItem productItem) {
        final String tag = "createPurchaseIntentWithPrice";
        this.productItem = productItem;
        IapRequestHelper.createPurchaseIntentWithPrice(mClient, productItem, new PurchaseIntentResultCallback() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                if (result == null) {
                    Utils.showMessage(ConsumptionActivity.this, tag + getString(R.string.fail));
                    return;
                }
                Status status = result.getStatus();
                if (status == null || status.getResolution() == null) {
                    Utils.showMessage(ConsumptionActivity.this, tag + getString(R.string.fail));
                    return;
                }
                // verify signature, and pull up cashier page.
                boolean isSuccess = CipherUtil.doCheck(result.getPaymentData(), result.getPaymentSignature(), CipherUtil.getPublicKey());
                if (isSuccess) {
                    IapRequestHelper.startResolutionForResult(ConsumptionActivity.this, status, Constants.REQ_CODE_BUYWITHPRICE);
                } else {
                    // Verify signature fail.
                    Utils.showMessage(ConsumptionActivity.this, getString(R.string.verify_signature_fail));
                }
            }

            @Override
            public void onFail(Exception e) {
                Utils.showMessage(ConsumptionActivity.this, "createPurchaseIntentWithPrice, " + e.getMessage());
                Log.e(TAG, "createPurchaseIntentWithPrice, " + e.getMessage());
                int errorCode = ExceptionHandle.handle(ConsumptionActivity.this, e);
                if (errorCode != ExceptionHandle.SOLVED) {
                    // handle error scenarios.
                    switch(errorCode) {
                        // Account not logged in.
                        case OrderStatusCode.ORDER_HWID_NOT_LOGIN:
                        // User has not accepted the payment agreement.
                        case OrderStatusCode.ORDER_NOT_ACCEPT_AGREEMENT:
                            break;
                        case OrderStatusCode.ORDER_PRODUCT_OWNED:
                            queryPurchases();
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
                    Utils.showMessage(ConsumptionActivity.this, "Order has been canceled!");
                    break;
                case OrderStatusCode.ORDER_STATE_FAILED:
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    queryPurchases();
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
                // if success, you can call buyWithPrice API again
                buyWithPrice(productItem);
            } else {
                Log.e(TAG, getString(R.string.cancel));
            }
            return;
        }
    }

}
