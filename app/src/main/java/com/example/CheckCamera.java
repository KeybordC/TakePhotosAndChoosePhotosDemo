package com.example;

import android.hardware.Camera;
import android.os.Build;

/**
 * 检查设备是否有摄像头
 */

public class CheckCamera {

    public static boolean hasCamera() {
        return hasBackCamera() || hasFrontCamera();
    }

    /**
     * 检查设备是否有后置摄像头
     */
    private static boolean hasBackCamera() {
        final int CAMERA_FACING_BACK = 0;
        return isThereCamera(CAMERA_FACING_BACK);
    }

    /**
     * 检查设备是否有前置摄像头
     */
    private static boolean hasFrontCamera() {
        final int CAMERA_FACING_BACK = 1;
        return isThereCamera(CAMERA_FACING_BACK);
    }

    private static boolean isThereCamera(int direction) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            return false;
        }
        int count = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < count; i++) {
            Camera.getCameraInfo(i, info);
            if (direction == info.facing) {
                return true;
            }
        }
        return false;
    }

}