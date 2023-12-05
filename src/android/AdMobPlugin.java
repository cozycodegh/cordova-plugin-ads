// (c) 2023 cozycode.ca cordova-plugin-ads

package ca.cozycode.cordova.ads;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;

import android.content.Intent;
import android.util.Log;
import android.provider.Settings;
import android.app.Activity;
import android.net.Uri;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.view.Gravity;
import android.graphics.Rect;
//import android.annotation.SuppressLint;
//import android.os.Bundle;
//import android.provider.Settings;
//import android.util.Log;
//import android.view.OrientationEventListener;
//import android.view.WindowMetrics;
//import android.view.Window;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
/*import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.ads.mediation.admob.AdMobAdapter;*/
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

import ca.cozycode.cordova.ads.NextAsync;

public class AdMobPlugin extends CordovaPlugin {
    
    private static final String TAG = "AdMobPlugin";
    
    //Debug logging
    boolean mExtraDebugLoggingEnabled = false; //SET TO FALSE for app store, asks for more permissions set these in your androidManfiest.xml too
    
    //cordova plugin
    public final String PLUGIN_API_CALLS_CREATE_BANNER = "banner";
    public final String PLUGIN_API_CALLS_REMOVE_BANNER = "removeBanner";
    public final String PLUGIN_API_CALLS_CREATE_INTERSTITIAL = "interstitial";
    public final String PLUGIN_API_CALLS_READY_INTERSTITIAL = "isReadyInterstitial";
    public final String PLUGIN_API_CALLS_SHOW_INTERSTITIAL = "showInterstitial";
    public final String PLUGIN_API_CALLS_CREATE_REWARDED = "rewarded";
    public final String PLUGIN_API_CALLS_READY_REWARDED = "isReadyRewarded";
    public final String PLUGIN_API_CALLS_SHOW_REWARDED = "showRewarded";
    public final String PLUGIN_API_CALLS_CREATE_REWARDEDINTERSTITIAL = "rewardedInterstitial";
    public final String PLUGIN_API_CALLS_READY_REWARDEDINTERSTITIAL = "isReadyRewardedInterstitial";
    public final String PLUGIN_API_CALLS_SHOW_REWARDEDINTERSTITIAL = "showRewardedInterstitial";
    protected HashMap<String,String> PLUGIN_API_CALLS = new HashMap<String,String>();
    public final String PLUGIN_ERROR_CODES_INVALID_ARGUMENTS = "INVALID_ARGUMENTS";
    public final String PLUGIN_ERROR_CODES_DEVELOPER_ERROR = "PLUGIN_DEVELOPER_ERROR";
    public final String PLUGIN_ERROR_CODES_UNKNOWN_ERROR = "UNKNOWN_ERROR";
    public final String PLUGIN_ERROR_CODES_LOAD_AD_ERROR = "LOAD_AD_ERROR";
    public final String PLUGIN_ERROR_CODES_SHOW_AD_ERROR = "SHOW_AD_ERROR";
    protected HashMap<String,String> PLUGIN_ERROR_CODES = new HashMap<String,String>();
    protected boolean mInitialized = false;
    protected CallbackContext mCurrentCallbackContext;
    protected CordovaWebView mCordovaWebView;
    protected Activity mActivity;
    protected Context mContext;
    private Object mLock = new Object();
    protected boolean mAdsInitialized = false;
    
    //ads
    private ConsentInformation consentInformation;
    
    AdView mBannerAdView;
    InterstitialAd mInterstitialAd;
    RewardedAd mRewardedAd;
    RewardedInterstitialAd mRewardedInterstitialAd;
    
    RelativeLayout mBannerLayout;
    FrameLayout mBannerContainerLayout;
    RelativeLayout mInterstitialLayout;
    FrameLayout mInterstitialContainerLayout;
    
    NextAsync mBannerNext;
    NextAsync mIntersitialNext;
    NextAsync mRewarededNext;
    NextAsync mRewardedInterstitialNext;
    
