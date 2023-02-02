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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.iapdemo.common.IapApiCallback;
import com.huawei.iapdemo.common.Constants;
import com.huawei.iapdemo.common.ExceptionHandle;
import com.huawei.iapdemo.common.IapRequestHelper;
import com.huawei.iapdemo.subscription.SubscriptionActivity;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.util.IapClientHelper;
import com.iapdemo.huawei.R;

/**
 * Entry Activity for the app.
 *
 * @since 2019/12/9
 */
public class EntryActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "EntryActivity";
    private Button enterConsumablesPageBtn;
    private Button enterNonConsumablesPageBtn;
    private Button enterSubsribePageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queryIsReady();
    }

    /**
     * Initiating an isEnvReady request when entering the app.
     * Check if the account service country supports IAP.
     */
    private void queryIsReady() {
        IapClient mClient = Iap.getIapClient(this);
        IapRequestHelper.isEnvReady(mClient, new IapApiCallback<IsEnvReadyResult>() {
            @Override
            public void onSuccess(IsEnvReadyResult result) {
                initView();
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "isEnvReady fail, " + e.getMessage());
                ExceptionHandle.handle(EntryActivity.this, e);
            }
        });
    }

    private void initView() {
        setContentView(R.layout.activity_entry);
        enterConsumablesPageBtn = (Button) findViewById(R.id.enter_consumables_scene);
        enterNonConsumablesPageBtn = (Button) findViewById(R.id.enter_non_consumables_scene);
        enterSubsribePageBtn = (Button) findViewById(R.id.enter_subscription_scene);

        enterConsumablesPageBtn.setOnClickListener(this);
        enterNonConsumablesPageBtn.setOnClickListener(this);
        enterSubsribePageBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.enter_consumables_scene:
                intent = new Intent(this, ConsumptionActivity.class);
                startActivity(intent);
                break;
            case R.id.enter_non_consumables_scene:
                intent = new Intent(this, NonConsumptionActivity.class);
                startActivity(intent);
                break;
            case R.id.enter_subscription_scene:
                intent = new Intent(this, SubscriptionActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQ_CODE_LOGIN) {
            // Parse the returnCode from intent.
            int returnCode = IapClientHelper.parseRespCodeFromIntent(data);
            Log.i(TAG,"onActivityResult, returnCode: " + returnCode);
            if (returnCode == OrderStatusCode.ORDER_STATE_SUCCESS) {
                initView();
            } else if(returnCode == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED) {
                Toast.makeText(EntryActivity.this, "This is unavailable in your country/region.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(EntryActivity.this, "User cancel login.", Toast.LENGTH_LONG).show();
            }
            return;
        }

    }
}
