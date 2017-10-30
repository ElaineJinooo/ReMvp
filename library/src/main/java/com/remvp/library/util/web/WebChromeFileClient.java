package com.remvp.library.util.web;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.remvp.library.R;
import com.remvp.library.util.PermissionUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * WebChromeClient帮助类，主要实现了不同Android版本上传图片功能和打印log
 * 主要一定要调{@link #onActivityResult(int, int, Intent)}
 */
public class WebChromeFileClient extends WebChromeClient {
    private static final String TAG = "WebChromeFileClient";
    private Activity context;
    private ValueCallback<Uri> filePathCallback = null;
    private ValueCallback<Uri[]> mFilePathCallbackArray = null;
    private String mCameraFilePath = null;
    private final static int FILECHOOSER_RESULTCODE = 1;// 表单的结果回调</span>
    private PermissionUtil permissionUtil;

    public WebChromeFileClient(Activity context) {
        this.context = context;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        Log.i(TAG, "WebView: onConsoleMessage: msg=" +
                consoleMessage.message() + " line=" +
                consoleMessage.lineNumber() + " sourceId=" +
                consoleMessage.sourceId());
        return super.onConsoleMessage(consoleMessage);
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        Log.i(TAG, "WebView: openFileChooser: 3.0+");
        if (filePathCallback != null) return;
        filePathCallback = uploadMsg;
        checkPermission();
    }

    // For Android < 3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        Log.i(TAG, "WebView: openFileChooser: < 3.0");
        openFileChooser(uploadMsg, "");
    }

    // For Android  > 4.1.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        Log.i(TAG, "WebView: openFileChooser: > 4.1.1");
        openFileChooser(uploadMsg, acceptType);
    }

    // For Android  > 5.0
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        if (mFilePathCallbackArray == null) {
            mFilePathCallbackArray = filePathCallback;
        }
        checkPermission();
        return true;
    }

    private void checkPermission() {
        if (permissionUtil != null) {
            permissionUtil.checkPermission();
            return;
        }
        permissionUtil = new PermissionUtil(context, new PermissionUtil.PerListener() {
            @Override
            public void onPermissionSuccess() {
                if (filePathCallback != null) {
                    selectImage();
                } else if (mFilePathCallbackArray != null) {
                    createIntent();
                }
            }

            @Override
            public void onPermissionFail() {

            }

            @Override
            public int continuePermissionRequest() {
                return R.string.request_upload_image_permission;
            }
        }, Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void createIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra("PhotoPath", mCameraFilePath);
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCameraFilePath = "file:" + photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
            } else {
                takePictureIntent = null;
            }
        }
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");
        contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        contentSelectionIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        Intent[] intentArray;
        if (takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        context.startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
    }

    /**
     * More info this method can be found at
     * http://developer.android.com/training/camera/photobasics.html
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    private void selectImage() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        Intent chooser = createChooserIntent(createCameraIntent());
        chooser.putExtra(Intent.EXTRA_INTENT, i);
        context.startActivityForResult(chooser, FILECHOOSER_RESULTCODE);
    }

    private Intent createCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File externalDataDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);
        File cameraDataDir = new File(externalDataDir.getAbsolutePath() +
                File.separator + "browser-photos");
        cameraDataDir.mkdirs();
        mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator +
                System.currentTimeMillis() + ".jpg";
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
        return cameraIntent;
    }

    private Intent createChooserIntent(Intent... intents) {
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
        chooser.putExtra(Intent.EXTRA_TITLE, "File Chooser");
        return chooser;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i(TAG, "WebView: onActivityResult: requestCode=" + requestCode +
                " resultCode=" + resultCode + " intent=" + intent);
        if (resultCode != RESULT_OK) {
            if (null != filePathCallback) {
                filePathCallback.onReceiveValue(null);
                filePathCallback = null;
            }
            if (null != mFilePathCallbackArray) {
                mFilePathCallbackArray.onReceiveValue(null);
                mFilePathCallbackArray = null;
            }
            return;
        }
        if (requestCode == FILECHOOSER_RESULTCODE) {
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            Log.i(TAG, "WebView: onActivityResult: result=" + result);
            if (result == null && intent == null && resultCode == RESULT_OK) {
                File cameraFile = new File(mCameraFilePath);
                if (cameraFile.exists()) {
                    result = Uri.fromFile(cameraFile);
                    // Broadcast to the media scanner that we have a new photo
                    // so it will be added into the gallery for the user.
                    context.sendBroadcast(
                            new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if (null == filePathCallback) return;
                result = Uri.fromFile(MagicFileChooser.getFileFromUri(context, result));
                filePathCallback.onReceiveValue(result);
                filePathCallback = null;
            } else {
                if (mFilePathCallbackArray == null) {
                    return;
                }
                Uri[] results = null;
                // Check that the response is a good one
                if (resultCode == RESULT_OK) {
                    if (intent == null) {
                        // If there is not data, then we may have taken a photo
                        if (mCameraFilePath != null) {
                            results = new Uri[]{Uri.parse(mCameraFilePath)};
                        }
                    } else {
                        String dataString = intent.getDataString();
                        //多选
                        final ClipData clipData = intent.getClipData();
                        if (clipData != null) {
                            int count = clipData.getItemCount();
                            if (count > 0) {
                                results = new Uri[count];
                                for (int i = 0; i < count; ++i) {
                                    results[i] = Uri.fromFile(MagicFileChooser.getFileFromUri(
                                            context, clipData.getItemAt(i).getUri()));
                                }
                            }
                        }
                        Log.i(TAG, "WebView: onActivityResult: " +
                                "dataString = " + dataString
                                + " clipData = " + clipData);
                        //单选
                        if (dataString != null && results == null) {
                            results = new Uri[]{Uri.fromFile(MagicFileChooser.
                                    getFileFromUri(context, Uri.parse(dataString)))};
                        }
                    }
                }
                mFilePathCallbackArray.onReceiveValue(results);
                mFilePathCallbackArray = null;
            }
        }
    }
}
