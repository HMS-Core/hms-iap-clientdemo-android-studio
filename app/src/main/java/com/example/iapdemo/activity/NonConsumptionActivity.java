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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.iapdemo.adapter.ProductListAdapter;
import com.example.iapdemo.callback.ProductInfoCallback;
import com.example.iapdemo.callback.PurchaseIntentResultCallback;
import com.example.iapdemo.callback.QueryPurchasesCallback;
import com.example.iapdemo.common.CipherUtil;
import com.example.iapdemo.common.Constants;
import com.example.iapdemo.common.ExceptionHandle;
import com.example.iapdemo.common.IapRequestHelper;
import com.example.iapdemo.common.ProductItem;
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
import com.iapdemo.huawei.R;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class NonConsumptionActivity extends Activity {
    private String TAG = "NonConsumptionActivity";

    private IapClient mClient;

    private ListView nonconsumableProductListview;
    private List<ProductItem> nonconsumableProducts = new ArrayList<ProductItem>();
    private ProductListAdapter productListAdapter;
    private LinearLayout hasOwnedHiddenLevelLayout;

    private static String HIDDEN_LEVEL_PRODUCTID = "NonCProduct01";
    private static boolean isHiddenLevelPurchased = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_consumption);
        mClient = Iap.getIapClient(this);
        initView();
        queryPurchases();
    }

    private void initView() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.content).setVisibility(View.INVISIBLE);
        nonconsumableProductListview = (ListView) findViewById(R.id.nonconsumable_product_list);
        hasOwnedHiddenLevelLayout = (LinearLayout) findViewById(R.id.layout_hasOwnedHiddenLevel);
    }

    /**
     * Call the obtainOwnedPurchases API during startup to obtain the data about non-consumable products that a user has purchased.
     */
    private void queryPurchases() {
        // Query users' purchased non-consumable products.
        IapRequestHelper.obtainOwnedPurchases(mClient, IapClient.PriceType.IN_APP_NONCONSUMABLE, new QueryPurchasesCallback() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                Log.i(TAG, "obtainOwnedPurchases, success");
                checkHiddenLevelPurchaseState(result);
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "obtainOwnedPurchases, type=" + IapClient.PriceType.IN_APP_NONCONSUMABLE + ", " + e.getMessage());
                Toast.makeText(NonConsumptionActivity.this, "get Purchases fail, " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    // To check if the HiddenLevel has been purchased.
    private void checkHiddenLevelPurchaseState(OwnedPurchasesResult result) {
        if (result == null || result.getInAppPurchaseDataList() == null) {
            Log.i(TAG, "result is null");
            queryProducts();
            return;
        }

        List<String> inAppPurchaseDataList = result.getInAppPurchaseDataList();
        List<String> inAppSignature= result.getInAppSignature();
        for (int i = 0; i < inAppPurchaseDataList.size(); i++) {
            if (CipherUtil.doCheck(inAppPurchaseDataList.get(i), inAppSignature.get(i), CipherUtil.getPublicKey())) {
                try {
                    InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseDataList.get(i));
                    if (inAppPurchaseDataBean.getPurchaseState() == InAppPurchaseData.PurchaseState.PURCHASED) {
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
            deliverProduct();
        } else {
            queryProducts();
        }
    }

    private void deliverProduct() {
        // User has purchased hidden level.
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.content).setVisibility(View.VISIBLE);

        nonconsumableProductListview.setVisibility(View.GONE);
        hasOwnedHiddenLevelLayout.setVisibility(View.VISIBLE);
    }

    private void queryProducts() {
        List<String> productIds = new ArrayList<String> ();
        productIds.add(HIDDEN_LEVEL_PRODUCTID);
        IapRequestHelper.obtainProductInfo(mClient, productIds, IapClient.PriceType.IN_APP_NONCONSUMABLE, new ProductInfoCallback() {
            @Override
            public void onSuccess(ProductInfoResult result) {
                Log.i(TAG, "obtainProductInfo, success");
                if (result == null || result.getProductInfoList() == null) {
                    Toast.makeText(NonConsumptionActivity.this, "error", Toast.LENGTH_SHORT).show();
                    return;
                }
                // to show product information
                showProducts(result.getProductInfoList());
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "obtainProductInfo: " + e.getMessage());
                Toast.makeText(NonConsumptionActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProducts(List<ProductInfo> products) {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.content).setVisibility(View.VISIBLE);

        nonconsumableProductListview.setVisibility(View.VISIBLE);
        hasOwnedHiddenLevelLayout.setVisibility(View.GONE);

        for (ProductInfo productInfo:products) {
            ProductItem productItem = new ProductItem(productInfo);
            nonconsumableProducts.add(productItem);
        }

        productListAdapter = new ProductListAdapter(this, nonconsumableProducts);
        nonconsumableProductListview.setAdapter(productListAdapter);
        productListAdapter.notifyDataSetChanged();
        nonconsumableProductListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gotoBuy(position);
            }
        });

    }

    private void gotoBuy(int index) {
        ProductItem productItem = nonconsumableProducts.get(index);
        IapRequestHelper.createPurchaseIntent(mClient, productItem.getProductInfo().getProductId(), IapClient.PriceType.IN_APP_NONCONSUMABLE, new PurchaseIntentResultCallback() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                if (result == null) {
                    Log.e(TAG, "result is null");
                    return;
                }
                // you should pull up the page to complete the payment process
                IapRequestHelper.startResolutionForResult(NonConsumptionActivity.this, result.getStatus(), Constants.REQ_CODE_BUY);
            }

            @Override
            public void onFail(Exception e) {
                int errorCode = ExceptionHandle.handle(NonConsumptionActivity.this, e);
                if (errorCode != ExceptionHandle.SOLVED) {
                    Log.i(TAG, "createPurchaseIntent, returnCode: " + errorCode);
                    // handle error scenarios
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
            PurchaseResultInfo buyResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data);
            switch(buyResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    Utils.showMessage(this,"Order has been canceled!");
                    break;
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    queryPurchases();
                    break;
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    if (CipherUtil.doCheck(buyResultInfo.getInAppPurchaseData(), buyResultInfo.getInAppDataSignature(), CipherUtil.getPublicKey())) {
                        isHiddenLevelPurchased = true;
                        deliverProduct();
                    } else {
                        Utils.showMessage(this, getString(R.string.pay_success_signfail));
                    }

                    break;
                default:
                    break;
            }
        }
    }
}
