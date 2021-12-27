package com.example.mywhatsath.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mywhatsath.databinding.ItemReceivedMsgBinding
import com.example.mywhatsath.databinding.ItemSentMsgBinding
import com.example.mywhatsath.models.ModelMessage
import com.example.mywhatsath.utils.MyApplication
import com.google.firebase.auth.FirebaseAuth

// do not implement specific viewholder.
class MessageAdapter(
    val context: Context, val messageList: ArrayList<ModelMessage>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var sentBinding: ItemSentMsgBinding
    private lateinit var receivedBinding: ItemReceivedMsgBinding

    private lateinit var fbAuth: FirebaseAuth

    private val MESSAGE_SENT = 0
    private val MESSAGE_RECEIVED = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // inflate sentBinding
        if(viewType == MESSAGE_SENT){
            sentBinding = ItemSentMsgBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
            return SentViewHolder(sentBinding.root)
        }else{ // inflate receivedBinding
            receivedBinding = ItemReceivedMsgBinding.inflate(
                LayoutInflater.from(context), parent, false)
            return ReceivedViewHolder(receivedBinding.root)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMsg = messageList[position]
        val msg = currentMsg.message
        val timestamp = currentMsg.timestamp
        val msgDate = MyApplication.formatTimeStamp(timestamp!!)
        val timeago = MyApplication.formatTimeAgo(msgDate)
        val imageUrl = currentMsg.imageUrl

        if(holder.javaClass == SentViewHolder::class.java){
            val viewHolder = holder as SentViewHolder

            if(imageUrl == "" || imageUrl == null){
                viewHolder.sentMsg.text = msg
                viewHolder.msgDate.text = timeago
            } else{
                if(msg == "" || msg == null){
                    viewHolder.sentIv.visibility = View.VISIBLE
                    viewHolder.sentMsg.visibility = View.GONE
                    viewHolder.msgDate.text = timeago
                    //set image
                    try{
                        Glide.with(context)
                            .load(imageUrl)
                            .into(viewHolder.sentIv)
                    } catch(e: Exception){
                        Log.d("MessageAdapter_TAG", "onBindViewHolder: Failed to load a picture from sentViewHolder")
                    }
                }else{
                    viewHolder.sentIv.visibility = View.VISIBLE
                    viewHolder.sentMsg.text = msg
                    viewHolder.msgDate.text = timeago
                    //set image
                    try{
                        Glide.with(context)
                            .load(imageUrl)
                            .into(viewHolder.sentIv)
                    } catch(e: Exception){
                        Log.d("MessageAdapter_TAG", "onBindViewHolder: Failed to load a picture from sentViewHolder")
                    }
                }

            }

        }else{ // ReceivedViewHolder
            val viewHolder = holder as ReceivedViewHolder
            if(imageUrl == "" || imageUrl == null){
                viewHolder.receivedMsg.text = currentMsg.message
                viewHolder.msgDate.text = timeago
            }else{
                if(msg == "" || msg == null) {
                    viewHolder.receivedIv.visibility = View.VISIBLE
                    viewHolder.receivedMsg.visibility = View.GONE
                    viewHolder.msgDate.text = timeago
                    //set image
                    try{
                        Glide.with(context)
                            .load(imageUrl)
                            .into(viewHolder.receivedIv)
                    } catch(e: Exception){
                        Log.d("MessageAdapter_TAG", "onBindViewHolder: Failed to load a picture from receivedViewHolder")
                    }
                }else{
                    viewHolder.receivedIv.visibility = View.VISIBLE
                    viewHolder.receivedMsg.text = msg
                    viewHolder.msgDate.text = timeago
                    //set image
                    try{
                        Glide.with(context)
                            .load(imageUrl)
                            .into(viewHolder.receivedIv)
                    } catch(e: Exception){
                        Log.d("MessageAdapter_TAG", "onBindViewHolder: Failed to load a picture from receivedViewHolder")
                    }
                }

            }
        }
    }

    // to determine which viewbinding should be selected depending on its type
    override fun getItemViewType(position: Int): Int {
        fbAuth = FirebaseAuth.getInstance()
        val currentUserId = fbAuth.currentUser!!.uid
        val currentMsg = messageList[position]

        if(currentMsg.senderId == currentUserId){
            return MESSAGE_SENT
        }else{
            return MESSAGE_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val sentMsg = sentBinding.sentMsgTv
        val msgDate = sentBinding.msgDate
        val sentIv = sentBinding.sentIv
    }

   inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val receivedMsg = receivedBinding.receivedMsgTv
        val msgDate = receivedBinding.msgDate
        val receivedIv = receivedBinding.receivedIv
    }



}