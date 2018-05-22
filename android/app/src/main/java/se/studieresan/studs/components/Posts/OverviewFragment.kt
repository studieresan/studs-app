package se.studieresan.studs.components.posts

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spotify.mobius.Connection
import com.spotify.mobius.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_overview.*
import se.studieresan.studs.*
import se.studieresan.studs.adapters.OverviewAdapter
import se.studieresan.studs.domain.SelectActivity
import se.studieresan.studs.domain.StudsModel
import se.studieresan.studs.models.Activity
import se.studieresan.studs.ui.AntiScrollLinearLayoutManager


class OverviewFragment : Fragment(), OnLocationSelectedListener {

    private val adapter: OverviewAdapter by lazy {
        val adapter = OverviewAdapter(this, context!!)
        updates_rv.layoutManager = AntiScrollLinearLayoutManager(context!!)
        updates_rv.adapter = adapter
        adapter
    }

    private var disposable: Disposable? = null
    private var activities: OverviewAdapter.Model by didChange(OverviewAdapter.Model()) {
        adapter.dataSource = it
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_overview, container, false)

    override fun onResume() {
        super.onResume()
        val connection = object : Connection<StudsModel> {
            override fun accept(model: StudsModel) {
                val newActivities = model.activities // TODO optimize
                        .filter { it.isUserActivity ?: false }

                if (newActivities.isNotEmpty() && model.users.isNotEmpty()) {
                    activities = OverviewAdapter.Model(
                            newActivities,
                            model.users
                    )
                }
                val isLoading = model.loadingAllUsers || model.loadingActivities
                shares_progress.show(isLoading)
                updates_rv.show(!isLoading)
            }
            override fun dispose() {}
        }

        val loop = (activity as MainActivity).loop
        disposable = loop.observe(connection)
    }

    override fun onPause() {
        disposable?.dispose()
        super.onPause()
    }

    override fun onLocationSelected(location: Activity) {
        (activity as MainActivity).dispatch(SelectActivity(activityId = location.id))
    }

}
