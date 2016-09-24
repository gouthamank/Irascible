package app.drool.irascible.utils;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import app.drool.irascible.Constants;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class CacheUtils {
    private static final String TAG = "CacheUtils";

    public static void clearSessionLog(Context context) {
        context.deleteFile(Constants.STORAGE.FILES.sessionLog);
    }

    public static void clearFragmentLog(Context context, String fragment) {
        context.deleteFile(Constants.STORAGE.FILES.fragmentLog + fragment);
    }

    public static void addToSessionLog(Context context, String message) {
        try {
            FileOutputStream outputStream = context.openFileOutput(Constants.STORAGE.FILES.sessionLog, Context.MODE_APPEND);
            BufferedSink output = Okio.buffer(Okio.sink(outputStream));

            output.write((message + "\n").getBytes());
            output.flush();
            output.close();

        } catch (IOException e) {
            Log.e(TAG, "addToSessionLog: Could not add to session log", e);
        }
    }

    public static void addToFragmentLog(Context context, String fragment, String message) {
        try {
            FileOutputStream outputStream = context.openFileOutput(Constants.STORAGE.FILES.fragmentLog + fragment, Context.MODE_APPEND);
            BufferedSink output = Okio.buffer(Okio.sink(outputStream));
            output.write((message + "\n").getBytes());
            output.flush();
            output.close();
        } catch (IOException e) {
            Log.e(TAG, "addToFragmentLog: Could not add to fragment log " + fragment, e);
        }
    }

    public static String getSessionLog(Context context) {
        try {
            FileInputStream inputStream = context.openFileInput(Constants.STORAGE.FILES.sessionLog);
            BufferedSource source = Okio.buffer(Okio.source(inputStream));
            String fileContent = source.readString(Charset.defaultCharset());
            if (fileContent.length() > 0)
                return fileContent;
        } catch (IOException e) {
            Log.e(TAG, "getSessionLog: Could not retrieve session log", e);
            return null;
        }

        return null;
    }

    public static String getFragmentLog(Context context, String fragment) {
        try {
            FileInputStream inputStream = context.openFileInput(Constants.STORAGE.FILES.fragmentLog + fragment);
            BufferedSource source = Okio.buffer(Okio.source(inputStream));
            String fileContent = source.readString(Charset.defaultCharset());
            if (fileContent.length() > 0)
                return fileContent;
        } catch (IOException e) {
            Log.e(TAG, "getFormatLog: Could not retrieve fragment log " + fragment, e);
        }
        return null;
    }
}
