package com.example.permission;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;

import com.joker.api.Permissions4M;
import com.joker.api.wrapper.ListenerWrapper;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

import static com.joker.api.Permissions4M.PageType.ANDROID_SETTING_PAGE;

/**
 * 动态申请摄像机权限
 */

public class CameraPermission {

    private static boolean result = false;

    public static boolean applyForCameraPermission(final Activity activity) {
        if (Build.VERSION.SDK_INT <= 23) {
            Permissions4M.get(activity)
                    .requestForce(true)// 是否强制弹出权限申请对话框
                    .requestUnderM(true)// 是否支持 5.0 权限申请
                    .requestPermissions(Manifest.permission.CAMERA)// 申请的权限
                    .requestCodes(13)// 申请权限的权限码;
                    .requestListener(new ListenerWrapper.PermissionRequestListener() {
                        @Override
                        public void permissionGranted(int i) {
                            // 同意
                            result = true;
                        }

                        @Override
                        public void permissionDenied(int i) {
                            // 不同意且没有选择【不再提示】
                            result = false;
                        }

                        @Override
                        public void permissionRationale(int i) {
                            // 不同意且选择【不再提示】
                            result = false;
                        }
                    })
                    .requestCustomRationaleListener(new ListenerWrapper.PermissionCustomRationaleListener() {
                        @Override
                        public void permissionCustomRationale(int i) {
                            new AlertDialog.Builder(activity)
                                    .setCancelable(false)
                                    .setTitle("授权")
                                    .setMessage("APP需要启用摄像功能，是否授予摄像机的使用权限?\n（不授予该权限则无法启用拍照功能）")
                                    .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            Permissions4M.get(activity)
                                                    .requestOnRationale()
                                                    .requestPermissions(Manifest.permission.CAMERA)
                                                    .requestCodes(13)
                                                    .request();
                                        }
                                    })
                                    .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            result = false;
                                        }
                                    })
                                    .show();
                        }
                    })
                    .requestPageType(ANDROID_SETTING_PAGE)// 权限被完全禁止时回调函数中返回   ANDROID_SETTING_PAGE 系统设置界面，MANAGER_PAGE 手机管家界面
                    .requestPage(new ListenerWrapper.PermissionPageListener() {
                        @Override
                        public void pageIntent(int i, final Intent intent) {
                            new AlertDialog.Builder(activity)
                                    .setCancelable(false)
                                    .setTitle("授权")
                                    .setMessage("APP启用摄像功能，需要授予摄像机的使用权限?\n（不授予该权限则无法启用拍照功能）")
                                    .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            activity.startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            result = false;
                                        }
                                    })
                                    .show();
                        }
                    })
                    .request();
        } else {
            RxPermissions permissions = new RxPermissions(activity);
            permissions.requestEach(Manifest.permission.CAMERA).subscribe(new Consumer<Permission>() {
                @Override
                public void accept(Permission permission) throws Exception {
                    if (permission.granted) {
                        // 同意权限
                        result = true;
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // 用户拒绝权限并且没有选择【不再提示】
                        result = false;
                    } else {
                        // 用户拒绝权限并且选择【不再提示】
                        result = false;
                    }
                }
            });
        }
        return result;
    }

}