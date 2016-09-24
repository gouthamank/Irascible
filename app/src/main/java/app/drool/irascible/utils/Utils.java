package app.drool.irascible.utils;


import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {
    public static int getPixelsFromDP(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static String joinStrings(List<String> list) {
        return joinStrings(list, " ");
    }

    public static String joinStrings(String[] list) {
        return joinStrings(list, " ");
    }

    public static String joinStrings(List<String> list, String demarcator) {
        return joinStrings(list.toArray(new String[list.size()]), demarcator);
    }

    public static String joinStrings(String[] list, String demarcator) {
        if (list.length == 0)
            return "";

        if (list.length == 1)
            return list[0];

        StringBuilder builder = new StringBuilder();
        for(String part : list) {
            if (builder.length() > 0) builder.append(demarcator);
            builder.append(part);
        }

        return builder.toString();
    }

    public static String getReadableDate(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
        Date date = new Date(Long.valueOf(dateStr));
        return format.format(date);
    }

    public String getSHA1Hash(String plaintext) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = plaintext.getBytes("UTF-8");
            messageDigest.update(bytes, 0, bytes.length);
            bytes = messageDigest.digest();
            StringBuilder builder = new StringBuilder();

            for(byte b : bytes) {
                int topHalf = (b >>> 4) & 0x0F;
                int botHalf = b & 0x0F;

                builder.append(topHalf >= 0 && topHalf <= 9 ? '0' + topHalf : 'a' + (topHalf - 10));
                builder.append(botHalf >= 0 && botHalf <= 9 ? '0' + botHalf : 'a' + (botHalf - 10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return null;
        }
    }
}
