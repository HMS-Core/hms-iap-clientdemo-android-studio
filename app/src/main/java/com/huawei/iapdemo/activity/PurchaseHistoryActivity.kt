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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ListView
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.OwnedPurchasesResult
import com.huawei.iapdemo.activity.PurchaseHistoryActivity
import com.huawei.iapdemo.adapter.BillListAdapter
import com.huawei.iapdemo.common.CipherUtil
import com.huawei.iapdemo.common.ExceptionHandle
import com.huawei.iapdemo.common.IapApiCallback
import com.huawei.iapdemo.common.IapRequestHelper
import com.iapdemo.huawei.R
import java.util.*

/**
 * Activity for purchase recored.
 *
 * @since 2019/12/9
 */
class PurchaseHistoryActivity : AppCompatActivity() {
    private val TAG = "PurchaseHistoryActivity"
    private var billListView: ListView? = null
    var billList: MutableList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_history)
        findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
        findViewById<View>(R.id.bill_listview).visibility = View.GONE
        billListView = findViewById(R.id.bill_listview)
        setTitle(R.string.purchase_history_title)
    }

    override fun onResume() {
        super.onResume()
        queryHistoryInterface()
    }

    private fun queryHistoryInterface() {
        val iapClient = Iap.getIapClient(this)
        IapRequestHelper.obtainOwnedPurchaseRecord(iapClient, IapClient.PriceType.IN_APP_CONSUMABLE, continuationToken, object : IapApiCallback<OwnedPurchasesResult?> {
            override fun onSuccess(result: OwnedPurchasesResult?) {
                Log.i(TAG, "obtainOwnedPurchaseRecord, success")
                val inAppPurchaseDataList = result!!.inAppPurchaseDataList
                val signatureList = result.inAppSignature
                if (inAppPurchaseDataList == null) {
                    onFinish()
                    return
                }
                Log.i(TAG, "list size: " + inAppPurchaseDataList.size)
                for (i in signatureList.indices) {
                    val success = CipherUtil.doCheck(inAppPurchaseDataList[i], signatureList[i], CipherUtil.publicKey)
                    if (success) {
                        billList.add(inAppPurchaseDataList[i])
                    }
                }
                continuationToken = result.continuationToken
                if (!TextUtils.isEmpty(continuationToken)) {
                    queryHistoryInterface()
                } else {
                    onFinish()
                }
            }

            override fun onFail(e: Exception?) {
                Log.e(TAG, "obtainOwnedPurchaseRecord, " + e!!.message)
                ExceptionHandle.handle(this@PurchaseHistoryActivity, e)
                onFinish()
            }
        })
    }

    private fun onFinish() {
        findViewById<View>(R.id.progressBar).visibility = View.GONE
        findViewById<View>(R.id.bill_listview).visibility = View.VISIBLE
        Log.i(TAG, "onFinish")
        val billAdapter = BillListAdapter(this@PurchaseHistoryActivity, billList)
        billListView!!.adapter = billAdapter
    }

    companion object {
        private var continuationToken: String? = null
    }
}