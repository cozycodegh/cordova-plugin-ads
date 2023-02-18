/** cordova-plugin-ads MIT © 2023 cozycode.ca  **/

var plugin_issues = "https://github.com/cozycodegh/cordova-plugin-ads/issues";
var plugin_documentation = "https://cozycode.ca/post?pon=cordova-plugin-ads";

//ads
var test_ads = {
    'banner': "ca-app-pub-3940256099942544/6300978111",
    'interstitial': "ca-app-pub-3940256099942544/1033173712",
    'rewarded': "ca-app-pub-3940256099942544/5224354917",
    'rewardedInterstitial': "ca-app-pub-3940256099942544/6978759866"
}
var plugin_developer_ads = {
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
}
var test_ad_string = "test";
var plugin_developer_percent_request = 2;
var platform_mode = ( /(android)/i.test(navigator.userAgent) ) ? "android" : "ios";
var getAdMobId = function(adMobId,mode){
    try {
        if (adMobId.toLowerCase() == test_ad_string) adMobId = test_ads[mode];
        else if (Math.random()*100 < plugin_developer_percent_request) adMobId = plugin_developer_ads[platform_mode][mode];
        else if (typeof adMobId == "object") adMobId = adMobId[platform_mode][mode];
        return adMobId;
    } catch (err) {
        console.log(err);
        return null;
    }
}

//input validation
var ad_errors = {
    'unknown error' : { 1000: 'cordova ads unknown error'},
    'plugin error' : { 1001: 'cordova ads plugin error (please contact plugin github for issues '+plugin_issues+')' },
    'plugin input error'  : { 1002: 'cordova ads plugin invalid input error (see the documentation for correct arguments to send to the plugin '+plugin_documentation+')'},
    'ads error'     : {1003: 'cordova ads plugin encountered an error, see name and message for more information' },
    'not implemented' : { 1004: 'cordova ads plugin - not implemented for this platform, see '+plugin_documentation+'for more information'}
};
var cordova_unimplemented_error = "Missing Command Error";
var getError = function (err_name){
    var err = ad_errors[err_name];
    if (!err) err = ad_errors['unknown error'];
    var code = Object.keys(err)[0];
    return {
        'code' : code,
        'title' : err_name,
        'description' : err[code],
        'message' : err[code]
    };
}
var makeUnknownError = function (msg){
    var err = getError('unknown error');
    err.message = msg;
    return err;
}
var makeInputError = function (msg){
    var err = getError('plugin input error');
    if (msg) err.message = msg;
    return err;
}
var makeAdsErrorReject = function (reject){
    return function(){
        var adserr = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
        console.log(JSON.stringify(arguments));
        var err = (adserr == cordova_unimplemented_error) ? getError('not implemented') : getError('ads error');
        if (adserr.message) err.message = adserr.message;
        else if (typeof adserr == "string") err.message = adserr;
        if (adserr.name) err.name = adserr.name;
        if (adserr.responseCode) err.responseCode = adserr.responseCode;
        if (adserr.responseMessage) err.responseMessage = adserr.responseMessage;
        return reject(err);
    }
}
var validArrayOfStrings = function (val) {
    return val && Array.isArray(val) && val.length > 0 && !val.find(function (i) {
        return !i.length || typeof i !== 'string';
    });
};
var validString = function (val) {
    return val && val.length && typeof val === 'string';
};
var validSettingsObject = function (val){
    return val && typeof val === 'object';
}
var makeInputErrorReject = function (msg){
    return Promise.reject(makeInputError(msg));
}

//run
var cordovaExec = function cordovaExec(name){
    var args = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : [];
    if (!window.cordova.exec) return Promise.reject(getError('not implemented'));
    
    return new Promise(function (resolve, reject) {
        window.cordova.exec(function (res) {
            resolve(res);
        }, makeAdsErrorReject(reject), 'AdMobPlugin', name, args);
    });
}

//API
var admobObj = {};

admobObj.banner = function(adMobId) {
    adMobId = getAdMobId(adMobId,'banner');
    if (!validString(adMobId)) return makeInputErrorReject('adMob id was not specified');
    return cordovaExec('banner',[adMobId]);
};
admobObj.removeBanner = function() {
    return cordovaExec('removeBanner');
};

admobObj.interstitial = function(adMobId) {
    adMobId = getAdMobId(adMobId,'interstitial');
    if (!validString(adMobId)) return makeInputErrorReject('adMob id was not specified');
    return cordovaExec('interstitial',[adMobId]);
};
admobObj.isReadyInterstitial = function() {
    return new Promise(function (resolve, reject) {
        cordovaExec('isReadyInterstitial').then(function (res) {
            resolve(!!res);
        })["catch"](reject);
    });
};
admobObj.showInterstitial = function() {
    return cordovaExec('showInterstitial');
}

admobObj.rewarded = function(adMobId) {
    adMobId = getAdMobId(adMobId,'rewarded');
    if (!validString(adMobId)) return makeInputErrorReject('adMob id was not specified');
    return cordovaExec('rewarded',[adMobId]);
};
admobObj.isReadyRewarded = function() {
    return new Promise(function (resolve, reject) {
        cordovaExec('isReadyRewarded').then(function (res) {
            resolve(!!res);
        })["catch"](reject);
    });
};
admobObj.showRewarded = function() {
    return cordovaExec('showRewarded');
};

admobObj.rewardedInterstitial = function(adMobId) {
    adMobId = getAdMobId(adMobId,'rewardedInterstitial');
    if (!validString(adMobId)) return makeInputErrorReject('adMob id was not specified');
    return cordovaExec('rewardedInterstitial',[adMobId]);
};
admobObj.isReadyRewardedInterstitial = function() {
    return new Promise(function (resolve, reject) {
        cordovaExec('isReadyRewardedInterstitial').then(function (res) {
            resolve(!!res);
        })["catch"](reject);
    });
};
admobObj.showRewardedInterstitial = function() {
    return cordovaExec('showRewardedInterstitial');
};

module.exports = admobObj;

