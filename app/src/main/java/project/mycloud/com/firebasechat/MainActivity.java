package project.mycloud.com.firebasechat;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import project.mycloud.com.firebasechat.adapter.ArrayAdapterWithIcon;
import project.mycloud.com.firebasechat.adapter.ChatFirebaseAdapter;
import project.mycloud.com.firebasechat.adapter.IClickListenerChatFirebase;
import project.mycloud.com.firebasechat.model.ChatModel;
import project.mycloud.com.firebasechat.model.FileModel;
import project.mycloud.com.firebasechat.model.MapModel;
import project.mycloud.com.firebasechat.model.UserModel;
import project.mycloud.com.firebasechat.util.Utils;
import project.mycloud.com.firebasechat.view.BaseActivity;
import project.mycloud.com.firebasechat.view.LoginActivity;

//
// date : 25 July 2016
//

public class MainActivity extends BaseActivity implements IClickListenerChatFirebase,
        View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CHAT_REFERENCE = "chatmodel";
    //
    private static final int REQUEST_CAMERA_IMAGE = 1 ;
    private static final int REQUEST_GALLERY_IMAGE = 2;
    private static final int REQUEST_PICKER_PLACE = 3;

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
    private ImageView ivAttachButton;

    //
    // https://android-arsenal.com/details/3/3812#description
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG,"Environment.DIRECTORY_PICTURES : "
                + Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ) );

        Log.d(TAG,"Environment.DIRECTORY_DCIM : "
                + Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DCIM )  );


        if ( !Utils.verifyConnection(this)) {

            Utils.initToast(this, "Please Check Internet Status!");
            finish();

        } else {
            //Utils.initToast(this, "bindViews()!");
            bindViews();
            verifyUserLogin();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this,this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API)
                        .build();
        }

    }

    // setOnClickListener
    @Override
    public void onClick(View view) {
        switch( view.getId() ) {
            case R.id.imageview_message_send:
                sendMessageFirebase();
                break;
            case R.id.imageview_attach:
                //showAttachPopup();
                showAttachImageDialog();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        // Utils.URL_STORAGE_REFERENCE : "gs://fir-chat-b753c.appspot.com";
        // Utils.FOLDER_STORAGE_IMG : "images"
        StorageReference storageRef =
                storage.getReferenceFromUrl(Utils.URL_STORAGE_REFERENCE)
                            .child(Utils.FOLDER_STORAGE_IMG);
        //
        // Request Camera
        //
        if ( requestCode == REQUEST_CAMERA_IMAGE) {
            if ( resultCode == RESULT_OK ) {

                if ( filePathImageCamera != null && filePathImageCamera.exists() ) {

                    showProgrssDialogMessage("Uploading...");

                    StorageReference imageReference =
                            storageRef.child(filePathImageCamera.getName()+"_camera");

                    uploadFileToFirebase(imageReference, filePathImageCamera);

                } else {
                    // IS NULL
                    Log.e(TAG, "filePathImageCamera is null:" + filePathImageCamera );
                }
            }
        }
        //
        // Request Gallery
        //
        else if (requestCode == REQUEST_GALLERY_IMAGE ) {

            if ( resultCode == RESULT_OK ) {

                Uri selectedImageUri = data.getData();
                if ( selectedImageUri != null ) {

                    showProgrssDialogMessage("Uploading...");

                    uploadUriToFirebase(storageRef, selectedImageUri );

                } else {
                    // URI IS NULL
                    Log.e(TAG, "selectedImageUri is null:" + selectedImageUri );
                }
            }
        }
        //
        // Request Place
        //
        else if ( requestCode == REQUEST_PICKER_PLACE ) {
            if ( resultCode == RESULT_OK ) {
                Place place = PlacePicker.getPlace(this, data);
                if ( place != null ) {
                    LatLng latLng = place.getLatLng();

                    // ChatModel - MapModel
                    MapModel mapModel = new MapModel( latLng.latitude+"", latLng.longitude+"");
                    ChatModel chatModel = new ChatModel(userModel,
                            Calendar.getInstance().getTime().getTime()+"", mapModel);

                    mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                } else {
                    // PLACE IS NULL
                    Log.e(TAG, "place is null:" + place );
                }
            }
        }
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
//            case R.id.menu_send_photo:
//                photoCameraIntent();
//                break;
//            case R.id.menu_send_photo_gallery:
//                photoGalleryIntent();
//                break;
//            case R.id.menu_send_location:
//                break;
            case R.id.menu_sign_out:
                signOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadUriToFirebase(StorageReference storageReference, final Uri file) {

        if (storageReference != null) {

            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            StorageReference imageGalleryRef = storageReference.child(name+"_gallery");
            UploadTask uploadTask = imageGalleryRef.putFile(file);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    // hide Progress Dialog
                    hideProgressDialog();

                    Log.e(TAG, "onFailure uploadFileToFirebase:" + e.getMessage() );

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // get download url
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    // ChatModel - FileModel
                    FileModel fileModel = new FileModel("img", downloadUrl.toString(), name, "");
                    ChatModel chatModel = new ChatModel(userModel,
                            "",
                            Calendar.getInstance().getTime().getTime()+"",
                            fileModel);

                    mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);

                    // hide Progress Dialog
                    hideProgressDialog();

                    Log.i(TAG, "onSuccess uploadFileToFirebase");

                }
            });

        } else {
            // IS NULL
        }

    }
    private void uploadFileToFirebase(StorageReference storageReference, final File file) {

        if ( storageReference != null  ) {

            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date() ).toString();

            StorageReference imageGalleryRef = storageReference.child( name+"_gallery");
            UploadTask uploadTask = imageGalleryRef.putFile(Uri.fromFile(file));

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {


                    // hide Progress Dialog
                    hideProgressDialog();

                    Log.e(TAG, "onFailure uploadFileToFirebase " + e.getMessage() );


                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // get download url
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    // ChatModel - FileModel
                    FileModel fileModel =
                            new FileModel("img", downloadUrl.toString(), name,"" );
                    ChatModel chatModel =
                            new ChatModel(userModel, "",
                                    Calendar.getInstance().getTime().getTime()+"",
                                    fileModel );
                    //
                    mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);

                    // hide Progress Dialog
                    hideProgressDialog();

                    Log.i(TAG, "onSuccess uploadFileToFirebase");

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

        // attach button & listener
        ivAttachButton = (ImageView)findViewById(R.id.imageview_attach);
        ivAttachButton.setOnClickListener(this);
        // send button & listener
        ivSendButton = (ImageView)findViewById(R.id.imageview_message_send);
        ivSendButton.setOnClickListener( this );

        // emoji button & action
        buttonEmoji = (ImageView)findViewById(R.id.imageview_emoji_button);
        emojiIcon =new EmojIconActions(this, linearlayoutMain, editTextEmo, buttonEmoji);
        emojiIcon.ShowEmojIcon();

        // recycler view
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



    private void showAttachImageDialog() {

        final String[] items = new String[] {
                getString(R.string.sendPhoto), getString(R.string.sendPhotoGallery),
                getString(R.string.sendLocation)
        };
        final Integer[] icons = new Integer[]{
                R.drawable.ic_camera_enhance_black_24dp,R.drawable.ic_insert_photo_black_24dp,
                R.drawable.ic_location_on_black_24dp
        };
        ListAdapter adapter = new ArrayAdapterWithIcon( this ,items, icons );

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Attach");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            // the user clicke on menus[which]
            if ( items[i].equals( getString(R.string.sendPhoto) ) ) {
                photoCameraIntent();
            } else if ( items[i].equals( getString(R.string.sendPhotoGallery) ) ) {
                photoGalleryIntent();
            } else if ( items[i].equals( getString(R.string.sendLocation) ) ) {
                locationPlacesIntent();
            }
            }
        });
        builder.show();
    }


