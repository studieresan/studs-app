package se.studieresan.studs.components.Posts

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import se.studieresan.studs.OnLocationSelectedListener
import se.studieresan.studs.R
import se.studieresan.studs.StudsViewModel
import se.studieresan.studs.adapters.OverviewAdapter
import se.studieresan.studs.extensions.observeNotNull
import se.studieresan.studs.models.Location
import se.studieresan.studs.ui.AntiScrollLinearLayoutManager


class OverviewFragment : LifecycleFragment(), OnLocationSelectedListener {

    val adapter: OverviewAdapter by lazy {
        OverviewAdapter(this, activity)
    }
    val model: StudsViewModel by lazy {
      ViewModelProviders.of(activity).get(StudsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater!!.inflate(R.layout.fragment_overview, container, false)

        val rv = v.findViewById(R.id.updates_rv) as RecyclerView
        rv.layoutManager = AntiScrollLinearLayoutManager(context)
        rv.adapter = adapter
        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model = ViewModelProviders.of(activity).get(StudsViewModel::class.java)
        model.getPosts()?.observeNotNull(this) { posts ->
            adapter.dataSource = posts
        }
        model.getUsers()?.observeNotNull(this) { users ->
            adapter.users = users
        }
    }

    override fun onLocationSelected(location: Location) {
        model.selectPost(location)
    }

}
