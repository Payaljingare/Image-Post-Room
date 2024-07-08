package com.example.imagepostroom;





import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import java.io.IOException;

public class FileUtils {

    public static MultipartBody.Part prepareFilePart(String partName, Uri fileUri, Context context) {
        File file = getFileFromUri(context, fileUri);
        if (file != null) {
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse(context.getContentResolver().getType(fileUri)),
                    file
            );
            return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
        } else {
            return null;
        }
    }

    public static RequestBody createPartFromString(String description) {
        return RequestBody.create(
                MultipartBody.FORM, description
        );
    }

    private static File getFileFromUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String fileName = getFileName(contentResolver, uri);
        File file = new File(context.getCacheDir(), fileName);

        try (InputStream inputStream = contentResolver.openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private static String getFileName(ContentResolver contentResolver, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}


