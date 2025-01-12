package com.example.had.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import com.example.had.databinding.ActivityChangeProfileBinding
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File
import android.widget.Toast
import androidx.activity.viewModels

import androidx.annotation.NonNull
import com.example.had.FireStorageViewModel
import com.example.had.PreferenceUtil
import com.example.had.PreferenceUtil.setImage
import com.example.had.R

import com.google.android.gms.tasks.OnFailureListener

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database

import com.google.firebase.storage.StorageReference

import com.google.firebase.storage.FirebaseStorage


class ChangeProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeProfileBinding
    private val TAG = this.javaClass.simpleName
    private val storage = Firebase.storage
    val user = Firebase.auth.currentUser
    val storageRef = storage.reference


    private val viewModel: FireStorageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = Firebase.database
        val reference = database.getReference("Users")

        binding.NewProfileImage.setImageBitmap(PreferenceUtil.StringtoBitmap(this))

        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl

            // Check if user's email is verified
            val emailVerified = user.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            val uid = user.uid
        }
        if (user != null) {
            reference.child(user.uid).child("name").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val name = dataSnapshot.value.toString()
                    binding.NewNickname.setText(name)
                }
                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                }
            })
        }

        binding.getImageB.setOnClickListener { // 이미지 불러오기

            //val ref = storageRef.child("profileImages/${user?.uid}.jpg")
            /*var file =
            var uploadTask = ref.putFile(file)

            val urlTask = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    getFirebaseImage()
                } else {
                    // Handle failures
                    // ...
                }
            }*/

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = MediaStore.Images.Media.CONTENT_TYPE
            intent.type = "image/*"
            intent.putExtra("crop", true)
            launcher.launch(intent)
        }

        binding.saveChangeProfileB.setOnClickListener{
            if (user != null) {
                reference.child(user.uid).child("name").setValue(binding.NewNickname.text.toString())
            }
            uploadProfileImage()
            Handler().postDelayed(Runnable {
                //딜레이 후 시작할 코드 작성
                finish()
            }, 1000)
        }
    }

    var launcher = registerForActivityResult(
        StartActivityForResult()
    ) {
        result ->
        if (result.resultCode == RESULT_OK) {
            Log.e(TAG, "result : $result")
            val intent = result.data
            Log.e(TAG, "intent : $intent")
            val uri = intent!!.data
            Log.e(TAG, "uri : $uri")

            binding.NewProfileImage.setImageURI(uri)
        }
    }

    private fun uploadProfileImage() {
        //firebase에 사진 업로드
        //val storageRef = storage.reference
        val profileImageRef = storageRef.child("profileImages/${user?.uid}.jpg")
        binding.NewProfileImage.isDrawingCacheEnabled = true
        binding.NewProfileImage.buildDrawingCache()
        val bitmap = (binding.NewProfileImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val profileUpdates = userProfileChangeRequest {
            displayName = "Jane Q. User"
            if (user != null) {
                photoUri = Uri.parse("profileImages/${user.uid}.jpg")
            }
        }
        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated.")
                    PreferenceUtil.BitmaptoString(this, bitmap)
                }
            }

        var uploadTask = profileImageRef.putBytes(data)
        uploadTask.addOnFailureListener {

        }.addOnSuccessListener { taskSnapshot -> }
    }



}
