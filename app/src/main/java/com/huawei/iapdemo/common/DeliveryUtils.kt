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
package com.huawei.iapdemo.common

import android.content.Context
import android.text.TextUtils
import java.util.*

/**
 * An util class containing methods to simulate shipping.
 *
 * @since 2019/12/9
 */
object DeliveryUtils {
    private const val PURCHASETOKEN_KEY = "purchasetokenSet"
    private const val GEMS_COUNT_KEY = "gemsCount"
    private const val DATA_NAME = "database"
    private val numOfGems: Map<String, Int?>
        private get() {
            val map: MutableMap<String, Int?> = HashMap()
            map["CProduct01"] = 5
            map["CustomizedCProduct01"] = 10
            return map
        }

    /**
     * Determine whether the purchased goods have been shipped.
     * @param context Context.
     * @param purchasetoken Generated by the Huawei payment server during product payment and returned to the app through InAppPurchaseData.
     * @return boolean
     */
    fun isDelivered(context: Context, purchasetoken: String): Boolean {
        val sharedPreferences = context.getSharedPreferences(DATA_NAME, Context.MODE_PRIVATE)
        val stringSet = sharedPreferences.getStringSet(PURCHASETOKEN_KEY, null)
        return if (stringSet != null && stringSet.contains(purchasetoken)) {
            true
        } else false
    }

    /**
     * Ship and return the shipping result.
     * @param context Context.
     * @param productId Id of the purchased product.
     * @param purchaseToken Generated by the Huawei payment server during product payment and returned to the app through InAppPurchaseData.
     * @return boolean
     */
    fun deliverProduct(context: Context, productId: String?, purchaseToken: String): Boolean {
        if (TextUtils.isEmpty(productId) || TextUtils.isEmpty(purchaseToken)) {
            return false
        }
        if (!numOfGems.containsKey(productId)) {
            return false
        }
        val sharedPreferences = context.getSharedPreferences(DATA_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        var count = sharedPreferences.getLong(GEMS_COUNT_KEY, 0)
        count += numOfGems[productId]!!
        editor.putLong(GEMS_COUNT_KEY, count)
        val stringSet = sharedPreferences.getStringSet(PURCHASETOKEN_KEY, HashSet())
        stringSet.add(purchaseToken)
        editor.putStringSet(PURCHASETOKEN_KEY, stringSet)
        return editor.commit()
    }

    /**
     * Obtain the current number of gems.
     * @param context Context.
     * @return long
     */
    fun getCountOfGems(context: Context): Long {
        val sharedPreferences = context.getSharedPreferences(DATA_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getLong(GEMS_COUNT_KEY, 0)
    }
}