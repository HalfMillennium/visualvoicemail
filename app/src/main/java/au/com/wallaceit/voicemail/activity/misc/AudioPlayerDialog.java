package au.com.wallaceit.voicemail.activity.misc;
/*
 * Copyright 2015 Michael Boyde Wallace (http://wallaceit.com.au)
 * This file is part of Voicemail.
 *
 * Voicemail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Voicemail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Voicemail (COPYING). If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by michael on 17/09/15.
 */
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import au.com.wallaceit.voicemail.Preferences;
import au.com.wallaceit.voicemail.R;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.helper.PlayerUtilities;

public class AudioPlayerDialog extends Dialog {
    private Preferences mPreferences;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private boolean mSpeakerphone = true;
    private Uri mUri;
    private final Handler handler = new Handler();
    private final Runnable seekUpdateTask;
    private final SeekBar seekBar;
    private final TextView progressText;
    private final ImageButton playButton;

    public AudioPlayerDialog(Context context, Uri uri) {
        super(context);
        setOwnerActivity((Activity) context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.audio_player);
        mPreferences = Preferences.getPreferences(context);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.STREAM_VOICE_CALL);
        mSpeakerphone = mPreferences.getStorage().getBoolean("playerSpeaker", true);
        mUri = uri;

        progressText = (TextView) findViewById(R.id.player_progress_text);

        findViewById(R.id.player_close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        playButton = (ImageButton) findViewById(R.id.player_play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer.isPlaying()){
                    mMediaPlayer.pause();
                    ((ImageButton) v).setImageResource(android.R.drawable.ic_media_play);
                } else {
                    mMediaPlayer.start();
                    ((ImageButton) v).setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });

        final ImageButton speakerButton = (ImageButton) findViewById(R.id.player_speaker_button);
        if (mSpeakerphone) {
            speakerButton.setColorFilter(0xff99cc00, PorterDuff.Mode.MULTIPLY);
        } else {
            speakerButton.setColorFilter(null);
        }
        speakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpeakerphone){
                    ((ImageButton) v).setColorFilter(null);
                    setSpeakerphone(false);
                } else {
                    ((ImageButton) v).setColorFilter(0xff99cc00, PorterDuff.Mode.MULTIPLY);
                    setSpeakerphone(true);
                }
                mSpeakerphone = !mSpeakerphone;
            }
        });

        seekBar = (SeekBar) findViewById(R.id.player_seekbar);
        /**
         * Background Runnable thread
         * */
        seekUpdateTask = new Runnable() {
            public void run() {
                int currentDuration = mMediaPlayer.getCurrentPosition();

                // Displaying time completed playing
                progressText.setText(PlayerUtilities.milliSecondsToTimer(currentDuration));

                // Updating progress bar
                seekBar.setProgress(currentDuration);

                // Running this thread after 100 milliseconds
                handler.postDelayed(this, 100);
            }
        };
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // remove message Handler from updating progress bar
                handler.removeCallbacks(seekUpdateTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(seekUpdateTask);

                // forward or backward to certain seconds
                mMediaPlayer.seekTo(seekBar.getProgress());

                if (!mMediaPlayer.isPlaying()) {
                    playButton.setImageResource(android.R.drawable.ic_media_pause);
                    mMediaPlayer.start();
                }

                // update timer progress again
                handler.postDelayed(seekUpdateTask, 100);
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playButton.setImageResource(android.R.drawable.ic_media_play);
                Log.w(VisualVoicemail.LOG_TAG, "On complete called");
            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(getContext(), "Failed to play the audio file", Toast.LENGTH_SHORT).show();
                dismiss();
                return false;
            }
        });
    }

    public void show() {
        super.show();
        setSpeakerphone(mSpeakerphone);
        try {
            // prepare & start media
            mMediaPlayer.setDataSource(getContext(), mUri);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            // setup ui
            seekBar.setMax(mMediaPlayer.getDuration());
            ((TextView) findViewById(R.id.player_time_text)).setText(PlayerUtilities.milliSecondsToTimer(mMediaPlayer.getDuration()));
            handler.postDelayed(seekUpdateTask, 100);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to open the audio file", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    @Override
    public void onStop() {
        handler.removeCallbacks(seekUpdateTask);
        mMediaPlayer.stop();
        mMediaPlayer.setAudioStreamType(AudioManager.MODE_NORMAL); // clear audio settings, this effects other applications WTF android.
        mMediaPlayer.release();
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.setSpeakerphoneOn(true);
        mPreferences.getStorage().edit().putBoolean("playerSpeaker", mSpeakerphone).commit();
        dismiss();
    }

    private void setSpeakerphone(boolean on){
        if (on){
            mAudioManager.setSpeakerphoneOn(true);
        } else {
            mAudioManager.setSpeakerphoneOn(false);
        }
    }

    public void setOkButtonCallback(final OnClickListener clickListener){
        Button okayButton = (Button) findViewById(R.id.okay_btn);
        okayButton.setVisibility(View.VISIBLE);
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(AudioPlayerDialog.this, 0);
            }
        });
    }

    public void setCancelButtonCallback(final OnClickListener clickListener){
        Button okayButton = (Button) findViewById(R.id.cancel_btn);
        okayButton.setVisibility(View.VISIBLE);
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(AudioPlayerDialog.this, 0);
            }
        });
    }
}
