// (c) 2023 cozycode.ca cordova-plugin-ads

package ca.cozycode.cordova.ads;

import android.app.Activity;
import android.content.Context;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;

/**
 * NextAsync
 * link asynchronous calls with error messages
 **/
//abstract
public class NextAsync  {
    
    protected NextAsync thisNext;
    private NextAsync mNext;
    protected AdMobPlugin AdMobPlugin;
    protected Activity activityContext;
    protected CallbackContext callbackContext;
    protected JSONArray args;
    protected String action;
    private String mFailErrorName;
    public String name = "next";
    private String mArgsAdMobId;
    private String mArgsAdSize;
    private boolean mArgsAdPostitionIsTop;
    
    //main
    public NextAsync(AdMobPlugin theAdMobPlugin, Activity theActivityContext, CallbackContext theCallbackContext, JSONArray theArgs, String theAction){
        thisNext = this;
        AdMobPlugin = theAdMobPlugin;
        activityContext = theActivityContext;
        callbackContext = theCallbackContext;
        args = theArgs;
        action = theAction;
        mFailErrorName = AdMobPlugin.PLUGIN_ERROR_CODES_UNKNOWN_ERROR;
    }
    public NextAsync(NextAsync nextNext){
        thisNext = this;
        mNext = nextNext;
        AdMobPlugin = mNext.AdMobPlugin;
        activityContext = mNext.activityContext;
        callbackContext = mNext.callbackContext;
        args = mNext.args;
        action = mNext.action;
        mFailErrorName = AdMobPlugin.PLUGIN_ERROR_CODES_UNKNOWN_ERROR;
    }
    
    //next - extend to next call to link async calls with error checking
    //public abstract void OnNext();
    public void OnNext(){}
    public void OnNext(boolean result){
        callbackContext.error(AdMobPlugin.makeError(AdMobPlugin.PLUGIN_ERROR_CODES_DEVELOPER_ERROR, "Developer Plugin Error: AdMobPlugin's OnNext not implemented"));
    }
    //error - error back to cordova
    public void OnError(String errorName, String errorMessageAppend){
        callbackContext.error(AdMobPlugin.makeError(errorName,errorMessageAppend));
    }
    public void OnError(String errorName, Integer responseCode, String responseMessage, String errorMessageAppend){
        callbackContext.error(AdMobPlugin.makeError(errorName,responseCode,responseMessage,errorMessageAppend));
    }
    
    //args
    public void setArgs(NextAsync next){
        args = next.args;
    }
    public String getArgsAdMobId(){
        return getArgsAdMobId(false);
    }
    public String getArgsAdMobId(boolean force){
        if (mArgsAdMobId != null) return mArgsAdMobId;
        String adMobId;
        if (args.length() == 0){
            if (force){
                callbackContext.error(AdMobPlugin.makeError(AdMobPlugin.PLUGIN_ERROR_CODES_INVALID_ARGUMENTS,"Invalid AdMob ID Argument - Missing AdMob Id Argument"));
            }
            return null;
        } else {
            try {
                adMobId = args.getString(0);
                //if (args.length() > 1) { developerPayload = args.getString(1); }
            } catch (JSONException e) {
                callbackContext.error(AdMobPlugin.makeError(AdMobPlugin.PLUGIN_ERROR_CODES_INVALID_ARGUMENTS,"Unreadable AdMob ID argument "+e.toString()));
                return null;
            }
        }
        mArgsAdMobId = adMobId;
        return mArgsAdMobId;
    }
    public String getArgsAdSize(){
        return getArgsAdSize(false);
    }
    public String getArgsAdSize(boolean force){
        if (mArgsAdSize != null) return mArgsAdSize;
        String adSize;
        if (args.length() < 2){
            if (force){
                callbackContext.error(AdMobPlugin.makeError(AdMobPlugin.PLUGIN_ERROR_CODES_INVALID_ARGUMENTS,"Invalid AdMob Argument - Missing AdMob ad_size argument"));
            }
            return null;
        } else {
            try {
                adSize = args.getString(1);
                //if (args.length() > 1) { developerPayload = args.getString(1); }
            } catch (JSONException e) {
                callbackContext.error(AdMobPlugin.makeError(AdMobPlugin.PLUGIN_ERROR_CODES_INVALID_ARGUMENTS,"Unreadable AdMob ad_size argument "+e.toString()));
                return null;
            }
        }
        mArgsAdSize = adSize;
        return mArgsAdSize;
    }
    public boolean getArgsAdPositionIsTop(){
        if (mArgsAdPostitionIsTop) return mArgsAdPostitionIsTop;
        boolean adPostitionIsTop = false;
        if (args.length() > 2){
            try {
                String adPosition = args.getString(2).trim().toUpperCase();
                adPostitionIsTop = (adPosition.charAt(0) == 'T' && adPosition.charAt(1) == 'O' && adPosition.charAt(2) == 'P');
            } catch (JSONException e) {
                callbackContext.error(AdMobPlugin.makeError(AdMobPlugin.PLUGIN_ERROR_CODES_INVALID_ARGUMENTS,"Unreadable AdMob ad_position argument "+e.toString()));
                return false;
            }
        }
        mArgsAdPostitionIsTop = adPostitionIsTop;
        return mArgsAdPostitionIsTop;
    }
    public String getArgsAdPositionText(){
        boolean adPostitionIsTop = getArgsAdPositionIsTop();
        if (adPostitionIsTop) return "TOP";
        return "BOTTOM";
    }
    
    @Override
    public String toString(){
        return "AdMobPlugin Next ("+name+") { on: "+action+" for args: "+args.toString()+" }";
    }
}
