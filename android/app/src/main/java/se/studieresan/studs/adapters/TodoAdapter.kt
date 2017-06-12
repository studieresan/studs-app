package se.studieresan.studs.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import se.studieresan.studs.OnTodoSelectedListener
import se.studieresan.studs.R
import se.studieresan.studs.models.Todo
import kotlin.properties.Delegates

class TodoAdapter(val callback: OnTodoSelectedListener) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {

    var dataSource: List<Todo> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder? {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_todo, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val todo = dataSource[pos]
        holder.title.text = todo.name
        holder.subTitle.text = todo.description
        val fallback = holder.itemView.context.getDrawable(R.drawable.ic_location_on_black_24dp)
        holder.image.setImageDrawable(fallback)
        holder.itemView.setOnClickListener {
            callback.onTodoSelected(todo)
        }
    }

    override fun getItemCount() = dataSource.count()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById(R.id.title) as TextView
        val subTitle = view.findViewById(R.id.sub_title) as TextView
        val image = view.findViewById(R.id.image) as ImageView
    }
}
