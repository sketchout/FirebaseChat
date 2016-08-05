package project.mycloud.com.firebasechat.view;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import project.mycloud.com.firebasechat.R;
import project.mycloud.com.firebasechat.model.ChatModel;
import project.mycloud.com.firebasechat.model.UserModel;
import project.mycloud.com.firebasechat.util.EndPoints;

/**
 * Created by admin on 2016-07-27.
 */
public class BaseActivity extends AppCompatActivity {


    private static final String TAG = BaseActivity.class.getSimpleName();

    //firebase
    private FirebaseAuth mFbAuth = null;
    private FirebaseUser mFbUser = null;
    private DatabaseReference mFbDbRef;
    private FirebaseStorage mFbStorage = null;
    // google api
    protected GoogleApiClient mGoogleApiClient;

    // method
    protected FirebaseAuth getFbAuth() {
        if ( mFbAuth == null ) {
            mFbAuth = FirebaseAuth.getInstance();
        }
        return mFbAuth;
    }
    protected FirebaseUser getFbUser() {
        if ( mFbUser == null ) {
            mFbUser = getFbAuth().getCurrentUser();
        }
        return mFbUser;
    }

    protected UserModel getUserModel() {
        return new UserModel(
                getFbUser().getDisplayName(),           // name
                getFbUser().getPhotoUrl().toString(),   // photo url
                getFbUser().getUid() );                 // uid
    }

    protected void pushChat(String chatKey, ChatModel chatModel) {

        getFbDatabaseRef().child(chatKey).push().setValue(chatModel);
    }

    protected DatabaseReference getFbDatabaseRef() {
        if ( mFbDbRef == null ) {
            mFbDbRef = FirebaseDatabase.getInstance().getReference();
        }
        return mFbDbRef;
    }
    protected StorageReference getFbStorageRef() {
        if ( mFbStorage == null ) {
            mFbStorage = FirebaseStorage.getInstance();
        }
        return mFbStorage.getReferenceFromUrl(EndPoints.URL_STORAGE_REFERENCE)
                .child(EndPoints.FOLDER_STORAGE_IMG);
    }

    // dialog progress
    private ProgressDialog mProgressDialog;

    protected void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
            //mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Loading...");
        }
        mProgressDialog.show();
    }

    protected void showProgrssDialogMessage(String message) {

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
