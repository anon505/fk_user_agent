package io.flutterfastkit.fk_user_agent

import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler

/**
 * FkUserAgentPlugin
 */
class FkUserAgentPlugin : FlutterPlugin, MethodCallHandler {
  /** The MethodChannel that will the communication between Flutter and native Android
   *
   * This local reference serves to register the plugin with the Flutter Engine and unregister it
   * when the Flutter Engine is detached from the Activity */
  private var channel: MethodChannel? = null
  private var applicationContext: Context? = null
  private var constants: MutableMap<String, Any>? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.getBinaryMessenger(), "fk_user_agent")
    channel.setMethodCallHandler(this)
    applicationContext = flutterPluginBinding.getApplicationContext()
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if ("getProperties" == call.method) {
      result.success(properties)
    } else {
      result.notImplemented()
    }
  }

  private val properties: Map<String, Any>
    get() {
      if (constants != null) {
        return constants
      }
      constants = HashMap()

      val packageManager = applicationContext!!.packageManager
      val packageName = applicationContext!!.packageName
      val shortPackageName = packageName.substring(packageName.lastIndexOf(".") + 1)
      var applicationName = ""
      var applicationVersion = ""
      var buildNumber = 0
      val userAgent = userAgent
      var packageUserAgent = userAgent

      try {
        val info = packageManager.getPackageInfo(packageName, 0)
        applicationName = applicationContext!!.applicationInfo.loadLabel(
          applicationContext!!.packageManager
        ).toString()
        applicationVersion = info.versionName
        buildNumber = info.versionCode
        packageUserAgent = "$shortPackageName/$applicationVersion.$buildNumber $userAgent"
      } catch (e: NameNotFoundException) {
        e.printStackTrace()
      }

      constants["systemName"] = "Android"
      constants["systemVersion"] = Build.VERSION.RELEASE
      constants["packageName"] = packageName
      constants["shortPackageName"] = shortPackageName
      constants["applicationName"] = applicationName
      constants["applicationVersion"] = applicationVersion
      constants["applicationBuildNumber"] = buildNumber
      constants["packageUserAgent"] = packageUserAgent
      constants["userAgent"] = userAgent
      constants["webViewUserAgent"] = webViewUserAgent

      return constants
    }

  private val userAgent: String
    get() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        return System.getProperty("http.agent")
      }

      return ""
    }

  private val webViewUserAgent: String
    get() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        return WebSettings.getDefaultUserAgent(applicationContext)
      }

      val webView = WebView(applicationContext!!)
      val userAgentString = webView.settings.userAgentString

      destroyWebView(webView)

      return userAgentString
    }

  private fun destroyWebView(webView: WebView) {
    webView.loadUrl("about:blank")
    webView.stopLoading()

    webView.clearHistory()
    webView.removeAllViews()
    webView.destroyDrawingCache()

    // NOTE: This can occasionally cause a segfault below API 17 (4.2)
    webView.destroy()
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPluginBinding?) {
    channel.setMethodCallHandler(null)
    channel = null
    applicationContext = null
  }
}

