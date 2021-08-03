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

package com.huawei.iapdemo.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An util class containing methods to simulate shipping.
 *
 * @since 2019/12/9
 */
public class DeliveryUtils {
    // Key value to store the set that stores delivered purchaseToken.
    private static final String PURCHASETOKEN_KEY = "purchasetokenSet";

    // Key value to store the number of gems.
    private static final String GEMS_COUNT_KEY = "gemsCount";

    // The name of the SharedPreferences.
    private static final String DATA_NAME = "database";

    /**
     * Obtains the map which stores the number of gems corresponding to an product.
     *
     * @return Map
     */
    private static Map<String, Integer> getNumOfGems() {
        Map<String, Integer> map = new HashMap<String, Integer>();

        map.put("CProduct01", 5);
        map.put("CProduct02", 10);

        return map;
    }

    /**
     * Determine whether the purchased goods have been shipped.
     *
     * @param context Context.
     * @param purchasetoken Generated by the Huawei payment server during product payment and returned to the app through InAppPurchaseData.
     * @return boolean
     */
    public static boolean isDelivered(Context context, String purchasetoken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(DATA_NAME, Context.MODE_PRIVATE);
        Set<String> stringSet = sharedPreferences.getStringSet(PURCHASETOKEN_KEY, null);
        if (stringSet != null && stringSet.contains(purchasetoken)) {
            return true;
        }
        return false;
    }

    /**
     * Ship and return the shipping result.
     *
     * @param context Context.
     * @param productId Id of the purchased product.
     * @param purchaseToken Generated by the Huawei payment server during product payment and returned to the app through InAppPurchaseData.
     * @return boolean
     */
    public static boolean deliverProduct(Context context, String productId, String purchaseToken) {
        if (TextUtils.isEmpty(productId) || TextUtils.isEmpty(purchaseToken)) {
            return false;
        }
        if (!getNumOfGems().containsKey(productId)) {
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(DATA_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        long count = sharedPreferences.getLong(GEMS_COUNT_KEY, 0);
        count += getNumOfGems().get(productId);
        editor.putLong(GEMS_COUNT_KEY, count);

        Set<String> stringSet = sharedPreferences.getStringSet(PURCHASETOKEN_KEY, new HashSet<String>());
        stringSet.add(purchaseToken);
        editor.putStringSet(PURCHASETOKEN_KEY, stringSet);
        return editor.commit();
    }

    /**
     * Obtain the current number of gems.
     *
     * @param context Context.
     * @return long
     */
    public static long getCountOfGems(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(DATA_NAME, Context.MODE_PRIVATE);
        long count = sharedPreferences.getLong(GEMS_COUNT_KEY, 0);
        return count;
    }

}
