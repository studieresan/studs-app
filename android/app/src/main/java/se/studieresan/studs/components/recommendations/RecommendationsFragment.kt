package se.studieresan.studs.components.recommendations

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spotify.mobius.Connection
import com.spotify.mobius.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_todo.*
import se.studieresan.studs.MainActivity
import se.studieresan.studs.R
import se.studieresan.studs.adapters.ActivityAdapter
import se.studieresan.studs.components.register.RegisterFragment
import se.studieresan.studs.didChange
import se.studieresan.studs.domain.SelectActivity
import se.studieresan.studs.domain.StudsModel
import se.studieresan.studs.models.Activity
import se.studieresan.studs.show
import se.studieresan.studs.ui.AntiScrollLinearLayoutManager


class RecommendationsFragment: Fragment(), ActivityAdapter.ActivitySelectedListener {

    private val adapter: ActivityAdapter by lazy {
        val adapter = ActivityAdapter()
        adapter.listener = this
        todos_rv.layoutManager = AntiScrollLinearLayoutManager(context!!)
        todos_rv.adapter = adapter
        adapter
    }

    private var disposable: Disposable? = null
    private var activitiesCache: Set<Activity> by didChange(emptySet()) { activities ->
        adapter.updateActivities(activities.toList())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_todo, container, false)

    override fun onRegisterForEventSelected(activity: Activity) =
        RegisterFragment.display(activity.id, fragmentManager!!)

    override fun onCalendarSelected(activity: Activity) {
        if (activity.start != null && activity.end != null) {
            val intent = Intent(Intent.ACTION_EDIT)
            intent.type = "vnd.android.cursor.item/event"
            intent.putExtra("beginTime", activity.start.time)
            intent.putExtra("endTime", activity.end.time)
            intent.putExtra(CalendarContract.Events.TITLE, activity.title)
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, activity.location.address)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val connection = object : Connection<StudsModel> {
            override fun accept(model: StudsModel) {
                activitiesCache = model.activities
                        .filterNot { it.isUserActivity ?: false }
                        .toSet()

                val isLoading = model.isLoadingAllUsers ||
                        model.isLoadingCities ||
                        model.isLoadingActivities

                todos_progress.show(isLoading)
                todos_rv.show(!isLoading)
            }

            override fun dispose() {}
        }

        disposable = (activity as MainActivity).loop.observe(connection)
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    override fun onShowOnMapSelected(activity: Activity) {
        (this.activity as MainActivity).loop.dispatch(SelectActivity(activity.id))
    }
}
