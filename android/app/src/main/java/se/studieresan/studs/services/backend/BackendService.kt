package se.studieresan.studs.services.backend

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import se.studieresan.studs.models.StudsUser

interface BackendService {
    @POST("login")
    fun login(@Body login: Login): Completable

    @GET("graphql?query=${allUsersQuery}")
    fun fetchUsers(): Single<Users>
}

data class Login(val email: String, val password: String)

data class Users(val data: AllUsers)
data class AllUsers(val studsUsers: List<StudsUser>)

const val userFields =
        """
          email
          firstName
          lastName
          phone
          picture
        """

const val userQuery =
        """query {
             user {
               id
               profile {
                 ${userFields}
               }
               permissions
             }
           }"""

const val allUsersQuery =
        """
        query {
          studsUsers: users(memberType: studs_member) {
            id
            profile { ${userFields} }
          }
        }
        """
