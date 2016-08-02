package project.mycloud.com.firebasechat.view;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

import project.mycloud.com.firebasechat.R;

/**
 * Created by admin on 2016-07-27.
 */
public class BaseActivity extends AppCompatActivity {


    private static final String TAG= BaseActivity.class.getSimpleName();


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
