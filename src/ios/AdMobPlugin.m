// (c) 2023 cozycode.ca

#import <CoreLocation/CLLocation.h>
#import <AppTrackingTransparency/AppTrackingTransparency.h>
#import <AdSupport/AdSupport.h>
#import <AdSupport/ASIdentifierManager.h>

#import "AdMobPlugin.h"
#import <Cordova/CDVPlugin.h>

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
@import GoogleMobileAds;
@import AppTrackingTransparency;
@import AdSupport;

#define NILABLE(obj) ((obj) != nil ? (NSObject *)(obj) : (NSObject *)[NSNull null])

@interface AdMobPlugin () <GADBannerViewDelegate,GADFullScreenContentDelegate,GADFullScreenContentDelegate>
@end

@implementation AdMobPlugin

//initialize
- (void)pluginInitialize{
    [super pluginInitialize];
}
- (void) requestAdsTrackingPermission {
    if (@available(iOS 14, *)) {
        NSLog(@"ADMOBPLUGIN: requesting tracking authorization");
        [ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status) {
            if (status == ATTrackingManagerAuthorizationStatusAuthorized)
                NSLog(@"ADMOBPLUGIN: tracking authorized");
            // Now that we are authorized we can get the IDFA
            //print(ASIdentifierManager.shared().advertisingIdentifier)
            else if (status == ATTrackingManagerAuthorizationStatusDenied)
                NSLog(@"ADMOBPLUGIN: tracking authorization denied");
            else if (status == ATTrackingManagerAuthorizationStatusNotDetermined)
                // Tracking authorization dialog has not been shown
                NSLog(@"ADMOBPLUGIN: tracking authorization unknownm, undetermined");
            else if (status == ATTrackingManagerAuthorizationStatusRestricted)
                NSLog(@"ADMOBPLUGIN: tracking authorization unknown, restricted");
            else
                NSLog(@"ADMOBPLUGIN: tracking authorization unknown");
        }];
    } else {
        [GADMobileAds.sharedInstance startWithCompletionHandler:nil];
    }
}
- (void) initializeAds {
    if(! self.adsInitialized) {
        NSLog(@"ADMOBPLUGIN: initializing ads");
        [self requestAdsTrackingPermission];
        self.adsInitialized = YES;
    }
}

