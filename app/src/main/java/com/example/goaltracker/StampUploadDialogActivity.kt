package com.example.goaltracker

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class StampUploadDialogActivity : AppCompatActivity() {
    private lateinit var close_dialog_button : ImageButton
    private lateinit var certImage_imageView : ImageView
    private lateinit var comment_editText : EditText
    private lateinit var commentUpload_button : Button

    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var currentPhotoPath : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_stamp_upload_dialog)

        close_dialog_button = findViewById(com.example.goaltracker.R.id.close_dialog_button)
        certImage_imageView = findViewById(com.example.goaltracker.R.id.certImage_imageView)
        comment_editText = findViewById(com.example.goaltracker.R.id.comment_editText)
        commentUpload_button = findViewById(com.example.goaltracker.R.id.commentUpload_button)

        certImage_imageView.setOnClickListener {
            Toast.makeText(it.context, "You Click Certification Image Button", Toast.LENGTH_SHORT).show()

            if(checkPersmission()){
                dispatchTakePictureIntent()
            } else{
                requestPermission()
            }


//            val intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.type = "image/*"
//            startActivityForResult(intent, GALLERY)

//            StampUploadDialogInterface.onUploadImageClicked()
        }

        commentUpload_button.setOnClickListener {
            if (comment_editText.text.toString().trim().isEmpty()){
                Toast.makeText(it.context, "comment is empty", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(it.context, "You Click Comment Upload Button", Toast.LENGTH_SHORT).show()

                finish()
            }


//            StampUploadDialogInterface.onUploadButtonClicked()

//            dlg.dismiss()
        }

        close_dialog_button.setOnClickListener {
            Toast.makeText(it.context, "You Click Close Button", Toast.LENGTH_SHORT).show()

            finish()

//            dlg.dismiss()
        }
    }

    private fun dispatchTakePictureIntent(){
        // 갤러리 혹은 카메라 열기
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                // 찍은 사진을 그림파일로 만들기
                val photoFile: File? =
                    try {
                        // Create an image file name
                        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        File.createTempFile(
                            "JPEG_${timeStamp}_", /* prefix */
                            ".jpg", /* suffix */
                            storageDir /* directory */
                        ).apply {
                            // Save a file: path for use with ACTION_VIEW intents
                            currentPhotoPath = absolutePath
                        }
                    } catch (ex: IOException) {
                        Log.d("TAG", "그림파일 만드는도중 에러 발생")
                        null
                    }

                if (Build.VERSION.SDK_INT < 24) {
                    if(photoFile != null){
                        val photoURI = Uri.fromFile(photoFile)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    }
                }
                else{
                    // 그림파일을 성공적으로 만들었다면 startActivityForResult로 보내기
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this, "com.example.goaltracker.fileprovider", it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    }
                }

                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                intent.putExtra("crop", true)
                intent.action = Intent.ACTION_GET_CONTENT

                val chooserIntent = Intent.createChooser(intent, "선택해주세요")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePictureIntent))
                startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode){
            1 -> {
                if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){

                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    }

                    // 카메라로부터 받은 데이터가 있을경우에만
                    val file = File(currentPhotoPath)
                    val selectedUri = Uri.fromFile(file)
                    try{
                        selectedUri?.let {
                            if (Build.VERSION.SDK_INT < 28) {
                                val bitmap = MediaStore.Images.Media
                                    .getBitmap(contentResolver, selectedUri)
                                launchImageCrop(selectedUri)
                            }
                            else{
                                val decode = ImageDecoder.createSource(this.contentResolver, selectedUri)
                                val bitmap = ImageDecoder.decodeBitmap(decode)
                                launchImageCrop(selectedUri)
                            }
                        }

                    }catch (e: java.lang.Exception){
                        e.printStackTrace()
                    }
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if(resultCode == Activity.RESULT_OK){
                    result.uri?.let {

                        certImage_imageView.setImageBitmap(result.bitmap)
                        certImage_imageView.setImageURI(result.uri)

                    }
                }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    val error = result.error
                    Toast.makeText(this@StampUploadDialogActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 카메라 권한 요청
    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE, CAMERA),
            REQUEST_IMAGE_CAPTURE)
    }

    // 카메라 권한 체크
    private fun checkPersmission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    // 권한요청 결과
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Permission: " + permissions[0] + "was " + grantResults[0] + "Camera authorized")
        }else{
            Log.d("TAG","Camera not authorized")
        }
    }

    private fun launchImageCrop(uri: Uri?){
        CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)
    }
}