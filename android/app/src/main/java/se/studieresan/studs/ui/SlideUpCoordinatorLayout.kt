package se.studieresan.studs.ui

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import se.studieresan.studs.R

class SlideUpCoordinatorLayout : CoordinatorLayout {

    // TODO @JvmArgs here instead of this mess
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private var setPadding = false

    override fun onApplyWindowInsets(insets: WindowInsets?): WindowInsets {
        insets ?: return super.onApplyWindowInsets(insets)

        val topNav = findViewById<View>(R.id.bottom_nav)
        if (!setPadding && topNav.paddingTop == 0) {
            topNav.setPadding(paddingLeft, paddingTop + insets.systemWindowInsetTop, paddingRight, paddingBottom)
            setPadding = true
        }
        return super.onApplyWindowInsets(insets)
    }
}
