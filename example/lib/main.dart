import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:baidu_face_detect/baidu_face_detect.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String? callBack;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await BaiduFaceDetect.platformVersion ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }
    BaiduFaceDetect.init("kdqb-demo-face-android", "idl-license.face-android");
    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              Text('Running on: ${callBack}'),
              FlatButton(
                child: Text('活体检测'),
                onPressed: () {
                  BaiduFaceDetect.liveness().then((result) {
                    setState(() {
                      callBack = result.image;
                    });
                  });
                },
              ),
              FlatButton(
                child: Text('人脸检测'),
                onPressed: () {
                  BaiduFaceDetect.detect().then((result) {
                    setState(() {
                      callBack = result.image;
                    });
                  });
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
