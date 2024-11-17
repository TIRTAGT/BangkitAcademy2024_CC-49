package com.tirtagt.sgp_bangkit.api

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {
    private lateinit var loginButton: Button
    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res ->
        val response = res.idpResponse

        if (response == null) {
            this.onFirebaseAuthResult(null, "Null Response")
            return@registerForActivityResult
        }

        if (res.resultCode == RESULT_OK) {
            this.onFirebaseAuthResult(response, null)
            return@registerForActivityResult
        }

        val error = response.error
        if (error != null) {
            this.onFirebaseAuthResult(null, error.message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Jika sudah login, langsung masuk ke main activity saja
        if (checkAlreadyLoggedIn()) {
            val a = Intent(this, MainActivity::class.java)
            startActivity(a)
            finish()
        }

        loginButton = findViewById(R.id.button2)
        loginButton.setOnClickListener { onLoginButtonClicked() }
    }

    private fun onLoginButtonClicked() {
        if (!loginButton.isEnabled) { return; }
        loginButton.isEnabled = false

        // https://firebase.google.com/docs/auth/android/firebaseui

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun onFirebaseAuthResult(result: IdpResponse?, error: String?) {
        if (error != null) {
            Toast.makeText(this, "Error: ".plus(error), Toast.LENGTH_SHORT).show()
            return
        }

        if (result == null) {
            Toast.makeText(this, "Tidak ada respon dari firebase", Toast.LENGTH_SHORT).show()
            return
        }

        val userData = FirebaseAuth.getInstance().currentUser
        if (userData == null) {
            Toast.makeText(this, "Sukses login tetapi data user gagal di dapatkan", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Selamat datang ".plus(userData.displayName), Toast.LENGTH_SHORT).show()

        val a = Intent(this, MainActivity::class.java)
        startActivity(a)
        finish()
    }

    private fun checkAlreadyLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }
}