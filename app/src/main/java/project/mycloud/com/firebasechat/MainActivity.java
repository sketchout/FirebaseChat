package project.mycloud.com.firebasechat;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import project.mycloud.com.firebasechat.adapter.ChatFirebaseAdapter;
import project.mycloud.com.firebasechat.adapter.ClickListenerChatFirebase;
import project.mycloud.com.firebasechat.model.ChatModel;
import project.mycloud.com.firebasechat.model.FileModel;
import project.mycloud.com.firebasechat.model.UserModel;
import project.mycloud.com.firebasechat.util.Util;
import project.mycloud.com.firebasechat.view.LoginActivity;

public class MainActivity extends BaseActivity implements ClickListenerChatFirebase, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CHAT_REFERENCE = "chatmodel";
    //
    private static final int IMAGE_CAMERA_REQUEST = 2 ;
    private static final int IMAGE_GALLERY_REQUEST = 1;

    // view
    private View linearlayoutMain;
    private EmojiconEditText editTextEmo;
    private ImageView ivSendButton;
    private ImageView buttonEmoji;
    private EmojIconActions emojiIcon;
    private RecyclerView messageRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;
    //firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private UserModel userModel;
    //
    private DatabaseReference mFirebaseDatabaseReference;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    //
    private GoogleApiClient mGoogleApiClient;
    private File filePathImageCamera;

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
            //Util.initToast(this, "bindViews()!");
            bindViews();
            verifyUserLogin();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this,this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API)
                        .build();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        // Util.URL_STORAGE_REFERENCE : "gs://fir-chat-b753c.appspot.com";
        // Util.FOLDER_STORAGE_IMG : "images"
        
        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE)
                            .child(Util.FOLDER_STORAGE_IMG);

        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if ( resultCode == RESULT_OK ) {

                Uri selectedImageUri = data.getData();

                if ( selectedImageUri != null ) {

                    showProgrssDialogMessage("Uploading...");

                    sendFileFirebase(storageRef, selectedImageUri );
                } else {
                    // URI IS NULL
                }
            }
        }
        else if ( requestCode == IMAGE_CAMERA_REQUEST ) {
            if ( resultCode == RESULT_OK ) {

                if ( filePathImageCamera != null && filePathImageCamera.exists() ) {

                    showProgrssDialogMessage("Uploading...");

                    StorageReference imageCameraRef =
                            storageRef.child(filePathImageCamera.getName()+"_camera");
                    sendFileFirebase(imageCameraRef, filePathImageCamera);
                } else {
                    // IS NULL
                }
            }
        }

    }

    private void sendFileFirebase(StorageReference storageReference, final Uri file) {

        if (storageReference != null) {

            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();

            StorageReference imageGalleryRef = storageReference.child(name+"_gallery");

            UploadTask uploadTask = imageGalleryRef.putFile(file);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure sendFileFirebase:" + e.getMessage() );
                    hideProgressDialog();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Log.i(TAG, "onSuccess sendFileFirebase");
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    FileModel fileModel = new FileModel("img", downloadUrl.toString(), name, "");
                    ChatModel chatModel = new ChatModel(userModel,
                            "",
                            Calendar.getInstance().getTime().getTime()+"",
                            fileModel);
                    mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                    hideProgressDialog();

                }
            });

        } else {
            // IS NULL
        }

    }
    private void sendFileFirebase(StorageReference storageReference, final File file) {

        if ( storageReference != null  ) {

            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date() ).toString();

            StorageReference imageGalleryRef = storageReference.child( name+"_gallery");
            UploadTask uploadTask = imageGalleryRef.putFile(Uri.fromFile(file));

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage() );
                    hideProgressDialog();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "onSuccess sendFileFirebase");
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    FileModel fileModel =
                            new FileModel("img", downloadUrl.toString(), name,"" );

                    ChatModel chatModel =
                            new ChatModel(userModel, "", Calendar.getInstance().getTime().getTime()+"",
                                    fileModel );
                    mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                    hideProgressDialog();
                }
            });
        } else {
            // IS NULL
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

            showProgrssDialogMessage("Loading...");

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
                hideProgressDialog();
            }
        });

        this.messageRecyclerView.setLayoutManager(mLinearLayoutManager);
        this.messageRecyclerView.setAdapter(firebaseAdapter);

    }

    private void bindViews() {

        linearlayoutMain = findViewById(R.id.linearlayout_main);
        editTextEmo = (EmojiconEditText)findViewById(R.id.edittext_emo);

        ivSendButton = (ImageView)findViewById(R.id.imageview_message_send);
        // listener
        ivSendButton.setOnClickListener( this );

        buttonEmoji = (ImageView)findViewById(R.id.imageview_emoji_button);
        emojiIcon =new EmojIconActions(this, linearlayoutMain, editTextEmo, buttonEmoji);
        emojiIcon.ShowEmojIcon();

        messageRecyclerView = (RecyclerView)findViewById(R.id.recyclerview_main);
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
        switch( view.getId() ) {
            case R.id.imageview_message_send:
                sendMessageFirebase();
                break;
        }

    }

    private void sendMessageFirebase() {

        String inputMessage = editTextEmo.getText().toString();

        if ( inputMessage.isEmpty() ) return;

        ChatModel model = new ChatModel( userModel,inputMessage,
                Calendar.getInstance().getTime().getTime()+"",
                null );
        mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);
        editTextEmo.setText(null);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Util.initToast( this, "Google Play Services error.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_chat, menu );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch( item.getItemId() ) {
            case R.id.menu_send_photo:
                photoCameraIntent();
                break;
            case R.id.menu_send_photo_gallery:
                photoGalleryIntent();
                break;
            case R.id.menu_send_location:
                break;
            case R.id.menu_sign_out:
                signOut();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void photoGalleryIntent() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i,
                    getString(R.string.select_picture_title)),
                IMAGE_GALLERY_REQUEST);
    }

    private void photoCameraIntent() {
        String nameFoto = DateFormat.format("yyyy-MM-dd_hhmmss", new Date() ).toString();
        filePathImageCamera = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                nameFoto+"camera.jpg");
        
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE );
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filePathImageCamera));
        startActivityForResult(i, IMAGE_CAMERA_REQUEST);
        
    }

    private void signOut() {
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }
}
