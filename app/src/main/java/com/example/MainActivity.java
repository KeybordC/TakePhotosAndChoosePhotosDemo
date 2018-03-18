package com.example;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.permission.CameraPermission;
import com.example.permission.SDCardReadPermission;
import com.example.permission.SDCardWritePermission;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Button take_photo;
    private Button choose_photo;
    private ImageView show_photo;

    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 3;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        click();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        // 将拍摄的照片显示出来
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        show_photo.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    handleImage(data);
                }
                break;
            default:
                break;
        }
    }

    private void initView() {
        take_photo = (Button) findViewById(R.id.take_photo);
        choose_photo = (Button) findViewById(R.id.choose_photo);
        show_photo = (ImageView) findViewById(R.id.show_photo);
    }

    private void click() {
        // 拍照
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckCamera.hasCamera()) {
                    if (CameraPermission.applyForCameraPermission(MainActivity.this)) {
                        if (SDCardWritePermission.applyForSDCardWritePermission(MainActivity.this)) {
                            takePhoto();
                        }
                    }
                }
            }
        });
        // 从相册选择照片
        choose_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SDCardReadPermission.applyForSDCardReadPermission(MainActivity.this)) {
                    // 打开相册
                    Intent intent = new Intent("android.intent.action.GET_CONTENT");
                    intent.setType("image/*");
                    startActivityForResult(intent, CHOOSE_PHOTO);
                }
            }
        });
    }

    private void takePhoto() {
        File file = new File(getExternalCacheDir(), "test.jpg");
        try {
            if (file.exists()) {
                file.delete();// 照片已经存在的情况下先删除
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 获取照片的路径
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.photo.fileprovider", file);
        } else {
            imageUri = Uri.fromFile(file);
        }
        // 启动相机
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void handleImage(Intent intent) {
        String imagePath = null;
        Uri uri = intent.getData();
        if (Build.VERSION.SDK_INT >= 19) {
            // android4.4及以上系统处理图片的方法
            if (DocumentsContract.isDocumentUri(MainActivity.this, uri)) {
                // document类型的Uri，通过document的id处理
                String documentsID = DocumentsContract.getDocumentId(uri);
                if (uri.getAuthority().equals("com.android.providers.media.documents")) {
                    String id = documentsID.split(":")[1];// 解析出数字格式的id
                    String selection = MediaStore.Images.Media._ID + "=" + id;
                    imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                } else if (uri.getAuthority().equals("com.android.downloads.documents")) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentsID));
                    imagePath = getImagePath(contentUri, null);
                }
            } else if (uri.getScheme().equals("content")) {
                // content类型的Uri，使用普通方式处理
                imagePath = getImagePath(uri, null);
            } else if (uri.getScheme().equals("file")) {
                // file类型的Uri，直接获取图片路径
                imagePath = uri.getPath();
            }
            showPhoto(imagePath);
        } else {
            // android4.4以下处理图片的方法
            imagePath = getImagePath(uri, null);
            showPhoto(imagePath);
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实图片的路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void showPhoto(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            show_photo.setImageBitmap(bitmap);
        } else {
            Toast.makeText(MainActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
        }
    }

}