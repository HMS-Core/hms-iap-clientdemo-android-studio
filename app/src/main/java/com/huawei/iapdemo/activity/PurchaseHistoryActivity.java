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

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.iapdemo.R;
import com.huawei.iapdemo.adapter.BillListAdapter;
import com.huawei.iapdemo.common.CipherUtil;
import com.huawei.iapdemo.common.ExceptionHandle;
import com.huawei.iapdemo.common.IapApiCallback;
import com.huawei.iapdemo.common.IapRequestHelper;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for purchase recored.
 *
 * @since 2019/12/9
 */
public class PurchaseHistoryActivity extends AppCompatActivity {

    private String TAG = "PurchaseHistoryActivity";

    // ListView for displaying the purchased products.
    private ListView billListView;

    // The list includes the purchase data String.
    List<String> billList = new ArrayList<String>();

    // A data location flag for a query in pagination mode.
    private static String continuationToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_history);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.bill_listview).setVisibility(View.GONE);
        billListView = findViewById(R.id.bill_listview);
        setTitle(R.string.purchase_history_title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryPurchasedConsumables();
    }

    /**
     * Obtains the purchase data of all purchased and consumed consumables.
     */
    private void queryPurchasedConsumables() {
        IapClient iapClient = Iap.getIapClient(this);
        IapRequestHelper.obtainOwnedPurchaseRecord(iapClient, IapClient.PriceType.IN_APP_CONSUMABLE, continuationToken, new IapApiCallback<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                Log.i(TAG, "obtainOwnedPurchaseRecord, success");
                List<String> inAppPurchaseDataList = result.getInAppPurchaseDataList();
                List<String> signatureList = result.getInAppSignature();
                if (inAppPurchaseDataList == null) {
                    showBillList();
                    return;
                }
                Log.i(TAG, "list size: " + inAppPurchaseDataList.size());
                for (int i = 0; i < signatureList.size(); i++) {
                    // Check whether the signature of the purchase data is valid.
                    boolean success = CipherUtil.doCheck(inAppPurchaseDataList.get(i), signatureList.get(i), CipherUtil.getPublicKey());
                    if (success) {
                        billList.add(inAppPurchaseDataList.get(i));
                    }
                }
                continuationToken = result.getContinuationToken();
                // If the continuationToken is not empty, you need to continue the query to get all purchase data.
                if (!TextUtils.isEmpty(continuationToken)) {
                    queryPurchasedConsumables();
                } else {
                    showBillList();
                }

            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "obtainOwnedPurchaseRecord, " + e.getMessage());
                ExceptionHandle.handle(PurchaseHistoryActivity.this, e);
                showBillList();
            }
        });
    }

    /**
     * Displays the purchased product list.
     */
    private void showBillList() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.bill_listview).setVisibility(View.VISIBLE);
        Log.i(TAG, "onFinish");
        BillListAdapter billAdapter = new BillListAdapter(PurchaseHistoryActivity.this, billList);
        billListView.setAdapter(billAdapter);
    }

}
