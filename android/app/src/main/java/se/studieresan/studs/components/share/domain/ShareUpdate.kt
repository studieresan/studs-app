package se.studieresan.studs.components.share.domain

import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import se.studieresan.studs.DESCRIPTION_MAX_CHARS
import se.studieresan.studs.DESCRIPTION_MAX_LINES
import se.studieresan.studs.components.share.domain.ShareError.*
import se.studieresan.studs.next

fun update(model: ShareModel, event: ShareEvent): Next<ShareModel, ShareEffect> =
        when (event) {

            is UsersChanged ->
                next(model = model.copy(users = event.users, isLoadingUsers = false))

            is GoingChanged ->
                next(model = model.copy(goingUserIds = event.goingUserIds))

            is CategoryChanged ->
                next(model = model.copy(selectedCategory = event.category))

            is DescriptionChanged -> {
                val description = event.description
                val errors = model.errors.toMutableSet().apply {
                    if (isDescriptionTooManyLines(description)) this += DescriptionLineCount
                    else this -= DescriptionLineCount

                    if (isDescriptionTooLong(description)) this += DescriptionCharCount
                    else this -= DescriptionCharCount

                    if (description.isBlank()) this += DescriptionMissing
                    else this -= DescriptionMissing
                }.toSet()

                next(model = model.copy(
                        description = event.description,
                        errors = errors
                ))
            }

            is LocationChanged -> {
                val errors =
                        if (event.location.isBlank()) model.errors + LocationMissing
                        else model.errors - LocationMissing

                next(model = model.copy(
                        location = event.location,
                        errors = errors - LocationAddressInvalid
                ))
            }

            is SubmitClicked -> {
                val errors = findErrors(model)
                if (errors.isEmpty()) {
                    dispatch<ShareModel, ShareEffect>(setOf(Submit(model)))
                } else {
                    next(model = model.copy(errors = model.errors + errors))
                }
            }

            is PersonGoing ->
                next(model = model.copy(goingUserIds = model.goingUserIds + event.person.id))

            is PersonNotGoing ->
                next(model = model.copy(goingUserIds = model.goingUserIds - event.person.id))


            is ErrorAdded -> next(model = model.copy(errors = model.errors + event.error))

            is StateChanged -> next(model = model.copy(state = event.state))

        }

private fun findErrors(model: ShareModel): List<ShareError> =
        listOfNotNull(
                if (model.description.isBlank()) DescriptionMissing else null,
                if (isDescriptionTooManyLines(model.description)) DescriptionLineCount else null,
                if (isDescriptionTooLong(model.description)) DescriptionCharCount else null,
                if (model.location.isBlank()) LocationMissing else null
        )

private fun isDescriptionTooLong(description: String) = description.count() > DESCRIPTION_MAX_CHARS
private fun isDescriptionTooManyLines(description: String) = description.lines().count() > DESCRIPTION_MAX_LINES
