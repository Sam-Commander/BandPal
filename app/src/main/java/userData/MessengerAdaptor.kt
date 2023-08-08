package userData

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.sam.tippy.Chat
import com.sam.tippy.ViewMatchProfile
import com.sam.tippy.R
import com.squareup.picasso.Picasso

class MessengerAdaptor(val context: Context, val userList: ArrayList<User>):
    RecyclerView.Adapter<MessengerAdaptor.MessengerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessengerViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.messages_layout, parent, false)
        return MessengerViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessengerViewHolder, position: Int) {
        val currentUser = userList[position]

        holder.displayName.text = currentUser.displayName
        Picasso.get().load(currentUser.imageUrl?.toUri()).into(holder.displayPap)

        holder.displayPap.setOnClickListener {
            val intent = Intent(context, Chat::class.java) // holder.displayPap back to holder.itemView

            // being sent to chat page
            intent.putExtra("name", currentUser.displayName)
            intent.putExtra("uid", currentUser.uid)

            context.startActivity(intent)
        }

        holder.myButton.setOnClickListener {
            val intent = Intent(context, ViewMatchProfile::class.java)
            intent.putExtra("uid", currentUser.uid)
            intent.putExtra("radius", "0")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class MessengerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val displayName: TextView = itemView.findViewById(R.id.messengerName)
        val displayPap: ImageView = itemView.findViewById(R.id.messagesImage)
        val myButton: Button = itemView.findViewById(R.id.viewProfileMessage)
    }
}