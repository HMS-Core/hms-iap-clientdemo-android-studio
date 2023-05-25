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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.*
import com.huawei.iapdemo.R
import com.huawei.iapdemo.adapter.ProductListAdapter
import com.huawei.iapdemo.common.*
import org.json.JSONException
import java.util.*

/**
 * Activity for Consumables.
 *
 * @since 2019/12/9
 */
class ConsumptionActivity : Activity() {
    private val TAG = "ConsumptionActivity"
    private var countTextView: TextView? = null

    // ListView for displaying consumables.
    private var productsListview: ListView? = null

    // The list of products to be purchased.
    private var products: List<ProductInfo?> = ArrayList()

    // The Adapter for productsListview.
    private var adapter: ProductListAdapter<*>? = null

    // Click this button to start the PurchaseHistoryActivity which displays information about purchased products.
    private var purchaseHisBtn: Button? = null

    // Use this IapClient instance to call the APIs of IAP.
    private var mClient: IapClient? = null

    // Record the customized product that the user is purchasing.
    private var productItem: ProductItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consumption)
        mClient = Iap.getIapClient(this)
        initView()
        // To check if there exists consumable products that a user has purchased but has not been delivered.
        queryPurchases(null)

    }

    /**
     * Initialize the UI.
     */
    private fun initView() {
        findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
        findViewById<View>(R.id.content).visibility = View.GONE
        countTextView = findViewById<View>(R.id.gems_count) as TextView
        countTextView!!.text = DeliveryUtils.getCountOfGems(this).toString()
        productsListview = findViewById<View>(R.id.consumable_product_list1) as ListView
        productsListview!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val productInfo = products[position]
            buy(productInfo!!.productId)
        }
        purchaseHisBtn = findViewById<View>(R.id.enter_purchase_his) as Button
        purchaseHisBtn!!.setOnClickListener {
            val intent = Intent(this@ConsumptionActivity, PurchaseHistoryActivity::class.java)
            startActivity(intent)
        }
        queryProducts()
    }

    /**
     * Obtains product details of products and show the products.
     */
    private fun queryProducts() {
        val productIds: MutableList<String> = ArrayList()
        productIds.add("CProduct01")
        productIds.add("CProduct02")
        IapRequestHelper.obtainProductInfo(mClient, productIds, IapClient.PriceType.IN_APP_CONSUMABLE, object : IapApiCallback<ProductInfoResult?> {
            override fun onSuccess(result: ProductInfoResult?) {
                Log.i(TAG, "obtainProductInfo, success")
                if (result == null) {
                    return
                }
                if (result.productInfoList != null) {
                    products = result.productInfoList
                }
                showProducts()
            }

            override fun onFail(e: Exception?) {
                Log.e(TAG, "obtainProductInfo: " + e!!.message)
                ExceptionHandle.handle(this@ConsumptionActivity, e)
                showProducts()
            }
        })
    }

    /**
     * Show products on the page.
     */
    private fun showProducts() {
        findViewById<View>(R.id.progressBar).visibility = View.GONE
        findViewById<View>(R.id.content).visibility = View.VISIBLE
        adapter = ProductListAdapter(this@ConsumptionActivity, products)
        productsListview!!.adapter = adapter
        adapter!!.notifyDataSetChanged()
    }

    /**
     * Call the obtainOwnedPurchases API to obtain the data about consumable products that a user has purchased but has not been delivered.
     */
    private fun queryPurchases(continuationToken: String?) {
        val tag = "obtainOwnedPurchases"
        IapRequestHelper.obtainOwnedPurchases(mClient, IapClient.PriceType.IN_APP_CONSUMABLE, continuationToken, object : IapApiCallback<OwnedPurchasesResult?> {
            override fun onSuccess(result: OwnedPurchasesResult?) {
                if (result == null) {
                    Log.e(TAG, "$tag result is null")
                    return
                }
                Log.i(TAG, "obtainOwnedPurchases, success")
                if (result.inAppPurchaseDataList != null) {
                    val inAppPurchaseDataList = result.inAppPurchaseDataList
                    val inAppSignature = result.inAppSignature
                    for (i in inAppPurchaseDataList.indices) {
                        val inAppPurchaseData = inAppPurchaseDataList[i]
                        val inAppPurchaseDataSignature = inAppSignature[i]
                        deliverProduct(inAppPurchaseData, inAppPurchaseDataSignature)
                    }
                }
                if (!TextUtils.isEmpty(result.continuationToken)) {
                    queryPurchases(result.continuationToken)
                }
            }

            override fun onFail(e: Exception?) {
                Log.e(TAG, "obtainOwnedPurchases, type=" + IapClient.PriceType.IN_APP_CONSUMABLE + ", " + e!!.message)
                ExceptionHandle.handle(this@ConsumptionActivity, e!!)
            }
        })
    }

    /**
     * Deliver the product.
     *
     * @param inAppPurchaseDataStr Includes the purchase details.
     * @param inAppPurchaseDataSignature The signature String of inAppPurchaseDataStr.
     */
    private fun deliverProduct(inAppPurchaseDataStr: String, inAppPurchaseDataSignature: String) {
        // Check whether the signature of the purchase data is valid.
        if (CipherUtil.doCheck(inAppPurchaseDataStr, inAppPurchaseDataSignature, CipherUtil.publicKey)) {
            try {
                val inAppPurchaseDataBean = InAppPurchaseData(inAppPurchaseDataStr)
                if (inAppPurchaseDataBean.purchaseState != InAppPurchaseData.PurchaseState.PURCHASED) {
                    return
                }
                val purchaseToken = inAppPurchaseDataBean.purchaseToken
                val tmpProductId = inAppPurchaseDataBean.productId
                if (DeliveryUtils.isDelivered(this@ConsumptionActivity, purchaseToken)) {
                    Toast.makeText(this@ConsumptionActivity, "$tmpProductId has been delivered", Toast.LENGTH_SHORT).show()
                    IapRequestHelper.consumeOwnedPurchase(mClient, purchaseToken)
                } else {
                    if (DeliveryUtils.deliverProduct(this, tmpProductId, purchaseToken)) {
                        Log.i(TAG, "delivery success")
                        Toast.makeText(this@ConsumptionActivity, "$tmpProductId delivery success", Toast.LENGTH_SHORT).show()
                        updateNumberOfGems()
                        // To consume the product after successfully delivering.
                        IapRequestHelper.consumeOwnedPurchase(mClient, purchaseToken)
                    } else {
                        Log.e(TAG, "$tmpProductId delivery fail")
                        Toast.makeText(this@ConsumptionActivity, "$tmpProductId delivery fail", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: JSONException) {
                Log.e(TAG, "delivery:" + e.message)
            }
        } else {
            Log.e(TAG, "delivery:" + getString(R.string.verify_signature_fail))
            Toast.makeText(this@ConsumptionActivity, getString(R.string.verify_signature_fail), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Update the number of gems on the page.
     */
    private fun updateNumberOfGems() {
        // Update the number of gems.
        val countOfGems = DeliveryUtils.getCountOfGems(this@ConsumptionActivity).toString()
        countTextView!!.text = countOfGems
    }

    /**
     * Initiate a purchase.
     *
     * @param productId Item to be purchased.
     */
    private fun buy(productId: String) {
        IapRequestHelper.createPurchaseIntent(mClient, productId, IapClient.PriceType.IN_APP_CONSUMABLE, object : IapApiCallback<PurchaseIntentResult?> {
            override fun onSuccess(result: PurchaseIntentResult?) {
                if (result == null) {
                    Log.d(TAG, "result is null")
                    return
                }
                val status = result.status
                if (status == null) {
                    Log.d(TAG, "status is null")
                    return
                }
                // You should pull up the page to complete the payment process.
                IapRequestHelper.startResolutionForResult(this@ConsumptionActivity, status, Constants.REQ_CODE_BUY)
            }

            override fun onFail(e: Exception?) {
                val errorCode = ExceptionHandle.handle(this@ConsumptionActivity, e!!)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        Log.i(TAG, "onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQ_CODE_BUY) {
            if (data == null) {
                Log.e(TAG, "data is null")
                return
            }
            // Parses payment result data.
            val purchaseResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data)
            when (purchaseResultInfo.returnCode) {
                OrderStatusCode.ORDER_STATE_CANCEL -> Toast.makeText(this@ConsumptionActivity, "Order has been canceled!", Toast.LENGTH_SHORT).show()
                OrderStatusCode.ORDER_STATE_FAILED, OrderStatusCode.ORDER_PRODUCT_OWNED -> queryPurchases(null)
                OrderStatusCode.ORDER_STATE_SUCCESS -> deliverProduct(purchaseResultInfo.inAppPurchaseData, purchaseResultInfo.inAppDataSignature)
                else -> {
                }
            }
            return
        }
    }
}
