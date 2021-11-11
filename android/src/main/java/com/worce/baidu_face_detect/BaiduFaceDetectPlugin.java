package com.worce.baidu_face_detect;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.baidu.idl.face.example.Application;
import com.baidu.idl.face.example.FaceDetectExpActivity;
import com.baidu.idl.face.example.FaceLivenessExpActivity;
import com.baidu.idl.face.platform.FaceSDKManager;
import com.baidu.idl.face.platform.LivenessTypeEnum;
import com.baidu.idl.face.platform.listener.IInitCallback;
import com.baidu.idl.face.platform.ui.utils.IntentUtils;
import com.baidu.idl.face.platform.utils.DensityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/** BaiduFaceDetectPlugin */
public class BaiduFaceDetectPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {
  private static final String TAG = "BaiduFaceDetectPlugin";
  private MethodChannel channel;
  private static Context mContext;
  private static Activity activity;
  private static Result mResult;
  private static boolean mIsInitSuccess = false;
  private static final int REQUEST_CODE_DETECT = 102;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "baidu_face_detect");
    channel.setMethodCallHandler(this);
    mContext = flutterPluginBinding.getApplicationContext();


  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if(call.method.equals("initSDK")){
      mResult = result;
      initSdk(call.argument("licenseId").toString(), call.argument("licenseFileName").toString());
    }else if(call.method.equals("liveness")){
      mResult = result;
      liveness();
    }else if(call.method.equals("detect")){
      mResult = result;
      detect();
    }else if(call.method.equals("requestPermissions")){
      mResult = result;
      requestPermissions();
    }else if(call.method.equals("addLiveAction")){
      List<String> actionNames = call.argument("actions");
      addLiveAction(actionNames);
    }else {
      result.notImplemented();
    }
  }

  private void addLiveAction(List<String> actionNames) {
    if(actionNames==null || actionNames.isEmpty()){
      return;
    }else{
      Application.livenessList.clear();
      for (String actionName:actionNames){
        LivenessTypeEnum type = LivenessTypeEnum.valueOf(actionName);
        if(type!=null){
          Application.livenessList.add(type);
        }
      }
    }
  }

  private void requestPermissions() {
    requestPermissions(99);
  }

  /**
   * 百度sdk初始化
   * @param licenseId
   * @param licenseFileName
   */
  private void initSdk(String licenseId, String licenseFileName) {
    final boolean success = Application.setFaceConfig(mContext);
    if (!success) {
      Application.showToast(mContext,"初始化失败 = json配置文件解析出错");
      return;
    }
    // 为了android和ios 区分授权，appId=appname_face_android ,其中appname为申请sdk时的应用名
    // 应用上下文
    // 申请License取得的APPID
    // assets目录下License文件名
    FaceSDKManager.getInstance().initialize(mContext, licenseId, licenseFileName, new IInitCallback() {
              @Override
              public void initSuccess() {
                activity.runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    Log.i(TAG, "init Baidu Face Detect sdk Success");
                    mIsInitSuccess = true;
                    mResult.success(true);
                  }
                });

              }
              @Override
              public void initFailure(final int errCode, final String errMsg) {
               activity.runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                   Log.i(TAG, "initFailure: "+errMsg);
                   mIsInitSuccess = false;
                   mResult.success(false);
                 }
               });
              }
            });
  }
  
  /**
   * 人脸检测
   */
  private void detect() {
    if(mIsInitSuccess){
      Intent intent = new Intent(mContext, FaceDetectExpActivity.class);
      activity.startActivityForResult(intent, REQUEST_CODE_DETECT);
    }
  }

  /**
   * 人脸检测设置动作
   */
  private void liveness() {
    if(mIsInitSuccess){
      Intent intent = new Intent(mContext, FaceLivenessExpActivity.class);
      activity.startActivityForResult(intent, REQUEST_CODE_DETECT);
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.i(TAG, "onActivityResult: "+requestCode);
    if(resultCode==Activity.RESULT_OK){
      Map result = new HashMap();
      String bmpStr = IntentUtils.getInstance().getBitmap();

      if (TextUtils.isEmpty(bmpStr)) {
        result.put("success",false);
      }else{
        result.put("success", true);
        result.put("image", bmpStr);
      }
      mResult.success(result);
    }
    return false;
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
    binding.addActivityResultListener(this);
    binding.addRequestPermissionsResultListener(this);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    binding.removeActivityResultListener(this);
    binding.removeRequestPermissionsResultListener(this);
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    boolean flag = false;
    for (int i = 0; i < permissions.length; i++) {
      if (PackageManager.PERMISSION_GRANTED == grantResults[i]) {
        flag = true;
      }
    }
    mResult.success(flag);
    return flag;
  }

  // 请求权限
  public void requestPermissions(int requestCode) {
    try {
      if (Build.VERSION.SDK_INT >= 23) {
        ArrayList<String> requestPerssionArr = new ArrayList<>();
        int hasCamrea = mContext.checkSelfPermission(Manifest.permission.CAMERA);
        if (hasCamrea != PackageManager.PERMISSION_GRANTED) {
          requestPerssionArr.add(Manifest.permission.CAMERA);
        }

        int hasSdcardRead = mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasSdcardRead != PackageManager.PERMISSION_GRANTED) {
          requestPerssionArr.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        int hasSdcardWrite = mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasSdcardWrite != PackageManager.PERMISSION_GRANTED) {
          requestPerssionArr.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        // 是否应该显示权限请求
        if (requestPerssionArr.size() >= 1) {
          String[] requestArray = new String[requestPerssionArr.size()];
          for (int i = 0; i < requestArray.length; i++) {
            requestArray[i] = requestPerssionArr.get(i);
          }
          activity.requestPermissions(requestArray, requestCode);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
