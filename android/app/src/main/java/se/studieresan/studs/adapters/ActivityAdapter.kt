package se.studieresan.studs.adapters

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import se.studieresan.studs.R
import se.studieresan.studs.adapters.ActivityAdapter.ViewHolder
import se.studieresan.studs.models.Activity
import se.studieresan.studs.show
import java.text.SimpleDateFormat
import java.util.*

class ActivityAdapter: RecyclerView.Adapter<ViewHolder>() {

    private var activities: List<Activity> = emptyList()
    private val dateFormatter = SimpleDateFormat("HH:mm MMM dd ", Locale.ENGLISH)

    var listener: ActivitySelectedListener? = null

    fun updateActivities(newActivities: List<Activity>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
                = activities[oldItemPosition].id == newActivities[newItemPosition].id

            override fun getOldListSize(): Int = activities.size

            override fun getNewListSize(): Int = newActivities.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
                = activities[oldItemPosition] == newActivities[newItemPosition]

        })
        activities = newActivities
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_activity_expanded, parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = activities.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activities[position]

        holder.title.text = activity.title

        holder.description.show(activity.description != null)
        holder.description.text = activity.description

        holder.date.show(activity.start != null && activity.end != null)
        val start = activity.start
        val end = activity.end
        if (start != null && end != null) {
            val first = dateFormatter.format(start)
            val second = dateFormatter.format(end)
            holder.date.text = "$first to $second"
        }

        holder.price.show(activity.description != null)
        holder.price.text = activity.price

        holder.register.setOnClickListener { listener?.onRegisterForEventSelected(activity) }
        holder.showOnMap.setOnClickListener { listener?.onShowOnMapSelected(activity) }
        holder.date.setOnClickListener { listener?.onCalendarSelected(activity) }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val description: TextView = view.findViewById(R.id.description)
        val price: TextView = view.findViewById(R.id.price)
        val date: Button = view.findViewById(R.id.date)
        val register: Button = view.findViewById(R.id.register)
        val showOnMap: Button = view.findViewById(R.id.show_on_map)
    }

    interface ActivitySelectedListener {
        fun onRegisterForEventSelected(activity: Activity)
        fun onCalendarSelected(activity: Activity)
        fun onShowOnMapSelected(activity: Activity)
    }
}
