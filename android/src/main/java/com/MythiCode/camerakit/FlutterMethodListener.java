package com.MythiCode.camerakit;

import com.google.mlkit.vision.barcode.Barcode;
import io.flutter.plugin.common.MethodChannel;

public interface FlutterMethodListener {

    void onBarcodeRead(Barcode barcode);

    void onTakePicture(MethodChannel.Result result, String filePath);

    void onTakePictureFailed(MethodChannel.Result result, String errorCode, String errorMessage);
}
