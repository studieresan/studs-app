package se.studieresan.studs.ui

import android.content.Context
import android.support.v7.widget.LinearLayoutManager

/**
 * Created by jespersandstrom on 2017-06-07.
 */
class AntiScrollLinearLayoutManager(context: Context): LinearLayoutManager(context) {
    override fun canScrollVertically() = false
}

