package com.udacity.project4.authentication

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import kotlin.math.sign

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

 private  var idpResponse: IdpResponse? = null
  private lateinit var signInIntent : Intent

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
      idpResponse =  IdpResponse.fromResultIntent(signInIntent)
        if(result.resultCode == Activity.RESULT_OK){
                //if the user is authenticated ,send him to reminders activity
                val intent = Intent(this,RemindersActivity::class.java)
                startActivity(intent)
                Toast.makeText(this,"login success",Toast.LENGTH_SHORT).show()
        }


    }

    override fun onBackPressed() {

        if (idpResponse == null){
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setMessage(R.string.alert_dialog_message)
                setPositiveButton(R.string.alert_dialog_ok)
                    { _, _ ->
                        // User clicked OK button
                        super.onBackPressed()
                    }
                setNegativeButton(R.string.alert_dialog_cancel)
                   { dialog, _ ->
                        // User cancelled the dialog
                       dialog.dismiss()
                    }
            }.create()
        }
        else{super.onBackPressed()}


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
        signInIntent =
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setAuthMethodPickerLayout(customLayout)
                .setTheme(R.style.CustomLoginTheme)
                .build()
        signInLauncher.launch(signInIntent)


    }
}
