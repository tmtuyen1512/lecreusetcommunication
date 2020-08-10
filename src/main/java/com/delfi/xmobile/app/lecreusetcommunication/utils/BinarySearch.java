
package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.util.Log;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * compile 'com.ibm.icu:icu4j:4.8'
 */
public final class BinarySearch {
    private static final String TAG = BinarySearch.class.getSimpleName();
    public static final String UTF8_BOM = "\ufeff";
    public static final int NEW_LINE_LENTH = 2;

    public BinarySearch() {
    }

    public static String findOne(String filePath, String firstField, int firstFieldLength) {
        File f = new File(filePath);
        String result = "";
        if (f.exists() && !f.isDirectory()) {
            try {
                RandomAccessFile raf = new RandomAccessFile(f, "r");
                if (firstField.length() > firstFieldLength) {
                    throw new IllegalArgumentException(firstField + " must be " + firstFieldLength + " character");
                }

                firstField = padRight(firstField, firstFieldLength - firstField.length());
                String mEncoding = getEncodingFile(filePath);
                raf.seek(0L);
                String line = raf.readLine();
                line = new String(line.getBytes("ISO-8859-1"), mEncoding);
                int offset = line.getBytes(mEncoding).length + 2;
                if (line.startsWith("\ufeff")) {
                    line = line.replace("\ufeff", "");
                }

                if (line.substring(0, firstFieldLength).compareTo(firstField) > 0) {
                    return "";
                }

                if (line.substring(0, firstFieldLength).compareTo(firstField) == 0) {
                    return line;
                }

                if (!mEncoding.contains("utf-16") && !mEncoding.contains("UTF-16")) {
                    result = searchUTF8_ANSI(firstField, firstFieldLength, raf, mEncoding);
                } else {
                    result = searchUnicode(firstField, firstFieldLength, raf, mEncoding, offset);
                }

                raf.close();
            } catch (Exception var9) {
                var9.printStackTrace();
            }
        }

        return result;
    }

    private static String searchUTF8_ANSI(String firstField, int firstFieldLength, RandomAccessFile raf, String mEncoding) throws IOException {
        String result = "";
        long begin = 0L;
        long end = raf.length();

        while(begin <= end) {
            long middle = (begin + end) / 2L;
            raf.seek(middle);
            raf.readLine();
            String line = raf.readLine();
            Log.v(TAG, "before: " + line);
            line = new String(line.getBytes("ISO-8859-1"), mEncoding);
            Log.i(TAG, "after: " + line);
            int comparison = line.substring(0, firstFieldLength).compareTo(firstField);
            if (comparison == 0) {
                result = line;
                break;
            }

            if (comparison < 0) {
                begin = middle + 1L;
            } else {
                end = middle - 1L;
            }
        }

        return result;
    }

    private static String searchUnicode(String firstField, int firstFieldLength, RandomAccessFile raf, String mEncoding, int lineSize) throws IOException {
        String result = "";
        int numberOfLines = (int)(raf.length() / (long)lineSize);
        byte[] lineBuffer = new byte[lineSize];
        long begin = 0L;
        long end = (long)numberOfLines;

        while(begin <= end) {
            long middle = (begin + end) / 2L;
            raf.seek(middle * (long)lineSize + 2L);
            raf.read(lineBuffer);
            String line = new String(lineBuffer, "ISO-8859-1");
            Log.v(TAG, "before: " + line);
            line = new String(line.getBytes("ISO-8859-1"), mEncoding);
            Log.w(TAG, "after: " + line);
            int comparison = line.substring(0, firstFieldLength).compareTo(firstField);
            if (comparison == 0) {
                result = line;
                break;
            }

            if (comparison < 0) {
                begin = middle + 1L;
            } else {
                end = middle - 1L;
            }
        }

        return result;
    }

    public static String getEncodingFile(String filePath) throws IOException {
        String mEncoding = "windows-1252";
        File file = new File(filePath);
        if(file.exists()) {
            byte[] fileData = new byte[(int) file.length()];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(fileData);
            dis.close();
            CharsetMatch match = (new CharsetDetector()).setText(fileData).detect();
            if (match != null) {
                mEncoding = match.getName();
                Log.d("BinarySearch", "For file: " + file.getName() + " guessed enc: " + match.getName() + " conf: " + match.getConfidence());
            }
        }

        return mEncoding;
    }

    private static String padRight(String s, int n) {
        for(int i = 0; i < n; ++i) {
            s = s + " ";
        }

        return s;
    }
}
