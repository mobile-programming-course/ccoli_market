package com.example.ccoli_market

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ccoli_market.DBKey.Companion.DB_ARTICLES
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class AddItemFragment() : Fragment() {
    private val GALLERY_CODE=10;
    private var selectedUri: Uri? = null
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }
    private val articleDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_ARTICLES)
    }
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                when(result.resultCode){
                    GALLERY_CODE-> {
                        data?.data?.let { uri ->
                            view?.findViewById<ImageView>(R.id.imageView)?.setImageURI(uri)
                            selectedUri = uri
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "사진을 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_item, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.uploadbtn).setOnClickListener {
            startContentProvider()
//            when {
//                ContextCompat.checkSelfPermission(
//                    requireContext(),
//                    android.Manifest.permission.READ_EXTERNAL_STORAGE
//                ) == PackageManager.PERMISSION_GRANTED -> {
//                    startContentProvider()
//                }
//                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
//                    showPermissionContextPopup()
//                }
//                else -> {
//                    requestPermissions(
//                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
//                        1010
//                    )
//                }
        }
        view.findViewById<Button>(R.id.addItembtn).setOnClickListener {
            val title =
                view.findViewById<EditText>(R.id.et_title).text.toString()
            val price =
                view.findViewById<EditText>(R.id.et_price).text.toString()
            val sellerId = auth.currentUser?.uid.orEmpty()

            if (selectedUri != null) {
                val photoUri = selectedUri ?: return@setOnClickListener
                uploadPhoto(
                    photoUri,
                    successHandler = { uri ->
                        uploadArticle(sellerId, title, price, uri)
                    },
                    errorHandler = {
                        Toast.makeText(
                            requireContext(),
                            "사진 업로드에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
//                        hideProgress()
                    }
                )
            } else {
                uploadArticle(sellerId, title, price, "")
            }
        }
    }
    private fun uploadPhoto(uri: Uri, successHandler: (String) -> Unit, errorHandler: () -> Unit) {
        val fileName = "${System.currentTimeMillis()}.png"
        storage.reference.child("Add_item").child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    storage.reference.child("Add_item").child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { downloadUrl ->
                            successHandler(downloadUrl.toString())
                        }
                        .addOnFailureListener {
                            errorHandler()
                        }
                } else {
                    errorHandler()
                }
            }
    }
    private fun uploadArticle(sellerId: String, title: String, price: String, imageUrl: String) {
        val model =
            ArticleModel(sellerId, title, System.currentTimeMillis(), "$price 원", imageUrl)
        articleDB.push().setValue(model)
    }

    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getContent.launch(intent)
    }

//    private fun showPermissionContextPopup() {
//        AlertDialog.Builder(requireContext())
//            .setTitle("권한이 필요합니다.")
//            .setMessage("사진을 가져오기 위해 필요합니다")
//            .setPositiveButton("동의") { _, _ ->
//                requestPermissions(
//                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
//                    1010
//                )
//            }
//            .create()
//            .show()
//    }
}
