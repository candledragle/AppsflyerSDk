package com.appsflyer.share;

import android.content.Context;

import com.appsflyer.AFLogger;
import com.appsflyer.AppsFlyerProperties;
import com.appsflyer.CreateOneLinkHttpTask;
import com.appsflyer.ServerConfigHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.appsflyer.share.Constants.AF_BASE_URL_FORMAT;
import static com.appsflyer.share.Constants.ONELINK_DEFAULT_DOMAIN;
import static com.appsflyer.share.Constants.URL_PATH_DELIMITER;

/**
 * @author  by gilmeroz on 02/01/2017.
 */

public class LinkGenerator {

    private String channel;
    private String campaign;
    private String mediaSource;
    private String referrerUID;
    private String referrerCustomerId;
    private String referrerName;
    private String referrerImageURL;
    private String baseURL;
    private String appPackage;
    private String deeplinkPath;
    private String baseDeeplink;
    private Map<String,String> parameters = new HashMap<>();
    private Map<String,String> linkGeneratorParameters = new HashMap<>();


    public LinkGenerator(String mediaSource) {
        this.mediaSource = mediaSource;
    }

    LinkGenerator setAppPackage(String appPackage) {
        this.appPackage = appPackage;
        return this;
    }

    public LinkGenerator setDeeplinkPath(String deeplinkPath) {
        this.deeplinkPath = deeplinkPath;
        return this;
    }

    public LinkGenerator setBaseDeeplink(String baseDeeplink) {
        this.baseDeeplink = baseDeeplink;
        return this;
    }

    LinkGenerator setBaseURL(String baseURL) {
        this.baseURL = baseURL;
        return this;
    }

    public LinkGenerator setChannel(String channel) {
        this.channel = channel;
        return this;
    }

    public String getChannel() {
        return channel;
    }

    public LinkGenerator setReferrerCustomerId(String referrerCustomerId) {
        this.referrerCustomerId = referrerCustomerId;
        return this;
    }

