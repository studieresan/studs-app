package se.studieresan.studs.components.share

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_person_going.view.*
import se.studieresan.studs.R
import se.studieresan.studs.circularImage
import se.studieresan.studs.models.StudsUser

class SharePersonAdapter(
        var listener: Listener
): RecyclerView.Adapter<SharePersonAdapter.ViewHolder>() {

    data class Model(
            val people: List<StudsUser> = emptyList(),
            val goingUserIds: Set<String> = emptySet()
    )

    var model: Model = Model()
        set(newModel) {
            if (model == newModel) return
            val oldPeople = model.people

            val (going, notGoing) = newModel.people
                    .partition { newModel.goingUserIds.contains(it.id) }
            val newPeople = going.sortedBy { it.profile.firstName } +
                    notGoing.sortedBy { it.profile.firstName }

            val diff = DiffUtil.calculateDiff(object: DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                        oldPeople[oldItemPosition].id == newPeople[newItemPosition].id

                override fun getOldListSize(): Int = oldPeople.size

                override fun getNewListSize(): Int = newPeople.size

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldPerson = oldPeople[oldItemPosition]
                    val newPerson = newPeople[newItemPosition]
                    return oldPerson == newPerson &&
                            model.goingUserIds.contains(oldPerson.id) ==
                            newModel.goingUserIds.contains(newPerson.id)
                }
            })
            field = newModel.copy(people = newPeople)
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.list_item_person_going, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = model.people.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val v = holder.itemView
        val person = model.people[position]
        val profile = person.profile

        val isGoing = model.goingUserIds.contains(person.id)

        v.name.text = "${profile.firstName} ${profile.lastName}"
        v.image.circularImage(profile.picture)

        v.button.text = if (isGoing) "Going" else "Not Going"

        v.button.setOnClickListener {
            if (isGoing) {
                listener.onPersonNotGoing(person)
            } else {
                listener.onPersonGoing(person)
            }
        }
    }

    interface Listener {
        fun onPersonGoing(person: StudsUser)
        fun onPersonNotGoing(person: StudsUser)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view)
}
