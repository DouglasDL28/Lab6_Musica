package com.example.douglasdeleon.lab6

import android.Manifest
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
import android.content.pm.PackageManager
import android.database.Cursor
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController
import com.example.douglasdeleon.lab6.MusicService.MusicBinder


class MainActivity : AppCompatActivity(), MediaPlayerControl {

    private var songList: ArrayList<Song> = ArrayList()
    private var songView: ListView? = null
    private var musicSrv: MusicService? = MusicService()
    private var playIntent: Intent? = Intent()
    private var musicBound = false
    private var controller: MediaController? = null
    private var paused = false
    private var playbackPaused = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        songView = findViewById(R.id.song_list)
        getSongList()

        controller = MediaController(this@MainActivity)

        Collections.sort(songList) { a, b -> a.title.compareTo(b.title) }

        val songAdt = SongAdapter(this@MainActivity, songList)
        songView!!.adapter = songAdt

        setController();

    }

    override fun onStart() {
        super.onStart()
        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playIntent)
        }
    }

    override fun onDestroy() {
        stopService(playIntent)
        musicSrv = null
        super.onDestroy()
    }

    //connect to the service
    private val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicBinder
            //get service
            musicSrv = binder.service
            //pass list
            musicSrv!!.setList(songList)
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

        if (ContextCompat.checkSelfPermission(this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    112
                    )

                // MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            val musicCursor: Cursor? = musicResolver.query(musicUri, null, null, null, null)

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

            musicCursor!!.close()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //menu item selected
        when (item.itemId) {
            R.id.action_end -> {
                stopService(playIntent)
                musicSrv = null
                System.exit(0)
            }

            R.id.action_shuffle -> {
                musicSrv!!.setShuffle();
            }
        }//shuffle
        return super.onOptionsItemSelected(item)
    }

    private fun setController() {
        //set the controller up
        controller = MusicController(this);

        controller!!.setPrevNextListeners(
            View.OnClickListener { playNext() },
            View.OnClickListener { playPrev() })

        controller!!.setMediaPlayer(this);
        controller!!.setAnchorView(findViewById(R.id.song_list));
        controller!!.setEnabled(true);

    }


    override fun isPlaying(): Boolean {
        if(musicSrv!=null && musicBound)
        return musicSrv!!.isPng();
        return false
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

     override fun getDuration(): Int {
        if(musicSrv!=null && musicBound && musicSrv!!.isPng())
        return musicSrv!!.getDur();
        else return 0;
    }

    override fun getBufferPercentage(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



    override fun getCurrentPosition(): Int {
        return if (musicSrv != null && musicBound && musicSrv!!.isPng())
            musicSrv!!.getPosn()
        else
            0
    }

    override fun seekTo(pos: Int) {
        musicSrv!!.seek(pos)
    }

    override fun start() {
        musicSrv!!.go()
    }

    override fun getAudioSessionId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        if (paused) {
            setController()
            paused = false
        }
    }

    override fun onStop() {
        controller!!.hide()
        super.onStop()
    }

    private fun playNext() {
        musicSrv!!.playNext()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        controller!!.show(0)
    }

    private fun playPrev() {
        musicSrv!!.playPrev()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        controller!!.show(0)
    }

    fun songPicked(view: View) {
        musicSrv!!.setSong(Integer.parseInt(view.tag.toString()))
        musicSrv!!.playSong()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        controller!!.show(0)
    }

    override fun pause() {
        playbackPaused = true
        musicSrv!!.pausePlayer()
    }

}
