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

package com.huawei.iapdemo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.huawei.hms.iap.entity.InAppPurchaseData
import com.huawei.iapdemo.R
import org.json.JSONException
import org.json.JSONObject

/**
 * Adapter for bill list.
 *
 * @since 2019/12/9
 */
class BillListAdapter(private val mContext: Context, private val mBillList: List<String>) : BaseAdapter() {
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return mBillList[position]
    }

    override fun getView(position: Int, convertView: View?, rootView: ViewGroup): View? {
        var convertView = convertView
        var holder: BillListViewHolder? = null
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.bill_list_item, null)
            holder = BillListViewHolder()
            holder.orderStatus = convertView.findViewById<View>(R.id.bill_status) as TextView
            holder.productName = convertView.findViewById<View>(R.id.bill_product_name) as TextView
            holder.productPrice = convertView.findViewById<View>(R.id.bill_product_price) as TextView
            convertView.tag = holder
        } else {
            holder = convertView.tag as BillListViewHolder
        }
        val billInfo = mBillList[position]
        try {
            val billInformation = JSONObject(billInfo)
            val productName = billInformation.optString("productName")
            val productPrice = billInformation.optInt("price")
            val currency = billInformation.optString("currency")
            val orderStatus = billInformation.optInt("purchaseState")
            holder!!.productName!!.text = productName
            val productPriceNumber = (productPrice / 100).toString() + "." + productPrice % 100 + " " + currency
            holder.productPrice!!.text = productPriceNumber
            when (orderStatus) {
                InAppPurchaseData.PurchaseState.PURCHASED -> holder.orderStatus!!.setText(R.string.success_state)
                InAppPurchaseData.PurchaseState.REFUNDED -> holder.orderStatus!!.setText(R.string.refund_state)
                InAppPurchaseData.PurchaseState.CANCELED -> holder.orderStatus!!.setText(R.string.cancel_state)
                else -> holder.orderStatus!!.setText(R.string.cancel_state)
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Json error occured!")
        }
        return convertView
    }

    override fun getCount(): Int {
        return mBillList.size
    }

    class BillListViewHolder {
        var productName: TextView? = null
        var productPrice: TextView? = null
        var orderStatus: TextView? = null
    }

    companion object {
        private const val TAG = "BillListAdapter"
    }

}
