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

import com.huawei.hms.iap.entity.ProductInfo;

public class ProductItem {

    private String price;

    private String productId;

    private String productName;

    private int numOfGems;

    private int priceType;

    private String currency;

    private String country;

    private String currencySymbol;

    private boolean isCustomized;

    private ProductInfo productInfo;

    public ProductItem(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    public ProductItem(String productId) {
        this.productId = productId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setNumOfGems(int numOfGems) {
        this.numOfGems = numOfGems;
    }

    public int getNumOfGems() {
        return numOfGems;
    }

    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public void setIsCustomized(boolean isCustomized) {
        this.isCustomized = isCustomized;
    }

    public boolean getIsCustomized() {
        return isCustomized;
    }

    public void setPriceType(int priceType) {
        this.priceType = priceType;
    }

    public int getPriceType() {
        return priceType;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

}
