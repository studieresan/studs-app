package se.studieresan.studs.ui

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View

/**
 * Created by jespersandstrom on 2017-05-27.
 */
class ParallaxBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attrs) {

    companion object {
        val TAG = ParallaxBehavior::class.java.simpleName
    }

    var scrollPos = 0

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View?, dep: View?): Boolean {
        val dependency = dep ?: return super.onDependentViewChanged(parent, child, dep)

        val oldPos = scrollPos
        scrollPos = dependency.scrollY
        val dyConsumed = scrollPos - oldPos
        val maxOffset = child?.height?.times(-0.15f) ?: 0f
        if (dyConsumed > 0) {
            child?.translationY = Math.max(-scrollPos * 0.5f, maxOffset)
        } else if (dyConsumed < 0 && scrollPos < Math.abs(maxOffset)) {
            child?.translationY = Math.min(-scrollPos, 0).toFloat()
        }

        return super.onDependentViewChanged(parent, child, dep)
    }

}
