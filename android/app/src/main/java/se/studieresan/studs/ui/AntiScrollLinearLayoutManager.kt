package se.studieresan.studs.ui

import android.content.Context
import android.support.v7.widget.LinearLayoutManager

class AntiScrollLinearLayoutManager(context: Context): LinearLayoutManager(context) {
    override fun canScrollVertically() = false
}

