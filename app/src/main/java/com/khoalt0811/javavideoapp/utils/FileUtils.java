package com.khoalt0811.javavideoapp.utils; // Thay package name nếu khác

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    private static final String TAG = "FileUtils";

    /**
     * Copies content from a Uri to a temporary file in the app's cache directory.
     * Handles potential errors during stream operations and file creation.
     */
    public static File getFileFromUri(Context context, Uri uri) {
        if (uri == null) {
            Log.e(TAG, "Input URI is null.");
            return null;
        }

        File tempFile = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        ContentResolver contentResolver = context.getContentResolver();
        String fileName = getFileName(contentResolver, uri);

        try {
            inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Could not open input stream for URI: " + uri);
                return null;
            }

            File cacheDir = context.getCacheDir();
            if (!cacheDir.exists()) {
                if (!cacheDir.mkdirs()) {
                    Log.e(TAG, "Failed to create cache directory.");
                    return null;
                }
            }

            if (fileName == null || fileName.isEmpty()) {
                // Generate a fallback filename if needed
                String extension = getFileExtension(context, uri);
                fileName = "temp_video_" + System.currentTimeMillis() + (extension != null ? "." + extension : "");
            } else {
                // Sanitize file name to prevent path traversal or invalid characters
                fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            }

            tempFile = new File(cacheDir, fileName);

            // Ensure the file does not exist or delete if it does (overwrite)
            if (tempFile.exists()) {
                if (!tempFile.delete()) {
                    Log.w(TAG, "Could not delete existing temp file: " + tempFile.getAbsolutePath());
                    // Optionally, try a different filename or handle the error
                }
            }

            outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4 * 1024]; // 4k buffer
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            Log.d(TAG, "File copied successfully to temporary file: " + tempFile.getAbsolutePath());
            return tempFile;

        } catch (Exception e) {
            Log.e(TAG, "Error copying file from URI: " + uri, e);
            // Clean up the partially created file if it exists
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            return null;
        } finally {
            // Ensure streams are closed properly
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing input stream", e);
            }
            try {
                if (outputStream != null) outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing output stream", e);
            }
        }
    }

    /**
     * Gets the display name of the file from a content URI.
     */
    private static String getFileName(ContentResolver contentResolver, Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    } else {
                        Log.w(TAG, "DISPLAY_NAME column not found for URI: " + uri);
                    }
                } else {
                    Log.w(TAG, "Cursor is null or empty for URI: " + uri);
                }
            } catch (Exception e) {
                // Catch potential SecurityException or other errors during query
                Log.e(TAG, "Failed to query display name from content URI: " + uri, e);
            }
        }
        // Fallback for non-content URIs or if content query fails
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    /**
     * Gets the MIME type of the content associated with a URI.
     */
    public static String getMimeType(Context context, Uri uri) {
        if (uri == null) return null;
        ContentResolver contentResolver = context.getContentResolver();
        try {
            return contentResolver.getType(uri);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get MIME type for URI: " + uri, e);
            return null; // Return null on error
        }
    }

    /**
     * Gets the file extension based on the MIME type or filename.
     */
    public static String getFileExtension(Context context, Uri uri) {
        if (uri == null) return null;
        String extension = null;

        // Try getting extension from MIME type first
        String mimeType = getMimeType(context, uri);
        if (mimeType != null) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        }

        // If MIME type didn't provide an extension, try parsing the filename
        if (extension == null) {
            String fileName = getFileName(context.getContentResolver(), uri);
            if (fileName != null) {
                int dotIndex = fileName.lastIndexOf('.');
                // Ensure dot is present and not the first or last character
                if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                    extension = fileName.substring(dotIndex + 1).toLowerCase();
                }
            }
        }
        return extension;
    }
}