//banner
- (void)banner:(CDVInvokedUrlCommand *)command {
    id adMobId = [command.arguments objectAtIndex:0];
    if (![adMobId isKindOfClass:[NSString class]]) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
            @"name": @"INVALID_ARGUMENTS",
            @"message": @"adMobId must be a string"
        }];
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    [self initializeAds];
    
    if (!self.bannerView){
        self.bannerView = [[GADBannerView alloc] initWithAdSize:GADAdSizeBanner];
        [self addBannerViewToView:self.bannerView];
        self.bannerView.adUnitID = adMobId;
        self.bannerView.rootViewController = self.viewController; //self;
        self.bannerView.delegate = self;
    }
    
    if (!self.requestingBannerAd){
        self.requestingBannerAd = true;
        self.cdvBannerCallbackId = [NSString stringWithFormat: @"%@", command.callbackId];
    }

    NSLog(@"ADMOBPLUGIN: requesting banner ad %@",adMobId);
    GADRequest *request = [GADRequest request];
    [self.bannerView loadRequest:request];
    
    //Invalid callback id received by sendPluginResult

}
- (void)removeBanner:(CDVInvokedUrlCommand *)command {
    NSLog(@"ADMOBPLUGIN: removing banner ad");
    
    if (self.bannerView){
        [self.bannerView removeFromSuperview];
        self.bannerView = nil;
    }
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

//interstitial
- (void)interstitial:(CDVInvokedUrlCommand *)command {
    id adMobId = [command.arguments objectAtIndex:0];
    if (![adMobId isKindOfClass:[NSString class]]) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
            @"name": @"INVALID_ARGUMENTS",
            @"message": @"adMobId must be a string"
        }];
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    [self initializeAds];
    
    if (!self.requestingInterstitialAd){
        self.requestingInterstitialAd = YES;
        self.interstitialAd = nil;
        self.cdvInterstitialCallbackId = [NSString stringWithFormat: @"%@", command.callbackId];
    }
    
    NSLog(@"ADMOBPLUGIN: requesting interstitial ad %@",adMobId);
    GADRequest *request = [GADRequest request];
    [GADInterstitialAd loadWithAdUnitID:adMobId
                                request:request
                      completionHandler:^(GADInterstitialAd *ad, NSError *error) {
        if (error) {
            NSLog(@"ADMOBPLUGIN: failed to prepare interstitial ad - %@",[error localizedDescription]);
            if (self.requestingInterstitialAd && self.cdvInterstitialCallbackId){
                self.requestingInterstitialAd = NO;
                CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                    @"name": @"LOAD_AD_ERROR",
                    @"message": @"a request to load an interstitial ad failed",
                    @"responseCode": @([error code]),
                    @"responseMessage": [error localizedDescription]
                }];
                [pluginResult setKeepCallbackAsBool:NO];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvInterstitialCallbackId];
                self.cdvInterstitialCallbackId = nil;
            }
        } else {
            NSLog(@"ADMOBPLUGIN: got interstitial ad %@",adMobId);
            if (self.requestingInterstitialAd && self.cdvInterstitialCallbackId){
                self.requestingInterstitialAd = NO;
                CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvInterstitialCallbackId];
                self.cdvInterstitialCallbackId = nil;
            }
            self.interstitialAd = ad;
            self.interstitialAd.fullScreenContentDelegate = self;
        }
    }];
}
- (void)isReadyInterstitial:(CDVInvokedUrlCommand *)command {
    NSLog(@"ADMOBPLUGIN: checking if interstitial ad is ready");
    
    BOOL requestingInterstitialAdReady = false;
    if (self.interstitialAd) requestingInterstitialAdReady = YES;
        
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:requestingInterstitialAdReady];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
- (void)showInterstitial:(CDVInvokedUrlCommand *)command {
    NSLog(@"ADMOBPLUGIN: showing interstitial ad");
    
    
    if (!self.requestingShowInterstitialAd){
        self.requestingShowInterstitialAd = true;
        self.cdvInterstitialShowCallbackId = [NSString stringWithFormat: @"%@", command.callbackId];
    }
    
    if (self.interstitialAd) {
       [self.interstitialAd presentFromRootViewController:self.viewController];
        
        NSLog(@"ADMOBPLUGIN: presented interstitial ad");
        //CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        //[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
     } else {
         CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
             @"name": @"SHOW_AD_ERROR",
             @"message": @"tried to show an ad that has not yet been loaded"
         }];
         [pluginResult setKeepCallbackAsBool:NO];
         [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
     }
}
//rewarded
- (void)rewarded:(CDVInvokedUrlCommand *)command {
    id adMobId = [command.arguments objectAtIndex:0];
    if (![adMobId isKindOfClass:[NSString class]]) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
            @"name": @"INVALID_ARGUMENTS",
            @"message": @"adMobId must be a string"
        }];
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    [self initializeAds];
    
    if (!self.requestingRewardedAd){
        self.requestingRewardedAd = YES;
        self.rewardedAd = nil;
        self.requestingRewardedAdRewarded = NO;
        self.requestingRewardedAdRewardedType = nil;
        self.requestingRewardedAdRewardedAmount = 0;
        self.cdvRewardedCallbackId = [NSString stringWithFormat: @"%@", command.callbackId];
    }
    
    NSLog(@"ADMOBPLUGIN: requesting rewarded ad %@",adMobId);
    
    GADRequest *request = [GADRequest request];
    [GADRewardedAd loadWithAdUnitID:adMobId
                            request:request
                  completionHandler:^(GADRewardedAd *ad, NSError *error) {
        if (error) {
            NSLog(@"ADMOBPLUGIN: failed to prepare rewarded ad - %@",[error localizedDescription]);
            if (self.requestingRewardedAd && self.cdvRewardedCallbackId){
                self.requestingRewardedAd = NO;
                CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                    @"name": @"LOAD_AD_ERROR",
                    @"message": @"a request to load a rewarded ad failed",
                    @"responseCode": @([error code]),
                    @"responseMessage": [error localizedDescription]
                }];
                [pluginResult setKeepCallbackAsBool:NO];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvRewardedCallbackId];
                self.cdvRewardedCallbackId = nil;
            }
        } else {
            NSLog(@"ADMOBPLUGIN: got rewarded ad %@",adMobId);
            if (self.requestingRewardedAd && self.cdvRewardedCallbackId){
                self.requestingRewardedAd = NO;
                CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvRewardedCallbackId];
                self.cdvRewardedCallbackId = nil;
            }
            self.rewardedAd = ad;
            self.rewardedAd.fullScreenContentDelegate = self;
        }
    }];
}
- (void)isReadyRewarded:(CDVInvokedUrlCommand *)command {
    NSLog(@"ADMOBPLUGIN: checking if rewarded ad is ready");
    
    BOOL requestingRewardedAdReady = false;
    if (self.rewardedAd) requestingRewardedAdReady = YES;
        
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:requestingRewardedAdReady];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
- (void)showRewarded:(CDVInvokedUrlCommand *)command {
    NSLog(@"ADMOBPLUGIN: showing rewarded ad");
    
    if (!self.requestingShowRewardedAd){
        self.requestingShowRewardedAd = true;
        self.cdvRewardedShowCallbackId = [NSString stringWithFormat: @"%@", command.callbackId];
    }
    
    if (self.rewardedAd) {
        [self.rewardedAd presentFromRootViewController:self.viewController
                              userDidEarnRewardHandler:^{
            GADAdReward *reward = self.rewardedAd.adReward;
            NSLog(@"ADMOBPLUGIN: earned reward of %@ %@",reward.amount,reward.type);
            if (self.requestingShowRewardedAd && self.cdvRewardedShowCallbackId){
                self.requestingShowRewardedAd = NO;
                self.requestingRewardedAdRewarded = YES;
                self.requestingRewardedAdRewardedType = reward.type;
                self.requestingRewardedAdRewardedAmount = reward.amount;
            }
        }];
        NSLog(@"ADMOBPLUGIN: presented rewarded ad");
    } else {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
            @"name": @"SHOW_AD_ERROR",
            @"message": @"tried to show an ad that has not yet been loaded"
        }];
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}
//rewarded interstitial
- (void)rewardedInterstitial:(CDVInvokedUrlCommand *)command {
    id adMobId = [command.arguments objectAtIndex:0];
    if (![adMobId isKindOfClass:[NSString class]]) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
            @"name": @"INVALID_ARGUMENTS",
            @"message": @"adMobId must be a string"
        }];
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    [self initializeAds];
    
    if (!self.requestingRewardedInterstitialAd){
        self.requestingRewardedInterstitialAd = YES;
        self.rewardedInterstitialAd = nil;
        self.cdvRewardedInterstitialCallbackId = [NSString stringWithFormat: @"%@", command.callbackId];
        self.requestingRewardedInterstitialAdRewarded = NO;
        self.requestingRewardedInterstitialAdRewardedType = nil;
        self.requestingRewardedInterstitialAdRewardedAmount = 0;
    }
    
    NSLog(@"ADMOBPLUGIN: requesting rewarded interstitial ad %@",adMobId);
    [GADRewardedInterstitialAd loadWithAdUnitID:adMobId
                                        request:[GADRequest request]
                              completionHandler:^(GADRewardedInterstitialAd* _Nullable ad, NSError* _Nullable error) {
        if (error) {
            NSLog(@"ADMOBPLUGIN: failed to prepare rewarded interstitial ad - %@",[error localizedDescription]);
            if (self.requestingRewardedInterstitialAd && self.cdvRewardedInterstitialCallbackId){
                self.requestingInterstitialAd = NO;
                CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
                    @"name": @"LOAD_AD_ERROR",
                    @"message": @"a request to load a rewarded interstitial ad failed",
                    @"responseCode": @([error code]),
                    @"responseMessage": [error localizedDescription]
                }];
                [pluginResult setKeepCallbackAsBool:NO];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvRewardedInterstitialCallbackId];
                self.cdvRewardedInterstitialCallbackId = nil;
            }
        } else {
            NSLog(@"ADMOBPLUGIN: got rewarded interstitial ad %@",adMobId);
            if (self.requestingRewardedInterstitialAd && self.cdvRewardedInterstitialCallbackId){
                self.requestingRewardedInterstitialAd = NO;
                CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvRewardedInterstitialCallbackId];
                self.cdvRewardedInterstitialCallbackId = nil;
            }
            self.rewardedInterstitialAd = ad;
            self.rewardedInterstitialAd.fullScreenContentDelegate = self;
        }
    }];
}
- (void)isReadyRewardedInterstitial:(CDVInvokedUrlCommand *)command {
    NSLog(@"ADMOBPLUGIN: checking if interstitial ad is ready");
    
    BOOL requestingRewardedInterstitialAdReady = false;
    if (self.rewardedInterstitialAd) requestingRewardedInterstitialAdReady = YES;
        
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:requestingRewardedInterstitialAdReady];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
- (void)showRewardedInterstitial:(CDVInvokedUrlCommand *)command {
    NSLog(@"ADMOBPLUGIN: showing rewarded interstitial ad");
    
    if (!self.requestingShowRewardedInterstitialAd){
        self.requestingShowRewardedInterstitialAd = true;
        self.cdvRewardedInterstitialShowCallbackId = [NSString stringWithFormat: @"%@", command.callbackId];
    }
    
    if (self.rewardedInterstitialAd) {
        [self.rewardedInterstitialAd presentFromRootViewController:self.viewController
                              userDidEarnRewardHandler:^{
            GADAdReward *reward = self.rewardedAd.adReward;
            NSLog(@"ADMOBPLUGIN: earned interstitial reward of %@ %@",reward.amount,reward.type);
            if (self.requestingShowRewardedInterstitialAd && self.cdvRewardedInterstitialShowCallbackId){
                self.requestingRewardedInterstitialAdRewarded = YES;
                self.requestingRewardedInterstitialAdRewardedType = reward.type;
                self.requestingRewardedInterstitialAdRewardedAmount = reward.amount;
            }
        }];
        NSLog(@"ADMOBPLUGIN: presented rewarded ad");
     } else {
         CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
             @"name": @"SHOW_AD_ERROR",
             @"message": @"tried to show an ad that has not yet been loaded"
         }];
         [pluginResult setKeepCallbackAsBool:NO];
         [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
     }
}

