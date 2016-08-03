package project.mycloud.com.firebasechat.view;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import project.mycloud.com.firebasechat.R;
import project.mycloud.com.firebasechat.adapter.CircleTransform;

public class FullScreenImageActivity extends AppCompatActivity {

    private static final String TAG = FullScreenImageActivity.class.getSimpleName();

    private ProgressDialog progressDialog;
    private TouchImageView touchImageView;
    private ImageView imageViewUser;
    private TextView textViewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        findViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setValues();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if ( id == android.R.id.home ) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.gc();
        finish();
    }

    private void findViews() {

        progressDialog = new ProgressDialog(this);

        touchImageView = (TouchImageView)findViewById(R.id.imageview_touch);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_fullscreen);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageViewUser = (ImageView)toolbar.findViewById(R.id.imageview_avatar_fullscreen);
        textViewUser = (TextView)toolbar.findViewById(R.id.text_title_fullscreen);
    }

    private void setValues() {

        // get value
        String nameUser = getIntent().getStringExtra("nameUser");
        String urlPhotoUser = getIntent().getStringExtra("urlPhotoUser");
        String urlPhotoClick = getIntent().getStringExtra("urlPhotoClick");

        Log.i(TAG, "image url :" + urlPhotoClick );

        // set value
        textViewUser.setText(nameUser);
        Glide.with(this).load(urlPhotoUser).asBitmap()
                    .centerCrop().transform(new CircleTransform(this))
                    .override(40,40).into(imageViewUser);

        Glide.with(this).load(urlPhotoClick).asBitmap()
                    .override(640,640).fitCenter()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource,
                                                    GlideAnimation<? super Bitmap> glideAnimation) {
                            progressDialog.dismiss();
                            touchImageView.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadStarted(Drawable placeholder) {
                            //super.onLoadStarted(placeholder);
                            progressDialog.setMessage("Loading...");
                            progressDialog.show();
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            //super.onLoadFailed(e, errorDrawable);
                            Toast.makeText(FullScreenImageActivity.this,"Error, Try Again !",
                                    Toast.LENGTH_LONG ).show();
                            progressDialog.dismiss();
                        }
                    });
    }

}
