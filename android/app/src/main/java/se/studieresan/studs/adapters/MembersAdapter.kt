package se.studieresan.studs.adapters

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import se.studieresan.studs.R
import se.studieresan.studs.circularImage
import se.studieresan.studs.models.Registration
import se.studieresan.studs.models.StudsUser
import se.studieresan.studs.show
import java.text.SimpleDateFormat
import java.util.*

class MembersAdapter: RecyclerView.Adapter<MembersAdapter.ViewHolder>() {

    var listener: OnUserInteractionListener? = null

    private var users: List<StudsUser> = emptyList()
    private var registeredUsers: Set<Registration> = emptySet()
    private var usersLoading: Set<String> = emptySet()

    fun update(
            newUsers: List<StudsUser> = users,
            newRegistrations: Set<Registration> = registeredUsers,
            newLoading: Set<String> = usersLoading
    ) {
        val old = users
        val (registered, unregistered) = newUsers
                .sortedBy { it.profile.firstName }
                .partition { user -> newRegistrations.any { it.userId == user.id } }
        val new = registered + unregistered

        val diff = DiffUtil.calculateDiff(object: DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    old[oldItemPosition].id == new[newItemPosition].id

            override fun getOldListSize(): Int = old.size

            override fun getNewListSize(): Int = new.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldUser = old[oldItemPosition]
                val newUser = new[newItemPosition]
                val oldRegistration = registeredUsers.find { it.userId == oldUser.id }
                val newRegistration = newRegistrations.find { it.userId == newUser.id }

                return oldUser == newUser &&
                        (usersLoading.contains(oldUser.id) ==
                                newLoading.contains(newUser.id)) &&
                        newRegistration == oldRegistration
            }
        })
        registeredUsers = newRegistrations
        usersLoading = newLoading
        users = new
        diff.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = users.size

    val timeFormat = SimpleDateFormat("HH:mm, MMM d", Locale.ENGLISH)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        val profile = user.profile
        holder.name.text = "${profile.firstName} ${profile.lastName}"
        holder.picture.circularImage(profile.picture)
        holder.progress.show(usersLoading.contains(user.id))

        val registration = registeredUsers.find { it.userId == user.id }
        holder.registeredBy.show(registration != null)
        if (registration == null) {
            holder.register.text = "Register"
            holder.register.setOnClickListener {
                listener?.register(user.id)
            }
        } else {
            val registeredBy = users.find { it.id == registration.registeredById }?.profile
            val regTime = registration.time
            val time = if (regTime != null) timeFormat.format(regTime) else null
            holder.register.text = "Unregister"
            holder.registeredBy.text =
                    "${registeredBy?.firstName} ${registeredBy?.lastName?.firstOrNull()} @ $time"
            holder.register.setOnClickListener {
                listener?.unregister(registration)
            }
        }
        holder.call.setOnClickListener {
            listener?.call(user)
        }
        val isLoading = usersLoading.contains(user.id)
        holder.progress.show(isLoading)
        holder.register.show(!isLoading)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_sign_up, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val registeredBy: TextView = view.findViewById(R.id.registered_by)
        val picture: ImageView = view.findViewById(R.id.picture)
        val register: Button = view.findViewById(R.id.register_button)
        val call: View = view.findViewById(R.id.call_button)
        val progress: View = view.findViewById(R.id.progress)
    }

    interface OnUserInteractionListener {
        fun register(userId: String)
        fun unregister(registration: Registration)
        fun call(user: StudsUser)
    }

}
