# rewarded interstitial ads

Rewarded full screen ads which are commonly displayed at break-points in an app, rewarding a user for a longer ad. <br>
Ask the user if they want to watch an ad to get a reward before calling the showRewardedInterstitial method.

jump to: [rewarded](#rewarded-interstitial) | [showRewarded](#show-rewarded-interstitial) | [Example](#example)
<hr/>

<p align="center">
<img src="rewardedInterstitial.png" alt="rewarded interstitial ad" width="300" align="center" />
</p>

# adMob.rewarded(rewarded_id) <a id="rewarded-interstitial"></a><br>

## Usage:
```js
adMob.rewardedInterstitial(rewarded_interstitial_id).then(function () {
    // reward video ad is ready to be shown
    return adMob.showRewardedInterstitial();
}).then(function(reward){
    // finished showing rewarded ads
    if (reward.rewarded){
        // user earned reward for watching the ad
    }
}).catch (function(err){
    // view or handle error messages
});
```

## Description:
 - prepare a rewarded full screen ad
 - it may take 5 - 10 seconds to prepare
 - can use after the start up of the device (`onDeviceReady`)
 - use at break-points in the app to show full screen ads if the user chooses to view a full one in exchange for a reward that you've set

## Parameters:
` - Google AdMob id for a rewarded interstitial ad ` <br>
can be `"test"` for test ads, and ad string (`"ca-app-pub-4029587076166791/6431168058"`), or an ad_id object:
```
var admob_ids = {
    'android' : {
        'banner': "ca-app-pub-4029587076166791/6431168058",
        'interstitial': "ca-app-pub-4029587076166791/1370413062",
        'rewarded': "ca-app-pub-4029587076166791/9712771663",
        'rewardedInterstitial': "ca-app-pub-4029587076166791/3530506691"
    }, 'ios' : {
        'banner': "ca-app-pub-4029587076166791/6694891931",
        'interstitial': "ca-app-pub-4029587076166791/2436352227",
        'rewarded': "ca-app-pub-4029587076166791/5286441495",
        'rewardedInterstitial': "ca-app-pub-4029587076166791/2300620853"
    }
};
```

# adMob.showRewardedInterstitial() <a id="show-rewarded-interstitial"></a><br>

## Usage:
```js
adMob.showRewarded().then(function (reward) {
    // do anything after rewarded ad was dismissed
    // eg. unpause a game that was paused to show the full screen ad
    if (reward.rewarded){
        // give user the reward they earned
        // can view reward.amount and reward.type
    }
}).catch (function(err){
    // view or handle error messages
});
```

## Description:
 - show the rewarded full screen ad
 - use anytime after a call to rewardedInterstial to show the ad

## Parameters:
- none needed

## Returns:
```
- reward["rewarded"]        //boolean: true if completed the video reward ad 
- reward["type"]            //string: the reward type that was set on Google AdMob
- reward["amount"]          //int: the reward amount that was set on Google AdMob
```
status of the whether the rewarded video ad was watched

## Errors:
```
- error["description"]      //short description about where the error is coming from 
- error["name"]             //name of the error (LOAD_AD_ERROR, SHOW_AD_ERROR, INVALID_ARGUMENTS, etc.) 
- error["message"]          //error message, more information about the error
- error["responseCode"]     //ad error response code from Google (if there is one)
- error["responseMessage"]  //ad error response message from Google (if there is one)
```
common error names: <br>
- `LOAD_AD_ERROR` may occur when an ad id is not reconized or not ready to show ads yet
- `SHOW_AD_ERROR` will occur when an ad is called to show before it is ready

# Example <a id="example"></a><br>
```js
var ready_to_show_ad = false;
function prepareAd() {
    adMob.rewardedInterstitial(interstitial_id).then(function({
        ready_to_show_ad = true;
    }).catch(function(err){});
}
function onClickedGetReward(){
    if (!ready_to_show_ad) setTimeout(onClickedGetReward, 100);
    adMob.showRewardedInterstitial().then(function(reward){
        if (reward.rewarded){
            alert("gained "+reward.amount+" "+reward.type);
        }
    }).catch(function(err){
        alert("could not load reward ad");
    });
}
```

<hr/>

<p align="center">

[go to main](../README.md#plugin-usage)

</p>
