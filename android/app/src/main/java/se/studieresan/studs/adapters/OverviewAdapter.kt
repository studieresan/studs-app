package se.studieresan.studs.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import jp.wasabeef.glide.transformations.CropCircleTransformation
import se.studieresan.studs.OnLocationSelectedListener
import se.studieresan.studs.R
import se.studieresan.studs.models.Location
import se.studieresan.studs.models.User
import se.studieresan.studs.models.getTimeAgo
import kotlin.properties.Delegates

/**
 * Created by jespersandstrom on 2017-06-07.
 */
class OverviewAdapter(val callback: OnLocationSelectedListener) : RecyclerView.Adapter<OverviewAdapter.ViewHolder>() {

    var dataSource: List<Location> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }
    var users: List<User> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder? {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_post, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val location = dataSource[pos]
        val user = users.find { it.id == location.user }
        holder.title.text = location.message
        holder.subTitle.text = user?.name ?: "Unknown was here"
        holder.time.text = getTimeAgo(location.timestamp)
        if (user != null) {
            holder.image.circularImage(url = user.picture)
        } else {
            val fallback = holder.itemView.context.getDrawable(R.drawable.ic_person_black_24dp)
            holder.image.setImageDrawable(fallback)
        }
        holder.itemView.setOnClickListener {
            callback.onLocationSelected(location)
        }
    }

    override fun getItemCount() = dataSource.count()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById(R.id.title) as TextView
        val subTitle = view.findViewById(R.id.sub_title) as TextView
        val image = view.findViewById(R.id.image) as ImageView
        val time = view.findViewById(R.id.time) as TextView
    }
}

fun ImageView.circularImage(url: String?) =
        Glide.with(context)
                .load(url)
                .centerCrop()
                .bitmapTransform(CropCircleTransformation(context))
                .into(this)
