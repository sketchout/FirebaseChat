package project.mycloud.com.firebasechat.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import project.mycloud.com.firebasechat.R;

/**
 * Created by admin on 2016-07-25.
 */
// https://github.com/AleBarreto/FirebaseAndroidChat/

public class ChatViewHolder extends RecyclerView.ViewHolder {


    private TextView tvTimestamp;
    private EmojiconTextView txtMessage;
    private TextView tvLocation;
    private ImageView ivChatPhoto;
    private ImageView ivUser;

    public ChatViewHolder(View itemView) {
        super(itemView);

        tvTimestamp =(TextView)itemView.findViewById(R.id.timestamp);
        txtMessage = (EmojiconTextView)itemView.findViewById(R.id.editTextMessage);
        tvLocation = (TextView)itemView.findViewById(R.id.tvLocation);
        ivChatPhoto = (ImageView)itemView.findViewById(R.id.img_chat);
        ivUser =(ImageView)itemView.findViewById(R.id.ivUserChat);
    }




}
