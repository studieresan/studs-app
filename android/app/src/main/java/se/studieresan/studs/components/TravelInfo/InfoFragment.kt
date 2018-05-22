package se.studieresan.studs.components.travelInfo

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import se.studieresan.studs.R

class InfoFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_info, container, false)
        // TODO
//        val webView = view?.findViewById(R.id.web_view) as WebView
//        model.getStaticContent()?.observeNotNull(this) { text ->
//            webView.loadData(text, "text/html; charset=utf-8", "utf-8")
//        }
        return view
    }
}

