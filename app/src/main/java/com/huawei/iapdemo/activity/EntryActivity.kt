/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.iapdemo.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.entity.IsEnvReadyResult
import com.huawei.hms.iap.entity.OrderStatusCode
import com.huawei.hms.iap.util.IapClientHelper
import com.huawei.iapdemo.activity.EntryActivity
import com.huawei.iapdemo.common.Constants
import com.huawei.iapdemo.common.ExceptionHandle
import com.huawei.iapdemo.common.IapApiCallback
import com.huawei.iapdemo.common.IapRequestHelper
import com.huawei.iapdemo.subscription.SubscriptionActivity
import com.iapdemo.huawei.R

/**
 * Entry Activity for the app.
 *
 * @since 2019/12/9
 */
class EntryActivity : AppCompatActivity(), View.OnClickListener {
    private var enterConsumablesTheme: Button? = null
    private var enterNonConsumablesTheme: Button? = null
    private var enterSubsribeTheme: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        queryIsReady()
    }

    /**
     * Initiating an isEnvReady request when entering the app.
     * Check if the account service country supports IAP.
     */
    private fun queryIsReady() {
        val mClient = Iap.getIapClient(this)
        IapRequestHelper.isEnvReady(mClient, object : IapApiCallback<IsEnvReadyResult?> {
            override fun onSuccess(result: IsEnvReadyResult?) {
                initView()
            }

            override fun onFail(e: Exception?) {
                Log.e(TAG, "isEnvReady fail, " + e!!.message)
                ExceptionHandle.handle(this@EntryActivity, e)
            }
        })
    }

    private fun initView() {
        setContentView(R.layout.activity_entry)
        enterConsumablesTheme = findViewById<View>(R.id.enter_consumables_scene) as Button
        enterNonConsumablesTheme = findViewById<View>(R.id.enter_non_consumables_scene) as Button
        enterSubsribeTheme = findViewById<View>(R.id.enter_subscription_scene) as Button
        enterConsumablesTheme!!.setOnClickListener(this)
        enterNonConsumablesTheme!!.setOnClickListener(this)
        enterSubsribeTheme!!.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val intent: Intent
        when (view.id) {
            R.id.enter_consumables_scene -> {
                intent = Intent(this, ConsumptionActivity::class.java)
                startActivity(intent)
            }
            R.id.enter_non_consumables_scene -> {
                intent = Intent(this, NonConsumptionActivity::class.java)
                startActivity(intent)
            }
            R.id.enter_subscription_scene -> {
                intent = Intent(this, SubscriptionActivity::class.java)
                startActivity(intent)
            }
            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQ_CODE_LOGIN) {
            // Parse the returnCode from intent.
            val returnCode = IapClientHelper.parseRespCodeFromIntent(data)
            Log.i(TAG, "onActivityResult, returnCode: $returnCode")
            if (returnCode == OrderStatusCode.ORDER_STATE_SUCCESS) {
                initView()
            } else if (returnCode == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED) {
                Toast.makeText(this@EntryActivity, "This is unavailable in your country/region.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@EntryActivity, "User cancel login.", Toast.LENGTH_LONG).show()
            }
            return
        }
    }

    companion object {
        private const val TAG = "EntryActivity"
    }
}