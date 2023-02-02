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

/**
 * Constants Class.
 *
 * @since 2019/12/9
 */
public class Constants {
    /** RequestCode for pull up the pay page */
    public static final int REQ_CODE_BUYWITHPRICE = 4001;

    /** RequestCode for pull up the pmsPay page */
    public static final int REQ_CODE_BUY = 4002;

    /** RequestCode for pull up the login page or agreement page for createPurchaseIntentWithPrice interface*/
    public static final int REQ_CODE_BUYWITHPRICE_CONTINUE = 4005;

    /** RequestCode for pull up the login page for isEnvReady interface */
    public static final int REQ_CODE_LOGIN = 2001;

}