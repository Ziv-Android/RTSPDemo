package com.ziv.rtsplibrary.stream.video.screen.media;

import android.media.MediaCodec;

public abstract class MediaCodecBase {

    protected MediaCodec mEncoder;

    protected boolean isRun = false;

    public abstract void prepare();

    public abstract void release();

    protected H264DataCollector mH264Collector;

}
