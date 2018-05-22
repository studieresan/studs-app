package se.studieresan.studs

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import jp.wasabeef.glide.transformations.CropCircleTransformation


fun View.show(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun EditText.onTextChange(onChange: (String) -> Unit): TextWatcher {
    val textWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable) = Unit

        override fun beforeTextChanged(s: CharSequence, start: Int,
                                       count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence, start: Int,
                                   before: Int, count: Int) {
            onChange(s.toString())
        }
    }
    this.addTextChangedListener(textWatcher)
    return textWatcher
}

fun TextView.setColors(color: ColorStateList) {
    animateColor(fromColor = textColors.defaultColor, toColor = color.defaultColor) { animator ->
        val currentColor = ColorStateList.valueOf(animator.animatedValue as Int)
        compoundDrawables.forEach {
            it?.colorFilter = PorterDuffColorFilter(currentColor.defaultColor, PorterDuff.Mode.SRC_IN)
        }
        setTextColor(currentColor)
    }
}

val ViewGroup.children: List<View>
    get() {
        val children = mutableListOf<View>()
        for (i in 0 until childCount) {
            children.add(getChildAt(i))
        }
        return children
    }

fun GoogleMap.style(context: Context, theme: Int) {
    try {
        val success = setMapStyle(
                MapStyleOptions.loadRawResourceStyle(context, theme)
        )

        if (!success) {
            Log.e("GOOGLE_MAP", "Style parsing failed.")
        }
    } catch (e: Resources.NotFoundException) {
        Log.e("GOOGLE_MAP", "Can't find style. Error: ", e)
    }
}

fun ImageView.circularImage(url: String?) =
        Glide.with(context)
                .load(url)
                .centerCrop()
                .override(200, 200)
                .bitmapTransform(CropCircleTransformation(context))
                .dontAnimate()
                .into(this)

private fun animateColor(fromColor: Int, toColor: Int, duration: Long = 100, body: (ValueAnimator) -> Unit) {
    val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
    colorAnimation.duration = duration
    colorAnimation.addUpdateListener(body)
    colorAnimation.start()
}
