package se.studieresan.studs.ui

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View

class BottomNavBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attrs) {

    companion object {
        val TAG = BottomNavBehavior::class.java.simpleName
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View?, dep: View?): Boolean {
        when (dep) {
            is SlideupNestedScrollview ->
                if (dep.isAtTop) {
                    child?.visibility = View.VISIBLE
                    child?.elevation = 21f
                } else {
                    child?.visibility = View.INVISIBLE
                }
        }
        return super.onDependentViewChanged(parent, child, dep)
    }

}
