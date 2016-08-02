package project.mycloud.com.firebasechat.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

/**
 * Created by admin on 2016-07-25.
 */
public class Utils {

    public static final String URL_STORAGE_REFERENCE =
            "gs://fir-chat-b753c.appspot.com";
    public static final String FOLDER_STORAGE_IMG =
            "images";


    public static void initToast(Context c, String message) {
        Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean verifyConnection(Context c) {
        boolean connected;
        ConnectivityManager conMgr =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        connected = conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected();
        return connected;
    }

    public static String local(String latitudeFinal, String longitudeFinal) {
        return "https://maps.googleapis.com/maps/api/staticmap?center="+
                latitudeFinal+","+longitudeFinal+
                "&zoom=1&size=280x280&markers=color:red|"+
                latitudeFinal+","+longitudeFinal;
    }
}

