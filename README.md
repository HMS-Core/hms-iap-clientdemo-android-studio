# Huawei In-App Purchases (IAP) Demo

English | [中文](readme_zh.md)

The iap_demo App demonstrates Huawei In-App Purchases (IAP) client APIs and usages. 

Documentation can be found at this 
[link](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050033062).

## Table of Content

- [Introduction](#introduction)
- [Getting Started](#getting-started)
- [Supported Environments](#supported-environments)
- [Result](#result)
  - [Purchasing consumable product](#purchasing-consumable-product)
  - [Purchasing non-consumable product](#purchasing-non-consumable-product)
  - [Purchasing auto-renewable subscription service](#purchasing-auto-renewable-subscription-service)
- [Question or issues](#question-or-issues)
- [Licensing](#licensing)

## Introduction 

Huawei In-App Purchases provides 3 types of product: consumable, non-consumable and 
auto-renewable subscription. 

* Consumable : Consumables are product that can be consumed once. When consumed, it's 
    depleted and can be purchased again.

* Non-consumable : Non-consumables can be only purchased once and do not expire. 

* Auto-renewable subscription : Once purchased, Users can access to value-added functions 
    or content in a specified period of time. The subscriptions will automatically renew on
    a recurring basis until users decide to cancel.

This demo app provides all 3 types of product to demonstrate the procedure and capability of
Huawei IAP.

Disclaimer: The demo only demonstrates the purchase procedure, and it does not have a real use of purchased products.

You also can use HMS Toolkit to quickly integrate the kit and run the demo project, as well as debug the app using a remote device for free. For details, please visit https://developer.huawei.com/consumer/en/doc/development/Tools-Guides/getting-started-0000001077381096.

## Getting Started

   1. Check whether the Android studio development environment is ready. Open the sample code project directory with file "build.gradle" in Android Studio. 

   2. Finish the configuration in AppGallery Connect. 
   See details: [Configuring AppGallery Connect](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/config-agc-0000001050033072)

   3. Add your products on the AppGallery Connect. See details: [Configuring In-App Product Information](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/config-product-0000001050033076)

   4. To build this demo, please first import the demo in the Android Studio.

   5. Configure the sample code:
      - Download the file "agconnect-services.json" of the app on AGC, and add the file to the app root directory(\app) of the demo. 
      - Add the certificate file to the project and add your configuration to  in the app-level `build.gradle` file. 
      - Open the app-level `build.gradle` file, and change the value of applicationId to your app package name.
      - Replace the PUBLIC_KEY in the CipherUtil class with the public key of your app. For details about how to obtain the public key, please refer to [Querying IAP Information](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/query-payment-info-0000001050166299).
      - Replace products in the demo with your products.

   6. Run the sample on your Android device or emulator.

## Supported Environments
* JDK version: 1.8 or later
* Android Studio version: 3.6.1 or later

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

    Note: In production you should validate the result on server side (deliver the purchase)
    before calling `consumeOwnedPurchase`.

4. Tap **History**, the demo will call the `obtainOwnedPurchaseRecord` API to obtain the purchase history.

    <img src="images/consumable/purchase-history.jpg" alt="consumable purchase history" height="600"/>

### Purchasing non-consumable product

The demo provides *hidden level* as an example of non-consumable product.

1. Tap the **Non-consumable product**, you should be able to see the home page for non-consumable demo.
    The demo will call the `obtainOwnedPurchases` API to obtain purchased non-consumable product.

2. Assuming you have not purchased the *hidden level*, you will see the following screenshot.
    Tap **hidden level** to start the purchase procedure (which is the same as purchasing consumable product).

    <img src="images/non-consumable/not-purchased.jpg" alt="hidden level not purchased" height="600"/>

3. After the purchase finishes (Or you have purchased the *hidden level* before),
    the demo will display the hidden level as purchased.
    
    <img src="images/non-consumable/purchased.jpg" alt="hidden level have been purchased" height="600"/>

### Purchasing auto-renewable subscription service

The demo provides *Service-One* and *Service-Two* as examples of subscription group,
each contains 2 options of auto-renewable subscription.

(For more detail of subscription and subscription group, see related
[documentation](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/subscription-functions-0000001050130264).)

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

## Question or issues
If you want to evaluate more about HMS Core, [r/HMSCore on Reddit](https://www.reddit.com/r/HuaweiDevelopers/) is for you to keep up with latest news about HMS Core, and to exchange insights with other developers.

If you have questions about how to use HMS samples, try the following options:
- [Stack Overflow](https://stackoverflow.com/questions/tagged/huawei-mobile-services) is the best place for any programming questions. Be sure to tag your question with 
`huawei-mobile-services`.
- [Huawei Developer Forum](https://forums.developer.huawei.com/forumPortal/en/home?fid=0101187876626530001) HMS Core Module is great for general questions, or seeking recommendations and opinions.

If you run into a bug in our samples, please submit an [issue](https://github.com/HMS-Core/hms-iap-clientdemo-android-studio/issues) to the Repository. Even better you can submit a [Pull Request](https://github.com/HMS-Core/hms-iap-clientdemo-android-studio/pulls) with a fix.

## Licensing

This demo is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
