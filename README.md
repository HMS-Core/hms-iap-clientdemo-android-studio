# HMS Core In-App Purchases Client Sample Code
English | [中文](README_ZH.md)

This section describes how to develop a client for accessing In-App Purchases (IAP) of HMS Core, helping you quickly understand the client APIs provided by IAP and how to use them.

Click [here](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050033062?ha_source=hms1) to learn more about the kit.


## Contents
  - [Introduction](#Introduction)
  - [Preparations](#Preparations)
  - [Environment Requirements](#Environment-Requirements)
  - [Result](#Result)
    - [Consumables](#Consumables)
    - [Non-Consumables](#Non-consumables)
    - [Subscriptions](#Subscriptions)
  - [Technical Support](#Technical-Support)
  - [License](#License)

## Introduction

IAP allows your users to buy three types of products, namely consumables, non-consumables, and subscriptions, in your app.

* Consumables: Such products are depleted once they are used and can be purchased again.

* Non-consumables: Such products are purchased once and never expire.

* Subscriptions: Such products provide users with ongoing access to content or services in your app. Users are charged on a recurring basis until they decide to cancel.

This demo showcases the integration process and functions of IAP using the three products.

The products in the demo are only used to demonstrate the purchase process, and will not be put into commercial use.

You can use HMS Toolkit to quickly run the sample code. HMS Toolkit supports one-stop kit integration, and provides functions such as free app debugging on remote real devices. For details about HMS Toolkit, please refer to the [HMS Toolkit documentation](https://developer.huawei.com/consumer/en/doc/development/Tools-Guides/getting-started-0000001077381096?ha_source=hms1).

## Preparations

1. Install Android Studio on your computer. Use Android Studio to open the project-level **build.gradle** file of the sample code.
2. Create an app in AppGallery Connect and configure the app information. For details, please refer to [Configuring App Information in AppGallery Connect](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/config-agc-0000001050033072?ha_source=hms1).
3. Create and configure your products in AppGallery Connect. For details, please refer to [Configuring Your Products](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/config-product-0000001050033076?ha_source=hms1).
4. Import the demo to Android Studio 3.0 or later and build the demo.
5. Configure the sample code:
  
   * Download the **agconnect-services.json** file of your app from AppGallery Connect, and add the file to the app-level directory of the demo.
   * Add the signing certificate and add configurations to the app-level **build.gradle** file.
   * Open the AndroidManifest file and change the package name to your app package name.
   * Replace **PUBLIC_KEY** in the **CipherUtil** class with the public key of your app. For details about how to obtain the public key, please refer to [Querying IAP Information](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/query-payment-info-0000001050166299?ha_source=hms1).
   * Replace the products in this demo with your products.
6. Run the sample code on an Android device or simulator.

## Environment Requirements

Android SDK 22 or later and JDK 1.8 or later are recommended.

## Result

The following screen will be displayed when the demo is running.

<img src="images/homepage.jpg" alt="demo home page" height="600"/>

### Consumables

We use gems in the game to showcase how to purchase consumables.

1. Tap **Consumable products** to view the home page for purchasing consumables. The `obtainProductInfo` API is called to obtain the detailed information about this type of products.

 <img src="images/consumable/homepage.jpg" alt="consumable demo page" height="600"/>

2. Tap **5 gems**, and the `createPurchaseIntent` API will be called to jump to the payment screen, which is supported by IAP.

    <img src="images/consumable/checkout-page.jpg" alt="consumable payment selection" height="600"/>

3. After the payment is successful, you can own more gems. The `consumeOwnedPurchase` API will be called to notify the IAP server that the products have been consumed.

    <img src="images/consumable/purchase-result.jpg" alt="gem purchase result" height="600"/>

Note: If an exception (such as network error or process termination) occurs after the payment is made, the demo will update the gem quantity when you enter the screen again. (The `obtainOwnedPurchases` API is called to obtain information about the purchased consumables, and the `consumeOwnedPurchase` API is called to consume the products.)

4. Tap **History**, and the `obtainOwnedPurchaseRecord` API will be called to obtain the purchase history.

    <img src="images/consumable/purchase-history.jpg" alt="consumable purchase history" height="600"/>

### Non-consumables

This demo uses the hidden level product as an example to demonstrate how to purchase a non-consumable product.

1. Tap **Non-consumable product** to view the home page for purchasing non-consumables. The `obtainOwnedPurchases` API will be called to obtain information about the non-consumables you have purchased.

2. If you have not purchased hidden level, the screen below will be displayed. Tap **hidden level** to start purchase. The purchase process is the same as that of consumables.

    <img src="images/non-consumable/not-purchased.jpg" alt="hidden level not purchased" height="600"/>

3. After the purchase is successful or the hidden level has been purchased before, the demo will display the hidden level that you have purchased.

    <img src="images/non-consumable/purchased.jpg" alt="hidden level have been purchased" height="600"/>

### Subscriptions

The demo uses the subscription groups Service-One and Service-Two as examples to demonstrate the purchase process of subscriptions. Each subscription group contains two products and each product contains two options: **BUY** and **ACTIVE**.

(For more information about subscriptions and subscription groups, please refer to [Subscription Functions](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/subscription-functions-0000001050130264?ha_source=hms1).

1. Tap **Auto-renewable subscription** to view the home page for purchasing subscriptions. The `obtainOwnedPurchase` API will be called to obtain information about the subscriptions. If you have subscribed to a product, the status of the product is displayed as **ACTIVE**.

    <img src="images/subscription/homepage-service-active.jpg" alt="subscription homepage with active subscription" height="600"/>

2. Tap the **BUY** button of the product to be subscribed to. The demo will call the `createPurchaseIntent` API to start purchase.

    <img src="images/subscription/payment-selection.jpg" alt="subscription payment selection" height="600"/>

   Note: Currently, only Alipay is supported for subscription purchase.

3. You will be asked to authorize the automatic renewal agreement. After the purchase is successful, the purchase result will be displayed on the screen, which is supported by IAP.

    <img src="images/subscription/payment-result.jpg" alt="subscription payment selection" height="600"/>

4. Tap **MANAGE SUBSCRIPTION**, and the subscription management screen will be displayed. This screen shows all the products you have subscribed to before, including expired ones.

    <img src="images/subscription/manage-sub.jpg" alt="subscription manage" height="600"/>

5. Tap **Happy Subscribe** to edit the subscription. You can select another subscription in the same subscription group, or click **UNSUBSCRIBE** to cancel the subscription. The subscription is available until it expires.

    <img src="images/subscription/edit-sub-plan.jpg" alt="edit subscription" height="600"/>

## Technical Support
You can visit the [Reddit community](https://www.reddit.com/r/HuaweiDevelopers/) to obtain the latest information about HMS Core and communicate with other developers.

If you have any questions about the sample code, try the following:
- Visit [Stack Overflow](https://stackoverflow.com/questions/tagged/huawei-mobile-services?tab=Votes), submit your questions, and tag them with `huawei-mobile-services`. Huawei experts will answer your questions.
- Visit the HMS Core section in the [HUAWE Developer Forum](https://forums.developer.huawei.com/forumPortal/en/home?fid=0101187876626530001?ha_source=hms1) and communicate with other developers.

If you encounter any issues when using the sample code, submit your [issues](https://github.com/HMS-Core/hms-location-demo-android-studio/issues) or submit a [pull request](https://github.com/HMS-Core/hms-location-demo-android-studio/pulls).

## License

The sample code is licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
