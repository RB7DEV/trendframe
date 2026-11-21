package com.example

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
    )
    setContent {
      MyUniverseHostScreen()
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MyUniverseHostScreen() {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val spaceBackground = Color(0xFF030610)

  val webView = remember {
    WebView(context).apply {
      layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
      )
      setBackgroundColor(AndroidColor.parseColor("#030610"))
      overScrollMode = View.OVER_SCROLL_NEVER
      isVerticalScrollBarEnabled = false
      isHorizontalScrollBarEnabled = false
      setLayerType(View.LAYER_TYPE_HARDWARE, null)

      settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        allowFileAccess = true
        useWideViewPort = true
        loadWithOverviewMode = true
        setSupportZoom(false)
        builtInZoomControls = false
        displayZoomControls = false
        mediaPlaybackRequiresUserGesture = false
        cacheMode = WebSettings.LOAD_DEFAULT
      }

      webViewClient = WebViewClient()
      webChromeClient = WebChromeClient()
      loadUrl("file:///android_asset/index.html")
    }
  }

  DisposableEffect(lifecycleOwner, webView) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> webView.onResume()
        Lifecycle.Event.ON_PAUSE -> {
          webView.evaluateJavascript("window.__saveUniverseState && window.__saveUniverseState();", null)
          webView.onPause()
        }
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      webView.destroy()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(spaceBackground)
      .windowInsetsPadding(WindowInsets.safeDrawing)
  ) {
    AndroidView(
      factory = { webView },
      modifier = Modifier
        .fillMaxSize()
        .testTag("my_universe_webview")
    )
  }
}
