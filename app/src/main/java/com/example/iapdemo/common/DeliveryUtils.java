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

package com.example.iapdemo.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeliveryUtils {

    private static final String PURCHASETOKEN_KEY = "purchasetokenSet";
    private static final String GEMS_COUNT_KEY = "gemsCount";
    private static final String DATA_NAME = "database";

    public static Map<String, Integer> getNumOfGems() {
        Map<String, Integer> map = new HashMap<String, Integer>();

        map.put("CProduct01", 5);
        map.put("CustomizedCProduct01", 10);

        return map;
    }

    public static boolean isDelivered(Context context, String purchasetoken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(DATA_NAME, Context.MODE_PRIVATE);
        Set<String> stringSet = sharedPreferences.getStringSet(PURCHASETOKEN_KEY, null);
        if (stringSet != null && stringSet.contains(purchasetoken)) {
            return true;
        }
        return false;
    }

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

    public static long getCountOfGems(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(DATA_NAME, Context.MODE_PRIVATE);
        long count = sharedPreferences.getLong(GEMS_COUNT_KEY, 0);
        return count;
    }

}
