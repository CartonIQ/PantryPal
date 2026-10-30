package com.cartoniq.pantrypal.ads

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {
    /** Google test banner unit. */
    const val BANNER_ID = "ca-app-pub-9771662959477951/8998487145"

    private val started = AtomicBoolean(false)
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    /** Gathers UMP consent (if required), then initializes the Mobile Ads SDK. */
    fun initialize(activity: Activity) {
        val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().build()
        consentInfo.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                    startAds(activity, consentInfo)
                }
            },
            { _ ->
                startAds(activity, consentInfo)
            }
        )
        // Consent from a previous session may already allow ads.
        startAds(activity, consentInfo)
    }

    private fun startAds(activity: Activity, consentInfo: ConsentInformation) {
        if (consentInfo.canRequestAds() && started.compareAndSet(false, true)) {
            MobileAds.initialize(activity.applicationContext) { _ ->
                _ready.value = true
            }
        }
    }
}

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdManager.BANNER_ID
                loadAd(AdRequest.Builder().build())
            }
        },
        onRelease = { it.destroy() }
    )
}
