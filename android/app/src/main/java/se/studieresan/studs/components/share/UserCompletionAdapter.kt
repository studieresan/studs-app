package se.studieresan.studs.components.share

import android.content.Context
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import se.studieresan.studs.didChange
import se.studieresan.studs.models.StudsUser

class UserCompletionAdapter(
        context: Context,
        private val autoCompleteTextView: AutoCompleteTextView
): ArrayAdapter<String>(context, android.R.layout.simple_list_item_1) {

    var list: List<StudsUser> by didChange(mutableListOf()) { list ->
        super.clear()
        super.addAll(list.map { it.fullName() })
        notifyDataSetInvalidated()
    }

    private fun StudsUser.fullName() = "${profile.firstName} ${profile.lastName}"

    fun setListener(body: (StudsUser) -> Unit) {
        autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener {
            _, _, _, posAbsolute ->
            val user = list
                    .find { it.fullName() == super.getItem(posAbsolute.toInt()) }
                    ?: return@OnItemClickListener
            body(user)
        }
    }

    fun clearListener() {
        autoCompleteTextView.onItemClickListener = null
    }

}
