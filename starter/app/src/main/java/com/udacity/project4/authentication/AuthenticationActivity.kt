package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */

class AuthenticationActivity : AppCompatActivity() {
   private lateinit var signInLauncher:ActivityResultLauncher<Intent>

   private lateinit var authenticationViewModel: AuthenticationViewModel
   private var resultCode:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding:ActivityAuthenticationBinding =
            DataBindingUtil.setContentView(this,R.layout.activity_authentication)
        binding.lifecycleOwner = this
        binding.authenticationActivity = this
        val authenticationViewModel1:AuthenticationViewModel by viewModel()
        authenticationViewModel = authenticationViewModel1
        //TODO optional if user is auth'ed go directly to RemindersActivity
         signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { res ->
            this.onSignInResult(res)
        }
    }


    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
         resultCode = result.resultCode
        when(resultCode){
           Activity.RESULT_OK ->{
                //if the user is authenticated ,send him to reminders activity
                val intent = Intent(this,RemindersActivity::class.java)
                startActivity(intent)
                Toast.makeText(this,"Successfully logged in",Toast.LENGTH_SHORT).show()
            }
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

      val  signInIntent =
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setAuthMethodPickerLayout(customLayout)
                .setTheme(R.style.CustomLoginTheme)
                .build()
        signInLauncher.launch(signInIntent)

    }
}
