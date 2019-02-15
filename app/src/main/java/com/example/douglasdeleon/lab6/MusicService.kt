package com.example.douglasdeleon.lab6

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.provider.MediaStore
import android.util.Log;


class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {

    //media player
    private val player: MediaPlayer = MediaPlayer()
    //song list
    private var songs: ArrayList<Song> = ArrayList()
    //current position
    private val songPosn: Int = 0

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return null
    }

    override fun onPrepared(mp: MediaPlayer?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCompletion(mp: MediaPlayer?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        super.onCreate()

        initMusicPlayer()
    }

    fun initMusicPlayer() {
        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this@MusicService)
        player.setOnCompletionListener(this@MusicService)
        player.setOnErrorListener(this@MusicService)
    }

    fun setList(theSongs: ArrayList<Song>) {
        songs = theSongs
    }

    inner class MusicBinder : Binder() {
        internal val service: MusicService
            get() = this@MusicService
    }
}