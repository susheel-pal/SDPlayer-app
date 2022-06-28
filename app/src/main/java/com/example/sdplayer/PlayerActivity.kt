package com.example.sdplayer

import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.SeekBar
import android.annotation.SuppressLint
import android.os.Bundle
import com.example.sdplayer.R
import com.example.sdplayer.PlayerActivity
import android.content.Intent
import android.os.Parcelable
import android.media.MediaPlayer
import android.graphics.PorterDuff
import android.widget.SeekBar.OnSeekBarChangeListener
import android.view.animation.TranslateAnimation
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.media.MediaPlayer.OnCompletionListener
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.net.Uri
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import java.io.File
import java.lang.IllegalStateException
import java.util.ArrayList

class PlayerActivity : AppCompatActivity() {
    var btnPlay: Button? = null
    var btnNext: Button? = null
    var btnPrevious: Button? = null
    var btnFastForward: Button? = null
    var btnFastBackward: Button? = null
    var txtSongName: TextView? = null
    var txtSongStart: TextView? = null
    var txtSongEnd: TextView? = null
    var seekMusicBar: SeekBar? = null

    //BarVisualizer barVisualizer;
    var imageView: ImageView? = null
    var songName: String? = null
    var position = 0
    var mySongs: ArrayList<File>? = null
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        /*if(barVisualizer != null){
            barVisualizer.release();
        }*/
        super.onDestroy()
    }

    var updateSeekBar: Thread? = null
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        supportActionBar!!.title = "SD Player"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnPlay = findViewById(R.id.btnPlay)
        btnFastForward = findViewById(R.id.btnFastForward)
        btnFastBackward = findViewById(R.id.btnFastBackward)
        txtSongName = findViewById(R.id.txtSong)
        txtSongStart = findViewById(R.id.txtSongStart)
        txtSongEnd = findViewById(R.id.txtSongEnd)
        seekMusicBar = findViewById(R.id.seekBar)
        //barVisualizer = findViewById(R.id.wave);
        imageView = findViewById(R.id.imgView)
        if (mediaPlayer != null) {
            mediaPlayer!!.start()
            mediaPlayer!!.release()
        }
        val intent = intent
        val bundle = intent.extras
        /** for type casting in kotlin use as (unsafe) or as? (safe) afterword use dataType
        in which you want to convert */
        mySongs = bundle!!.getParcelableArrayList<Parcelable>("songs") as? ArrayList<File> //Parcelable
        val sName = intent.getStringExtra("songname")
        position = bundle.getInt("pos", 0)
        txtSongName?.setSelected(true)
        val uri = Uri.parse(mySongs!![position].toString())
        songName = mySongs!![position].name
        txtSongName?.setText(songName)
        mediaPlayer = MediaPlayer.create(applicationContext, uri)
        mediaPlayer?.start()
        updateSeekBar = object : Thread() {
            override fun run() {
                val totalDuration = mediaPlayer?.getDuration()
                var currentPosition = 0
                while (currentPosition < totalDuration!!) {
                    try {
                        sleep(500)
                        currentPosition = mediaPlayer?.getCurrentPosition()!!
                        seekMusicBar?.setProgress(currentPosition)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        mediaPlayer?.getDuration()?.let { seekMusicBar?.setMax(it) }
        updateSeekBar?.start()
        seekMusicBar?.getProgressDrawable()?.setColorFilter(resources.getColor(R.color.black), PorterDuff.Mode.MULTIPLY)
        seekMusicBar?.getThumb()?.setColorFilter(resources.getColor(R.color.black), PorterDuff.Mode.SRC_IN)
        seekMusicBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mediaPlayer?.seekTo(seekBar.progress)
            }
        })
        val endTime = createTime(mediaPlayer?.getDuration()!!)
        txtSongEnd?.setText(endTime)
        val handler = Handler()
        val delay = 1000
        handler.postDelayed(object : Runnable {
            override fun run() {
                val currentTime = createTime(mediaPlayer?.getCurrentPosition()!!)
                txtSongStart?.setText(currentTime)
                handler.postDelayed(this, delay.toLong())
            }
        }, delay.toLong())
        btnPlay?.setOnClickListener(View.OnClickListener {
            if (mediaPlayer?.isPlaying()!!) {
                btnPlay?.setBackgroundResource(R.drawable.ic_play)
                mediaPlayer?.pause()
            } else {
                btnPlay?.setBackgroundResource(R.drawable.ic_pause)
                mediaPlayer?.start()
                //val moveAnim = TranslateAnimation(-25, 25, -25, 25)
                val moveAnim = TranslateAnimation(-25.0f, 25.0f, -25.0f, 25.0f)
                moveAnim.interpolator = AccelerateInterpolator()
                moveAnim.duration = 600
                moveAnim.isFillEnabled = true
                moveAnim.fillAfter = true
                moveAnim.repeatMode = Animation.REVERSE
                moveAnim.repeatCount = 1
                imageView?.startAnimation(moveAnim)
            }
        })
        mediaPlayer?.setOnCompletionListener(OnCompletionListener { btnNext?.performClick() })

        /*int audioSessionId = mediaPlayer.getAudioSessionId();
        if(audioSessionId != -1){
            barVisualizer.setAudioSessionId(audioSessionId);
        }*/btnNext?.setOnClickListener(View.OnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            position = (position + 1) % mySongs!!.size
            val uri = Uri.parse(mySongs!![position].toString())
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            songName = mySongs!![position].name
            txtSongName?.setText(songName)
            mediaPlayer?.start()
            val endTime = createTime(mediaPlayer?.getDuration()!!)
            txtSongEnd?.setText(endTime)
            startAnimation(imageView, 360f)
        })
        btnPrevious?.setOnClickListener(View.OnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            position = if (position - 1 < 0) mySongs!!.size - 1 else position - 1
            val uri = Uri.parse(mySongs!![position].toString())
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            songName = mySongs!![position].name
            txtSongName?.setText(songName)
            mediaPlayer?.start()
            val endTime = createTime(mediaPlayer?.getDuration()!!)
            txtSongEnd?.setText(endTime)
            startAnimation(imageView, -360f)
        })
        btnFastForward?.setOnClickListener(View.OnClickListener {
            if (mediaPlayer?.isPlaying()!!) {
                mediaPlayer?.seekTo(mediaPlayer?.getCurrentPosition()!! + 10000)
            }
        })
        btnFastBackward?.setOnClickListener(View.OnClickListener {
            if (mediaPlayer?.isPlaying()!!) {
                mediaPlayer?.seekTo(mediaPlayer?.getCurrentPosition()!! - 10000)
            }
        })
    }

    fun startAnimation(view: View?, degree: Float?) {
        val objectAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, degree!!)
        objectAnimator.duration = 1000
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(objectAnimator)
        animatorSet.start()
    }

    fun createTime(duration: Int): String {
        var time = ""
        val min = duration / 1000 / 60
        val sec = duration / 1000 % 60
        time = "$time$min:"
        if (sec < 10) {
            time += "0"
        }
        time += sec
        return time
    }

    companion object {
        const val EXTRA_NAME = "song_name"
        var mediaPlayer: MediaPlayer? = null
    }

}