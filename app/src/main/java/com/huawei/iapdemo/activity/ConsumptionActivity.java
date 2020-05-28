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

import android.widget.Toast;
import com.huawei.iapdemo.adapter.ProductListAdapter;
import com.huawei.iapdemo.common.CipherUtil;
import com.huawei.iapdemo.common.Constants;
import com.huawei.iapdemo.common.ExceptionHandle;
import com.huawei.iapdemo.common.IapApiCallback;
import com.huawei.iapdemo.common.IapRequestHelper;
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
public class ConsumptionActivity extends Activity {
    private String TAG = "ConsumptionActivity";
    private TextView countTextView;

    // consumable product.
    private ListView consumableProductsListview;
    private List<ProductInfo> consumableProducts = new ArrayList<ProductInfo>();
    private ProductListAdapter adapter;
    private Button purchaseHisBtn;

    private IapClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumption);
        mClient = Iap.getIapClient(this);
        initView();
        // To check if there exists consumable products that a user has purchased but has not been delivered.
        queryPurchases(null);
    }

    private void initView() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.content).setVisibility(View.GONE);
        countTextView = (TextView) findViewById(R.id.gems_count);
        countTextView.setText(String.valueOf(DeliveryUtils.getCountOfGems(this)));
        consumableProductsListview = (ListView) findViewById(R.id.consumable_product_list1);
        consumableProductsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                buy(position);

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
                    consumableProducts = result.getProductInfoList();
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
    private void queryPurchases(String continuationToken) {
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

    private void deliverProduct(final String inAppPurchaseDataStr, final String inAppPurchaseDataSignature) {
        if (CipherUtil.doCheck(inAppPurchaseDataStr, inAppPurchaseDataSignature, CipherUtil.getPublicKey())) {
            try {
                InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseDataStr);
                if (inAppPurchaseDataBean.getPurchaseState() != InAppPurchaseData.PurchaseState.PURCHASED) {
                    return;
                }
                String purchaseToken = inAppPurchaseDataBean.getPurchaseToken();
                String productId = inAppPurchaseDataBean.getProductId();
                if (DeliveryUtils.isDelivered(ConsumptionActivity.this, purchaseToken)) {
                    Toast.makeText(this, productId + " has been delivered", Toast.LENGTH_SHORT).show();
                    IapRequestHelper.consumeOwnedPurchase(mClient, purchaseToken);
                } else {
                    if (DeliveryUtils.deliverProduct(this, productId, purchaseToken)) {
                        Log.i(TAG, "delivery success");
                        Toast.makeText(this, productId + " delivery success", Toast.LENGTH_SHORT).show();
                        updateNumberOfGems();
                        // To consume the product after successfully delivering.
                        IapRequestHelper.consumeOwnedPurchase(mClient, purchaseToken);
                    } else {
                        Log.e(TAG, productId + " delivery fail");
                        Toast.makeText(this, productId + " delivery fail", Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (JSONException e) {
                Log.e(TAG, "delivery:" + e.getMessage());
            }
        } else {
            Log.e(TAG, "delivery:" + getString(R.string.verify_signature_fail));
            Toast.makeText(this, getString(R.string.verify_signature_fail), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNumberOfGems() {
        // Update the number of gems.
        String countOfGems = String.valueOf(DeliveryUtils.getCountOfGems(ConsumptionActivity.this));
        countTextView.setText(countOfGems);
    }

    private void buy(int index) {
        ProductInfo productInfo = consumableProducts.get(index);
        IapRequestHelper.createPurchaseIntent(mClient, productInfo.getProductId(), IapClient.PriceType.IN_APP_CONSUMABLE, new IapApiCallback<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                if (result == null) {
                    Log.e(TAG, "result is null");
                    return;
                }
                Status status = result.getStatus();
                if (status == null) {
                    Log.e(TAG, "status is null");
                    return;
                }
                // You should pull up the page to complete the payment process.
                IapRequestHelper.startResolutionForResult(ConsumptionActivity.this, status, Constants.REQ_CODE_BUY);
            }

            @Override
            public void onFail(Exception e) {
                int errorCode = ExceptionHandle.handle(ConsumptionActivity.this, e);
                if (errorCode != ExceptionHandle.SOLVED) {
                    Log.e(TAG, "createPurchaseIntent, returnCode: " + errorCode);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQ_CODE_BUY) {
            if (data == null) {
                Log.e(TAG, "data is null");
                return;
            }
            PurchaseResultInfo purchaseIntentResult = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data);
            switch(purchaseIntentResult.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    Toast.makeText(this, "Order has been canceled!", Toast.LENGTH_SHORT).show();
                    break;
                case OrderStatusCode.ORDER_STATE_FAILED:
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    queryPurchases(null);
                    break;
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    deliverProduct(purchaseIntentResult.getInAppPurchaseData(), purchaseIntentResult.getInAppDataSignature());
                    break;
                default:
                    break;
            }
            return;
        }
    }

}
