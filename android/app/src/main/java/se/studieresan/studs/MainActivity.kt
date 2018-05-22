package se.studieresan.studs

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.spotify.mobius.Connection
import com.spotify.mobius.First
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.AndroidLogger
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.slide_layout.*
import se.studieresan.studs.components.login.LoginActivity
import se.studieresan.studs.components.posts.OverviewFragment
import se.studieresan.studs.components.share.ShareActivity
import se.studieresan.studs.components.recommendations.RecommendationsFragment
import se.studieresan.studs.components.travelInfo.InfoFragment
import se.studieresan.studs.domain.*
import se.studieresan.studs.domain.Page.*
import se.studieresan.studs.models.Activity
import se.studieresan.studs.models.Coordinate
import se.studieresan.studs.ui.SlideupNestedScrollview
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    companion object {
        val TAG = MainActivity::class.java.simpleName!!
    }
    private var theme by didChange(Theme.Night) { theme ->
        map?.style(this, theme.resource)
    }
    private var firstActivity: Activity? = null
    private var activities: Set<Activity> by Delegates.observable(emptySet()) { _, old, new ->
        val oldIds = old.map { it.id }
        val newMarkers = new.filterNot { oldIds.contains(it.id) }.toSet()
        newMarkers.forEach {
            val point = it.location.latLng!!
            val position = LatLng(point.latitude, point.longitude)
            val icon = if (it.isUserActivity == true) HUE_RED else HUE_YELLOW
            val marker = map!!.addMarker(
                    MarkerOptions()
                            .title(it.title ?: it.description)
                            .position(position)
                            .icon(defaultMarker(icon))
            )
            activityMarkerMap += (it.id to (marker to (it.isUserActivity ?: false)))
            if (firstActivity == null && it.isUserActivity != null) {
                firstActivity = it
                dispatch(SelectActivity(firstActivity!!.id))
            }
        }
        toggleMarkersForPage(controller?.model?.page ?: UserShares)
    }
    private var page: Page? by Delegates.observable<Page?>(null) { _, oldPage, page ->
        if (page == null || oldPage == page) return@observable

        // Show the relevant fragment for the page
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val (fragment, id, pageIndex) =
                when (page) {
                    UserShares -> Triple(OverviewFragment(), "overview", 0)
                    Info -> Triple(InfoFragment(), "info", 1)
                    TravelTips -> Triple(RecommendationsFragment(), "todo", 2)
                }
        fragmentTransaction.replace(R.id.fragment_container, fragment, id)
        fragmentTransaction.commit()

        // Highlight the selected tab
        listOf(bottom_nav, top_nav).forEach {
            val nav = it as ViewGroup
            nav.children.forEachIndexed { index, view ->
                val color =
                        if (index != pageIndex) Color.WHITE
                        else ContextCompat.getColor(this@MainActivity, R.color.colorAccent)
                (view as ImageButton).setColorFilter(color)
            }
        }

        toggleMarkersForPage(page)
    }
    private var location: Coordinate? by didChange<Coordinate?>(null) { coordinate ->
        if (coordinate == null) return@didChange
        val latLng = LatLng(coordinate.latitude, coordinate.longitude)
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
    }


    var disposable: Disposable? = null
    private var controller: MobiusLoop.Controller<StudsModel, StudsEvent>? = null
    private val PERMISSIONS_REQUEST_FINE_LOCATION = 1
    val PERMISSIONS_REQUEST_IGNORE = 2
    private val bottomSheet by lazy {
        findViewById<SlideupNestedScrollview>(R.id.slide_up)
    }
    private val mapFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    }

    var map: GoogleMap? = null
    var activityMarkerMap: Map<String, Pair<Marker, Boolean>> = emptyMap()

    val dispatch: (StudsEvent) -> Unit = { event ->
        loop.dispatch(event)
    }
    val loop: ObservableConnectable<StudsModel, StudsEvent> = ObservableConnectable()

    private fun toggleMarkersForPage(page: Page) {
        val (sharedByUsers, recommended) = activityMarkerMap.entries
                .partition { it.value.second }

        sharedByUsers.forEach { it.value.first.isVisible = page != TravelTips }
        recommended.forEach { it.value.first.isVisible = page != UserShares }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val map = map!!

        var firstTouch = true
        map.setOnMarkerClickListener {
            if (firstTouch) {
                val width = findViewById<View>(R.id.fab_share).width * 1.25f
                map.setPadding(0, 0, width.toInt(), 0)
                firstTouch = false
            }
            false
        }
        renderMap(controller?.model)
        val theme = controller?.model?.theme?.resource ?: R.raw.night_style_json
        map.style(this, theme)
    }

    private fun showDeviceLocation () {
        val map = map ?: return
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_FINE_LOCATION)
            return
        }

        map.isMyLocationEnabled = true
        map.uiSettings?.isMyLocationButtonEnabled = false
        dispatch(SelectUserLocation)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_FINE_LOCATION ->
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    showDeviceLocation()
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Launcher)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseMessaging.getInstance().subscribeToTopic("locations")

        fab_my_location.setOnClickListener(this)
        fab_share.setOnClickListener(this)
        setupBottomNav()

        mapFragment.getMapAsync(this)

        val effectHandler = (application as Application).effectHandler
        val loopFactory = loopFrom(::update, effectHandler)
                .init {
                    First.first(
                            it.copy(
                                    loadingCities = true,
                                    loadingActivities = true,
                                    loadingAllUsers = true
                            ),
                            setOf(
                                    Fetch(UsersLoadable),
                                    Fetch(CitiesLoadable),
                                    Fetch(ActivitiesLoadable),
                                    RestoreTheme
                            ))
                }
                .logger(AndroidLogger.tag("MainLoop"))
        controller = MobiusAndroid.controller(loopFactory, StudsModel())
        controller?.connect { loop.connect(it) }
    }

    private fun setupBottomNav() {
        listOf(bottom_nav, top_nav).forEach {
            it.findViewById<View>(R.id.nav_1).setOnClickListener {
                slideUp()
                dispatch(SelectPage(UserShares))
            }
            it.findViewById<View>(R.id.nav_2).setOnClickListener {
                slideUp()
                dispatch(SelectPage(Info))
            }
            it.findViewById<View>(R.id.nav_3).setOnClickListener {
                slideUp()
                dispatch(SelectPage(TravelTips))
            }
        }
        dispatch(SelectPage(UserShares))
    }

    override fun onBackPressed() =
            if (bottomSheet.isAtTop) {
                bottomSheet.obscure()
            } else {
                super.onBackPressed()
            }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_my_location -> showDeviceLocation()
            R.id.fab_share -> {
                startActivity(Intent(this, ShareActivity::class.java))
            }
        }
    }

    private fun animateToMarker(activity: Activity, markerMap: Map<String, Pair<Marker, Boolean>>) {
        val location = activity.location.latLng ?: return
        val latLng = LatLng(location.latitude, location.longitude)
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))

        markerMap[activity.id]?.first?.showInfoWindow()
        if (bottomSheet.isAtTop) bottomSheet.obscure()
        dispatch(UnselectActivity)
    }

    private fun slideUp() = bottomSheet.preview()

    override fun onStart() {
        super.onStart()
        logout.setOnClickListener { logout() }

        val themes = listOf(
                Theme.Night,
                Theme.Standard,
                Theme.BlackAndWhite,
                Theme.Dark,
                Theme.Retro
        )
        val options = listOf("Theme") + themes.map { it.description }
        val themeAdapter = object: ArrayAdapter<String>(
                this, R.layout.spinner_item, options) {
            override fun isEnabled(position: Int): Boolean = position != 0
        }
        toggle_map_theme.adapter = themeAdapter
        toggle_map_theme.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, pos: Long) {
                if (pos > 0) {
                    val theme = themes[pos.toInt() - 1]
                    dispatch(ThemeChanged(theme))
                }
            }
        }

        disposable = loop.observe(object: Connection<StudsModel> {
            override fun accept(model: StudsModel) {
                theme = model.theme
                if (model.selectedActivityId != null) {
                    val activity = model.activities.find { it.id == model.selectedActivityId }
                    animateToMarker(activity!!, activityMarkerMap)
                }
                renderMap(model)
                debug.setModel(model)
                page = model.page
                location = model.location
            }

            override fun dispose() {
                logout.setOnClickListener(null)
                toggle_map_theme.onItemSelectedListener = null
            }
        })
    }

    private fun renderMap(model: StudsModel?) {
        if (map != null && model != null) {
            activities = model.activities.toSet()
        }
    }

    override fun onResume() {
        super.onResume()
        controller?.start()
        // this is done to prevent the page from scrolling up when returning from another activity
        page = null
    }

    override fun onPause() {
        super.onPause()
        controller?.stop()
    }

    override fun onDestroy() {
        controller?.disconnect()
        disposable?.dispose()
        super.onDestroy()
    }

    val stateKey = "state"
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        val model = controller?.model
        outState?.putParcelable(stateKey, model)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.getParcelable<StudsModel>(stateKey)?.let {
            controller?.replaceModel(it)
            Log.d(TAG, "restored: $it")
        }
    }

    fun logout() {
        dispatch.invoke(Logout)
        finish()
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
