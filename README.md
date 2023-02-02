# Huawei In-App Purchases (IAP)

The iap_demo App demonstrates Huawei In-App Purchases (IAP) client APIs and usages.

Documentation can be found at this
[link](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/introduction-0000001050033062-V5).

## Table of Content
- [Introduction](#introduction)
- [Getting Started](#getting-started)
- [Supported Environments](#supported-environments)
- [Result](#result)
  - [Purchasing consumable product](#purchasing-consumable-product)
  - [Purchasing non-consumable product](#purchasing-non-consumable-product)
  - [Purchasing auto-renewable subscription service](#purchasing-auto-renewable-subscription-service)
- [Licensing](#licensing)


## Introduction

Huawei In-App Purchases provides 3 types of product: consumable, non-consumable and
auto-renewable subscription.

* Consumable : Consumables are product that can be consumed once. When consumed, it's
    depleted and can be purchased again.

* Non-consumable : Non-consumables can be only purchased once and do not expire.

* Auto-renewable subscription : Once purchased, Users can access to value-added functions or content in a specified period of time. The subscriptions will automatically renew on a recurring basis until users decide to cancel.

This demo app provides all 3 types of product to demonstrate the procedure and capability of Huawei IAP.

Disclaimer: The demo only demonstrates the purchase procedure, and it does not have a real use of purchased products.

## Getting Started

   1. Check whether the Android studio development environment is ready. Open the sample code project directory with file "build.gradle" in Android Studio. 

   2. Finish the configuration in AppGallery Connect. 
   See details: [Configuring AppGallery Connect](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/config-agc-0000001050033072-V5)

   3. Add your products on the AppGallery Connect. See details: [Configuring In-App Product Information](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/config-product-0000001050033076-V5)

   4. To build this demo, please first import the demo in the Android Studio (3.x+).

   5. Configure the sample code:
      - Download the file "agconnect-services.json" of the app on AGC, and add the file to the app root directory(\app) of the demo. 
      - Add the certificate file to the project and add your configuration to  in the app-level `build.gradle` file. 
      - Open the `AndroidManifest` file and change the value of package to your app package name.  
      - Replace the PUBLIC_KEY in the CipherUtil class with the public key of your app. For details about how to obtain the public key, please refer to [Querying IAP Information](https://developer.huawei.com/consumer/en/doc/HMSCore-Guides-V5/query-payment-info-0000001050166299-V5).
      - Replace products in the demo with your products.

   6. Run the sample on your Android device or emulator.

## Supported Environments
* JDK version: 1.8 or later
* Android Studio version: 3.6.1 or later
  - minSdkVersion: 19 or later
  - targetSdkVersion: 30 (recommended)
  - compileSdkVersion: 30 (recommended)
  - Gradle version: 5.4.1 or later (recommended)

## Result
Once you start the demo, you should be able to see the following page.

<img src="images/homepage.jpg" alt="demo home page" height="600"/>

### Purchasing consumable product

The demo provides *gem* as an example of consumable product.

1. Tap **Consumable products**, you should be able to see the home page for consumable demo.
    The demo will call the `obtainProductInfo` API, to query the detail of managed products.
    (And also retries `consumeOwnedPurchase`, see below.)

    <img src="images/consumable/homepage.jpg" alt="consumable demo page" height="600"/>

2. Tap **5 gems**, the demo will call the `createPurchaseIntent` API,
    and jump to the checkout page which is provided by IAP Service.
    
    <img src="images/consumable/checkout-page.jpg" alt="consumable payment selection" height="600"/>

3. Once payment finishes, the consumable demo will increase user's gems counter
    and call `consumeOwnedPurchase` API to notify Huawei IAP Service that user has consumed the purchase.

    <img src="images/consumable/purchase-result.jpg" alt="gem purchase result" height="600"/>

    Note: If an exception (such as network error or process termination) occurs
    after a successful payment, the demo app will attempt to update the gem count
    when you re-enter the page. (Using `obtainOwnedPurchases` API to obtain consumable
    purchases and `consumeOwnedPurchase` to retry consuming purchases)

4. Tap **History**, the demo will call the `obtainOwnedPurchaseRecord` API to obtain the purchase history.

    <img src="images/consumable/purchase-history.jpg" alt="consumable purchase history" height="600"/>

### Purchasing non-consumable product

The demo provides *hidden level* as an example of non-consumable product.

1. Tap the **Non-consumable product**, you should be able to see the home page for non-consumable demo. The demo will call the `obtainOwnedPurchases` API to obtain purchased non-consumable product.

2. Assuming you have not purchased the *hidden level*, you will see the following screenshot. Tap **hidden level** to start the purchase procedure (which is the same as purchasing consumable product).
    <img src="images/non-consumable/not-purchased.jpg" alt="hidden level not purchased" height="600"/>

3. After the purchase finishes (Or you have purchased the *hidden level* before),
    the demo will display the hidden level as purchased.

    <img src="images/non-consumable/purchased.jpg" alt="hidden level have been purchased" height="600"/>

### Purchasing auto-renewable subscription service

The demo provides *Service-One* and *Service-Two* as examples of subscription group,
each contains 2 options of auto-renewable subscription.

(For more detail of subscription and subscription group, see related
[documentation](https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/iap-subscription-scenario).)

1. Tap the **Auto-renewable subscription**, you should be able to see the home page for auto-renewable subscription demo.
    The demo will call the `obtainOwnedPurchase` API to obtain purchased subscription product.
    Active subscription will be displayed as ACTIVE.

    <img src="images/subscription/homepage-service-active.jpg" alt="subscription homepage with active subscription" height="600"/>

2. Tap one of **BUY** buttons, the demo will start the purchase procedure by calling `createPurchaseIntent`.

    <img src="images/subscription/payment-selection.jpg" alt="subscription payment selection" height="600"/>

    Note: currently we only support Alipay for subscription payment.

3. You will be prompted to authorize automatic fee deduction agreement.
    Once purchase succeed, IAP Service will display the purchase result.

    <img src="images/subscription/payment-result.jpg" alt="subscription payment selection" height="600"/>

4. Tap **Manage Subscription**, the demo will jump to subscription manage page.
    The page will list all subscribed products, including expired subscriptions.

    <img src="images/subscription/manage-sub.jpg" alt="subscription manage" height="600"/>

5. Tap **Happy Subscribe** on Subscription manage page, you will be able to edit subscription and choose other subscription options
    in same subscription group, or tap **UNSUBSCRIBE** to cancel the subscription. The subscription will remain valid until the expire
    date.

    <img src="images/subscription/edit-sub-plan.jpg" alt="edit subscription" height="600"/>

## Licensing

This demo is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
