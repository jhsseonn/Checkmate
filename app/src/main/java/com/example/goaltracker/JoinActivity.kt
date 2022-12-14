package com.example.goaltracker

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_join.*

class JoinActivity : AppCompatActivity() {

    lateinit var signupID : EditText
    //lateinit var emailID_error : TextView
    lateinit var signupPW : EditText
    lateinit var pwCheck : EditText
    lateinit var pwCheck_error : TextView
    var firebaseAuth: FirebaseAuth?=null
    var fireStore : FirebaseFirestore?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        signupID=findViewById(R.id.signupID)
        //emailID_error=findViewById(R.id.emailID_error)
        signupPW=findViewById<EditText?>(R.id.signupPW)
        pwCheck=findViewById(R.id.signupPWcheck)
        pwCheck_error=findViewById(R.id.pwCheck_error)

        firebaseAuth= FirebaseAuth.getInstance()
        fireStore= FirebaseFirestore.getInstance()

        backtologin.setOnClickListener {
            
            finish()
        }

        pwCheck.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                var password = signupPW.text.toString()
                var passwordCheck = signupPWcheck.text.toString()
                if (password != passwordCheck) {
                    pwCheck_error.setText("??????????????? ???????????? ????????????.") // ?????? ?????????
                    pwCheck.setBackgroundResource(R.drawable.red_edittext) // ?????? ????????? ??????
                } else {
                    pwCheck_error.setText("") //?????? ????????? ??????
                    pwCheck.setBackgroundResource(R.drawable.pwcheck_edittext) //????????? ???????????? ??????
                }
            } // afterTextChanged()..
        })

        joinCheckButton.setOnClickListener {
            signinAndSignup()
            startActivity(Intent(this, ProfileActivity::class.java))
        }



    }

    fun signinAndSignup() {
        if (signupID.text != null && signupPW.text != null) {
            var password = signupPW.text.toString()
            var passwordCheck = pwCheck.text.toString()
            if (password == passwordCheck) {
                firebaseAuth?.createUserWithEmailAndPassword(signupID.text.toString(), signupPW.text.toString())
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //account db ??????
                            var userAccount = Account()
//                            var friends = Friends()
                            var accountName : String ?= ""
//                            var friendsName : String = "asdfghjk"

                            accountName=firebaseAuth?.currentUser?.uid.toString()
                            userAccount.Email= firebaseAuth?.currentUser?.email.toString()
//                            userAccount.password=signupPW
//                            friends.Email="friendsCollectionTest"

                            fireStore?.collection("Account")?.document(accountName)?.set(userAccount)
                            //Friends, Notification ????????? ??????
//                            fireStore?.collection("Account")?.document(accountName)?.collection("Friends")
//                            fireStore?.collection("Account")?.document(accountName)?.collection("Notification")
                            Toast.makeText(this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show()
                        } else if(task.exception?.message.isNullOrEmpty()) {
                            // Show the error message
                            Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                            Toast.makeText(this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show()
                        } else {
                            // Alert if you have account
                            Toast.makeText(this, "?????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "??????????????? ???????????? ????????????", Toast.LENGTH_SHORT).show()
            }
        } else if (signupPW.text==null){
            Toast.makeText(this, "??????????????? ??????????????????", Toast.LENGTH_SHORT).show()
        } else if (signupID.text==null){
            Toast.makeText(this, "???????????? ??????????????????", Toast.LENGTH_SHORT).show()
        } else if (pwCheck.text==null){
            Toast.makeText(this, "??????????????? ??????????????????", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }

}