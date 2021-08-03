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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.iapdemo.huawei.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Adapter for bill list.
 *
 * @since 2019/12/9
 */
public class BillListAdapter extends BaseAdapter {

    private static final String TAG = "BillListAdapter";

    // Context instance.
    private Context mContext;

    // The list includes the purchaseData String.
    private List<String> mBillList;

    public BillListAdapter(Context context, List<String> billList) {
        mContext = context;
        mBillList = billList;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return mBillList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup rootView) {
        BillListViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.bill_list_item, null);
            holder = new BillListViewHolder();
            holder.orderStatus = (TextView) convertView.findViewById(R.id.bill_status);
            holder.productName = (TextView) convertView.findViewById(R.id.bill_product_name);
            holder.productPrice = (TextView) convertView.findViewById(R.id.bill_product_price);
            convertView.setTag(holder);
        } else {
            holder = (BillListViewHolder) convertView.getTag();
        }
        String billInfo = mBillList.get(position);
        try {
            JSONObject billInformation = new JSONObject(billInfo);
            String productName = billInformation.optString("productName");
            int productPrice = billInformation.optInt("price");
            String currency = billInformation.optString("currency");
            int orderStatus = billInformation.optInt("purchaseState");
            holder.productName.setText(productName);
            String productPriceNumber = productPrice / 100 + "." + productPrice % 100 + " " + currency;
            holder.productPrice.setText(productPriceNumber);
            switch (orderStatus) {
                case InAppPurchaseData.PurchaseState.PURCHASED:
                    holder.orderStatus.setText(R.string.success_state);
                    break;
                case InAppPurchaseData.PurchaseState.REFUNDED:
                    holder.orderStatus.setText(R.string.refund_state);
                    break;
                case InAppPurchaseData.PurchaseState.CANCELED:
                default:
                    holder.orderStatus.setText(R.string.cancel_state);
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json error occured!");
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return mBillList.size();
    }

    public static class BillListViewHolder {
        TextView productName;
        TextView productPrice;
        TextView orderStatus;
    }

}
