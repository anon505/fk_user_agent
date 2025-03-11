package io.flutterfastkit.fk_user_agent

import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.NonNull
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

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "fk_user_agent")
    channel?.setMethodCallHandler(this)
    applicationContext = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    if ("getProperties" == call.method) {
      result.success(properties)
    } else {
      result.notImplemented()
    }
  }

  private val properties: MutableMap<String, Any>
    get() {
      if (constants != null) {
        return constants as MutableMap<String, Any>
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
        applicationVersion = info.versionName.toString()
        buildNumber = info.versionCode
        packageUserAgent = "$shortPackageName/$applicationVersion.$buildNumber $userAgent"
      } catch (e: NameNotFoundException) {
        e.printStackTrace()
      }

      (constants as HashMap<String, Any>)["systemName"] = "Android"
      (constants as HashMap<String, Any>)["systemVersion"] = Build.VERSION.RELEASE
      (constants as HashMap<String, Any>)["packageName"] = packageName
      (constants as HashMap<String, Any>)["shortPackageName"] = shortPackageName
      (constants as HashMap<String, Any>)["applicationName"] = applicationName
      (constants as HashMap<String, Any>)["applicationVersion"] = applicationVersion
      (constants as HashMap<String, Any>)["applicationBuildNumber"] = buildNumber
      (constants as HashMap<String, Any>)["packageUserAgent"] = packageUserAgent
      (constants as HashMap<String, Any>)["userAgent"] = userAgent
      (constants as HashMap<String, Any>)["webViewUserAgent"] = webViewUserAgent

      return constants as HashMap<String, Any>
    }

  private val userAgent: String
    get() {
      return System.getProperty("http.agent")?.toString() ?: ""
    }

  private val webViewUserAgent: String
    get() {
      return WebSettings.getDefaultUserAgent(applicationContext)
      

      /*val webView = WebView(applicationContext!!)
      val userAgentString = webView.settings.userAgentString

      destroyWebView(webView)

      return userAgentString*/
    }

  /*private fun destroyWebView(webView: WebView) {
    webView.loadUrl("about:blank")
    webView.stopLoading()

    webView.clearHistory()
    webView.removeAllViews()
    webView.destroyDrawingCache()

    // NOTE: This can occasionally cause a segfault below API 17 (4.2)
    webView.destroy()
  }*/

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel?.setMethodCallHandler(null)
    channel = null
    applicationContext = null
  }

}

