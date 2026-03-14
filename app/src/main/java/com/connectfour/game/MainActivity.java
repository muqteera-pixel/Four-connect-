package com.connectfour.game;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import android.widget.*;
import android.view.*;

import com.google.android.gms.ads.*;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class MainActivity extends Activity {

    WebView webView;
    RewardedAd rewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        MobileAds.initialize(this);

        webView = findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.addJavascriptInterface(new AdInterface(), "AdMob");
        webView.loadUrl("file:///android_asset/index.html");

        loadRewardedAd();
    }

    class AdInterface {
        @android.webkit.JavascriptInterface
        public void showReward() {
            runOnUiThread(() -> showRewardedAd());
        }
    }

    private void loadRewardedAd() {
        RewardedAd.load(this, "ca-app-pub-5367408521620850/9459841281",
            new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(RewardedAd ad) {
                    rewardedAd = ad;
                    rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            rewardedAd = null;
                            loadRewardedAd();
                        }
                    });
                }
                @Override
                public void onAdFailedToLoad(LoadAdError e) { rewardedAd = null; }
            });
    }

    private void showRewardedAd() {
        if (rewardedAd != null) {
            rewardedAd.show(this, rewardItem -> {
                webView.evaluateJavascript("receiveReward && receiveReward();", null);
                Toast.makeText(this, "Reward earned!", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Ad not ready yet!", Toast.LENGTH_SHORT).show();
            loadRewardedAd();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
