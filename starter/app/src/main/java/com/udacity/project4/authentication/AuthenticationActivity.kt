package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 *
 *
 */

private const val TAG = "AuthenticationActivity"

class AuthenticationActivity : AppCompatActivity() {
   private lateinit var signInLauncher:ActivityResultLauncher<Intent>

   private lateinit var authenticationViewModel: AuthenticationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding:ActivityAuthenticationBinding =
            DataBindingUtil.setContentView(this,R.layout.activity_authentication)
        binding.lifecycleOwner = this
        binding.authenticationActivity = this
        val authenticationView:AuthenticationViewModel by viewModel()
        authenticationViewModel = authenticationView
         signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { res ->
            this.onSignInResult(res)
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if(result.resultCode == Activity.RESULT_OK){
            //if the user is authenticated ,send him to reminders activity
            val intent = Intent(this,RemindersActivity::class.java)
            startActivity(intent)
            Toast.makeText(this,"login success",Toast.LENGTH_SHORT).show()
        }
        else{
            authenticationViewModel.authenticatedState.observe(this, Observer { authState ->
                when(authState){
                    AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> Log.i(TAG,"user login success")
                    else -> findNavController(R.id.nav_host_fragment).popBackStack()
                }
            })
            Toast.makeText(this,"login failed",Toast.LENGTH_SHORT).show()
        }
    }


    fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account. If users
        // choose to register with their email, they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val customLayout = AuthMethodPickerLayout
            .Builder(R.layout.authentication_layout)
            .setGoogleButtonId(R.id.google_login_button)
            .setEmailButtonId(R.id.email_login_button)
            .build()

        // Create and launch sign-in intent. We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code.
        val signInIntent =
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setAuthMethodPickerLayout(customLayout)
                .setTheme(R.style.CustomLoginTheme)
                .build()
        signInLauncher.launch(signInIntent)


    }
}
//TODO fix issue where app crashes after back button press