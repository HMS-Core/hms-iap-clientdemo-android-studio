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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.ProductInfo
import com.huawei.iapdemo.common.ProductItem
import com.iapdemo.huawei.R

/**
 * Adapter for product list.
 *
 * @since 2019/12/9
 */
class ProductListAdapter<T : ProductInfo?>(private val mContext: Context, private val products: List<T>?) : BaseAdapter() {
    override fun getCount(): Int {
        return products!!.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val detail = products!![position]
        var holder: ProductListViewHolder? = null
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout, null)
            holder = ProductListViewHolder(convertView)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ProductListViewHolder
        }
        if (detail is ProductItem) {
            val productItem = detail as ProductItem
            holder.productName.text = productItem.productName
            holder!!.productPrice.text = productItem.currencySymbol + productItem.price
        } else {
            holder!!.productName.text = detail!!.productName
            holder.productPrice.text = detail.price
            if (detail.priceType == IapClient.PriceType.IN_APP_NONCONSUMABLE) {
                holder.imageView.visibility = View.GONE
            }
        }
        return convertView
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any? {
        return if (products != null && products.size > 0) {
            products[position]
        } else null
    }

    internal class ProductListViewHolder(view: View) {
        var productName: TextView
        var productPrice: TextView
        var imageView: ImageView

        init {
            productName = view.findViewById<View>(R.id.item_name) as TextView
            productPrice = view.findViewById<View>(R.id.item_price) as TextView
            imageView = view.findViewById<View>(R.id.item_image) as ImageView
        }
    }

}