package se.studieresan.studs.components.TravelInfo

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import se.studieresan.studs.R
import se.studieresan.studs.StudsViewModel
import se.studieresan.studs.extensions.observeNotNull

class InfoFragment: LifecycleFragment() {
    val model: StudsViewModel by lazy {
        ViewModelProviders.of(activity).get(StudsViewModel::class.java)
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_info, container, false)
        val webView = view?.findViewById(R.id.web_view) as WebView
        model.getStaticContent()?.observeNotNull(this) { text ->
            webView.loadData(text, "text/html; charset=utf-8", "utf-8")
        }
        return view
    }
}

