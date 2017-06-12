package se.studieresan.studs

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import se.studieresan.studs.adapters.TodoAdapter
import se.studieresan.studs.models.Todo
import se.studieresan.studs.ui.AntiScrollLinearLayoutManager

class TodoFragment: LifecycleFragment(), OnTodoSelectedListener {
    val adapter: TodoAdapter by lazy {
        TodoAdapter(this, activity)
    }
    val model: StudsViewModel by lazy {
        ViewModelProviders.of(activity).get(StudsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.fragment_todo, container, false)
        val rv = v.findViewById(R.id.todos_rv) as RecyclerView
        rv.layoutManager = AntiScrollLinearLayoutManager(context)
        rv.adapter = adapter
        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model.getTodos()?.observe(this, Observer {  todos ->
            todos ?: return@Observer
            adapter.dataSource = todos
        })
    }

    override fun onTodoSelected(todo: Todo) {
        model.selectTodo(todo)
    }
}
