package com.example.myapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.finpatch.data.repo.UserRepo
import com.example.myapp.model.UserModel
import com.example.myapp.repository.UserRepoImpl


class UserViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(UserRepoImpl()) as T
    }
}


class UserViewModel(private val repo: UserRepo) : ViewModel() {

    private val _user = MutableLiveData<UserModel?>()
    val user: MutableLiveData<UserModel?> get() = _user

    private val _allUsers = MutableLiveData<List<UserModel>?>()
    val allUsers: MutableLiveData<List<UserModel>?> get() = _allUsers


    fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) = repo.login(email, password, callback)

    fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) = repo.register(email, password, callback)

    /**
     * Sign in or register with a Google account.
     * Passes the LandingScreen choices ([username], [currency]) so they can be
     * persisted for brand-new users.
     */
    fun signInWithGoogle(
        idToken: String,
        username: String,
        currency: String,
        callback: (Boolean, String) -> Unit
    ) = repo.signInWithGoogle(idToken, username, currency, callback)

    fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) = repo.forgetPassword(email, callback)

    fun deleteAccount(
        userId: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) = repo.deleteAccount(userId, password, callback)

    fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) = repo.addUserToDatabase(userId, model, callback)

    fun editProfile(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) = repo.editProfile(userId, model, callback)

    fun getUserById(userId: String) {
        repo.getUserById(userId) { success, _, data ->
            if (success) _user.postValue(data)
        }
    }

    fun getAllUser() {
        repo.getAllUser { success, _, data ->
            if (success) _allUsers.postValue(data)
        }
    }
}
