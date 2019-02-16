package com.example.douglasdeleon.lab6

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.content.ContentUris;
import android.media.AudioManager;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioAttributes.USAGE_MEDIA
import android.provider.MediaStore
import com.example.douglasdeleon.lab6.R.drawable.rand
import android.media.AudioTrack






class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private val musicBind = MusicBinder()

    //media player
    private val player: MediaPlayer = MediaPlayer()
    //song list
    private var songs: ArrayList<Song> = ArrayList()
    //current position
    private var songPosn: Int = 0
    private var songTitle: String = ""
    private val NOTIFY_ID: Int = 1
    private var shuffle = false
    private var rand: Random? = null

    override fun onBind(intent: Intent): IBinder? {
        return musicBind
    }

    override fun onUnbind(intent: Intent): Boolean {
        player.stop()
        player.release()
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        //start playback
        mp.start()

        val notIntent = Intent(this@MusicService, MainActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder: Notification.Builder =  Notification.Builder(this@MusicService)

        builder.setContentIntent(pendInt)
            .setSmallIcon(R.drawable.play)
            .setTicker(songTitle)
            .setOngoing(true)
            .setContentTitle("Playing")
        .setContentText(songTitle);
         val not:Notification = builder.build()

        startForeground(NOTIFY_ID, not);
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mp.reset()
        return false
    }

    override fun onCreate() {
        super.onCreate()

        initMusicPlayer()
        rand = Random()
    }

    override fun onDestroy() {
        stopForeground(true)
    }

    fun initMusicPlayer() {
        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK)

        player.setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build())

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

    fun playSong() {
        //play a song
        player.reset();

        //get song
        val playSong = songs[songPosn]

        songTitle = playSong.title
        //get id
        val currSong = playSong.id
        //set uri
        val trackUri = ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            currSong


        )

        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }

        player.prepareAsync()
    }

    fun setSong(songIndex: Int) {
        songPosn = songIndex
    }

    fun getPosn(): Int {
        return player.currentPosition
    }

    fun getDur(): Int {
        return player.duration
    }

    fun isPng(): Boolean {
        return player.isPlaying
    }

    fun pausePlayer() {
        player.pause()
    }

    fun seek(posn: Int) {
        player.seekTo(posn)
    }

    fun go() {
        player.start()
    }

    fun playPrev(){
        songPosn --
        if(songPosn < 0) songPosn = songs.size-1
        playSong()
    }

    //skip to next
    fun playNext(){
            if(shuffle){
                var newSong = songPosn;
                while(newSong==songPosn){
                    newSong=rand!!.nextInt(songs.size)
                }
                songPosn=newSong
            }
            else{
                songPosn++;
                if(songPosn <= songs.size) songPosn=0
            }
            playSong();
        }

    fun setShuffle() {
        if (shuffle)
            shuffle = false
        else
            shuffle = true
    }


    override fun onCompletion(mp: MediaPlayer?) {
        if(player.currentPosition < 0){
            mp!!.reset()
            playNext()
        }
    }
}