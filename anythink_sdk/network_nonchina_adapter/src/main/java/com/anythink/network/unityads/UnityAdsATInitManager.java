package com.anythink.network.unityads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.mediation.IUnityAdsExtendedListener;
import com.unity3d.ads.metadata.MetaData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UnityAdsATInitManager extends ATInitMediation {

    private static final String TAG = UnityAdsATInitManager.class.getSimpleName();
    private String mGameId;
    private static UnityAdsATInitManager sIntance;
    private ConcurrentHashMap<String, Object> mLoadResultAdapterMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> mAdapterMap = new ConcurrentHashMap<>();

    private IUnityAdsExtendedListener mIUnityAdsExtendedListener = new IUnityAdsExtendedListener() {
        @Override
        public void onUnityAdsClick(String placementId) {
            Object adapter = mAdapterMap.get(placementId);
            try {
                if (adapter instanceof UnityAdsATInterstitialAdapter) {
                    ((UnityAdsATInterstitialAdapter) adapter).notifyClick(placementId);
                }
            } catch (Throwable e) {

            }

            try {
                if (adapter instanceof UnityAdsATRewardedVideoAdapter) {
                    ((UnityAdsATRewardedVideoAdapter) adapter).notifyClick(placementId);
                }
            } catch (Throwable e) {

            }
        }

        @Override
        public void onUnityAdsPlacementStateChanged(String placementId, UnityAds.PlacementState placementState, UnityAds.PlacementState placementState1) {

        }

        @Override
        public void onUnityAdsReady(String placementId) {
            Object adapter = mLoadResultAdapterMap.get(placementId);
            try {
                if (adapter instanceof UnityAdsATInterstitialAdapter) {
                    ((UnityAdsATInterstitialAdapter) adapter).notifyLoaded(placementId);
                }
            } catch (Throwable e) {

            }

            try {
                if (adapter instanceof UnityAdsATRewardedVideoAdapter) {
                    ((UnityAdsATRewardedVideoAdapter) adapter).notifyLoaded(placementId);
                }
            } catch (Throwable e) {

            }
            removeLoadResultAdapter(placementId);
        }

        @Override
        public void onUnityAdsStart(String placementId) {
            Object adapter = mAdapterMap.get(placementId);
            try {
                if (adapter instanceof UnityAdsATInterstitialAdapter) {
                    ((UnityAdsATInterstitialAdapter) adapter).notifyStart(placementId);
                }
            } catch (Throwable e) {

            }

            try {
                if (adapter instanceof UnityAdsATRewardedVideoAdapter) {
                    ((UnityAdsATRewardedVideoAdapter) adapter).notifyStart(placementId);
                }
            } catch (Throwable e) {

            }
        }

        @Override
        public void onUnityAdsFinish(String placementId, UnityAds.FinishState finishState) {
            Object adapter = mAdapterMap.get(placementId);
            try {
                if (adapter instanceof UnityAdsATInterstitialAdapter) {
                    ((UnityAdsATInterstitialAdapter) adapter).notifyFinish(placementId, finishState);
                }
            } catch (Throwable e) {

            }

            try {
                if (adapter instanceof UnityAdsATRewardedVideoAdapter) {
                    ((UnityAdsATRewardedVideoAdapter) adapter).notifyFinish(placementId, finishState);
                }
            } catch (Throwable e) {

            }
            removeAdapter(placementId);
        }

        @Override
        public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {
            for (Object adapter : mLoadResultAdapterMap.values()) {
                try {
                    if (adapter instanceof UnityAdsATInterstitialAdapter) {
                        ((UnityAdsATInterstitialAdapter) adapter).notifyLoadFail(unityAdsError.name(), s);
                    }
                } catch (Throwable e) {

                }
                try {
                    if (adapter instanceof UnityAdsATRewardedVideoAdapter) {
                        ((UnityAdsATRewardedVideoAdapter) adapter).notifyLoadFail(unityAdsError.name(), s);
                    }
                } catch (Throwable e) {

                }
            }
            mLoadResultAdapterMap.clear();
        }
    };

    private UnityAdsATInitManager() {

    }

    public synchronized static UnityAdsATInitManager getInstance() {
        if (sIntance == null) {
            sIntance = new UnityAdsATInitManager();
        }
        return sIntance;
    }


    protected synchronized void putLoadResultAdapter(String placementId, final Object adapter) {
        mLoadResultAdapterMap.put(placementId, adapter);
    }

    protected synchronized void removeLoadResultAdapter(String placementId) {
        mLoadResultAdapterMap.remove(placementId);
    }

    protected synchronized void putAdapter(String placementId, final Object adapter) {
        mAdapterMap.put(placementId, adapter);
    }

    protected synchronized void removeAdapter(String placementId) {
        mAdapterMap.remove(placementId);
    }


    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        MetaData gdprMetaData = new MetaData(context.getApplicationContext());
        gdprMetaData.set("gdpr.consent", isConsent);
        gdprMetaData.commit();
        return true;
    }

    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        if (!(context instanceof Activity)) {
            return;
        }

        String game_id = (String) serviceExtras.get("game_id");
        if (!TextUtils.isEmpty(game_id)) {
            if (TextUtils.isEmpty(mGameId) || !TextUtils.equals(mGameId, game_id)) {
                UnityAds.addListener(mIUnityAdsExtendedListener);
                UnityAds.initialize(((Activity) context), game_id);
                mGameId = game_id;
            }
            UnityAds.addListener(mIUnityAdsExtendedListener);
        }
    }

    @Override
    public String getNetworkName() {
        return "UnityAds";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.unity3d.services.monetization.UnityMonetization";
    }
}
