package se.studieresan.studs.debug

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.widget.FrameLayout
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.model_explorer.view.*
import se.studieresan.studs.R
import se.studieresan.studs.debug.list.fromTree
import java.util.concurrent.TimeUnit

class ModelExplorerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val adapter by lazy {
        addView(inflate(context, R.layout.model_explorer, null))
        val adapt = GroupAdapter<ViewHolder>()
        recyclerview.layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = adapt
        adapt
    }
    private var modelQueue: Subject<Any>? = null
    private var disposable: Disposable? = null

    fun <M: Any> setModel(model: M) = modelQueue?.onNext(model)

    override fun onAttachedToWindow() {
        modelQueue = PublishSubject.create()
        modelQueue!!
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.computation())
                .flatMap { model ->
                    val tree = fromTree(parse(model, "model"))
                    Observable.just(tree)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe { model ->
                    adapter.clear()
                    adapter.add(model)
                }
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        modelQueue?.onComplete()
        modelQueue = null
        disposable?.dispose()

        super.onDetachedFromWindow()
    }
}

