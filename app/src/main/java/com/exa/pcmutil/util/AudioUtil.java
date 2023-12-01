package com.exa.pcmutil.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * @author Liushihua
 * @date 2021-4-27 17:07
 * @描述
 */
public class AudioUtil {
    private AudioRecord recorder;
    /**
     * 录音源
     */
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    /**
     * android 原生提供降噪
     */
    private static final int AUDIO_SOURCE_DE_NOISE = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    /**
     * 录音的采样频率  16k采样率
     */
    private static final int AUDIO_RATE = 16000;
    /**
     * 录音的声道，单声道 立体声
     */
    private static final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    /**
     * 量化的深度  16bps比特率
     */
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * 缓存的大小
     */
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(AUDIO_RATE,
            AUDIO_CHANNEL, AUDIO_FORMAT);
    /**
     * 记录播放状态
     */
    private boolean isRecording = false;
    /**
     * PCM文件
     */
    private File pcmFile;
    private File mp3File;
    /**
     * WAV文件
     */
    private File wavFile;
    /**
     * 文件输出流
     */
    private OutputStream os;
    /**
     * 文件根目录
     */
    private final String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    /**
     * wav文件保存路径
     */
    private final String savePath;
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final AudioCallBack callBack;
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * @param savePath 文件保存地址
     */
    public AudioUtil(Context context, @NonNull String savePath, @NonNull AudioCallBack callBack) {
        this.savePath = savePath;
        this.callBack = callBack;
        //创建文件
        createFile();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        recorder = new AudioRecord(AUDIO_SOURCE, AUDIO_RATE, AUDIO_CHANNEL, AUDIO_FORMAT, BUFFER_SIZE);
    }

    /**
     * 获取录音文件的本地路径
     */
    public String getBasePath() {
        return rootPath;
    }

    /**
     * 读取录音数字数据线程
     */
    class WriteThread implements Runnable {
        @Override
        public void run() {
            writeData();
        }
    }

    //开始录音
    public void startRecord() {
        L.d("startRecord");
        isRecording = true;
        recorder.startRecording();
        recordData();
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        L.d("stopRecord");
        isRecording = false;
        recorder.stop();
//        convertWavFile(savePath);
        pcmToMp3();
    }

    /**
     * 将数据写入文件夹,文件的写入没有做优化
     */
    @SuppressWarnings("IOStreamConstructor")
    public void writeData() {
        byte[] noteArray = new byte[BUFFER_SIZE];
        try {
            os = new BufferedOutputStream(new FileOutputStream(pcmFile));
        } catch (IOException e) {
            handler.post(() -> {
                callBack.onRecordAudioErr("writeData IOException2");
            });
        }
        while (isRecording) {
            int recordSize = recorder.read(noteArray, 0, BUFFER_SIZE);
            if (recordSize > 0) {
                try {
                    os.write(noteArray);
                } catch (IOException e) {
                    handler.post(() -> {
                        callBack.onRecordAudioErr("writeData IOException2");
                    });
                }
            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 这里得到可播放的音频文件
     */
    private void convertWavFile(final String outFileName) {
        L.d("convertWaveFile:" + outFileName);
        service.execute(() -> {
            FileInputStream in = null;
            FileOutputStream out = null;
            long totalAudioLen;
            long totalDataLen;
            int channels = AUDIO_CHANNEL == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
            long byteRate = 16 * AUDIO_RATE * channels / 8;
            byte[] data = new byte[BUFFER_SIZE];
            try {
                in = new FileInputStream(pcmFile);
                out = new FileOutputStream(outFileName);
                totalAudioLen = in.getChannel().size();
                //由于不包括RIFF和WAV
                totalDataLen = totalAudioLen + 36;
                writeWaveFileHeader(out, totalAudioLen, totalDataLen, AUDIO_RATE, channels, byteRate);
                while (in.read(data) != -1) {
                    out.write(data);
                }
                out.flush();
                handler.post(() -> {
                    callBack.onRecordComplete(outFileName);
                });
            } catch (IOException e) {
                e.printStackTrace();
                L.e("convertWaveFile IOException: " + e.getMessage());
                handler.post(() -> {
                    callBack.onRecordAudioErr("convertWaveFile IOException");
                });
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void pcmToMp3() {
        PcmToMp3Util util = new PcmToMp3Util();
        util.setCallback(new PcmToMp3Util.Callback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onEncodeComplete(String pcmPath, String mp3Path) {
                L.dd("pcmPath=" + pcmPath + ", size=" + new File(pcmPath).length());
                L.dd("mp3Path=" + mp3Path + ", size=" + new File(mp3Path).length());
                callBack.onRecordComplete(mp3Path);
            }

            @Override
            public void onEncodeException(String exceptionMsg) {

            }
        });
        int channels = AUDIO_CHANNEL == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
        int byteRate = 16 * AUDIO_RATE * channels / 8;
        util.encode(pcmFile.getAbsolutePath(), mp3File.getAbsolutePath(),
                channels, byteRate, AUDIO_RATE);
    }

    /**
     * 任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，wave是RIFF文件结构，
     * 每一部分为一个chunk，其中有RIFF WAVE chunk， FMT Chunk，Fact chunk,Data chunk,
     * 其中Fact chunk是可以选择的，
     */
    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen,
                                     long longSampleRate,
                                     int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    //创建文件夹,首先创建目录，然后创建对应的文件
    public void createFile() {
        File file = new File(savePath);
        if (file.exists()) {
            file.delete();
        } else {
            file.getParentFile().mkdirs();
        }
        //PathUtil.getTempPath() 获取临时存放pcm文件的路径
        pcmFile = new File(file.getParent(), "audio_record_temp.pcm");
        mp3File = new File(file.getParent(), "audio_record_mp3.mp3");
        if (pcmFile.exists()) {
            pcmFile.delete();
        }
        if (mp3File.exists()) {
            mp3File.delete();
        }
        try {
            pcmFile.createNewFile();
            mp3File.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //记录数据
    private void recordData() {
        new Thread(new WriteThread()).start();
    }
}
