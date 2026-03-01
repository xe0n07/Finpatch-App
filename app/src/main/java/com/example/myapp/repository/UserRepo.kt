package com.example.finpatch.data.repo

import com.example.myapp.model.UserModel


interface UserRepo {

    fun login(email: String, password: String,
              callback: (Boolean, String) -> Unit)

    fun register(email: String, password: String,
                 callback: (Boolean, String, String) -> Unit)

    fun addUserToDatabase(userId: String, model: UserModel,
                          callback: (Boolean, String) -> Unit)

    /**
     * Authenticate with Google, then create or retrieve the user in Firebase DB.
     * On first sign-in, saves [username] and [currency] chosen on LandingScreen.
     */
    fun signInWithGoogle(
        idToken: String,
        username: String,
        currency: String,
        callback: (Boolean, String) -> Unit
    )

    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit)

    fun deleteAccount(userId: String, password: String,
                      callback: (Boolean, String) -> Unit)

    fun editProfile(userId: String, model: UserModel,
                    callback: (Boolean, String) -> Unit)

    fun getUserById(userId: String, callback: (Boolean, String, UserModel?) -> Unit)

    fun getAllUser(callback: (Boolean, String, List<UserModel>) -> Unit)
}
