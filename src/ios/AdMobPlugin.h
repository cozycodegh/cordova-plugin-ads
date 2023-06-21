// (c) 2023 cozycode.ca

#import <Cordova/CDVPlugin.h>
#import <UIKit/UIKit.h>
@import GoogleMobileAds;


@interface AdMobPlugin : CDVPlugin

@property (assign) BOOL adsInitialized;
@property (assign) BOOL requestingBannerAd;
@property (assign) BOOL requestingBannerAdPositionTop;
@property (assign) BOOL requestingInterstitialAd;
@property (assign) BOOL requestingShowInterstitialAd;
@property (assign) BOOL requestingRewardedAd;
@property (assign) BOOL requestingShowRewardedAd;
@property (assign) BOOL requestingRewardedAdRewarded;
@property (nonatomic, retain) NSString *requestingRewardedAdRewardedType;
@property (nonatomic, retain) NSDecimalNumber *requestingRewardedAdRewardedAmount;
@property (assign) BOOL requestingRewardedInterstitialAd;
@property (assign) BOOL requestingShowRewardedInterstitialAd;
@property (assign) BOOL requestingRewardedInterstitialAdRewarded;
@property (nonatomic, retain) NSString *requestingRewardedInterstitialAdRewardedType;
@property (nonatomic, retain) NSDecimalNumber *requestingRewardedInterstitialAdRewardedAmount;
@property (nonatomic, strong) GADBannerView *bannerView;
@property (nonatomic, strong) GADInterstitialAd *interstitialAd;
@property (nonatomic, strong) GADRewardedAd *rewardedAd;
@property (nonatomic, strong) GADRewardedInterstitialAd* rewardedInterstitialAd;
@property (nonatomic, retain) NSString *cdvBannerCallbackId;
@property (nonatomic, retain) NSString *cdvInterstitialCallbackId;
@property (nonatomic, retain) NSString *cdvInterstitialShowCallbackId;
@property (nonatomic, retain) NSString *cdvRewardedCallbackId;
@property (nonatomic, retain) NSString *cdvRewardedShowCallbackId;
@property (nonatomic, retain) NSString *cdvRewardedInterstitialCallbackId;
@property (nonatomic, retain) NSString *cdvRewardedInterstitialShowCallbackId;

- (void)banner:(CDVInvokedUrlCommand *)command;
- (void)removeBanner:(CDVInvokedUrlCommand *)command;
- (void)interstitial:(CDVInvokedUrlCommand *)command;
- (void)isReadyInterstitial:(CDVInvokedUrlCommand *)command;
- (void)showInterstitial:(CDVInvokedUrlCommand *)command;
- (void)rewarded:(CDVInvokedUrlCommand *)command;
- (void)isReadyRewarded:(CDVInvokedUrlCommand *)command;
- (void)showRewarded:(CDVInvokedUrlCommand *)command;
- (void)rewardedInterstitial:(CDVInvokedUrlCommand *)command;
- (void)isReadyRewardedInterstitial:(CDVInvokedUrlCommand *)command;
- (void)showRewardedInterstitial:(CDVInvokedUrlCommand *)command;


@end
