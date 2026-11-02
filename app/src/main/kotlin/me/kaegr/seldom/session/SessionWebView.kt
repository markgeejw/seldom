package me.kaegr.seldom.session

import android.content.Context
import android.view.MotionEvent
import android.view.PointerIcon
import android.webkit.WebView

class SessionWebView(context: Context) : WebView(context) {
    override fun onResolvePointerIcon(event: MotionEvent, pointerIndex: Int): PointerIcon =
        PointerIcon.getSystemIcon(context, PointerIcon.TYPE_NULL)
}