    Boolean mRewardedAdRewarded = false;
    boolean mBannerTopActive = false;
    int mRewardedAdRewardedAmount = 0;
    String mRewardedAdRewardedType = "";
    Boolean mRewardedInterstitialAdRewarded = false;
    int mRewardedInterstitialAdRewardedAmount = 0;
    String mRewardedInterstitialAdRewardedType = "";
    
    /**
     * Cordova plugin
     */
    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_cordova_test);
        //cwv = (CordovaWebView) findViewById(R.id.tutorialView);
        //Config.init(this);
        //cwv.loadUrl(Config.getStartUrl());
    }*/
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        PLUGIN_API_CALLS.put(PLUGIN_API_CALLS_CREATE_BANNER,"create or update a banner ad");
        PLUGIN_API_CALLS.put(PLUGIN_API_CALLS_REMOVE_BANNER,"remove the banner ad");
        PLUGIN_API_CALLS.put(PLUGIN_API_CALLS_CREATE_INTERSTITIAL,"prepare an interstitial ad");
        PLUGIN_API_CALLS.put(PLUGIN_API_CALLS_READY_INTERSTITIAL,"check if interstitial as is ready");
        PLUGIN_API_CALLS.put(PLUGIN_API_CALLS_SHOW_INTERSTITIAL,"show a previously prepared interstitial ad");
        PLUGIN_API_CALLS.put(PLUGIN_API_CALLS_CREATE_REWARDED,"prepare an rewarded ad");
        PLUGIN_API_CALLS.put(PLUGIN_API_CALLS_READY_REWARDED,"check if rewarded ad is ready");
        PLUGIN_API_CALLS.put(PLUGIN_API_CALLS_SHOW_REWARDED,"show a previously prepared rewarded ad");
        PLUGIN_API_CALLS.put(PLUGIN_API_CALLS_CREATE_REWARDEDINTERSTITIAL,"prepare an interstitial rewarded ad");
        PLUGIN_API_CALLS.put(PLUGIN_API_CALLS_READY_REWARDEDINTERSTITIAL,"check if interstitial rewarded ad is ready");
        PLUGIN_API_CALLS.put(PLUGIN_API_CALLS_SHOW_REWARDEDINTERSTITIAL,"show a previously prepared interstitial rewarded ad");
        PLUGIN_ERROR_CODES.put(PLUGIN_ERROR_CODES_INVALID_ARGUMENTS, "invalid arguments sent to the plugin, view the documentation");
        PLUGIN_ERROR_CODES.put(PLUGIN_ERROR_CODES_DEVELOPER_ERROR, "something went wrong with the plugin, contact github issues");
        PLUGIN_ERROR_CODES.put(PLUGIN_ERROR_CODES_UNKNOWN_ERROR, "an unexpected error occurred");
        PLUGIN_ERROR_CODES.put(PLUGIN_ERROR_CODES_LOAD_AD_ERROR, "a request to load an ad failed");
        PLUGIN_ERROR_CODES.put(PLUGIN_ERROR_CODES_SHOW_AD_ERROR, "tried to show an ad that has not yet been loaded");
        if (mInitialized) throw new RuntimeException("PLUGIN ADS INITIALIZATION_ERROR: too many instances");
        super.initialize(cordova, webView);
        mInitialized = true;
        mActivity = this.cordova.getActivity();
        mContext = mActivity.getApplicationContext(); //this.cordova.getContext(); //mActivity.getApplicationContext();
        mCordovaWebView = webView;
    }
    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) {
        try {
            mCurrentCallbackContext = callbackContext;
            checkSystemWritePermissions();
            logInfo(TAG+ " "+"executing "+ action+" with "+Integer.toString(args.length())+" arguments");
            adMobInitialize();
            NextAsync next = new NextAsync(this, mActivity, callbackContext, args, action);
            if (!PLUGIN_API_CALLS.containsKey(action)){
                callbackContext.error(makeError(PLUGIN_ERROR_CODES_DEVELOPER_ERROR, "Invalid API Request: "+action));
                return false;
            }
            if (PLUGIN_API_CALLS.get(action).equals(PLUGIN_API_CALLS.get(PLUGIN_API_CALLS_CREATE_BANNER))) {
                return banner(next);
            } else if (PLUGIN_API_CALLS.get(action).equals(PLUGIN_API_CALLS.get(PLUGIN_API_CALLS_REMOVE_BANNER))) {
                return removeBanner(next);
            } else if (PLUGIN_API_CALLS.get(action).equals(PLUGIN_API_CALLS.get(PLUGIN_API_CALLS_CREATE_INTERSTITIAL))) {
                return interstitial(next);
            } else if (PLUGIN_API_CALLS.get(action).equals(PLUGIN_API_CALLS.get(PLUGIN_API_CALLS_READY_INTERSTITIAL))) {
                return isReadyInterstitial(next);
            } else if (PLUGIN_API_CALLS.get(action).equals(PLUGIN_API_CALLS.get(PLUGIN_API_CALLS_SHOW_INTERSTITIAL))) {
                return showInterstitial(next);
            } else if (PLUGIN_API_CALLS.get(action).equals(PLUGIN_API_CALLS.get(PLUGIN_API_CALLS_CREATE_REWARDED))) {
                return rewarded(next);
            } else if (PLUGIN_API_CALLS.get(action).equals(PLUGIN_API_CALLS.get(PLUGIN_API_CALLS_READY_REWARDED))) {
                return isReadyRewarded(next);
            } else if (PLUGIN_API_CALLS.get(action).equals(PLUGIN_API_CALLS.get(PLUGIN_API_CALLS_SHOW_REWARDED))) {
                return showRewarded(next);
            } else if (PLUGIN_API_CALLS.get(action).equals(PLUGIN_API_CALLS.get(PLUGIN_API_CALLS_CREATE_REWARDEDINTERSTITIAL))) {
                return rewardedInterstitial(next);
            } else if (PLUGIN_API_CALLS.get(action).equals(PLUGIN_API_CALLS.get(PLUGIN_API_CALLS_READY_REWARDEDINTERSTITIAL))) {
                return isReadyRewardedInterstitial(next);
            } else if (PLUGIN_API_CALLS.get(action).equals(PLUGIN_API_CALLS.get(PLUGIN_API_CALLS_SHOW_REWARDEDINTERSTITIAL))) {
                return showRewardedInterstitial(next);
            }
            return false;
        } catch (Exception ex){
            callbackContext.error(makeError(PLUGIN_ERROR_CODES_UNKNOWN_ERROR, ex.toString()));
            return false;
        }
    }
    @Override
    public void onDestroy() {
        mAdsInitialized = false;
        mInitialized = false;
    }
    
    /* Ads Plugin API */
    /*private String checkSupportPluginDevelopment(String adId){
        if ((new Random()).nextInt(100) <= 3) return DEVELOPER_BANNER_ID;
        return TEST_BANNER_ID;
    }*/
    private View getView(){
        if(View.class.isAssignableFrom(CordovaWebView.class)) {
            return (View) mCordovaWebView;
        }
        return mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
    }
    private boolean banner(NextAsync next){
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    //get ad
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mBannerNext = next;
                    String admob_id = next.getArgsAdMobId(true);
                    String ad_size = next.getArgsAdSize();
                    logInfo("requested banner ad: "+next.getArgsAdPositionText()+" "+ad_size+" from "+admob_id);
                    
                    String[] ad_sizes = ad_size.split("x");
                    if (ad_sizes.length < 1){
                        next.callbackContext.error(makeError(PLUGIN_ERROR_CODES_INVALID_ARGUMENTS,"Invalid AdMob ad_size argument "+ad_size+", must be format ###x### eg. 320x50"));
                        return;
                    }
                    
                    boolean newBanner = false;
                    if (mBannerAdView == null){
                        newBanner = true;
                        //mBannerAdView = findViewById(R.id.adView);
                        mBannerAdView = new AdView(mActivity);
                        mBannerAdView.setAdListener(new BannerListener());
                        //settings
                        mBannerAdView.setAdUnitId(admob_id);
                        //mBannerAdView.setAdSize(AdSize.BANNER);
                        mBannerAdView.setAdSize(new AdSize(Integer.parseInt(ad_sizes[0]),Integer.parseInt(ad_sizes[1])));
                        //mBannerAdView.setAdSize(getAdSize());
                    }
                    
                    mBannerAdView.loadAd(adRequest);
                    if (newBanner){ //errors: mBannerLayout == null && !mBannerTopActive){
                        //show banner
                        View mainView = getView();
                        ViewGroup parentView = (ViewGroup) mainView.getParent();
                        if (next.getArgsAdPositionIsTop()){
                            mBannerTopActive = true;
                            parentView.addView(mBannerAdView, 0); //top
                        } else {
                            ViewGroup rootView = (ViewGroup) mainView.getRootView();
                            RelativeLayout mBannerLayout = new RelativeLayout(mActivity);
                            rootView.addView(mBannerLayout, new LayoutParams(-1, -1));
                            mBannerLayout.bringToFront();
                            //bottom center
                            FrameLayout frameLayoutOuter = new FrameLayout(mActivity);
                            mBannerLayout.addView(frameLayoutOuter,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.BOTTOM));
                            mBannerContainerLayout = new FrameLayout(mActivity);
                            frameLayoutOuter.addView(mBannerContainerLayout,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
                            mBannerContainerLayout.addView(mBannerAdView, 0);
                            mBannerLayout.setBackgroundColor(android.R.color.black);
                        }
                        mainView.requestFocus();
                    } else {
                        logInfo("ad requested on existing banner and settings");
                    }
                } catch (Exception ex){
                    next.callbackContext.error(makeError(PLUGIN_ERROR_CODES_UNKNOWN_ERROR, ex.toString()));
                }
                //next.callbackContext.success(); // Thread-safe.
            }
        });
        return true;
    }
    private boolean removeBanner(NextAsync next){
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (mBannerContainerLayout != null && mBannerAdView != null){
                        mBannerContainerLayout.removeView(mBannerAdView);
                    }
                    if (mBannerLayout != null) {
                        View mainView = getView();
                        ViewGroup rootView = (ViewGroup) mainView.getRootView();
                        rootView.removeView(mBannerLayout);
                        mainView.requestFocus();
                        mBannerLayout = null;
                    }
                    if (mBannerTopActive){
                        View mainView = getView();
                        ViewGroup parentView = (ViewGroup) mainView.getParent();
                        parentView.removeView(mBannerAdView);
                    }
                    if (mBannerAdView != null){
                        mBannerAdView.destroy();
                        logInfo("removed banner ads");
                    }
                    mBannerTopActive = false;
                    mBannerAdView = null;
                    next.callbackContext.success(); // Thread-safe.
                } catch (Exception ex){
                    next.callbackContext.error(makeError(PLUGIN_ERROR_CODES_UNKNOWN_ERROR, ex.toString()));
                }
            }
        });
        return true;
    }
    private boolean interstitial(NextAsync next){
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mIntersitialNext = next;
                    InterstitialAd.load(mActivity,next.getArgsAdMobId(true), adRequest, new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            mInterstitialAd = interstitialAd;
                            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                                @Override
                                public void onAdClicked() {
                                    logInfo("interstitial ad was clicked.");
                                }
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    logInfo("interstitial ad dismissed fullscreen content.");
                                    if (mIntersitialNext != null){
                                        mIntersitialNext.callbackContext.success();
                                        mIntersitialNext = null;
                                    }
                                    mInterstitialAd = null; //do not show again
                                    View mainView = getView();
                                    if (mainView != null) {
                                        mainView.requestFocus();
                                    }
                                }
                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                    logInfo("instertitial ad failed to load "+adError.toString());
                                    if (mIntersitialNext != null){
                                        mIntersitialNext.OnError(PLUGIN_ERROR_CODES_LOAD_AD_ERROR, adError.getCode(), adError.getMessage(), mIntersitialNext.getArgsAdMobId()); //adError.toString()));
                                        mIntersitialNext = null;
                                    }
                                }
                                @Override
                                public void onAdImpression() {
                                    logInfo("interstitial ad recorded an impression.");
                                }
                                @Override
                                public void onAdShowedFullScreenContent() {
                                    logInfo("interstitial ad showed fullscreen content.");
                                }
                            });
                            logInfo("interstitial ad loaded");
                            if (mIntersitialNext != null){
                                mIntersitialNext.callbackContext.success();
                            }
                        }
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            mInterstitialAd = null;
                            logInfo("instertitial ad did not load "+adError.toString());
                            if (mIntersitialNext != null){
                                mIntersitialNext.OnError(PLUGIN_ERROR_CODES_LOAD_AD_ERROR, adError.getCode(), adError.getMessage(), mIntersitialNext.getArgsAdMobId()); //adError.toString()));
                                mIntersitialNext = null;
                                //if (mInterstitialAd) mInterstitialAd.setFullScreenContentCallback(null);
                            }
                        }
                    });
                } catch (Exception ex){
                    next.callbackContext.error(makeError(PLUGIN_ERROR_CODES_UNKNOWN_ERROR, ex.toString()));
                }
                //next.callbackContext.success(); // Thread-safe.
            }
        });
        return true;
    }
    private boolean isReadyInterstitial(NextAsync next){
        next.callbackContext.success(mInterstitialAd != null ? 1 : 0);
        return true;
    }
    private boolean showInterstitial(NextAsync next){
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (mInterstitialAd != null){
                        if (mIntersitialNext != null) next.setArgs(mIntersitialNext);
                        mIntersitialNext = next;
                        mInterstitialAd.show(mActivity);
                    } else {
                        next.OnError(PLUGIN_ERROR_CODES_SHOW_AD_ERROR, mIntersitialNext.getArgsAdMobId());
                    }
                } catch (Exception ex){
                    next.callbackContext.error(makeError(PLUGIN_ERROR_CODES_UNKNOWN_ERROR, ex.toString()));
                }
            }
        });
        return true;
    }
    private JSONObject rewardObject(boolean rewarded, int amount, String type){
        JSONObject ret = new JSONObject();
        try {
            ret.put("rewarded", rewarded);
            ret.put("type", type);
            ret.put("amount", amount);
        } catch (JSONException e) {
            logError("ERROR: error creating response object "+e.toString());
        }
        return ret;
    }
    private boolean rewarded(NextAsync next){
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    mRewarededNext = next;
                    mRewardedAdRewarded = false;
                    mRewardedAdRewardedAmount = 0;
                    mRewardedAdRewardedType = "";
                    AdRequest adRequest = new AdRequest.Builder().build();
                    RewardedAd.load(mActivity, next.getArgsAdMobId(true), adRequest, new RewardedAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull RewardedAd ad) {
                            mRewardedAd = ad;
                            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                              @Override
                              public void onAdClicked() {
                                  logInfo("rewarded ad was clicked");
                              }
                               @Override
                              public void onAdDismissedFullScreenContent() {
                                  logInfo("rewarded ad dismissed fullscreen content.");
                                  if (mRewarededNext != null){
                                      mRewarededNext.callbackContext.success(rewardObject(mRewardedAdRewarded,mRewardedAdRewardedAmount,mRewardedAdRewardedType));
                                      mRewarededNext = null;
                                  }
                                  mRewardedAd = null; //do not show again
                                  View mainView = getView();
                                  if (mainView != null) {
                                      mainView.requestFocus();
                                  }
                              }
                              @Override
                              public void onAdFailedToShowFullScreenContent(AdError adError) {
                                  logInfo("rewarded ad failed to load "+adError.toString());
                                  if (mRewarededNext != null){
                                      mRewarededNext.OnError(PLUGIN_ERROR_CODES_LOAD_AD_ERROR, adError.getCode(), adError.getMessage(), mRewarededNext.getArgsAdMobId()); //adError.toString()));
                                      mRewarededNext = null;
                                  }
                              }
                              @Override
                              public void onAdImpression() {
                                  logInfo("rewarded ad was recorded an impression");
                              }
                              @Override
                              public void onAdShowedFullScreenContent() {
                                  logInfo("rewarded ad was showed full screen content");
                              }
                            });
                            logInfo("rewarded ad loaded");
                            if (mRewarededNext != null){
                                mRewarededNext.callbackContext.success();
                            }
                        }
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            mRewardedAd = null;
                            logInfo("rewarded ad did not load "+adError.toString());
                            if (mRewarededNext != null){
                                mRewarededNext.OnError(PLUGIN_ERROR_CODES_LOAD_AD_ERROR, adError.getCode(), adError.getMessage(), mRewarededNext.getArgsAdMobId()); //adError.toString()));
                                mRewarededNext = null;
                                //if (mInterstitialAd) mInterstitialAd.setFullScreenContentCallback(null);
                            }
                        }
                    });
                } catch (Exception ex){
                    next.callbackContext.error(makeError(PLUGIN_ERROR_CODES_UNKNOWN_ERROR, ex.toString()));
                }
            }
        });
        return true;
    }
    private boolean isReadyRewarded(NextAsync next){
        next.callbackContext.success(mRewardedAd != null ? 1 : 0);
        return true;
    }
    private boolean showRewarded(NextAsync next){
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (mRewardedAd != null){
                        if (mRewarededNext != null) next.setArgs(mRewarededNext);
                        mRewarededNext = next;
                        mRewardedAd.show(mActivity, new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                logInfo("rewarded ad has been earned");
                                mRewardedAdRewarded = true;
                                mRewardedAdRewardedAmount = rewardItem.getAmount();
                                mRewardedAdRewardedType = rewardItem.getType();
                            }
                          });
                    } else {
                        next.OnError(PLUGIN_ERROR_CODES_SHOW_AD_ERROR, mRewarededNext.getArgsAdMobId());
                    }
                } catch (Exception ex){
                    next.callbackContext.error(makeError(PLUGIN_ERROR_CODES_UNKNOWN_ERROR, ex.toString()));
                }
            }
        });
        return true;
    }
    private boolean rewardedInterstitial(NextAsync next){
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    mRewardedInterstitialNext = next;
                    mRewardedInterstitialAdRewarded = false;
                    mRewardedInterstitialAdRewardedAmount = 0;
                    mRewardedInterstitialAdRewardedType = "";
                    AdRequest adRequest = new AdRequest.Builder().build();
                    RewardedInterstitialAd.load(mActivity, next.getArgsAdMobId(true), adRequest, new RewardedInterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull RewardedInterstitialAd ad) {
                            mRewardedInterstitialAd = ad;
                            mRewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                              @Override
                              public void onAdClicked() {
                                  logInfo("rewarded interstitial ad was clicked");
                              }
                               @Override
                              public void onAdDismissedFullScreenContent() {
                                  logInfo("rewarded interstitial ad dismissed fullscreen content.");
                                  if (mRewardedInterstitialNext != null){
                                      mRewardedInterstitialNext.callbackContext.success(rewardObject(mRewardedInterstitialAdRewarded,mRewardedInterstitialAdRewardedAmount,mRewardedInterstitialAdRewardedType));
                                      mRewardedInterstitialNext = null;
                                  }
                                  mRewardedInterstitialAd = null; //do not show again
                                  View mainView = getView();
                                  if (mainView != null) {
                                      mainView.requestFocus();
                                  }
                              }
                              @Override
                              public void onAdFailedToShowFullScreenContent(AdError adError) {
                                  logInfo("rewarded interstitial ad failed to load "+adError.toString());
                                  if (mRewardedInterstitialNext != null){
                                      mRewardedInterstitialNext.OnError(PLUGIN_ERROR_CODES_LOAD_AD_ERROR, adError.getCode(), adError.getMessage(), mRewardedInterstitialNext.getArgsAdMobId()); //adError.toString()));
                                      mRewardedInterstitialNext = null;
                                  }
                              }
                              @Override
                              public void onAdImpression() {
                                  logInfo("rewarded interstitial ad was recorded an impression");
                              }
                              @Override
                              public void onAdShowedFullScreenContent() {
                                  logInfo("rewarded interstitial ad was showed full screen content");
                              }
                            });
                            logInfo("rewarded interstitial ad loaded");
                            if (mRewardedInterstitialNext != null){
                                mRewardedInterstitialNext.callbackContext.success();
                            }
                        }
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            mRewardedInterstitialAd = null;
                            logInfo("rewarded interstitial ad did not load "+adError.toString());
                            if (mRewardedInterstitialNext != null){
                                mRewardedInterstitialNext.OnError(PLUGIN_ERROR_CODES_LOAD_AD_ERROR, adError.getCode(), adError.getMessage(), mRewardedInterstitialNext.getArgsAdMobId()); //adError.toString()));
                                mRewardedInterstitialNext = null;
                                //if (mInterstitialAd) mInterstitialAd.setFullScreenContentCallback(null);
                            }
                        }
                    });
                } catch (Exception ex){
                    next.callbackContext.error(makeError(PLUGIN_ERROR_CODES_UNKNOWN_ERROR, ex.toString()));
                }
            }
        });
        return true;
    }
    private boolean isReadyRewardedInterstitial(NextAsync next){
        next.callbackContext.success(mRewardedInterstitialAd != null ? 1 : 0);
        return true;
    }
    private boolean showRewardedInterstitial(NextAsync next){
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (mRewardedInterstitialAd != null){
                        if (mRewardedInterstitialNext != null) next.setArgs(mRewardedInterstitialNext);
                        mRewardedInterstitialNext = next;
                        mRewardedInterstitialAd.show(mActivity, new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                logInfo("rewarded interstial ad has been earned");
                                mRewardedInterstitialAdRewarded = true;
                                mRewardedInterstitialAdRewardedAmount = rewardItem.getAmount();
                                mRewardedInterstitialAdRewardedType = rewardItem.getType();
                            }
                          });
                    } else {
                        next.OnError(PLUGIN_ERROR_CODES_SHOW_AD_ERROR, mRewardedInterstitialNext.getArgsAdMobId());
                    }
                } catch (Exception ex){
                    next.callbackContext.error(makeError(PLUGIN_ERROR_CODES_UNKNOWN_ERROR, ex.toString()));
                }
            }
        });
        return true;
    }
    
    /* Google AdMob */
    private void adMobInitialize() {
      synchronized (mLock) {
        if (!mAdsInitialized) {
            // Set tag for under age of consent. false means users are not under age
            // of consent.
            ConsentRequestParameters params = new ConsentRequestParameters
            .Builder()
            .setTagForUnderAgeOfConsent(false)
            .build();
            
            /*
            consentInformation = UserMessagingPlatform.getConsentInformation(mActivity);
            consentInformation.requestConsentInfoUpdate(mActivity, params,
                (ConsentInformation.OnConsentInfoUpdateSuccessListener) () -> {
                    /Load and show the consent form.
                com.google.android.ump.UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        this,
                        (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                            if (loadAndShowError != null) {
                            // Consent gathering failed.
                            Log.w(TAG, String.format("%s: %s",
                                loadAndShowError.getErrorCode(),
                                loadAndShowError.getMessage()));
                            }// Consent has been gathered.
                            logInfo("CONSENT HAS BEEN GATHERED");
                    });
                },
                (ConsentInformation.OnConsentInfoUpdateFailureListener) requestConsentError -> {
                    // Consent gathering failed.
                    Log.w(TAG, String.format("%s: %s", requestConsentError.getErrorCode(), requestConsentError.getMessage()));
            });
            //*/
            
          MobileAds.initialize(mContext, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                mAdsInitialized = true;
            }
          });
        }
      }
    }
    //banner
    private class BannerListener extends AdListener{
        @Override
        public void onAdClicked() {
            logInfo("banner ad clicked");
            // Code to be executed when the user clicks on an ad.
        }
        @Override
        public void onAdClosed() {
            logInfo("banner ad closed");
            // Code to be executed when the user is about to return
            // to the app after tapping on an ad.
        }
        @Override
        public void onAdFailedToLoad(LoadAdError adError) {
            logInfo("banner ad did not load "+adError.toString());
            if (mBannerNext != null){
                mBannerNext.OnError(PLUGIN_ERROR_CODES_LOAD_AD_ERROR, adError.getCode(), adError.getMessage(), mBannerNext.getArgsAdMobId()); //adError.toString()));
                mBannerNext = null;
            }
            // Code to be executed when an ad request fails.
        }
        @Override
        public void onAdImpression() {
            logInfo("banner ad impression");
            // Code to be executed when an impression is recorded
            // for an ad.
        }
        @Override
        public void onAdLoaded() {
            logInfo("banner ad loaded");
            if (mBannerNext != null){
                mBannerNext.callbackContext.success();
                mBannerNext = null;
            }
            // Code to be executed when an ad finishes loading.
        }
        @Override
        public void onAdOpened() {
            logInfo("banner ad opened");
            // Code to be executed when an ad opens an overlay that
            // covers the screen.
        }
    }
    //adaptive ads
    // Determine the screen width (less decorations) to use for the ad width.
    private AdSize getAdSize() {
        /*WindowMetrics windowMetrics = mActivity.getWindowManager().getCurrentWindowMetrics();
        Rect bounds = windowMetrics.getBounds();
        float adWidthPixels = getView().getWidth();
        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0f) {
            adWidthPixels = bounds.width();
        }
        float density = mActivity.getResources().getDisplayMetrics().density;
        int adWidth = (int) (adWidthPixels / density);*/
        ViewGroup rootView = (ViewGroup) getView().getRootView();
        int rw = rootView.getWidth()/2 - 10;
        int rh = 100;
        //int rh = rootView.getHeight();
        logInfo("ADD SIZE: "+Integer.toString(rw)+"x"+Integer.toString(rh));
        return new AdSize(rw, rh);
        //return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth);
    }
    
    /* Errors */
    protected JSONObject makeError(String name) {
        return makeError(name, null, null, null, null);
    }
    protected JSONObject makeError(String name, String message) {
        return makeError(name, null, null, null, message);
    }
    protected JSONObject makeError(String name, Integer responseCode, String responseMessage, String appendStr) {
        return makeError(name, null, responseCode, responseMessage, appendStr);
    }
    protected JSONObject makeError(String name, String message, Integer responseCode, String responseMessage, String appendStr) {
        String details;
        if (!PLUGIN_ERROR_CODES.containsKey(name)){
            message = name;
            name = PLUGIN_ERROR_CODES_UNKNOWN_ERROR;
        } else if (message == null){
            message = PLUGIN_ERROR_CODES.get(name);
        }
        if (appendStr != null){
            message += " - "+appendStr;
        }
        JSONObject error = new JSONObject();
        try {
            if (name != null) error.put("name", name);
            if (message != null) error.put("message", message);
            if (responseCode != null) error.put("responseCode", (int)responseCode);
            if (responseMessage != null) error.put("responseMessage", responseMessage);
        } catch (JSONException e) {
            logError("ERROR: error creating error object "+e.toString());
        }
        logError(error.toString());
        return error;
    }
    
    /**
     * Logging
     **/
    // some devices disable writing to log
    private void checkSystemWritePermissions() {
        if (!mExtraDebugLoggingEnabled) return;
        if (Settings.System.canWrite(mContext)) {
            return;
        } else {
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + mContext.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }
    public void logInfo(String msg){
        //Log.d(TAG, "Ad info: " + msg);
        if (!mExtraDebugLoggingEnabled) return;
        System.out.println("Ad info: " + msg);
    }
    public void logError(String msg) {
        //Log.e(TAG, "Ad error: " + msg);
        if (!mExtraDebugLoggingEnabled) return;
        logInfo(TAG + "Ad error: " + msg);
    }
    public void logWarning(String msg) {
        //Log.w(TAG, "Ad warning: " + msg);
        if (!mExtraDebugLoggingEnabled) return;
        logInfo(TAG + "Ad warning: " + msg);
    }
    
}
