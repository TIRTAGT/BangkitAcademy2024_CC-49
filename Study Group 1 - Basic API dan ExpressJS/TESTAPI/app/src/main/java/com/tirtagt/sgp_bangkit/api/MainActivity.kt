package com.tirtagt.sgp_bangkit.api

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var displayNameView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshItemButton: Button
    private lateinit var deleteItemButton: Button
    private lateinit var itemNameInput: EditText
    private lateinit var addItemButton: Button
    private lateinit var apiEndpoint: SGPBangkitAPIEndpoint
    private var itemDatasetCache: ArrayList<SGPBangkitItem> = arrayListOf()
    private lateinit var recyclerListAdapter: BasicListAdapter
    private var API_TOKEN = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        val userData = FirebaseAuth.getInstance().currentUser
        if (userData == null) {
            Toast.makeText(this, "Belum login", Toast.LENGTH_SHORT).show()

            val a = Intent(this, WelcomeActivity::class.java)
            startActivity(a)
            finish()
            return
        }

        apiEndpoint = SGPBangkitAPI.createApiInstance()
        val firebaseAuthToken = userData.getIdToken(true)
        firebaseAuthToken.addOnCompleteListener {
            val error = firebaseAuthToken.exception
            if (error != null) {
                getAuthTokenResponse(null, error.message)
                return@addOnCompleteListener
            }

            getAuthTokenResponse(firebaseAuthToken.result, null)
        }
        firebaseAuthToken.addOnFailureListener { e ->
            getAuthTokenResponse(null, e.message)
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        displayNameView = findViewById(R.id.textView2)
        displayNameView.text = "Selamat datang, %s".format(userData.displayName)

        refreshItemButton = findViewById(R.id.button3)
        refreshItemButton.setOnClickListener {
            refreshListItem()
        }

        itemNameInput = findViewById(R.id.editText2)
        addItemButton = findViewById(R.id.button4)
        addItemButton.setOnClickListener { tambahItem() }

        deleteItemButton = findViewById(R.id.button5)
        deleteItemButton.setOnClickListener {
            val selectedItemId = recyclerListAdapter.selectedItemId

            if (selectedItemId == null) {
                Toast.makeText(this, "Harap klik item dahulu sebelum menghapus", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            hapusItem(selectedItemId)
        }
    }

    private fun getAuthTokenResponse(firebaseAuthToken: GetTokenResult?, errorMessage: String?) {
        if (!errorMessage.isNullOrEmpty()) {
            Toast.makeText(this, "Gagal mendapatkan token firebase %s".format(errorMessage), Toast.LENGTH_SHORT).show()
            return
        }

        if (firebaseAuthToken == null) {
            MainScope().launch { onAuthTokenResponse(null) }
            return
        }

        MainScope().launch { onAuthTokenResponse(firebaseAuthToken.token) }
    }

    private fun onAuthTokenResponse(firebaseAuthToken: String?) {
        if (firebaseAuthToken == null) {
            Toast.makeText(this, "Sesi telah berakhir, harap login lagi", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()

            val a = Intent(this, WelcomeActivity::class.java)
            startActivity(a)
            finish()
            return
        }
        API_TOKEN = "Bearer %s".format(firebaseAuthToken)
    }

    private fun refreshListItem() {
        lifecycleScope.launch {
            var errorMessage = ""
            try {
                itemDatasetCache = apiEndpoint.getAll(API_TOKEN).data
            }
            catch (e: Exception) {
                errorMessage = e.message!!
            }

            MainScope().launch {
                refreshItemCompleted(errorMessage)
            }
        }
    }

    private fun refreshItemCompleted(errorMessage: String) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(this, "Gagal mendapatkan list item %s".format(errorMessage), Toast.LENGTH_SHORT).show()
            return
        }

        recyclerListAdapter = BasicListAdapter(itemDatasetCache)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerListAdapter
    }

    private fun tambahItem() {
        val text = itemNameInput.text.toString()
        if (text.isEmpty()) {
            Toast.makeText(this, "Nama item tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val item = SGPBangkitItem(0, text)

        lifecycleScope.launch(Dispatchers.IO) {
            var errorMessage = ""
            try {
                apiEndpoint.addItem(API_TOKEN, item)
            }
            catch (e: Exception) {
                errorMessage = e.message!!
            }

            MainScope().launch {
                tambahItemCompleted(errorMessage)
            }
        }
    }

    private fun tambahItemCompleted(errorMessage: String) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(this, "Gagal menambahkan item %s".format(errorMessage), Toast.LENGTH_SHORT).show()
            return
        }
        refreshListItem()
    }

    private fun hapusItem(id: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            var errorMessage = ""
            try {
                apiEndpoint.deleteById(API_TOKEN, id)
            }
            catch (e: Exception) {
                errorMessage = e.message!!
            }

            MainScope().launch {
                hapusItemCompleted(errorMessage)
            }
        }
    }

    private fun hapusItemCompleted(errorMessage: String) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(this, "Gagal menghapus item %s".format(errorMessage), Toast.LENGTH_SHORT).show()
            return
        }
        refreshListItem()
    }
}