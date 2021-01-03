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
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.*
import com.huawei.iapdemo.activity.NonConsumptionActivity
import com.huawei.iapdemo.adapter.ProductListAdapter
import com.huawei.iapdemo.common.*
import com.iapdemo.huawei.R
import org.json.JSONException
import java.util.*

/**
 * Activity for Non-consumables.
 *
 * @since 2019/12/9
 */
class NonConsumptionActivity : AppCompatActivity() {
    private val TAG = "NonConsumptionActivity"
    private var mClient: IapClient? = null
    private var nonconsumableProductListview: ListView? = null
    private val nonconsumableProducts: MutableList<ProductInfo?> = ArrayList()
    private var productListAdapter: ProductListAdapter<*>? = null
    private var hasOwnedHiddenLevelLayout: LinearLayout? = null
    private var isHiddenLevelPurchased = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_non_consumption)
        mClient = Iap.getIapClient(this)
        initView()
        // Call the obtainOwnedPurchases API during startup to obtain the data about non-consumable products that a user has purchased.
        queryPurchases(null)
    }

    private fun initView() {
        findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
        findViewById<View>(R.id.content).visibility = View.INVISIBLE
        nonconsumableProductListview = findViewById<View>(R.id.nonconsumable_product_list) as ListView
        hasOwnedHiddenLevelLayout = findViewById<View>(R.id.layout_hasOwnedHiddenLevel) as LinearLayout
    }

    private fun queryPurchases(continuationToken: String?) {
        // Query users' purchased non-consumable products.
        IapRequestHelper.obtainOwnedPurchases(mClient, IapClient.PriceType.IN_APP_NONCONSUMABLE, continuationToken, object : IapApiCallback<OwnedPurchasesResult?> {
            override fun onSuccess(result: OwnedPurchasesResult?) {
                Log.i(TAG, "obtainOwnedPurchases, success")
                checkHiddenLevelPurchaseState(result)
                if (result != null && !TextUtils.isEmpty(result.continuationToken)) {
                    queryPurchases(result.continuationToken)
                }
            }

            override fun onFail(e: Exception?) {
                Log.e(TAG, "obtainOwnedPurchases, type=" + IapClient.PriceType.IN_APP_NONCONSUMABLE + ", " + e!!.message)
                Toast.makeText(this@NonConsumptionActivity, "get Purchases fail, " + e.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun checkHiddenLevelPurchaseState(result: OwnedPurchasesResult?) {
        if (result == null || result.inAppPurchaseDataList == null) {
            Log.i(TAG, "result is null")
            queryProducts()
            return
        }
        val inAppPurchaseDataList = result.inAppPurchaseDataList
        val inAppSignature = result.inAppSignature
        for (i in inAppPurchaseDataList.indices) {
            if (CipherUtil.doCheck(inAppPurchaseDataList[i], inAppSignature[i], CipherUtil.publicKey)) {
                try {
                    val inAppPurchaseDataBean = InAppPurchaseData(inAppPurchaseDataList[i])
                    if (inAppPurchaseDataBean.purchaseState == InAppPurchaseData.PurchaseState.PURCHASED) {
                        if (HIDDEN_LEVEL_PRODUCTID == inAppPurchaseDataBean.productId) {
                            isHiddenLevelPurchased = true
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "delivery:" + e.message)
                }
            } else {
                Log.e(TAG, "delivery:" + ", verify signature error")
            }
        }
        if (isHiddenLevelPurchased) {
            deliverProduct()
        } else {
            queryProducts()
        }
    }

    private fun deliverProduct() {
        // User has purchased hidden level.
        findViewById<View>(R.id.progressBar).visibility = View.GONE
        findViewById<View>(R.id.content).visibility = View.VISIBLE
        nonconsumableProductListview!!.visibility = View.GONE
        hasOwnedHiddenLevelLayout!!.visibility = View.VISIBLE
    }

    private fun queryProducts() {
        val productIds: MutableList<String> = ArrayList()
        productIds.add(HIDDEN_LEVEL_PRODUCTID)
        IapRequestHelper.obtainProductInfo(mClient, productIds, IapClient.PriceType.IN_APP_NONCONSUMABLE, object : IapApiCallback<ProductInfoResult?> {
            override fun onSuccess(result: ProductInfoResult?) {
                Log.i(TAG, "obtainProductInfo, success")
                if (result == null || result.productInfoList == null) {
                    Toast.makeText(this@NonConsumptionActivity, "error", Toast.LENGTH_SHORT).show()
                    return
                }
                // to show product information
                showProducts(result.productInfoList)
            }

            override fun onFail(e: Exception?) {
                Log.e(TAG, "obtainProductInfo: " + e?.message)
                Toast.makeText(this@NonConsumptionActivity, "error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showProducts(products: List<ProductInfo>) {
        findViewById<View>(R.id.progressBar).visibility = View.GONE
        findViewById<View>(R.id.content).visibility = View.VISIBLE
        nonconsumableProductListview!!.visibility = View.VISIBLE
        hasOwnedHiddenLevelLayout!!.visibility = View.GONE
        for (productInfo in products) {
            nonconsumableProducts.add(productInfo)
        }
        productListAdapter = ProductListAdapter(this, nonconsumableProducts)
        nonconsumableProductListview!!.adapter = productListAdapter
        productListAdapter!!.notifyDataSetChanged()
        nonconsumableProductListview!!.onItemClickListener = OnItemClickListener { parent, view, position, id -> gotoBuy(position) }
    }

    private fun gotoBuy(index: Int) {
        val productInfo = nonconsumableProducts[index]
        IapRequestHelper.createPurchaseIntent(mClient, productInfo!!.productId, IapClient.PriceType.IN_APP_NONCONSUMABLE, object : IapApiCallback<PurchaseIntentResult?> {
            override fun onSuccess(result: PurchaseIntentResult?) {
                if (result == null) {
                    Log.e(TAG, "result is null")
                    return
                }
                // you should pull up the page to complete the payment process
                IapRequestHelper.startResolutionForResult(this@NonConsumptionActivity, result.status, Constants.REQ_CODE_BUY)
            }

            override fun onFail(e: Exception?) {
                val errorCode = ExceptionHandle.handle(this@NonConsumptionActivity, e!!)
                if (errorCode != ExceptionHandle.SOLVED) {
                    Log.i(TAG, "createPurchaseIntent, returnCode: $errorCode")
                    when (errorCode) {
                        OrderStatusCode.ORDER_PRODUCT_OWNED -> queryPurchases(null)
                        else -> {
                        }
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        isHiddenLevelPurchased = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQ_CODE_BUY) {
            if (data == null) {
                Log.e(TAG, "data is null")
                return
            }
            val buyResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data)
            when (buyResultInfo.returnCode) {
                OrderStatusCode.ORDER_STATE_CANCEL -> Toast.makeText(this, "Order has been canceled!", Toast.LENGTH_SHORT).show()
                OrderStatusCode.ORDER_PRODUCT_OWNED -> queryPurchases(null)
                OrderStatusCode.ORDER_STATE_SUCCESS -> if (CipherUtil.doCheck(buyResultInfo.inAppPurchaseData, buyResultInfo.inAppDataSignature, CipherUtil.publicKey)) {
                    isHiddenLevelPurchased = true
                    deliverProduct()
                } else {
                    Toast.makeText(this, getString(R.string.pay_success_signfail), Toast.LENGTH_SHORT).show()
                }
                else -> {
                }
            }
        }
    }

    companion object {
        private const val HIDDEN_LEVEL_PRODUCTID = "NonCProduct01"
    }
}