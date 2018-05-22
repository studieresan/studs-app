package se.studieresan.studs.debug.list

import android.view.View
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.list_item_header.view.*
import se.studieresan.studs.R

sealed class Items: Item()

val basePadding = 50

class HeaderItem(
        private val name: String,
        private val indentation: Int = 0
) : Items(), ExpandableItem {

    var toggle: ExpandableGroup? = null

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        toggle = onToggleListener
    }

    override fun getLayout() = R.layout.list_item_header

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView
        val toggle = toggle!!

        view.setPadding(basePadding, basePadding, basePadding, basePadding)
        view.setPaddingLeft(basePadding + indentation * basePadding)

        view.setOnClickListener {
            toggle.onToggleExpanded()
            view.title.text = "$name:" + if (toggle.isExpanded) "" else " {...}"
        }
        view.title.text = "$name:" + if (toggle.isExpanded) "" else " {...}"
    }
}

class NormalItem(
        private val name: String,
        private val value: String,
        private val indentation: Int = 0
): Items() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView
        view.setPadding(basePadding, basePadding, basePadding, basePadding)
        view.setPaddingLeft(basePadding + indentation * basePadding)
        view.title.text = "$name: $value"
    }

    override fun getLayout() = R.layout.list_item_normal

}

private fun View.setPaddingLeft(leftPad: Int) =
        setPadding(
                leftPad,
                paddingTop,
                paddingEnd,
                paddingLeft
        )
