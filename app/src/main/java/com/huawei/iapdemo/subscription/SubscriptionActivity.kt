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

package com.huawei.iapdemo.subscription

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.huawei.hms.iap.entity.OrderStatusCode
import com.huawei.hms.iap.entity.OwnedPurchasesResult
import com.huawei.hms.iap.entity.ProductInfo
import com.huawei.iapdemo.common.Constants
import com.huawei.iapdemo.subscription.SubscriptionContract.Presenter
import com.iapdemo.huawei.R
import java.util.*

/**
 * Activity for auto-renewable subscriptions.
 *
 * @since 2019/12/9
 */
class SubscriptionActivity : Activity(), SubscriptionContract.View {
    // Presenter of this page, which is used to process data interactions.
    private var presenter: Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)
        findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
        findViewById<View>(R.id.content).visibility = View.INVISIBLE
        val list = Arrays.asList(*SUBSCRIPTION_PRODUCT)
        presenter = SubscriptionPresenter(this)
        presenter!!.load(list)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQ_CODE_BUY) {
            if (resultCode == RESULT_OK) {
                val purchaseResult = SubscriptionUtils.getPurchaseResult(this, data)
                if (OrderStatusCode.ORDER_STATE_SUCCESS == purchaseResult) {
                    Toast.makeText(this, R.string.pay_success, Toast.LENGTH_SHORT).show()
                    presenter!!.refreshSubscription()
                    return
                }
                if (OrderStatusCode.ORDER_STATE_CANCEL == purchaseResult) {
                    Toast.makeText(this, R.string.cancel, Toast.LENGTH_SHORT).show()
                    return
                }
                Toast.makeText(this, R.string.pay_fail, Toast.LENGTH_SHORT).show()
            } else {
                Log.i(TAG, "cancel subscribe")
                Toast.makeText(this, R.string.cancel, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Show subscription products.
     *
     * @param productInfos Product list.
     */
    override fun showProducts(productInfos: List<ProductInfo>?) {
        if (null == productInfos) {
            Toast.makeText(this, R.string.external_error, Toast.LENGTH_SHORT).show()
            return
        }
        for (productInfo in productInfos) {
            showProduct(productInfo)
        }
        findViewById<View>(R.id.progressBar).visibility = View.GONE
        findViewById<View>(R.id.content).visibility = View.VISIBLE
    }

    /**
     * Update product purchase status.
     *
     * @param ownedPurchasesResult Purchases result.
     */
    override fun updateProductStatus(ownedPurchasesResult: OwnedPurchasesResult?) {
        for (productId in SUBSCRIPTION_PRODUCT) {
            val view = getView(productId)
            val button = view!!.findViewById<Button>(R.id.action)
            button.tag = productId
            // Check whether to offer subscription service.
            if (SubscriptionUtils.shouldOfferService(ownedPurchasesResult, productId)) {
                // Offer subscription service.
                button.setText(R.string.active)
                button.setOnClickListener(detailActionListener)
            } else {
                // Show the purchase Entry.
                button.setText(R.string.buy)
                button.setOnClickListener(buyActionListener)
            }
        }
    }

    /**
     * Jump to manage subscription page.
     *
     * @param view The view which has been clicked.
     */
    fun manageSubscription(view: View?) {
        presenter!!.showSubscription("")
    }

    /**
     * Show products on the page.
     *
     * @param productInfo Contains details of a product.
     */
    private fun showProduct(productInfo: ProductInfo) {
        val view = getView(productInfo.productId)
        if (view != null) {
            val productName = view.findViewById<TextView>(R.id.product_name)
            val productDesc = view.findViewById<TextView>(R.id.product_desc)
            val price = view.findViewById<TextView>(R.id.price)
            productName.text = productInfo.productName
            productDesc.text = productInfo.productDesc
            price.text = productInfo.price
        }
    }

    /**
     * Obtains the layout of the product.
     *
     * @param productId ProductId of the product.
     *
     * @return View
     */
    private fun getView(productId: String): View? {
        var view: View? = null
        if (SUBSCRIPTION_PRODUCT[0] == productId) {
            view = findViewById(R.id.service_one_product_one)
        }
        if (SUBSCRIPTION_PRODUCT[1] == productId) {
            view = findViewById(R.id.service_one_product_two)
        }
        if (SUBSCRIPTION_PRODUCT[2] == productId) {
            view = findViewById(R.id.service_two_product_one)
        }
        if (SUBSCRIPTION_PRODUCT[3] == productId) {
            view = findViewById(R.id.service_two_product_two)
        }
        return view
    }

    override val activity: Activity
        get() = this

    /**
     * Obtains the listener of the BUY button.
     *
     * @return View.OnClickListener
     */
    private val buyActionListener: View.OnClickListener
        private get() = View.OnClickListener { v ->
            val data = v.tag
            if (data is String) {
                presenter!!.buy(data)
            }
        }

    /**
     * Obtains the listener of the ACTIVE button.
     *
     * @return View.OnClickListener
     */
    private val detailActionListener: View.OnClickListener
        private get() = View.OnClickListener { v ->
            val data = v.tag
            if (data is String) {
                presenter!!.showSubscription(data)
            }
        }

    companion object {
        private const val TAG = "SubscriptionActivity"
        private val SUBSCRIPTION_PRODUCT = arrayOf("demosub101", "demosub102", "demosub201", "demosub202")
    }
}