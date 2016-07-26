package project.mycloud.com.firebasechat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import project.mycloud.com.firebasechat.adapter.ChatFirebaseAdapter;
import project.mycloud.com.firebasechat.adapter.ClickListenerChatFirebase;
import project.mycloud.com.firebasechat.model.UserModel;
import project.mycloud.com.firebasechat.util.Util;
import project.mycloud.com.firebasechat.view.LoginActivity;

public class MainActivity extends AppCompatActivity implements ClickListenerChatFirebase, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CHAT_REFERENCE = "chatmodel";

    // view
    private View contentRoot;
    private EmojiconEditText editTextMessage;
    private ImageView buttonMessage;
    private ImageView buttonEmoji;
    private EmojIconActions emojiIcon;
    private RecyclerView messageRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;
    //firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private UserModel userModel;
    private DatabaseReference mFirebaseDatabaseReference;
    private GoogleApiClient mGoogleApiClient;

    //
    // https://android-arsenal.com/details/3/3812#description
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ( !Util.verifyConnection(this)) {

            Util.initToast(this, "Please Check Internet Status!");
            finish();

        } else {
            Util.initToast(this, "bindViews()!");
            bindViews();
            verifyUserLogin();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this,this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API)
                        .build();
        }

    }

    private void verifyUserLogin() {

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if ( mFirebaseUser == null ) {

            Log.d(TAG,"verifyUserLogin : mFirebaseAuth is null, start Loginactivity");

            startActivity(new Intent(this, LoginActivity.class));
            finish();

        } else {

            Log.d(TAG,"verifyUserLogin : getMessages()");

            userModel = new UserModel(mFirebaseUser.getDisplayName(),
                    mFirebaseUser.getPhotoUrl().toString(),
                    mFirebaseUser.getUid() );
            getMessages() ;
        }

    }

    private void getMessages() {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        final ChatFirebaseAdapter firebaseAdapter = new ChatFirebaseAdapter(
                mFirebaseDatabaseReference.child(CHAT_REFERENCE),
                userModel.getName(), this ) ;

        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {

                super.onItemRangeInserted(positionStart, itemCount);

                int friendMessagecount = firebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastVisibleItemPosition();

                if ( lastVisiblePosition == -1 ||
                        (positionStart >= (friendMessagecount -1) &&
                            lastVisiblePosition ==(positionStart-1) ) ) {
                    messageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        this.messageRecyclerView.setLayoutManager(mLinearLayoutManager);
        this.messageRecyclerView.setAdapter(firebaseAdapter);

    }

    private void bindViews() {

        contentRoot = findViewById(R.id.contentRoot);
        editTextMessage = (EmojiconEditText)findViewById(R.id.editTextMessage);
        buttonMessage = (ImageView)findViewById(R.id.buttonMessage);
        // listener
        buttonMessage.setOnClickListener( this );

        buttonEmoji = (ImageView)findViewById(R.id.buttonEmoji);
        emojiIcon =new EmojIconActions(this, contentRoot, editTextMessage, buttonEmoji);
        emojiIcon.ShowEmojIcon();

        messageRecyclerView = (RecyclerView)findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
    }

    // ChatFirebaseAdapter
    @Override
    public void clickImageChat(View view, int position, String nameUser, String urlPhotoUser, String urlPhotoClick) {

    }

    // ChatFirebaseAdapter
    @Override
    public void clickImageMapChat(View view, int position, String latitude, String longitude) {

    }

    // setOnClickListener
    @Override
    public void onClick(View view) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
