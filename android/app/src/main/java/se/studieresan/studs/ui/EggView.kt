package se.studieresan.studs.ui

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.TextView

val egg = "🥚"
val explosion = "💥"
val animal = "⛄️"

class EggView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    val paint = Paint()
    var touches = 0

    init {
        text = egg
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        touches++
        if (touches < 3) {
            animate()
                    .rotationBy(30f)
                    .setInterpolator(AnticipateOvershootInterpolator())
                    .setDuration(300L)
                    .withEndAction { rotation = 0f }
                    .start()
        } else if (touches == 3) {
            text = explosion
            invalidate()
            animate()
                    .scaleX(2.0f)
                    .scaleY(2.0f)
                    .rotation(180f)
                    .setInterpolator(AnticipateOvershootInterpolator())
                    .setDuration(800L)
                    .withEndAction {
                        text = animal
                        scaleX = 1f
                        scaleY = 1f
                        rotation = 0f
                        invalidate()
                    }
                    .start()

        }
        return false
    }

}
