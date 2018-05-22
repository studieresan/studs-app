package se.studieresan.studs

import se.studieresan.studs.models.Activity

interface OnLocationSelectedListener {
    fun onShowActivity(showActivity: Activity)
    fun onRegisterForActivity(activity: Activity)
}
