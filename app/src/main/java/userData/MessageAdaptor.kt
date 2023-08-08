package userData

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.sam.tippy.Message
import com.sam.tippy.R

class MessageAdaptor(val context: Context, val messageList: ArrayList<Message>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECEIVE = 1
    val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        return if (viewType == 1){
            // receive is inflated
            val view: View = LayoutInflater.from(context).inflate(R.layout.received_message, parent, false)
            ReceiveViewHolder(view)
        }else{
            // sent is inflated
            val view: View = LayoutInflater.from(context).inflate(R.layout.sent_message, parent, false)
            SentViewHolder(view)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val currentMessage = messageList[position]
        if (holder.javaClass == SentViewHolder::class.java){
            // sent view holder
            val viewHolder = holder as SentViewHolder
            holder.sentMessage.text = currentMessage.message
        }else{
            // receive view holder
            val viewHolder = holder as ReceiveViewHolder
            holder.receivedMessage.text = currentMessage.message
        }
    }


    // Gets ViewType of item
    override fun getItemViewType(position: Int): Int
    {
        val currentMessage = messageList[position]

        return if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.idOfSender)){
            ITEM_SENT
        }else{
            ITEM_RECEIVE
        }
    }


    override fun getItemCount(): Int {
        return messageList.size
    }
    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val sentMessage: TextView = itemView.findViewById(R.id.sent_message)
    }
    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val receivedMessage: TextView = itemView.findViewById(R.id.received_message)
    }
}