package com.exa.pcmutil.util;

/**
 * @author lsh
 * @创建日期 2021-2-2 10:47
 * @描述 录音回调
 */
public interface AudioCallBack {
    /**
     * 录制失败
     * @param msg 失败消息
     */
    void onRecordAudioErr(String msg);

    /**
     * 录制完成
     * @param path 存储路径
     */
    void onRecordComplete(String path);

    /**
     * 回调音量大小（分贝）
     * @param ratio 音量大小
     */
   default void onVoiceRatio(double ratio){};
}
