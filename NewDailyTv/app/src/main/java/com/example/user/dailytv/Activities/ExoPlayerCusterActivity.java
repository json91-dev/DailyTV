package com.example.user.dailytv.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Surface;

import com.example.user.dailytv.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.quickblox.sample.groupchatwebrtc.App;

/**
 * Created by user on 2018-01-11.
 */

public class ExoPlayerCusterActivity extends Activity implements VideoRendererEventListener {
    SimpleExoPlayerView playerView;
    private SimpleExoPlayer player;



    final App.GlobalVariable global=App.getGlobal();

    SharedPreferences shared;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.exoplayerclusteractivity);

        SharedPreferences shared=getSharedPreferences("logininfo",Activity.MODE_PRIVATE);


        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        // 데이터의 Load를 조절하는 컨트롤러
        LoadControl loadControl = new DefaultLoadControl();

        // 3. Create the player
        // 플레이어 생성
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        playerView = new SimpleExoPlayerView(this);
        playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);


        //Set media controller
        //미디어 컨트롤러 생성
        playerView.setUseController(true);
        playerView.requestFocus();

        // Bind the player to the view.
        //플레이어 설정
        playerView.setResizeMode(3);
        playerView.setPlayer(player);

        //mp4 파일의 Uri 설정

        Intent intent=getIntent();
        String videourl=intent.getExtras().getString("videourl");


        Uri mp4VideoUri =Uri.parse(videourl);


        //Produces DataSource instances through which media data is loaded.
        //데이터 소스를 업데이트 하는 소스(스트림)
        DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), bandwidthMeterA);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();


        final MediaSource mediaSource=new ExtractorMediaSource(mp4VideoUri,dataSourceFactory,extractorsFactory,null,null);
        final LoopingMediaSource loopingSource = new LoopingMediaSource(mediaSource);

        player.prepare(mediaSource);
        player.addListener(new Player.EventListener() {

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                //시간이 변했을때의 리스너이다.
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                //트랙이 변했을때의 리스너이다.
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                //로딩이 변했을때의 리스너이다.
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                //플레이어의 상태변화가 있을때의 리스너이다.
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                //다시 재생 모드가 변했을때 리스너이다.
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                //플레이어에 에러가 있을때의 리스너이다.

                player.stop();
                player.prepare(mediaSource);
                player.setPlayWhenReady(true);
            }

            @Override
            public void onPositionDiscontinuity() {
                // 포지션에 변화가 있을때의 리스너이다.
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                //playback 상태가 변했을때의 리스너이다.
            }
        });

        player.setPlayWhenReady(true);
        player.setVideoDebugListener(this);
    }

    @Override
    public void onVideoEnabled(DecoderCounters counters) {
        //비디오 재생이 가능할때 호출되는 리스너이다.
    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
        //비디오 디코더가 초기화됬을때의 리스너이다.
    }

    @Override
    public void onVideoInputFormatChanged(Format format) {
        //비디오의 INput 포멧이 변했을때의 리스너이다.
    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {
        //프레임의 손실이 있을때 호출되는 리스너이다.
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        //비디오의 크기가 변했을때 호출되는 리스너이다.
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {
        //첫번쨰 프레임을 렌더링했을때 호출되는 리스너이다.
    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {
        //비디오가 중지되었을떄 호출되는 리스너이다.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}
