package se.studieresan.studs.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_post.view.*
import se.studieresan.studs.OnLocationSelectedListener
import se.studieresan.studs.R
import se.studieresan.studs.circularImage
import se.studieresan.studs.models.Activity
import se.studieresan.studs.models.StudsUser
import se.studieresan.studs.models.getIconForCategory
import kotlin.properties.Delegates

class OverviewAdapter(val callback: OnLocationSelectedListener, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val PLACEHOLDER_TYPE = 0
        val NORMAL_TYPE = 1
    }

    data class Model(
            val activities: List<Activity> = emptyList(),
            val users: Set<StudsUser> = emptySet()
    )

    var dataSource: Model by Delegates.observable(Model()) { _, oldModel, newModel ->
        if (oldModel == newModel) return@observable
        val old = oldModel.activities
        val new = newModel.activities

        val diff = DiffUtil.calculateDiff(object: DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    anyPostsExist(old) && old[oldItemPosition].id == new[newItemPosition].id

            override fun getOldListSize(): Int = if (anyPostsExist(old)) old.size else 1

            override fun getNewListSize(): Int = new.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    anyPostsExist(old) && old[oldItemPosition] == new[newItemPosition]
        })
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): RecyclerView.ViewHolder =
            if (anyPostsExist(dataSource.activities)) {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_post, parent, false)
                ViewHolder(v)
            } else {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_placeholder, parent, false)
                PlaceHolder(v)
            }

    override fun getItemViewType(position: Int) =
            if (anyPostsExist(dataSource.activities)) NORMAL_TYPE
            else PLACEHOLDER_TYPE

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        if (holder is ViewHolder) {
            val activity = dataSource.activities[pos]
            val user = dataSource.users.find { it.id == activity.author }
            val view = holder.itemView


            view.title.text = activity.description?.trim()

            val ic = getIconForCategory(activity.category ?: "other")
            val drawable = ContextCompat.getDrawable(context, ic)
            drawable?.setBounds( 0, 0, 60, 60 )
            view.title.setCompoundDrawables(null, null, drawable, null)
            view.sub_title.text = "${user?.profile?.firstName} ${user?.profile?.lastName}"
            // holder.time.text = location.getTimeAgo() TODO

            view.address.text = activity.location.address

            view.image.circularImage(url = user?.profile?.picture ?: "")
            view.post_root.setOnClickListener {
                callback.onLocationSelected(activity)
            }
        }
    }

    override fun getItemCount() =
            if (anyPostsExist(dataSource.activities)) dataSource.activities.size
            else 1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private fun anyPostsExist(activities: List<Activity>) = activities.isNotEmpty()

    class PlaceHolder(view: View) : RecyclerView.ViewHolder(view)
}
