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

import android.app.Activity
import android.content.ContentValues
import android.util.Log
import android.widget.Toast
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.entity.OrderStatusCode

/**
 * Handles the exception returned from the iap api.
 *
 * @since 2019/12/9
 */
object ExceptionHandle {
    /**
     * The exception is solved.
     */
    const val SOLVED = 0

    /**
     * Handles the exception returned from the IAP API.
     *
     * @param activity The Activity to call the IAP API.
     * @param e The exception returned from the IAP API.
     * @return int
     */
    @JvmStatic
    fun handle(activity: Activity?, e: Exception): Int {
        return if (e is IapApiException) {
            val iapApiException = e
            Log.i(ContentValues.TAG, "returnCode: " + iapApiException.statusCode)
            when (iapApiException.statusCode) {
                OrderStatusCode.ORDER_STATE_CANCEL -> {
                    Toast.makeText(activity, "Order has been canceled!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_STATE_PARAM_ERROR -> {
                    Toast.makeText(activity, "Order state param error!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_STATE_NET_ERROR -> {
                    Toast.makeText(activity, "Order state net error!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_VR_UNINSTALL_ERROR -> {
                    Toast.makeText(activity, "Order vr uninstall error!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_HWID_NOT_LOGIN -> {
                    IapRequestHelper.startResolutionForResult(activity, iapApiException.status, Constants.REQ_CODE_LOGIN)
                    SOLVED
                }
                OrderStatusCode.ORDER_PRODUCT_OWNED -> {
                    Toast.makeText(activity, "Product already owned error!", Toast.LENGTH_SHORT).show()
                    OrderStatusCode.ORDER_PRODUCT_OWNED
                }
                OrderStatusCode.ORDER_PRODUCT_NOT_OWNED -> {
                    Toast.makeText(activity, "Product not owned error!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_PRODUCT_CONSUMED -> {
                    Toast.makeText(activity, "Product consumed error!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED -> {
                    Toast.makeText(activity, "Order account area not supported error!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_NOT_ACCEPT_AGREEMENT -> {
                    Toast.makeText(activity, "User does not agree the agreement", Toast.LENGTH_SHORT).show();
                    SOLVED
                }
                else -> {
                    // handle other error scenarios
                    Toast.makeText(activity, "Order unknown error!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
            }
        } else {
            Toast.makeText(activity, "external error", Toast.LENGTH_SHORT).show()
            Log.e(ContentValues.TAG, e.message)
            SOLVED
        }
    }
}