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

package com.huawei.iapdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ProductInfo;
import com.iapdemo.huawei.R;

import java.util.List;

/**
 * Adapter for product list.
 *
 * @since 2019/12/9
 */
public class ProductListAdapter extends BaseAdapter {

    // Context instance.
    private Context mContext;

    // The list of products.
    private List<ProductInfo> productInfos;

    public ProductListAdapter(Context context, List<ProductInfo> productInfos) {
        mContext = context;
        this.productInfos = productInfos;
    }

    @Override
    public int getCount() {
        return productInfos.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ProductInfo productInfo = productInfos.get(position);
        ProductListViewHolder holder = null;
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout, null);
            holder = new ProductListViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ProductListViewHolder) convertView.getTag();
        }

        holder.productName.setText(productInfo.getProductName());
        holder.productPrice.setText(productInfo.getPrice());
        if (productInfo.getPriceType() == IapClient.PriceType.IN_APP_NONCONSUMABLE) {
            holder.imageView.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        if (productInfos != null && productInfos.size() > 0) {
            return productInfos.get(position);
        }
        return null;
    }

    static class ProductListViewHolder {
        TextView productName;
        TextView productPrice;
        ImageView imageView;

        ProductListViewHolder(View view) {
            productName = (TextView) view.findViewById(R.id.item_name);
            productPrice = (TextView) view.findViewById(R.id.item_price);
            imageView = (ImageView) view.findViewById(R.id.item_image);
        }
    }

}
