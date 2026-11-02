package me.kaegr.seldom.session

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.webkit.HttpAuthHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.kaegr.seldom.SeldomServices
import me.kaegr.seldom.hosts.Host
import me.kaegr.seldom.hosts.originOf

class SessionActivity : ComponentActivity() {
    private lateinit var services: SeldomServices
    private lateinit var keyboardCapture: KeyboardCapture
    private var web: SessionWebView? = null
    private var captured = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        services = SeldomServices.of(this)
        keyboardCapture = KeyboardCapture(window, componentName)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.attributes = window.attributes.apply {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = confirmDisconnect()
        })

        val hostId = intent.data?.lastPathSegment
        lifecycleScope.launch {
            val host = services.hostStore.hosts.first().firstOrNull { it.id == hostId }
            if (host == null) finish() else open(host)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun open(host: Host) {
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        val script = assets.open("seldom-input.js").use { it.readBytes().decodeToString() }
        val origin = originOf(host.url).orEmpty()
        val view = SessionWebView(this)
        web = view
        view.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
        }
        val fallbackScript = if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            WebViewCompat.addDocumentStartJavaScript(view, script, setOf(origin))
            null
        } else {
            script
        }
        view.webViewClient = SessionClient(host, origin, fallbackScript)
        setContentView(view)
        view.requestFocus()
        view.loadUrl(host.url)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        setCapture(hasFocus)
    }

    override fun onDestroy() {
        web?.destroy()
        web = null
        super.onDestroy()
    }

    private fun setCapture(enabled: Boolean) {
        captured = enabled
        keyboardCapture.set(enabled)
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (isCaptureToggle(event)) {
            setCapture(!captured)
            Toast.makeText(this, if (captured) "Keyboard capture on" else "Keyboard capture off", Toast.LENGTH_SHORT).show()
            return true
        }
        val view = web
        val copy = ArrowRewrite.rewrite(event)
        if (view != null && copy != null) {
            view.dispatchKeyEvent(copy)
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        val view = web
        if (view != null && event.actionMasked == MotionEvent.ACTION_HOVER_MOVE && event.isFromSource(InputDevice.SOURCE_MOUSE)) {
            val origin = IntArray(2)
            view.getLocationInWindow(origin)
            val x = event.x - origin[0]
            val y = event.y - origin[1]
            view.evaluateJavascript("window.__seldomHover && __seldomHover($x,$y)", null)
        }
        return super.dispatchGenericMotionEvent(event)
    }

    private fun confirmDisconnect() {
        AlertDialog.Builder(this)
            .setTitle("Disconnect?")
            .setPositiveButton("Disconnect") { _, _ -> finish() }
            .setNegativeButton("Stay", null)
            .show()
    }

    private inner class SessionClient(
        private val host: Host,
        private val origin: String,
        private val fallbackScript: String?,
    ) : WebViewClient() {
        private val auth = HttpAuthSession()

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if (!request.isForMainFrame) return false
            if (originOf(request.url.toString()) == origin) return false
            if (request.url.scheme?.lowercase() in WEB_SCHEMES) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, request.url).addCategory(Intent.CATEGORY_BROWSABLE))
                } catch (_: RuntimeException) {
                }
            }
            return true
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            fallbackScript?.let { view.evaluateJavascript(it, null) }
        }

        override fun onPageFinished(view: WebView, url: String) {
            fallbackScript?.let { view.evaluateJavascript(it, null) }
            auth.onPageFinished()?.let { remembered ->
                lifecycleScope.launch { services.vault.put(host.id, remembered.user, remembered.pass) }
            }
        }

        override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, authHost: String, realm: String) {
            val savedRejected = auth.onChallenge()
            val ownHost = isHostOf(authHost, host.url)
            lifecycleScope.launch {
                if (savedRejected) services.vault.forget(host.id)
                val saved = if (ownHost && !savedRejected) services.vault.get(host.id) else null
                if (saved != null) {
                    auth.savedSent()
                    handler.proceed(saved.user, saved.pass)
                    return@launch
                }
                HttpAuthPrompt.show(
                    context = this@SessionActivity,
                    title = authHost,
                    allowRemember = ownHost,
                    onSubmit = { user, pass, remember ->
                        if (remember && ownHost) auth.entered(Credentials(user, pass))
                        handler.proceed(user, pass)
                    },
                    onCancel = { handler.cancel() },
                )
            }
        }
    }

    companion object {
        fun intent(context: Context, host: Host): Intent =
            Intent(context, SessionActivity::class.java)
                .setData("seldom://host/${host.id}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)

        private val WEB_SCHEMES = setOf("http", "https")
    }
}
