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
import project.mycloud.com.firebasechat.util.AppDefines;
import project.mycloud.com.firebasechat.util.CommonUtil;
import project.mycloud.com.firebasechat.util.EndPoints;
import project.mycloud.com.firebasechat.view.BaseActivity;
import project.mycloud.com.firebasechat.view.FullScreenImageActivity;
import project.mycloud.com.firebasechat.view.LoginActivity;

/**
 * MainActivity
 *
 * begin date : 25 July 2016
 *
 */
public class MainActivity extends BaseActivity implements IClickListenerChatFirebase,
        View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // view
    private View linearlayoutMain;
    private EmojiconEditText editTextEmo;
    private ImageView ivSendButton;
    private ImageView buttonEmoji;
    private EmojIconActions emojiIcon;
    private RecyclerView messageRecyclerView;
    private ImageView ivAttachButton;

    private LinearLayoutManager mLinearLayoutManager;

    //
    private UserModel userModel;
    private File filePathImageCamera = null;


    //private Uri cameraImageUri;

    //
    // https://android-arsenal.com/details/3/3812#description
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ( !CommonUtil.verifyConnection(this)) {
            CommonUtil.initToast(this, "Please Check Internet Status!");
            finish();
        } else {
            //CommonUtil.initToast(this, "findViews()!");
            findViews();
            checkAuthAndLoading();

        }
    }
    @Override
    public void onClick(View view) {
        switch( view.getId() ) {
            case R.id.imageview_message_send:
                sendInputText();      break;
            case R.id.imageview_attach:
                //showAttachPopup();
                showAttachImageDialog();    break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu );
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId() ) {
            case R.id.menu_sign_out:
                signOut();  break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getGalleryImageAndUpload(Uri selectedImageUri) {
        //
        final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();

        showProgrssDialogMessage("Uploading...");
        // Set Storage Key
        StorageReference firebaseStRefChild =
                getFbStorageRef().child(name+"_gallery");
        upGalleryImage( firebaseStRefChild, selectedImageUri , name );
    }


    private void getCameraImageAndUpload() {

        showProgrssDialogMessage("Uploading...");
        // Set Storage Key
        StorageReference firebaseStRefChild =
                getFbStorageRef().child( filePathImageCamera.getName()+"_camera" );

        upCameraImage( firebaseStRefChild, filePathImageCamera );
    }


    private void getMapAndUpload(LatLng latLng) {

        //LatLng latLng = place.getLatLng();
        MapModel mapModel = new MapModel(
                    latLng.latitude+"",
                    latLng.longitude+"");
        // +User -File -Message +Map
        ChatModel chatModel = new ChatModel(
                    userModel,
                    Calendar.getInstance().getTime().getTime()+"", // timestamp
                    mapModel);
        sendChatMessage(chatModel);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {

        // http://stackoverflow.com/questions/37557343/resize-an-image-before-uploading-it-to-firebase

        if ( requestCode == AppDefines.REQUEST_CAMERA_IMAGE) {
            if ( resultCode == RESULT_OK ) {

                Log.e(TAG, "onActivityResult.filePathImageCamera  :" + filePathImageCamera );
                // http://stackoverflow.com/questions/33030933/android-6-0-open-failed-eacces-permission-denied
                if ( filePathImageCamera != null ) {
                    if ( filePathImageCamera.exists() ) {
                        getCameraImageAndUpload();
                    }
                    else {
                        Log.e(TAG, "onActivityResult.filePathImageCamera  :" + "Not Exist" );
                    }
                }
//                else {
//                    // IS NULL
//                }
            }
        }
        else if (requestCode == AppDefines.REQUEST_GALLERY_IMAGE ) {
            if ( resultCode == RESULT_OK ) {
                Uri selectedImageUri = returnIntent.getData();
                if ( selectedImageUri != null ) {
                    getGalleryImageAndUpload(selectedImageUri);
                }
//                else {
//                    Log.e(TAG, "selectedImageUri is null:" + selectedImageUri );
//                }
            }
        }
        else if ( requestCode == AppDefines.REQUEST_PICKER_PLACE ) {
            if ( resultCode == RESULT_OK ) {
                Place place = PlacePicker.getPlace(this, returnIntent);
                if ( place != null ) {
                    getMapAndUpload( place.getLatLng() );
                }
//                else {
//                    Log.e(TAG, "place is null:" + place );
//                }
            }
        }
    }



    /**
     *
     * @param firebaseStRefChild : firebase storage
     * @param file :  saved
     */
    private void upCameraImage(StorageReference firebaseStRefChild, final File file) {
        if ( firebaseStRefChild != null  ) {
            // @@Tanmay Sahoo
            byte[] bytes = CommonUtil.getBytesFromImageUri( this , Uri.fromFile(file));
            UploadTask uploadTask = firebaseStRefChild.putBytes( bytes );
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgressDialog(); // hide Progress Dialog
                    Log.e(TAG, "onFailure upCameraImage " + e.getMessage() );
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Log.i(TAG, "onSuccess upCameraImage");
                    //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    FileModel fileModel =
                                new FileModel("img",
                                        taskSnapshot.getDownloadUrl().toString(),
                                        file.getName(), file.length() + "");

                    // + User +File -Message -Map
                    ChatModel chatModel = new ChatModel(userModel,
                            "",     // message
                            Calendar.getInstance().getTime().getTime() + "",  // timestamp
                            fileModel);
                    sendChatMessage(chatModel); // save
                    hideProgressDialog(); // hide Progress Dialog
                }
            });
        }