/* ad requests delegate */

//banner
- (void)addBannerViewToView:(UIView *)bannerView {
    bannerView.translatesAutoresizingMaskIntoConstraints = NO;
    [self.viewController.view addSubview:bannerView];
    [self.viewController.view addConstraints:@[
        [NSLayoutConstraint constraintWithItem:bannerView
                                     attribute:NSLayoutAttributeBottom
                                     relatedBy:NSLayoutRelationEqual
                                        toItem:self.viewController.bottomLayoutGuide //errors: view.safeAreaLayoutGuide.bottomAnchor // bottomLayoutGuide
                                     attribute:NSLayoutAttributeTop
                                    multiplier:1
                                      constant:0],
        [NSLayoutConstraint constraintWithItem:bannerView
                                     attribute:NSLayoutAttributeCenterX
                                     relatedBy:NSLayoutRelationEqual
                                        toItem:self.viewController.view
                                     attribute:NSLayoutAttributeCenterX
                                    multiplier:1
                                      constant:0]
    ]];
}
- (void)bannerViewDidReceiveAd:(GADBannerView *)bannerView {
    NSLog(@"ADMOBPLUGIN: received banner ad");
    // Add bannerView to view and add constraints as above.
    [self addBannerViewToView:self.bannerView];
    if (self.requestingBannerAd && self.cdvBannerCallbackId){
        self.requestingBannerAd = NO;
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvBannerCallbackId];
        self.cdvBannerCallbackId = nil;
    }
}
- (void)bannerView:(GADBannerView *)bannerView didFailToReceiveAdWithError:(NSError *)error {
    NSLog(@"ADMOBPLUGIN: failed to received banner ad - %@",[error localizedDescription]);
    if (self.requestingBannerAd && self.cdvBannerCallbackId){
        self.requestingBannerAd = NO;
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
            @"name": @"LOAD_AD_ERROR",
            @"message": @"a request to load an ad failed",
            @"responseCode": @([error code]),
            @"responseMessage": [error localizedDescription]
        }];
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvBannerCallbackId];
        self.cdvBannerCallbackId = nil;
    }
}
- (void)bannerViewDidRecordImpression:(GADBannerView *)bannerView {
    NSLog(@"ADMOBPLUGIN: recorded impression for banner ad");
}
- (void)bannerViewWillPresentScreen:(GADBannerView *)bannerView {
    NSLog(@"ADMOBPLUGIN: will present screen for banner ad");
}
- (void)bannerViewWillDismissScreen:(GADBannerView *)bannerView {
    NSLog(@"ADMOBPLUGIN: will dismiss screen for banner ad");
}
- (void)bannerViewDidDismissScreen:(GADBannerView *)bannerView {
    NSLog(@"ADMOBPLUGIN: dismissed screen for banner ad");
}