    public String getMediaSource() {
        return mediaSource;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public LinkGenerator setCampaign(String campaign) {
        this.campaign = campaign;
        return this;
    }

    public String getCampaign() {
        return campaign;
    }

    public LinkGenerator addParameter(String key, String value){
        this.parameters.put(key, value);
        return this;
    }

    public LinkGenerator addParameters(Map<String,String> parameters){
        if (parameters != null) {
            this.parameters.putAll(parameters);
        }
        return this;
    }

    public LinkGenerator setReferrerUID(String referrerUID) {
        this.referrerUID = referrerUID;
        return this;

    }

    public LinkGenerator setReferrerName(String referrerName) {
        this.referrerName = referrerName;
        return this;

    }

    public LinkGenerator setReferrerImageURL(String referrerImageURL) {
        this.referrerImageURL = referrerImageURL;
        return this;

    }

    public LinkGenerator setBaseURL(String onelinkID, String domain, String appPackage) {
        if (onelinkID == null || onelinkID.length() <= 0) { // use app + ServerHost.DEFAULT_HOST + <package-name>
            baseURL = String.format(AF_BASE_URL_FORMAT, ServerConfigHandler.getUrl(Constants.APPSFLYER_DEFAULT_APP_DOMAIN), appPackage);
        } else {
            baseURL = buildOneLinkBaseUrl(onelinkID, domain);
        }
        return this;
    }

    private String buildOneLinkBaseUrl(String onelinkID, String domain) {
        if (domain == null || domain.length() < 5) { // use go.onelink.me/<one-link-id>
            domain = ONELINK_DEFAULT_DOMAIN;
        } // else use <one-link-domain>/<one-link-id>
        return String.format(AF_BASE_URL_FORMAT, domain, onelinkID);
    }


    private StringBuilder generateBaseURL() {

        StringBuilder builder = new StringBuilder();

        if(baseURL != null && baseURL.startsWith("http")){
            builder.append(baseURL);
        }
        else{
            builder.append(ServerConfigHandler.getUrl(Constants.BASE_URL_APP_APPSFLYER_COM));
        }

        if (appPackage != null) {
            builder.append('/')
                    .append(appPackage);
        }

        linkGeneratorParameters.put(Constants.URL_MEDIA_SOURCE, this.mediaSource);

        builder.append('?').append(Constants.URL_MEDIA_SOURCE)
                .append('=').append(getEncodedValue(mediaSource, "media source"));

        if (referrerUID != null) {
            linkGeneratorParameters.put(Constants.URL_REFERRER_UID, this.referrerUID);
            builder.append('&').append(Constants.URL_REFERRER_UID)
                    .append('=').append(getEncodedValue(referrerUID, "referrerUID"));
        }
        if (channel != null) {
            linkGeneratorParameters.put(Constants.URL_CHANNEL, this.channel);
            builder.append('&').append(Constants.URL_CHANNEL)
                    .append('=').append(getEncodedValue(channel, "channel"));
        }
        if (referrerCustomerId != null) {
            linkGeneratorParameters.put(Constants.URL_REFERRER_CUSTOMER_ID, this.referrerCustomerId);
            builder.append('&').append(Constants.URL_REFERRER_CUSTOMER_ID)
                    .append('=').append(getEncodedValue(referrerCustomerId, "referrerCustomerId"));
        }
        if (campaign != null) {
            linkGeneratorParameters.put(Constants.URL_CAMPAIGN, this.campaign);
            builder.append('&').append(Constants.URL_CAMPAIGN)
                    .append('=').append(getEncodedValue(campaign, "campaign"));
        }
        if (referrerName != null) {
            linkGeneratorParameters.put(Constants.URL_REFERRER_NAME, this.referrerName);
            builder.append('&').append(Constants.URL_REFERRER_NAME)
                    .append('=').append(getEncodedValue(referrerName, "referrerName"));
        }
        if (referrerImageURL != null) {
            linkGeneratorParameters.put(Constants.URL_REFERRER_IMAGE_URL, this.referrerImageURL);
            builder.append('&').append(Constants.URL_REFERRER_IMAGE_URL)
                    .append('=').append(getEncodedValue(referrerImageURL, "referrerImageURL"));
        }

        if (baseDeeplink != null) {
            StringBuilder deeplinkBuilder = new StringBuilder().append(this.baseDeeplink);
            deeplinkBuilder.append(baseDeeplink.endsWith(URL_PATH_DELIMITER) ? "" : "/");
            if (this.deeplinkPath != null) {
                deeplinkBuilder.append(this.deeplinkPath);
            }


            linkGeneratorParameters.put(Constants.URL_BASE_DEEPLINK, deeplinkBuilder.toString());


            builder.append('&').append(Constants.URL_BASE_DEEPLINK)
                    .append('=').append(getEncodedValue(baseDeeplink, "baseDeeplink"));

            if (deeplinkPath != null) {
                builder.append(baseDeeplink.endsWith(URL_PATH_DELIMITER) ? "" : "%2F")
                        .append(getEncodedValue(deeplinkPath, "deeplinkPath"));
            }
        }

        for (String key : this.parameters.keySet()){
            // on failure we generate URL based on data that contains also keys we already added.
            if(builder.toString().contains(key + "=" + getEncodedValue(this.parameters.get(key), key))){
                continue;
            }

            builder.append('&').append(key)
                    .append('=').append(getEncodedValue(this.parameters.get(key),key));
        }
        return builder;
    }

    private String getEncodedValue(String s, String name){
        try {
            return URLEncoder.encode(s,"utf8");
        } catch (UnsupportedEncodingException e) {
            AFLogger.afInfoLog("Illegal "+name+": "+s);
            return "";
        }
        catch (Throwable throwable){
            return "";
        }
    }

    public String generateLink(){
        return generateBaseURL().toString();
    }

    public void generateLink(Context context, CreateOneLinkHttpTask.ResponseListener listener){

        final String oneLinkId = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.ONELINK_ID);

        if (!parameters.isEmpty()) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                linkGeneratorParameters.put(entry.getKey(), entry.getValue());
            }
        }

        generateBaseURL();

        ShareInviteHelper.generateUserInviteLink(context, oneLinkId, this.linkGeneratorParameters, listener);
    }

    private Map<String,String> getLinkGeneratorParameters(){
        return linkGeneratorParameters;
    }
}
