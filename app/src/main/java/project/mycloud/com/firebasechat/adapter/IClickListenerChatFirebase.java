package project.mycloud.com.firebasechat.adapter;

import android.view.View;

/**
 * Created by admin on 2016-07-25.
 */
public interface IClickListenerChatFirebase {

    /**
     * click on image
     * @param view
     * @param position
     */
    void clickImageChat(View view, int position, String nameUser, String urlPhotoUser,
                        String urlPhotoClick );

    /**
     * click over map
     */
    void clickImageMapChat(View view, int position, String latitude, String longitude);
}