// interstitial & rewarded...
- (void)ad:(nonnull id<GADFullScreenPresentingAd>)ad
didFailToPresentFullScreenContentWithError:(nonnull NSError *)error {
    NSLog(@"ADMOBPLUGIN: failed to show ad - %@",[error localizedDescription]);
    if (self.requestingShowInterstitialAd && self.cdvInterstitialShowCallbackId){
        NSLog(@"ADMOBPLUGIN: failed to show interstitial ad - %@",[error localizedDescription]);
        self.requestingShowInterstitialAd = NO;
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
            @"name": @"LOAD_AD_ERROR",
            @"message": @"a request to present an interstitial ad failed",
            @"responseCode": @([error code]),
            @"responseMessage": [error localizedDescription]
        }];
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvInterstitialShowCallbackId];
        self.cdvInterstitialShowCallbackId = nil;
    }
    if (self.requestingShowRewardedAd && self.cdvRewardedShowCallbackId){
        NSLog(@"ADMOBPLUGIN: failed to show interstitial ad - %@",[error localizedDescription]);
        self.requestingShowRewardedAd = NO;
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
            @"name": @"LOAD_AD_ERROR",
            @"message": @"a request to present a rewarded ad failed",
            @"responseCode": @([error code]),
            @"responseMessage": [error localizedDescription]
        }];
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvRewardedShowCallbackId];
        self.cdvRewardedShowCallbackId = nil;
    }
    if (self.requestingShowRewardedInterstitialAd && self.cdvRewardedInterstitialShowCallbackId){
        NSLog(@"ADMOBPLUGIN: failed to show interstitial ad - %@",[error localizedDescription]);
        self.requestingShowRewardedInterstitialAd = NO;
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@{
            @"name": @"LOAD_AD_ERROR",
            @"message": @"a request to present a rewarded ad failed",
            @"responseCode": @([error code]),
            @"responseMessage": [error localizedDescription]
        }];
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvRewardedInterstitialShowCallbackId];
        self.cdvRewardedInterstitialShowCallbackId = nil;
    }
}
- (void)adWillPresentFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)ad {
    NSLog(@"ADMOBPLUGIN: ad will be shown");
}
- (void)adDidDismissFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)ad {
    NSLog(@"ADMOBPLUGIN: ad was dismissed");
    if (self.requestingShowInterstitialAd && self.cdvInterstitialShowCallbackId){
        NSLog(@"ADMOBPLUGIN: interstitial ad was dismissed");
        self.requestingShowInterstitialAd = NO;
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvInterstitialShowCallbackId];
        self.cdvInterstitialShowCallbackId = nil;
    }
    if (self.requestingShowRewardedAd && self.cdvRewardedShowCallbackId){
        NSLog(@"ADMOBPLUGIN: rewarded ad was dismissed");
        self.requestingShowRewardedAd = NO;
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{
            @"rewarded": @(self.requestingRewardedAdRewarded),
            @"type": NILABLE(self.requestingRewardedAdRewardedType),//NILABLE(nil),
            @"amount": NILABLE(self.requestingRewardedAdRewardedAmount)//@(0)
        }];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvRewardedShowCallbackId];
        self.cdvRewardedShowCallbackId = nil;
    }
    if (self.requestingShowRewardedInterstitialAd && self.cdvRewardedInterstitialShowCallbackId){
        NSLog(@"ADMOBPLUGIN: rewarded interstitial ad was dismissed");
        self.requestingShowRewardedInterstitialAd = NO;
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{
            @"rewarded": @(self.requestingRewardedInterstitialAdRewarded),
            @"type": NILABLE(self.requestingRewardedInterstitialAdRewardedType),//NILABLE(nil),
            @"amount": NILABLE(self.requestingRewardedInterstitialAdRewardedAmount)//@(0)
        }];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cdvRewardedInterstitialShowCallbackId];
        self.cdvRewardedInterstitialShowCallbackId = nil;
    }
}

@end
