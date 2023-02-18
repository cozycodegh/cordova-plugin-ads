# rewarded ads

Rewarded video ads which are commonly displayed at break-points in an app, rewarding a user for a longer ad. <br>
Rewarded interstitial ads are currently better supported. <br>
Ask the user if they want to watch an ad to get a reward before calling the showRewarded method. <br>

jump to: [rewarded](#rewarded) | [showRewarded](#show-rewarded) | [Example](#example)
<hr/>

<p align="center">
<img src="rewarded.png" alt="rewarded ad" width="300" align="center" />
</p>

# adMob.rewarded(rewarded_id) <a id="rewarded"></a><br>

## Usage:
```js
adMob.rewarded(rewarded_id).then(function () {
    // reward video ad is ready to be shown
    return adMob.showRewarded();
}).then(function(reward){
    alert("showed rewarded ads"+JSON.stringify(reward));
    if (reward.rewarded){
        alert("gained "+reward.amount+" "+reward.type);
    }
}).catch (function(err){
    // view or handle error messages
});
```

## Description:
 - prepare a rewarded video ad
 - it may take seconds to prepare, but currently on iOS the video loads right away
 - can use after the start up of the device (`onDeviceReady`)
 - use at break-points in the app to show full screen ads if the user chooses to view a full one in exchange for a reward that you've set

## Parameters:
` - Google AdMob id for a rewarded ad ` <br>
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

# adMob.showRewarded() <a id="show-rewarded"></a><br>

## Usage:
```js
adMob.showRewarded().then(function (reward) {
    // do anything after rewarded ad was dismissed
    // eg. unpause a game that was paused to show the full screen ad
    if (reward.rewarded){
        alert("gained "+reward.amount+" "+reward.type);
    }
}).catch (function(err){
    // view or handle error messages
});
```

## Description:
 - show the rewarded ad
 - use right after a call to rewarded to show the ad

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
- `LOAD_AD_ERROR` may occur when an ad id is not reconized or not ready to show ads yet <br>
- `SHOW_AD_ERROR` will occur when an ad is called to show before it is ready

# Example <a id="example"></a><br>
```js
function onClickedGetReward(){
    adMob.rewarded(test_ad_id).then(function(){
        console.log("loaded rewarded ads");
        return adMob.showRewarded();
    }).then(function(reward){
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
