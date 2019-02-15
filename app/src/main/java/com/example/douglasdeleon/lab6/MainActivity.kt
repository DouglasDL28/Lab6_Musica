package com.example.douglasdeleon.lab6

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import java.util.Collections
import android.widget.ListView
import kotlin.collections.ArrayList
import android.widget.MediaController.MediaPlayerControl
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import com.example.douglasdeleon.lab6.MusicService.MusicBinder



class MainActivity : AppCompatActivity(), MediaPlayerControl {

    private var songList: ArrayList<Song> = ArrayList()
    private val songView: ListView = findViewById(R.id.song_list)
    private var musicSrv: MusicService = MusicService()
    private val playIntent: Intent = Intent()
    private var musicBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getSongList()

        Collections.sort(songList) { a, b -> a.title.compareTo(b.title) }

        val songAdt = SongAdapter(this@MainActivity, songList)
        songView.adapter = songAdt

    }

    //connect to the service
    private val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicBinder
            //get service
            musicSrv = binder.service
            //pass list
            musicSrv.setList(songList)
            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

    fun getSongList() {
        //retrieve song info
        val musicResolver = contentResolver
        val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = musicResolver.query(musicUri, null, null, null, null)

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            val titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
            val artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
            //add songs to list
            do {
                val thisId = musicCursor.getLong(idColumn)
                val thisTitle = musicCursor.getString(titleColumn)
                val thisArtist = musicCursor.getString(artistColumn)
                songList.add(Song(thisId, thisTitle, thisArtist))
            } while (musicCursor.moveToNext())
        }

        musicCursor.close()
    }

    override fun isPlaying(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canSeekForward(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDuration(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBufferPercentage(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun seekTo(pos: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCurrentPosition(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canSeekBackward(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun start() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAudioSessionId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canPause(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
