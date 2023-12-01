//
// Created by Administrator on 2023/10/31.
//

#include <jni.h>
#include <syslog.h>
#include "LameEncoder.h"

LameEncoder::LameEncoder() {

}

LameEncoder::~LameEncoder() {

}

void LameEncoder::init() {
    lameClient = lame_init();
}

void
LameEncoder::encode(jobject obj, const char *pcmFilePath, const char *mp3FilePath, int channels,
                    int bitRate, int sampleRate) {
    pcmFile = fopen(pcmFilePath, "rb");
    if (pcmFile) {
        mp3File = fopen(mp3FilePath, "wb");
        if (mp3File) {
            lame_set_in_samplerate(lameClient, sampleRate);
            lame_set_out_samplerate(lameClient, sampleRate);
            lame_set_num_channels(lameClient, channels);
            lame_set_brate(lameClient, bitRate / 1000);
            lame_init_params(lameClient);

            int bufferSize = 1024 * 256;
            auto *buffer = new short[bufferSize / 2];
            auto *leftBuffer = new short[bufferSize / 4];
            auto *rightBuffer = new short[bufferSize / 4];
            auto *mp3_buffer = new unsigned char[bufferSize];
            size_t readBufferSize = 0;
            while ((readBufferSize = fread(buffer, 2, bufferSize / 2, pcmFile)) > 0) {
                for (int i = 0; i < readBufferSize; i++) {
                    if (i % 2 == 0) {
                        leftBuffer[i / 2] = buffer[i];
                    } else {
                        rightBuffer[i / 2] = buffer[i];
                    }
                }
                size_t writeSize = lame_encode_buffer(lameClient,
                                                      (short int *) leftBuffer,
                                                      (short int *) rightBuffer,
                                                      (int) (readBufferSize / 2),
                                                      mp3_buffer,
                                                      bufferSize);
                fwrite(mp3_buffer, 1, writeSize, mp3File);
            }
            delete[] buffer;
            delete[] leftBuffer;
            delete[] rightBuffer;
            delete[] mp3_buffer;
        }
    } else {
        syslog(3, "pcmFile open failed");
    }
}

void LameEncoder::destroy() {

}