//        else {
//        }
    }

    /**
     *
     * @param firebaseStRefChild : firebase storage
     * @param uri : resource
     * @param name : name
     */
    private void upGalleryImage(StorageReference firebaseStRefChild, final Uri uri, final String name) {

        if (firebaseStRefChild != null) {

            // @@Tanmay Sahoo : get Byte from Image Uri
            byte[] bytes = CommonUtil.getBytesFromImageUri(this, uri);
            UploadTask uploadTask = firebaseStRefChild.putBytes( bytes );

            // uploadTask = firebaseStRefChild.putFile(uri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // hide Progress Dialog
                    hideProgressDialog();
                    Log.e(TAG, "onFailure upCameraImage:" + e.getMessage() );
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Log.i(TAG, "onSuccess upCameraImage");

                    FileModel fileModel =
                                new FileModel("img",
                                        taskSnapshot.getDownloadUrl().toString(),
                                        name, "");
                    // + User +File -Message -Map
                    ChatModel chatModel = new ChatModel(userModel,
                            "",         // message
                            Calendar.getInstance().getTime().getTime()+"", // timestamp
                            fileModel);
                    sendChatMessage(chatModel);
                    // hide Progress Dialog
                    hideProgressDialog();
                }
            });
        }
//        else {
//        }
    }

    /**
     *
     * @param chatModel : chat model
     */
    private void sendChatMessage(ChatModel chatModel) {
        pushChat(EndPoints.CHAT_REFERENCE, chatModel);
    }

    /**
     * check Auth and loading Room
     */
    private void checkAuthAndLoading() {

        if ( getFbUser() == null ) {

            Log.d(TAG,"checkAuthAndLoading : mFirebaseAuth is null, start Loginactivity");

            startActivity(new Intent(this, LoginActivity.class));
            finish();

        } else {
            showProgrssDialogMessage("Loading...");
            // get User Information
            userModel = getUserModel();
            // set Google ApiClient
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this,this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build();
            // get Chat Messages
            getMessages( EndPoints.CHAT_REFERENCE ) ;

            Log.d(TAG,"checkAuthAndLoading : getMessages()");
        }
    }

    private void getMessages(String childKey) {

        final ChatFirebaseAdapter firebaseAdapter =
                new ChatFirebaseAdapter(
                        getFbDatabaseRef().child(childKey),
                        userModel.getName(),
                        this ) ;

        firebaseAdapter.registerAdapterDataObserver(
                new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {

                super.onItemRangeInserted(positionStart, itemCount);
                int friendMessagecount
                        = firebaseAdapter.getItemCount();
                int lastVisiblePosition
                        = mLinearLayoutManager.findLastVisibleItemPosition();

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

    private void findViews() {
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

    @Override
    public void clickImageChat(View view, int position, String nameUser, String urlPhotoUser, String urlPhotoClick) {
        Intent i = new Intent(this, FullScreenImageActivity.class);
        i.putExtra( "nameUser", nameUser );
        i.putExtra ( "urlPhotoUser", urlPhotoUser );
        i.putExtra ( "urlPhotoClick", urlPhotoClick );
        startActivity(i);
    }

    @Override
    public void clickImageMapChat(View view, int position, String latitude, String longitude) {
        // https://developers.google.com/maps/documentation/android-api/intents
        // z option : 0 (the whole world) ~ 21 ( individual buildings )
        String uri = String.format("geo:%s,%s?z=19&q=%s,%s",
                        latitude, longitude, latitude,longitude );
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri) );
        startActivity(i);
    }

    private void showAttachImageDialog() {

        final String[] items = new String[] {
                getString(R.string.sendPhoto), getString(R.string.sendPhotoGallery),
                getString(R.string.sendLocation)
        };
        final Integer[] icons = new Integer[]{
                R.drawable.ic_camera_enhance_black_24dp,
                R.drawable.ic_insert_photo_black_24dp,
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
                cameraImageIntent();
            } else if ( items[i].equals( getString(R.string.sendPhotoGallery) ) ) {
                galleryImageIntent();
            } else if ( items[i].equals( getString(R.string.sendLocation) ) ) {
                pickerPlaceIntent();
            }
            }
        });
        builder.show();
    }


    private void sendInputText() {

        String inputMessage = editTextEmo.getText().toString();
        if ( inputMessage.isEmpty() ) return;

        editTextEmo.setText(null);
        // + User -File +Message -Map
        ChatModel chatModel =
                new ChatModel( userModel,
                            inputMessage,           // message
                            Calendar.getInstance().getTime().getTime()+"",  // timstamp
                            null );
        sendChatMessage(chatModel);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        CommonUtil.initToast( this, "Google Play Services error.");
    }

    private void pickerPlaceIntent() {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult( builder.build(this), AppDefines.REQUEST_PICKER_PLACE);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    // http://stackoverflow.com/questions/2789276/android-get-real-path-by-uri-getpath
    private void galleryImageIntent() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i,getString(R.string.select_picture_title)),
                AppDefines.REQUEST_GALLERY_IMAGE);
    }

    private void cameraImageIntent() {

        String nameFoto = DateFormat.format("yyyyMMdd_hhmmss", new Date() ).toString();
        Log.d(TAG,"cameraImageIntent.nameFoto :" + nameFoto);

        filePathImageCamera = new File( Environment
                .getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ),
                nameFoto+"_camera.jpg");
//
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE, nameFoto+".jpg");
//        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE );
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filePathImageCamera));
        //i.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(i, AppDefines.REQUEST_CAMERA_IMAGE );
    }

    private void signOut() {
        // firebase
        getFbAuth().signOut();
        // google
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);

        // new Intent
        startActivity(new Intent(this,LoginActivity.class));
        finish();
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
//                    cameraImageIntent();
//                } else if ( menus[i].equals( getString(R.string.sendPhotoGallery) ) ) {
//                    galleryImageIntent();
//                } else if ( menus[i].equals( getString(R.string.sendLocation) ) ) {
//                }
//            }
//        });
//        builder.show();
//    }

}
