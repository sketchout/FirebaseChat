package project.mycloud.com.firebasechat.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * Created by admin on 2016-08-04.
 *
 * from :
 *  http://stackoverflow.com/questions/2789276/android-get-real-path-by-uri-getpath
 *
 *
 * https://github.com/nohana/Laevatein
 * ( this library is to take photo from camera or choose from galery ,
 *  if you choose from gallery he have a drawer with albums and just show local files)
 *
 */
public class RealPathUtil {

    /*
    Usage() {
        String realPath;

        if ( Build.VERSION.SDK_INT < 11 ) {
            realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, selectedImageUri);
        } else if ( Build.VERSION.SDK_INT < 19 ) {
            realPath = RealPathUtil.getRealPathFromURI_API11to18(this, selectedImageUri);
        } else { // Android 4.4
            realPath = RealPathUtil.getRealPathFromURI_API19(this, selectedImageUri);
        }
        Log.i(TAG,"->selectedImageUri : " + selectedImageUri );
        Log.i(TAG,"->realPath : " + realPath );
    }
*/

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}