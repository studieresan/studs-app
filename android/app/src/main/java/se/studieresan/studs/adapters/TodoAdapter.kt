package se.studieresan.studs.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import se.studieresan.studs.OnTodoSelectedListener
import se.studieresan.studs.R
import se.studieresan.studs.models.Todo
import se.studieresan.studs.models.getIconForCategory
import kotlin.properties.Delegates

class TodoAdapter(val callback: OnTodoSelectedListener, val context: Context) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {

    var dataSource: List<Todo?> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder? {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_todo, parent, false)
        return ViewHolder(v)
    }

    fun getDrawableForDaynight(daynight: String): Drawable {
        val ic = when (daynight) {
            "night" -> R.drawable.ic_brightness_3_black_24dp
            "day" -> R.drawable.ic_brightness_5_black_24dp
            else -> R.drawable.ic_brightness_4_black_24dp
        }
        val drawable = ContextCompat.getDrawable(context, ic)
        drawable.setBounds( 0, 0, 60, 60 );
        return drawable
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val todo = dataSource[pos] ?: return
        holder.title.text = todo.name
        val ic = getIconForCategory(todo.category)
        val drawable = ContextCompat.getDrawable(context, ic)
        drawable.setBounds( 0, 0, 60, 60 );
        holder.title.setCompoundDrawables(drawable, null, null, null)
        holder.subTitle.text = todo.description
        val daynight = getDrawableForDaynight(todo.daynight)
        holder.subTitle.setCompoundDrawables(daynight, null, null, null)
////        val fallback = holder.itemView.context.getDrawable(R.drawable.ic_location_on_black_24dp)
////        holder.image.setImageDrawable(fallback)
        holder.itemView.setOnClickListener {
            callback.onTodoSelected(todo)
        }
    }

    override fun getItemCount() = dataSource.count()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById(R.id.title) as TextView
        val subTitle = view.findViewById(R.id.sub_title) as TextView
    }
}
