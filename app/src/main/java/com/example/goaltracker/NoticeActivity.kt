package com.example.goaltracker

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.main_toolbar.*
import kotlinx.android.synthetic.main.notice_layer.view.*
import kotlinx.android.synthetic.main.notice_view.*

class NoticeActivity : AppCompatActivity() {

    lateinit var noticeAdapter: NoticesAdapter
//    val notices = mutableListOf<Notification>()
    var firebaseAuth : FirebaseAuth ?= null
    var firestore : FirebaseFirestore ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notice_view)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        notice_list.adapter = NoticesAdapter()

        backtomain.setOnClickListener {
            finish()
        }

    }

    inner class NoticesAdapter: RecyclerView.Adapter<NoticesAdapter.ViewHolder>(){
        private var noticeDto = arrayListOf<Notifications>()
        init {
            var accountUId: String? = ""
            accountUId = firebaseAuth?.currentUser?.uid.toString()
            firestore?.collection("Account")?.document(accountUId)?.collection("Notification")?.
            orderBy("timeStamp", Query.Direction.DESCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                noticeDto.clear()

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(Notifications::class.java)
//                    Log.d("item", item.toString())
                    noticeDto.add(item!!)
                }
                notifyDataSetChanged()
                Log.d("??????", noticeDto.size.toString())
            }
        }

        inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticesAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.notice_layer, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//            var viewHolder = (holder as ViewHolder).itemView
            val item = noticeDto[position]

            setNoticeList(holder, item)
//            viewHolder.notice_text.text = item.message
//            viewHolder.notice_profile_name.text = item.userName
        }

        override fun getItemCount(): Int = noticeDto.size

    }

    private fun setNoticeList(holder: NoticesAdapter.ViewHolder, item: Notifications) {
        var viewHolder = (holder as NoticesAdapter.ViewHolder).itemView

        if (item.type == 0) { // ????????? ??????
            viewHolder.notice_text.text = item.message
            viewHolder.notice_profile_name.text = item.userName?.substring(0, 1)
            viewHolder.notice_button.setVisibility(View.GONE)
            viewHolder.notice_cancel_button.setVisibility(View.GONE)
            var profileColor : GradientDrawable = viewHolder.notice_profile.background as GradientDrawable
            val color = item.userColor
            if (color != null) {
                profileColor.setColor(Color.parseColor(color))
            }

        } else if (item.type == 1) {  // ?????? ??????
            viewHolder.notice_text.text = item.userName+"?????? ?????? ????????? ???????????????."
            viewHolder.notice_profile_name.text = item.userName?.substring(0 , 1)
            viewHolder.notice_button.text = "?????? ??????"
            var profileColor : GradientDrawable = viewHolder.notice_profile.background as GradientDrawable
            val color = item.userColor
            if (color != null) {
                profileColor.setColor(Color.parseColor(color))
            }

        } else if (item.type == 2) {  // ??? ??????
            viewHolder.notice_text.text = item.userName+"\n????????? ?????? ?????????????????????."
            viewHolder.notice_profile_name.text = item.userName?.substring(0 , 1)
            viewHolder.notice_button.text = "Goal ??????"
            var profileColor : GradientDrawable = viewHolder.notice_profile.background as GradientDrawable
            val color : String? = item.userColor
            if (color != null) {
                profileColor.setColor(Color.parseColor(color))
            }

        } else if (item.type == 3) {  // ??? ?????????
            viewHolder.notice_text.text = "["+item.goalName+"]\n"+item.userName+"??? ??? ???????????? ??????????????????.\n??????, ????????? ????????? ???????????? ????????????????"
            viewHolder.notice_profile_name.text = item.userName?.substring(0 , 1)
            viewHolder.notice_button.setVisibility(View.GONE)
            viewHolder.notice_cancel_button.setVisibility(View.GONE)
            var profileColor : GradientDrawable = viewHolder.notice_profile.background as GradientDrawable
            val color :String = item.userColor.toString()
            if (color != null) {
                profileColor.setColor(Color.parseColor(color))
            }

        } else {
            throw Exception("?????? ????????? ????????? ???????????? ????????????.")
        }
    }
}