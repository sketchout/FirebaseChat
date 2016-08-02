package project.mycloud.com.firebasechat.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import project.mycloud.com.firebasechat.R;
import project.mycloud.com.firebasechat.model.ChatModel;
import project.mycloud.com.firebasechat.util.Utils;

/**
 * Created by admin on 2016-07-25.
 */
public class ChatFirebaseAdapter
        extends FirebaseRecyclerAdapter<ChatModel, ChatFirebaseAdapter.MyChatViewHolder> {

    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;

    private static final String TAG = ChatFirebaseAdapter.class.getSimpleName();

    private IClickListenerChatFirebase mIClickListenerChatFirebase;
    private String nameUser;

    public ChatFirebaseAdapter(DatabaseReference ref, String nameUser,
                               IClickListenerChatFirebase mClickListerChatFirebase) {
        super(ChatModel.class,
                R.layout.item_message_left,
                MyChatViewHolder.class,
                ref);

        this.nameUser = nameUser;

        this.mIClickListenerChatFirebase = mClickListerChatFirebase;

    }

    @Override
    public MyChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //return super.onCreateViewHolder(parent, viewType);

        View view;
        if ( viewType == RIGHT_MSG ) {

            view = LayoutInflater.from( parent.getContext())
                    .inflate(R.layout.item_message_right,parent, false);
            return new MyChatViewHolder( view );

        } else if ( viewType == LEFT_MSG ) {
            view = LayoutInflater.from( parent.getContext())
                    .inflate(R.layout.item_message_left,parent, false);
            return new MyChatViewHolder( view );

        } else if ( viewType == RIGHT_MSG_IMG ) {

            view = LayoutInflater.from( parent.getContext())
                    .inflate(R.layout.item_message_right_img,parent, false);
            return new MyChatViewHolder( view );

        } else {  // if ( viewType == LEFT_MSG_IMG ) {

            view = LayoutInflater.from( parent.getContext())
                    .inflate(R.layout.item_message_left_img,parent, false);
            return new MyChatViewHolder( view );
        }
    }

    @Override
    public int getItemViewType(int position) {
        // return super.getItemViewType(position);

        ChatModel model = getItem(position);

        if ( model.getMapModel() != null ) {
            if (model.getUserModel().getName().equals(nameUser)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else if ( model.getFile() != null ) {
            if ( model.getFile().getType().equals("img") &&
                    model.getUserModel().getName().equals(nameUser) ) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else if ( model.getUserModel().getName().equals(nameUser)) {
            return RIGHT_MSG;
        } else {
            return LEFT_MSG;
        }
    }

    @Override
    protected void populateViewHolder(MyChatViewHolder viewHolder,
                                      ChatModel model, int position) {

        // photo -> imageview user
        Log.d(TAG,"populateViewHolder: getPhoto_profile() : " + model.getUserModel().getPhoto_profile() );
        viewHolder.setIvUser( model.getUserModel().getPhoto_profile() );

        Log.d(TAG,"populateViewHolder: model.getMessage() : " + model.getMessage() );
        viewHolder.setTxtMessage( model.getMessage() );

        Log.d(TAG,"populateViewHolder: model.getTimeStamp() : " + model.getTimeStamp() );
        viewHolder.setTvTimestamp( model.getTimeStamp() );

        viewHolder.tvIsLocation( View.GONE );

        if ( model.getFile() != null ) {

            Log.d(TAG,"populateViewHolder: model.setIvChatPhoto() file : " + model.getFile().getUrl_file() );

            viewHolder.tvIsLocation(View.GONE );
            viewHolder.setIvChatPhoto( model.getFile().getUrl_file() );

        } else if ( model.getMapModel() != null ) {

            Log.d(TAG,"populateViewHolder: model.setIvChatPhoto() : Lat(" + model.getMapModel().getLatitude()
                        +") Long("+  model.getMapModel().getLongitude() +")" );

            viewHolder.setIvChatPhoto(
                    Utils.local(model.getMapModel().getLatitude(),model.getMapModel().getLongitude() )  );
            viewHolder.tvIsLocation( View.VISIBLE );
        }
    }

    public class MyChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvTimestamp;
        private TextView tvLocation;

        private EmojiconTextView txtMessage;

        private ImageView ivChatPhoto;
        private ImageView ivUser;

        public MyChatViewHolder(View itemView) {

            super(itemView);
            tvTimestamp =(TextView)itemView.findViewById(R.id.textview_timestamp);
            txtMessage = (EmojiconTextView)itemView.findViewById(R.id.textview_emo);
            tvLocation = (TextView)itemView.findViewById(R.id.textview_location);
            ivChatPhoto = (ImageView)itemView.findViewById(R.id.imageview_chat_photo);

            // model.getUserModel().getPhoto_profile()
            ivUser =(ImageView)itemView.findViewById(R.id.imageview_user);

            //Glide.with(ivUser.getContext()).load("http://goo.gl/gEgYUd").into(ivUser);
            //Glide.with(ivChatPhoto.getContext()).load("http://goo.gl/gEgYUd").into(ivChatPhoto);
        }

        //    public TextView getTvTimestamp() {
//        return tvTimestamp;
//    }

        public void setTvTimestamp(String timestamp) {
            if ( tvTimestamp == null ) return;
            //this.tvTimestamp = tvTimestamp;
            tvTimestamp.setText(convertTimestamp(timestamp));
        }

        private CharSequence convertTimestamp(String timestamp) {
            //return 0;
            return DateUtils.getRelativeTimeSpanString (
                    Long.parseLong(timestamp),
                    System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS   ) ;
        }

//    public EmojiconTextView getTxtMessage() {
//        return txtMessage;
//    }

        public void setTxtMessage(String message) {

            if ( message == null  || message.isEmpty() ) return;
            txtMessage.setText( message );
        }

//    public TextView getTvLocation() {
//        return tvLocation;
//    }

        public void setTvLocation(TextView tvLocation) {
            this.tvLocation = tvLocation;
        }

        public void tvIsLocation(int visible) {
            if(tvLocation == null ) return;
            tvLocation.setVisibility( visible );
        }

//    public ImageView getIvChatPhoto() {
//        return ivChatPhoto;
//    }

        public void setIvChatPhoto(String url) {

            Log.d(TAG,"setIvChatPhoto " + url );
            if ( url == null ) return;

            //Glide.with(ivUser.getContext()).load("http://goo.gl/gEgYUd").into(ivUser);
            Glide.with( ivChatPhoto.getContext())
                    .load(url)
                    .asBitmap()
                    .fitCenter()
                    //.placeholder(android.R.drawable.progress_horizontal)
                    .override(100,100)
                    .into(ivChatPhoto);

            ivChatPhoto.setOnClickListener(this);
        }

//    public ImageView getIvUser() {
//        return ivUser;
//    }

        public void setIvUser(String urlPhotoUser) {

            Log.d(TAG,"setIvUser " + urlPhotoUser );
            if( urlPhotoUser == null ) return;

            //Glide.with(this.ivChatPhoto.getContext()).load("http://goo.gl/gEgYUd").into(ivChatPhoto);
            Glide.with( ivUser.getContext() )
                    .load( urlPhotoUser )
                    .asBitmap() // Attempts to always load the resource as a {@link android.graphics.Bitmap}
                    .centerCrop()
                    .transform( new CircleTransform( ivUser.getContext() ) )
                    //.placeholder(android.R.drawable.progress_horizontal)
                    .override(40,40)
                    .into(ivUser) ;

            // .load()
            // A helper method equivalent to calling {@link #asDrawable()} and then
            // {@link RequestBuilder#load(Object)} with the given model.
            //
            // .asBitmap()
            // Attempts to always load the resource as a {@link android.graphics.Bitmap},
            // even if it could actually be animated.
        }

        @Override
        public void onClick(View view) {

            int position = getAdapterPosition();
            ChatModel model = getItem( position );

            if ( model.getMapModel() != null ) {
                mIClickListenerChatFirebase.clickImageMapChat(
                        view, position,
                        model.getMapModel().getLatitude(),
                        model.getMapModel().getLongitude() );
            } else {
                mIClickListenerChatFirebase.clickImageChat(
                        view, position,
                        model.getUserModel().getName(),
                        model.getUserModel().getPhoto_profile(),
                        model.getFile().getUrl_file() );
            }
        }
    }
}
