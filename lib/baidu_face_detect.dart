
import 'dart:async';

import 'package:flutter/services.dart';

class BaiduFaceDetect {
  static const MethodChannel _channel =
      const MethodChannel('baidu_face_detect');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future init(String licenseId, String licenseFileName) async{
    await _channel.invokeMethod("initSDK", {"licenseId":licenseId, "licenseFileName":licenseFileName});
  }

  static Future<LivenessResult> liveness() async {
    Map<dynamic, dynamic> map = await _channel.invokeMethod('liveness');
    return LivenessResult.fromMap(map);
  }

  static Future<DetectResult> detect() async {
    Map<dynamic, dynamic> map = await _channel.invokeMethod('detect');
    return DetectResult.fromMap(map);
  }

}


class LivenessResult {
  bool? success;
  String? image;

  LivenessResult({this.success,this.image});

  factory LivenessResult.fromMap(Map<dynamic, dynamic> map) =>
      LivenessResult(
        success: map['success'],
        image: map['image'],
      );


  @override
  String toString() => 'LivenessResult: $success,$image';
}

class DetectResult {
   bool? success;
   String? image;

  DetectResult({this.success, this.image});

  factory DetectResult.fromMap(Map<dynamic, dynamic> map) => new DetectResult(
    success: map['success'],
    image: map['image'],
  );

  @override
  String toString() => 'DetectResult: $success,$image';
}