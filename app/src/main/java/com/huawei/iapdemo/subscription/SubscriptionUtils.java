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

package com.huawei.iapdemo.subscription;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.iapdemo.common.CipherUtil;
import java.util.List;
import org.json.JSONException;

/**
 * Util for Subscription function.
 *
 * @since 2019/12/9
 */
public class SubscriptionUtils {
    private static final String TAG = "SubscriptionUtils";

    /**
     * Decide whether to offer subscription service.
     *
     * @param result the OwnedPurchasesResult from IapClient.obtainOwnedPurchases
     * @param productId subscription product id
     * @return decision result
     */
    public static boolean shouldOfferService(OwnedPurchasesResult result, String productId) {
        if (null == result) {
            Log.e(TAG, "OwnedPurchasesResult is null");
            return false;
        }

        List<String> inAppPurchaseDataList = result.getInAppPurchaseDataList();
        for (String data : inAppPurchaseDataList) {
            try {
                InAppPurchaseData inAppPurchaseData = new InAppPurchaseData(data);
                if (productId.equals(inAppPurchaseData.getProductId())) {
                    int index = inAppPurchaseDataList.indexOf(data);
                    String signature = result.getInAppSignature().get(index);
                    boolean credible = CipherUtil.doCheck(data, signature, CipherUtil.getPublicKey());

                    if (credible) {
                        return inAppPurchaseData.isSubValid();
                    } else {
                        Log.e(TAG, "check the data signature fail");
                        return false;
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "parse InAppPurchaseData JSONException", e);
                return false;
            }
        }
        return false;
    }

    /**
     * Parse PurchaseResult data from intent.
     *
     * @param activity Activity
     * @param data the intent from onActivityResult
     * @return result status
     */
    public static int getPurchaseResult(Activity activity, Intent data) {
        // Parse PurchaseResult data from intent.
        PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(activity).parsePurchaseResultInfoFromIntent(data);
        if (null == purchaseResultInfo) {
            Log.e(TAG, "PurchaseResultInfo is null");
            return OrderStatusCode.ORDER_STATE_FAILED;
        }

        int returnCode = purchaseResultInfo.getReturnCode();
        String errMsg = purchaseResultInfo.getErrMsg();
        switch (returnCode) {
            case OrderStatusCode.ORDER_PRODUCT_OWNED:
                Log.w(TAG, "you have owned this product");
                return OrderStatusCode.ORDER_PRODUCT_OWNED;

            case OrderStatusCode.ORDER_STATE_SUCCESS:
                // Check whether the signature of the purchase data is valid.
                boolean credible = CipherUtil.doCheck(purchaseResultInfo.getInAppPurchaseData(), purchaseResultInfo.getInAppDataSignature(), CipherUtil
                    .getPublicKey());
                if (credible) {
                    try {
                        InAppPurchaseData inAppPurchaseData = new InAppPurchaseData(purchaseResultInfo.getInAppPurchaseData());
                        if (inAppPurchaseData.isSubValid()) {
                            return OrderStatusCode.ORDER_STATE_SUCCESS;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "parse InAppPurchaseData JSONException", e);
                        return OrderStatusCode.ORDER_STATE_FAILED;
                    }
                } else {
                    Log.e(TAG, "check the data signature fail");
                }
                return OrderStatusCode.ORDER_STATE_FAILED;

            default:
                Log.e(TAG, "returnCode: " + returnCode + " , errMsg: " + errMsg);
                return returnCode;
        }
    }
}
