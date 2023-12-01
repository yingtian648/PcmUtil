//
// Created by Administrator on 2023/10/31.
//

#ifndef PCMUTIL_LAMEENCODER_H
#define PCMUTIL_LAMEENCODER_H

#include "libmp3lame/lame.h"

class LameEncoder {

private:
    FILE *pcmFile;
    FILE *mp3File;
    lame_t lameClient;

public:
    LameEncoder();

    ~LameEncoder();

    void init();

    void encode(jobject obj, const char *pcmFilePath, const char *mp3FilePath,
                int channels, int bitRate, int sampleRate);

    void destroy();

};


#endif //PCMUTIL_LAMEENCODER_H
