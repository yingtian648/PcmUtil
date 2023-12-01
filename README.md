# PcmUtil
use lame lib to convert pcm to mp3

## 打包aar（开发环境：Android studio）
1.将app下面的build.gradle配置成：com.android.library (默认是com.android.application)
2.将app下面的build.gradle中的applicationId配置注释掉
3.在右侧选则gradle -> 项目名称下面的Tasks -> other -> bundleDebugAar/bundleReleaseAar
4.编译完成后，在app/build/outputs下会生成一个aar文件夹，aar文件就在里面
