package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class AuthenticationViewModel : ViewModel(){
    private val firebaseUserLiveData = FirebaseUserLiveData()

    enum class AuthenticationState{
        AUTHENTICATED,UNAUTHENTICATED
    }

    val authenticatedState = firebaseUserLiveData.map {
        when{
            it !=null -> AuthenticationState.AUTHENTICATED
            else -> AuthenticationState.UNAUTHENTICATED
        }
    }
}