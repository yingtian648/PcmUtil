package com.exa.pcmutil.util;

import android.util.Log;

/**
 * @author lsh
 * @date 2023/10/31 14:39
 */
public class PcmToMp3Util {
    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    interface Callback {
        /**
         * PCM开始转换MP3
         */
        void onStart();

        /**
         * PCM转MP3 完成
         *
         * @param pcmPath pcm文件路径
         * @param mp3Path mp3文件路径
         */
        void onEncodeComplete(String pcmPath, String mp3Path);

        /**
         * PCM转MP3 报错
         *
         * @param exceptionMsg 错误信息
         */
        void onEncodeException(String exceptionMsg);
    }

    public PcmToMp3Util() {
        init();
    }

    /**
     * native 回调接口
     */
    public void onStart() {
        Log.d("PcmToMp3Util", "onStart");
        if (callback != null) {
            callback.onStart();
        }
    }

    /**
     * native 回调接口
     */
    public void onEncodeComplete(String pcmPath, String mp3Path) {
        Log.d("PcmToMp3Util", "pcmPath:" + pcmPath + ", mp3Path=" + mp3Path);
        if (callback != null) {
            callback.onEncodeComplete(pcmPath, mp3Path);
        }
    }

    /**
     * native 回调接口
     */
    public void onEncodeException(String exceptionMsg) {
        Log.e("PcmToMp3Util", "exceptionMsg");
        if (callback != null) {
            callback.onEncodeException(exceptionMsg);
        }
    }

    public void pcmToMp3(String pcmPath, String mp3Path,
                         int audioChannels, int bitRate, int sampleRate) {
        encode(pcmPath, mp3Path, audioChannels, bitRate, sampleRate);
    }

    public void onDestroy() {
        destroy();
    }

    public native void init();

    public native void encode(String pcmPath, String mp3Path,
                              int audioChannels, int bitRate, int sampleRate);

    public native void destroy();

    static {
        System.loadLibrary("pcmutil");
    }
}
