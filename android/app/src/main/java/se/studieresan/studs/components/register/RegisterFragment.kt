package se.studieresan.studs.components.register

import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.spotify.mobius.Connection
import com.spotify.mobius.First
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.AndroidLogger
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_register.view.*
import se.studieresan.studs.Application
import se.studieresan.studs.R
import se.studieresan.studs.adapters.MembersAdapter
import se.studieresan.studs.components.register.domain.*
import se.studieresan.studs.loopFrom
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
        fun display(activityId: String, fragmentManager: FragmentManager) {
            val ft = fragmentManager.beginTransaction()
            val prev = fragmentManager.findFragmentByTag("dialog")
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            val newFragment = RegisterFragment.createWithActivityKey(activityId)
            newFragment.show(ft, "dialog")
        }
    }

    private val adapter: MembersAdapter = MembersAdapter()
    private var controller: MobiusLoop.Controller<RegisterModel, RegisterEvent>? = null
    private var output: Consumer<RegisterEvent>? = null

    private val dispatch: (RegisterEvent) -> Unit = { event ->
        output?.accept(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val effectHandler = (activity!!.application as Application).registerEffectHandler
        val activityId = arguments?.getString(activityKey)!!
        val loopFactory = loopFrom(update, effectHandler)
                .init {
                    First.first(
                            it.copy(
                                    isLoadingUsers = true,
                                    isLoadingRegistrations = true
                            ),
                            setOf(
                                    FetchUsers,
                                    FetchRegistrations(activityId = activityId)
                            ))
                }
                .logger(AndroidLogger.tag("RegisterLoop"))
        controller = MobiusAndroid.controller(loopFactory, RegisterModel(activityId = activityId))
        controller?.connect(this::connectViews)
    }

    private fun connectViews(output: Consumer<RegisterEvent>?): Connection<RegisterModel> {
        this.output = output
        return object: Connection<RegisterModel> {
            override fun accept(value: RegisterModel) = render(value)
            override fun dispose() {}
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        view.recycler_view.adapter = adapter
        view.recycler_view.layoutManager = LinearLayoutManager(context)
        adapter.listener = this

        return view
    }

    fun render(model: RegisterModel) {
        view?.register_progress?.show(model.isLoadingRegistrations)
        view?.recycler_view?.show(model.users.isNotEmpty() && !model.isLoadingRegistrations)

        val registering = model.registeringUserIds
        val unregistering = model.unregisteringUserIds

        adapter.update(
                newRegistrations = model.registrations,
                newUsers = model.users.toList(),
                newLoading = registering + unregistering
        )
    }

    override fun onResume() {
        val window = dialog.window
        val size = Point()
        val display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        window.setLayout((size.x * 0.90).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
        super.onResume()

        controller?.start()
    }

    override fun onPause() {
        super.onPause()
        controller?.stop()
    }

    override fun register(userId: String) =
            dispatch(RegisterUser(userId))

    override fun unregister(registration: Registration) =
        dispatch(UnregisterUser(registration))

    override fun call(user: StudsUser) {
        TODO("not implemented")
    }

    val modelKey = "registerModelKey"
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller?.model?.let { model ->
            outState.putParcelable(modelKey, model)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getParcelable<RegisterModel>(modelKey)?.let {
            model ->
            controller?.replaceModel(model)
        }
    }
}
