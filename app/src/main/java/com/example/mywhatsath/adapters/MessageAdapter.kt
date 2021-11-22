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

    val ITEM_RECEIVED = 1
    val ITEM_SENT = 2

    private lateinit var sentBinding: ItemSentMsgBinding
    private lateinit var receivedBinding: ItemReceivedMsgBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // inflate receivedMessage
        if(viewType == 1){
            receivedBinding = ItemReceivedMsgBinding.inflate(
                LayoutInflater.from(context), parent, false)
            return ReceivedViewHolder(receivedBinding.root)
        }else{ // inflate sentMessage
            sentBinding = ItemSentMsgBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
            return SentViewHolder(sentBinding.root)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMsg = messageList[position]
        val timestamp = messageList[position].timestamp
        val msgDate = MyApplication.formatTimeStamp(timestamp!!)
        val timeago = MyApplication.formatTimeAgo(msgDate)
        val msgImage = messageList[position].msgImage

        if(holder.javaClass == SentViewHolder::class.java){
            if(msgImage.isNullOrEmpty()){
                val viewHolder = holder as SentViewHolder
                holder.sentMsg.text = currentMsg.message
                holder.msgDate.text = timeago
            }else{
                val viewHolder = holder as SentViewHolder
                holder.sentMsg.visibility = View.GONE
                holder.sentIv.visibility = View.VISIBLE
                holder.msgDate.text = timeago
                //set image
                try{
                    Glide.with(context)
                        .load(msgImage)
                        .into(holder.sentIv)
                } catch(e: Exception){
                    Log.d("MessageAdapter_TAG", "onBindViewHolder: Failed to load a picture from sentViewHolder")
                }
            }
        }else{ // ReceivedViewHolder
            if(msgImage.isNullOrEmpty()){
                val viewHolder = holder as ReceivedViewHolder
                holder.receivedMsg.text = currentMsg.message
                holder.msgDate.text = timeago
            }else{
                val viewHolder = holder as ReceivedViewHolder
                holder.receivedMsg.visibility = View.GONE
                holder.receivedIv.visibility = View.VISIBLE
                holder.msgDate.text = timeago
                //set image
                try{
                    Glide.with(context)
                        .load(msgImage)
                        .into(holder.receivedIv)
                } catch(e: Exception){
                    Log.d("MessageAdapter_TAG", "onBindViewHolder: Failed to load a picture from receivedViewHolder")
                }
            }
        }
    }

    // to determine which viewbinding should be selected depending on its type
    override fun getItemViewType(position: Int): Int {
        val currentMsg = messageList[position]

        if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMsg.senderId)){
            return ITEM_SENT
        }else{
            return ITEM_RECEIVED
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