# banner ads

Banner advertisements which display at the bottom of the app screen.

jump to: [banner](#banner) | [removeBanner](#remove-banner) | [Example](#example)
<hr/>

<p align="center">
<img src="banner.png" alt="banner ad" width="300" align="center" />
</p>

# adMob.banner(banner_id) <a id="banner"></a><br>

## Usage:
```js
adMob.banner(banner_id).then(function () {
    // do anything after banner has loaded successfully
}).catch (function(err){
    // view or handle error messages
});
```

## Description:
 - show a banner ad at the bottom of the app screen
 - use after the start up of the device (`onDeviceReady`)

## Parameters:
` - Google AdMob id for a banner ad ` <br>
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

## Tips:

- call this again if it fails

# adMob.removeBanner() <a id="remove-banner"></a><br>

## Usage:
```js
adMob.removeBanner().then(function () {
    // do anything after banners were removed
}).catch (function(err){
    // view or handle error messages (uncommon)
});
```

## Description:
 - remove the banner ad
 - use after a call to banner if you wish to remove it

## Parameters:
- none needed


# Example <a id="example"></a><br>
```js
function onDeviceReady() {
    adMob.banner(banner_id).catch(function(err){});
}
```

<hr/>

<p align="center">

[go to main](../README.md#plugin-usage)

</p>
