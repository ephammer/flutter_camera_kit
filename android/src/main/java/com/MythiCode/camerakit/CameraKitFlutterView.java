package com.MythiCode.camerakit;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.google.mlkit.vision.barcode.Barcode;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.platform.PlatformView;

import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraKitFlutterView implements PlatformView, MethodChannel.MethodCallHandler, FlutterMethodListener {


    private static final int REQUEST_CAMERA_PERMISSION = 10001;
    private final MethodChannel channel;
    private final ActivityPluginBinding activityPluginBinding;
    private CameraBaseView cameraView;

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull final MethodChannel.Result result) {
        if (call.method.equals("requestPermission")) {
            if (ActivityCompat.checkSelfPermission(activityPluginBinding.getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activityPluginBinding.getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                activityPluginBinding.addRequestPermissionsResultListener(new PluginRegistry.RequestPermissionsResultListener() {
                    @Override
                    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                        for (int i :
                                grantResults) {
                            if (i == PackageManager.PERMISSION_DENIED) {
                                try {
                                    result.success(false);
                                }catch (Exception e){

                                }
                                return false;
                            }
                        }
                        try {
                            result.success(true);
                        } catch (Exception e) {

                        }
                        return false;
                    }
                });
                return;
            } else {
                try {
                    result.success(true);
                } catch (Exception e) {

                }
            }
        } else if (call.method.equals("initCamera")) {
            boolean hasBarcodeReader = call.argument("hasBarcodeReader");
            char flashMode = call.argument("flashMode").toString().charAt(0);
            boolean isFillScale = call.argument("isFillScale");
            int barcodeMode = call.argument("barcodeMode");
            int androidCameraMode = call.argument("androidCameraMode");
            int cameraSelector = call.argument("cameraSelector");
            getCameraView().initCamera(hasBarcodeReader, flashMode, isFillScale, barcodeMode
                    , androidCameraMode, cameraSelector);
        } else if (call.method.equals("resumeCamera")) {
            getCameraView().resumeCamera();

        } else if (call.method.equals("pauseCamera")) {
            getCameraView().pauseCamera();
        } else if (call.method.equals("takePicture")) {
            getCameraView().takePicture(result);
        } else if (call.method.equals("changeFlashMode")) {
            char captureFlashMode = call.argument("flashMode").toString().charAt(0);
            getCameraView().changeFlashMode(captureFlashMode);
        } else if (call.method.equals("dispose")) {
            dispose();
        } else if (call.method.equals("setCameraVisible")) {
            boolean isCameraVisible = call.argument("isCameraVisible");
            getCameraView().setCameraVisible(isCameraVisible);
        } else {
            result.notImplemented();
        }
    }

    private CameraBaseView getCameraView() {
        return cameraView;
    }

    public CameraKitFlutterView(ActivityPluginBinding activityPluginBinding, DartExecutor dartExecutor, int viewId) {
        this.channel = new MethodChannel(dartExecutor, "plugins/camera_kit_" + viewId);
        this.activityPluginBinding = activityPluginBinding;
        this.channel.setMethodCallHandler(this);
        if (getCameraView() == null) {
            cameraView = new CameraBaseView(activityPluginBinding.getActivity(), this);
        }
    }

    @Override
    public View getView() {
        return getCameraView().getView();
    }

    @Override
    public void dispose() {
        if (getCameraView() != null) {
            getCameraView().dispose();
        }
    }

    @Override
    public void onBarcodeRead(Barcode barcode) {
        Map result = new HashMap();
        result.put("code",barcode.getRawValue());
        result.put("type",barcode.getFormat());
        result.put("rawBytes",barcode.getRawBytes());
        channel.invokeMethod("onBarcodeRead", result);
    }

    @Override
    public void onTakePicture(final MethodChannel.Result result, final String filePath) {
        activityPluginBinding.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                result.success(filePath);
            }
        });
    }

    @Override
    public void onTakePictureFailed(final MethodChannel.Result result, final String errorCode, final String errorMessage) {
        activityPluginBinding.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                result.error(errorCode, errorMessage, null);
            }
        });
    }
}
