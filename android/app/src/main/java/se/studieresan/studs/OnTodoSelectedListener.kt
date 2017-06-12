package se.studieresan.studs

import se.studieresan.studs.models.Todo

/**
 * Created by jakob on 2017-06-12.
 */
interface OnTodoSelectedListener {
    fun onTodoSelected(todo: Todo)
}
