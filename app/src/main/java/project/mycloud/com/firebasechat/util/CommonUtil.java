package project.mycloud.com.firebasechat.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

/**
 * Created by admin on 2016-07-25.
 */
public class CommonUtil {

    private static final String TAG = CommonUtil.class.getSimpleName();

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


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        Log.i(TAG, "BitmapSize : height x width = " + height +"," + width );

        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int hH = height / 2;
            final int hW = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ( ( hH / inSampleSize) < reqHeight || ( hW / inSampleSize) < reqWidth ){
                inSampleSize *= 2;
            }
        }

        Log.i(TAG, "BitmapSize : inSampleSize = " + inSampleSize );

        return inSampleSize;
    }

    /**
     * http://stackoverflow.com/questions/2789276/android-get-real-path-by-uri-getpath
     * by @@Tanmay Sahoo
     *
     * @param uri
     * @return
     */
    public static byte[] getBytesFromImageUri(Context context, Uri uri) {
        // TODO : get size of image
        // http://stackoverflow.com/questions/2789276/android-get-real-path-by-uri-getpath
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap=null;
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(
                    context.getContentResolver().openInputStream(uri),
                    null, options);

            options.inSampleSize = calculateInSampleSize(options,
                    AppDefines.MAX_IMAGE_WIDTH, AppDefines.MAX_IMAGE_WIDTH );
            options.inJustDecodeBounds = false;

            bitmap = BitmapFactory.decodeStream(
                    context.getContentResolver().openInputStream(uri),
                    null, options );

            Log.i(TAG, ">>>> image.getByteCount() : " + bitmap.getByteCount() );
            Log.i(TAG, ">>>> image.getByteCount() K : " + bitmap.getByteCount() / 1024 );

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, baos );
        return baos.toByteArray();
    }
}