//    private void showAttachPopup() {
//        final CharSequence[] menus = new CharSequence[] {
//                getString(R.string.sendPhoto),  getString(R.string.sendPhotoGallery),
//                getString(R.string.sendLocation)
//        };
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Attach");
//        builder.setItems(menus, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                // the user clicke on menus[which]
//                if ( menus[i].equals( getString(R.string.sendPhoto) ) ) {
//                    photoCameraIntent();
//                } else if ( menus[i].equals( getString(R.string.sendPhotoGallery) ) ) {
//                    photoGalleryIntent();
//                } else if ( menus[i].equals( getString(R.string.sendLocation) ) ) {
//                }
//            }
//        });
//        builder.show();
//    }

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
        Utils.initToast( this, "Google Play Services error.");
    }




    private void locationPlacesIntent() {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult( builder.build(this), REQUEST_PICKER_PLACE);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void photoGalleryIntent() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i,
                    getString(R.string.select_picture_title)),
                REQUEST_GALLERY_IMAGE);
    }

    private void photoCameraIntent() {

        String nameFoto = DateFormat.format("yyyyMMdd_hhmmss", new Date() ).toString();

        Log.d(TAG,"photoCameraIntent.nameFoto :" + nameFoto);
        Log.d(TAG,"getExternalStoragePublicDirectory : "
                + Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ).getAbsolutePath() );

        filePathImageCamera = new File( Environment
                .getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ),
                //nameFoto+"camera.jpg");
                "Camera/"+nameFoto+".jpg");

        Log.d(TAG,"filePathImageCamera " + filePathImageCamera);

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE );
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filePathImageCamera));
        startActivityForResult(i, REQUEST_CAMERA_IMAGE );
        
    }

    private void signOut() {
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }
}
