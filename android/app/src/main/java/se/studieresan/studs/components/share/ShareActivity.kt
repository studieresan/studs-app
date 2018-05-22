package se.studieresan.studs.components.share

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.spotify.mobius.Connection
import com.spotify.mobius.First
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.AndroidLogger
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.activity_share.*
import se.studieresan.studs.*
import se.studieresan.studs.components.share.domain.*
import se.studieresan.studs.components.share.domain.ShareError.*
import se.studieresan.studs.models.Category
import se.studieresan.studs.models.Category.*
import se.studieresan.studs.models.StudsUser

class ShareActivity : AppCompatActivity() {

    var controller: MobiusLoop.Controller<ShareModel, ShareEvent>? = null
    var adapter: SharePersonAdapter? = null
    val friendsAdapter by lazy {
        UserCompletionAdapter(this, friends)
    }
    var dispatch: (ShareEvent) -> Unit = {}
    var selectedCategory: Category? by didChange<Category?>(initialValue = null) { category ->
        val selectedColor =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.studs_red))
        val defaultColor =
                ColorStateList.valueOf(Color.BLACK)
        listOf(food, drink, attraction, other).forEach {
            it.setColors(defaultColor)
        }
        when (category) {
            Food -> food.setColors(selectedColor)
            Drink -> drink.setColors(selectedColor)
            Attraction -> attraction.setColors(selectedColor)
            Other -> other.setColors(selectedColor)
        }
    }
    var errors: Set<ShareError> = emptySet()
        set(value) {
            field = value
            listOf(description, address)
                    .forEach { it.error = null }

            errors.forEach {
                when (it) {
                    DescriptionMissing -> description.error = "Description cannot be blank"
                    DescriptionLineCount -> description.error = "Description cannot be longer than $DESCRIPTION_MAX_LINES lines"
                    DescriptionCharCount -> description.error = "Description cannot be longer than $DESCRIPTION_MAX_CHARS characters"
                    LocationMissing -> address.error = "Address cannot be blank"
                    LocationAddressInvalid -> address.error = "Location must be a valid address. Try being more specific?"
                }
            }
        }
    var state: ShareState? by didChange<ShareState?>(null) { state ->
        when (state) {
            ShareState.New -> Unit
            ShareState.Editing -> Unit
            ShareState.Finished -> finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        val effectHandler = (application as Application).shareEffectHandler
        val loopFactory = loopFrom(::update, effectHandler)
                .init { model ->
                    First.first(
                            model.copy(isLoadingUsers = true),
                            setOf(FetchUsers)
                    )
                }
                .logger(AndroidLogger.tag("ShareLoop"))
        controller = MobiusAndroid.controller(loopFactory, ShareModel())
        controller?.connect { connectViews(it) }
    }

    override fun onStart() {
        super.onStart()
        adapter = SharePersonAdapter(object : SharePersonAdapter.Listener {
            override fun onPersonGoing(person: StudsUser) = dispatch(PersonGoing(person))
            override fun onPersonNotGoing(person: StudsUser) = dispatch(PersonNotGoing(person))
        })
        recyclerview.layoutManager = object: LinearLayoutManager(this) {
            override fun canScrollVertically(): Boolean = false
        }
        recyclerview.adapter = adapter
        friends.threshold = 1
        friends.setAdapter(friendsAdapter)
    }

    private fun connectViews(output: Consumer<ShareEvent>): Connection<ShareModel> {
        fab_submit.setOnClickListener { dispatch(SubmitClicked) }
        val descriptionListener =
                description.onTextChange { dispatch(DescriptionChanged(it)) }
        val addressListener =
                address.onTextChange { dispatch(LocationChanged(it)) }
        categories.children
                .forEach { child ->
                    child.setOnClickListener { selectCategory(it) }
                }
        friendsAdapter.setListener { person ->
            dispatch(PersonGoing(person))
            friends.setText("")
        }
        dispatch = output::accept

        return object: Connection<ShareModel> {
            override fun accept(value: ShareModel) = render(value, output)
            override fun dispose() {
                fab_submit.setOnClickListener(null)
                description.removeTextChangedListener(descriptionListener)
                address.removeTextChangedListener(addressListener)
                categories.children.forEach { it.setOnClickListener(null) }
                friendsAdapter.clearListener()
            }
        }
    }

    private fun selectCategory(v: View) =
        when (v.id) {
            R.id.attraction -> dispatch(CategoryChanged(Attraction))
            R.id.food -> dispatch(CategoryChanged(Food))
            R.id.drink -> dispatch(CategoryChanged(Drink))
            R.id.other -> dispatch(CategoryChanged(Other))
            else -> throw IllegalStateException("Missing category")
        }

    fun render(model: ShareModel, output: Consumer<ShareEvent>) {
        selectedCategory = model.selectedCategory
        adapter?.model = SharePersonAdapter.Model(
                people = model.users,
                goingUserIds = model.goingUserIds
        )
        errors = model.errors
        friendsAdapter.list = model.users
                .filterNot { model.goingUserIds.contains(it.id) }
        state = model.state

        progress.show(model.isLoadingUsers)
        debug.setModel(model)
    }

    override fun onResume() {
        super.onResume()
        controller?.start()
    }

    override fun onPause() {
        super.onPause()
        controller?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.disconnect()
    }

    val stateKey = "state"
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        val model = controller?.model
        outState?.putParcelable(stateKey, model)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.getParcelable<ShareModel>(stateKey)?.let { model ->
            controller?.replaceModel(model)
            description.setText(model.description)
            address.setText(model.location)
        }
    }

}
