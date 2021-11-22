package com.example.mywhatsath.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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

        if(holder.javaClass == SentViewHolder::class.java){
            val viewHolder = holder as SentViewHolder
            holder.sentMsg.text = currentMsg.message
            holder.msgDate.text = timeago

        }else{ // ReceivedViewHolder
            val viewHolder = holder as ReceivedViewHolder
            holder.receivedMsg.text = currentMsg.message
            holder.msgDate.text = timeago
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
    }

    inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val receivedMsg = receivedBinding.receivedMsgTv
        val msgDate = receivedBinding.msgDate
    }



}