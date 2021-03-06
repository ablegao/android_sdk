package com.anythink.network.gdt;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATAdConst;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeADUnifiedListener;
import com.qq.e.ads.nativ.NativeMediaAD;
import com.qq.e.ads.nativ.NativeMediaADData;
import com.qq.e.ads.nativ.NativeUnifiedAD;
import com.qq.e.ads.nativ.NativeUnifiedADData;
import com.qq.e.comm.constants.AdPatternType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhou on 2018/1/16.
 */

public class GDTATAdapter extends CustomNativeAdapter implements GDTATNativeLoadListener {

    String mUnitId;
    int mAdCount;

    private int mAdWidth = ADSize.FULL_WIDTH, mAdHeight = ADSize.AUTO_HEIGHT;

    int ADTYPE = 3;

    int mUnitVersion = 2;

    int mVideoMuted;
    int mVideoAutoPlay;
    int mVideoDuration;

    private void startLoadAd(Context context) {
        try {
            switch (ADTYPE) {
                case 1:
                case 2:
                    if (mUnitVersion != 2) {
                        //Picture + Video Self Rendering
                        loadNativeVideoAD(context);
                    } else { //adslot 2.0
                        loadUnifiedAd(context);
                    }
                    break;
                default:
                    //Picture + video template
                    GDTATNativeExpressAd gdtatNativeExpressAd = new GDTATNativeExpressAd(context, mUnitId, mAdWidth, mAdHeight,
                            mVideoMuted, mVideoAutoPlay, mVideoDuration);
                    gdtatNativeExpressAd.loadAD(this);
            }
        } catch (Throwable e) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", e.getMessage());
            }
        }


    }

    /**
     * Self-rendering 2.0
     */
    private void loadUnifiedAd(final Context context) {
        NativeUnifiedAD nativeUnifiedAd = new NativeUnifiedAD(context, mUnitId, new NativeADUnifiedListener() {
            @Override
            public void onADLoaded(List<NativeUnifiedADData> list) {
                List<CustomNativeAd> resultList = new ArrayList<>();
                if (list != null && list.size() > 0) {
                    for (NativeUnifiedADData unifiedADData : list) {
                        GDTATNativeAd gdtNativeAd = new GDTATNativeAd(context, unifiedADData, mVideoMuted, mVideoAutoPlay, mVideoDuration);
                        resultList.add(gdtNativeAd);
                    }

                    CustomNativeAd[] customNativeAds = new CustomNativeAd[resultList.size()];
                    customNativeAds = resultList.toArray(customNativeAds);
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded(customNativeAds);
                    }
                } else {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", "Ad list is empty");
                    }
                }
            }

            @Override
            public void onNoAD(com.qq.e.comm.util.AdError gdtAdError) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(gdtAdError.getErrorCode() + "", gdtAdError.getErrorMsg());
                }
            }
        });

        if (mVideoDuration != -1) {
            nativeUnifiedAd.setMaxVideoDuration(mVideoDuration);
        }
        nativeUnifiedAd.setVideoPlayPolicy(GDTATInitManager.getInstance().getVideoPlayPolicy(context, mVideoAutoPlay));
        nativeUnifiedAd.loadData(mAdCount);

    }


    /***
     * init Self Rendering 1.0
     */
    private void loadNativeVideoAD(final Context context) {
        NativeMediaAD.NativeMediaADListener listener = new NativeMediaAD.NativeMediaADListener() {

            @Override
            public void onADLoaded(List<NativeMediaADData> adList) {
                GDTATNativeAd mGdtNativeAd;
                if (adList.size() > 0) {
                    List<CustomNativeAd> resultList = new ArrayList<>();
                    for (NativeMediaADData _nativeMediaADData : adList) {
                        NativeMediaADData mAD = _nativeMediaADData;
                        mGdtNativeAd = new GDTATNativeAd(context, mAD, mVideoMuted, mVideoAutoPlay, mVideoDuration);
                        resultList.add(mGdtNativeAd);
                        if (mAD.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
                            /**
                             * If the native ad is an ad with video material, you also need to call the preLoadVideo interface to load the video material:
                             *    - Loading success: NativeMediaADListener.onADVideoLoaded (NativeMediaADData adData)
                             *    - Loading failed: NativeMediaADListener.onADError (NativeMediaADData adData, int errorCode) , error code is 700
                             */
                            mAD.preLoadVideo();
                        }
                    }

                    CustomNativeAd[] customNativeAds = new CustomNativeAd[resultList.size()];
                    customNativeAds = resultList.toArray(customNativeAds);
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded(customNativeAds);
                    }
                }
            }

            @Override
            public void onNoAD(com.qq.e.comm.util.AdError adError) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(adError.getErrorCode() + "", adError.getErrorMsg());
                }
            }

            /**
             * The advertising status changes. For App ads, the download / installation status and download progress can change.
             *
             * @param ad    Ad objects with changed status
             */
            @Override
            public void onADStatusChanged(NativeMediaADData ad) {
            }

            @Override
            public void onADError(NativeMediaADData adData, com.qq.e.comm.util.AdError adError) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(adError.getErrorCode() + "", adError.getErrorMsg());
                }
            }

            @Override
            public void onADVideoLoaded(NativeMediaADData adData) {

            }

            @Override
            public void onADExposure(NativeMediaADData adData) {

            }

            @Override
            public void onADClicked(NativeMediaADData adData) {

            }
        };

        NativeMediaAD adManager = new NativeMediaAD(context, mUnitId, listener);
        if (mVideoDuration != -1) {
            adManager.setMaxVideoDuration(mVideoDuration);
        }
        adManager.loadAD(mAdCount);
    }

    @Override
    public String getNetworkName() {
        return GDTATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String appid = "";
        String unitId = "";

        if (serverExtra.containsKey("app_id")) {
            appid = serverExtra.get("app_id").toString();
        }
        if (serverExtra.containsKey("unit_id")) {
            unitId = serverExtra.get("unit_id").toString();
        }

        if (serverExtra.containsKey("unit_version")) { //version
            mUnitVersion = Integer.parseInt(serverExtra.get("unit_version").toString());
        }

        boolean adTypeServiceCallback = false;
        if (serverExtra.containsKey("unit_type")) {
            int unitType = Integer.parseInt(serverExtra.get("unit_type").toString());
            if (unitType == 1) { //Native Express
                ADTYPE = 3;
            } else if (unitType == 2) { //Self-rendering
                ADTYPE = 1;
            }
            adTypeServiceCallback = true;
        }

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "GTD appid or unitId is empty.");

            }
            return;
        }

        int requestNum = 1;
        try {
            if (serverExtra.containsKey(CustomNativeAd.AD_REQUEST_NUM)) {
                requestNum = Integer.parseInt(serverExtra.get(CustomNativeAd.AD_REQUEST_NUM).toString());
            }
        } catch (Exception e) {
        }

        mAdCount = requestNum;

        mUnitId = unitId;


        //location story
        try {
            if (!adTypeServiceCallback) {
                if (localExtra.containsKey(GDTATConst.ADTYPE)) {
                    ADTYPE = Integer.parseInt(localExtra.get(GDTATConst.ADTYPE).toString());
                }
            }

            if (localExtra.containsKey(GDTATConst.AD_WIDTH)) {
                mAdWidth = Integer.parseInt(localExtra.get(GDTATConst.AD_WIDTH).toString());
            } else if (localExtra.containsKey(ATAdConst.KEY.AD_WIDTH)) {
                mAdWidth = Integer.parseInt(localExtra.get(ATAdConst.KEY.AD_WIDTH).toString());
            }

            if (localExtra.containsKey(GDTATConst.AD_HEIGHT)) {
                mAdHeight = Integer.parseInt(localExtra.get(GDTATConst.AD_HEIGHT).toString());
            } else if (localExtra.containsKey(ATAdConst.KEY.AD_HEIGHT)) {
                mAdHeight = Integer.parseInt(localExtra.get(ATAdConst.KEY.AD_HEIGHT).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int isVideoMuted = 1;
        int isVideoAutoPlay = 1;
        int videoDuration = -1;
        if (serverExtra.containsKey("video_muted")) {
            isVideoMuted = Integer.parseInt(serverExtra.get("video_muted").toString());
        }
        if (serverExtra.containsKey("video_autoplay")) {
            isVideoAutoPlay = Integer.parseInt(serverExtra.get("video_autoplay").toString());
        }
        if (serverExtra.containsKey("video_duration")) {
            videoDuration = Integer.parseInt(serverExtra.get("video_duration").toString());
        }

        mVideoMuted = isVideoMuted;
        mVideoAutoPlay = isVideoAutoPlay;
        mVideoDuration = videoDuration;

        GDTATInitManager.getInstance().initSDK(context, serverExtra, new GDTATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context);
            }

            @Override
            public void onError() {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "GDT initSDK failed.");
                }
            }
        });
    }

    @Override
    public void destory() {
    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return GDTATConst.getNetworkVersion();
    }

    @Override
    public void notifyLoaded(CustomNativeAd... customNativeAds) {
        if (mLoadListener != null) {
            mLoadListener.onAdCacheLoaded(customNativeAds);
        }
    }

    @Override
    public void notifyError(String errorCode, String errorMsg) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError(errorCode, errorMsg);
        }
    }
}
