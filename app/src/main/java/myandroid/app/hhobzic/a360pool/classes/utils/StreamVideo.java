package myandroid.app.hhobzic.a360pool.classes.utils;


import android.net.Uri;
import android.widget.VideoView;

public class StreamVideo {

    private VideoView videoView;
    private String streamUrl;

    public StreamVideo(VideoView videoView, String streamUrl) {
        this.videoView = videoView;
        this.streamUrl = streamUrl;
        initializeStream();
    }

    private void initializeStream() {
        Uri uri = Uri.parse(streamUrl);
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(mp -> mp.setLooping(true));
        videoView.setOnErrorListener((mp, what, extra) -> {
            // Handle any errors that occur during playback
            return true;
        });
        videoView.start();
    }

    public void stopStreaming() {
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}
