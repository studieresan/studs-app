package se.studieresan.studs.ui

import android.content.Context
import android.graphics.Rect
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowInsets
import se.studieresan.studs.R


/**
 * Created by jespersandstrom on 2017-05-27.
 */
class SlideupNestedScrollview : NestedScrollView {

    val TAG = SlideupNestedScrollview::class.java.simpleName

    var isAtTop: Boolean = false
        get() {
            val paddingOffset = (height - peekHeight).toInt()
            return scrollY - paddingOffset >= 0
        }

    fun preview() {
        // Allow previewing up to a certain point
        if (scrollY < 100) smoothScrollBy(0, peekHeight.toInt() * 3)
    }

    fun obscure() {
        smoothScrollTo(0, peekHeight.toInt() * 3)
    }

    // Used for determining the location of the contents of this view
    private val viewRect = Rect()
    private val viewLocation = IntArray(2)

    private var peekHeight = 0f

    constructor(context: Context) : super(context) { setup() }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { setup(attrs) }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { setup(attrs) }

    fun setup(attrs: AttributeSet? = null) {
        attrs?.let {
            val tArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SlideupNestedScrollview, 0, 0)
            try {
                peekHeight = tArray.getDimension(R.styleable.SlideupNestedScrollview_peek_height, 0.0f)
            } finally {
                tArray.recycle()
            }
        }
    }

    var paddingSet = false
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (paddingTop == 0 && !paddingSet) {
            paddingSet = true
            val adjustedTopPadding = (height - peekHeight).toInt()
            setPadding(paddingLeft, adjustedTopPadding, paddingRight, paddingBottom)
        }
        super.onLayout(changed, l, t, r, b)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        ev ?: return super.onTouchEvent(ev)
        if (childCount > 0) {
            val contents = getChildAt(0)
            contents.getDrawingRect(viewRect)
            contents.getLocationOnScreen(viewLocation)
            viewRect.offset(viewLocation[0], viewLocation[1])
            super.onTouchEvent(ev)
            return viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt())
        }
        throw IllegalStateException("SlideupNestedScrollview must contain exactly 1 child")
    }

    override fun onApplyWindowInsets(insets: WindowInsets?): WindowInsets {
        insets ?: return super.onApplyWindowInsets(insets)
        val params = layoutParams
        if (params is ViewGroup.MarginLayoutParams) {
            params.setMargins(params.leftMargin, params.topMargin + insets.systemWindowInsetTop, params.rightMargin, params.bottomMargin)
            layoutParams = params
        }
        return super.onApplyWindowInsets(insets)
    }

}
