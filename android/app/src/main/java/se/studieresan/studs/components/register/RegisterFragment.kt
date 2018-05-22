package se.studieresan.studs.components.register

import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.spotify.mobius.Connection
import com.spotify.mobius.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_register.view.*
import se.studieresan.studs.MainActivity
import se.studieresan.studs.R
import se.studieresan.studs.adapters.MembersAdapter
import se.studieresan.studs.domain.*
import se.studieresan.studs.models.Registration
import se.studieresan.studs.models.StudsUser
import se.studieresan.studs.show


class RegisterFragment: DialogFragment(), MembersAdapter.OnUserInteractionListener {

    companion object {
        val activityKey = "activityKey"
        fun createWithActivityKey(activity: String): RegisterFragment {
            val fragment = RegisterFragment()
            val bundle = Bundle()
            bundle.putString(activityKey, activity)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val adapter: MembersAdapter = MembersAdapter()
    private var disposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        view.recycler_view.adapter = adapter
        view.recycler_view.layoutManager = LinearLayoutManager(context)
        adapter.listener = this

        return view
    }

    fun render(model: StudsModel) {
        view?.register_progress?.show(model.loadingRegistrations)
        view?.recycler_view?.show(model.users.isNotEmpty() && !model.loadingRegistrations)
        val key = arguments?.getString(activityKey)!!
        val registering = model.registeringUserIdsForActivity[key] ?: emptySet()
        val unregistering = model.unregisteringUserIdsForActivity[key] ?: emptySet()

        adapter.update(
                newRegistrations = model.registrations,
                newUsers = model.users.toList(),
                newLoading = registering + unregistering
        )
    }

    override fun onStart() {
        super.onStart()

        val connection = object : Connection<StudsModel> {
            override fun accept(model: StudsModel) = render(model)
            override fun dispose() {}
        }
        disposable = (activity as MainActivity).loop.observe(connection)
    }

    override fun onResume() {
        val window = dialog.window
        val size = Point()
        val display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        window.setLayout((size.x * 0.90).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)

        val key = arguments?.getString(activityKey)!!
        (activity as MainActivity).dispatch(Load(RegistrationsLoadable(key)))

        super.onResume()
    }

    override fun onStop() {
        disposable?.dispose()
        super.onStop()
    }

    override fun register(userId: String) {
        val key = arguments?.getString(activityKey)!!
        (activity as MainActivity).dispatch(RegisterUser(userId, key))
    }

    override fun unregister(registration: Registration) =
        (activity as MainActivity).dispatch(UnregisterUser(registration))

    override fun call(user: StudsUser) {
        TODO("not implemented")
    }

}
