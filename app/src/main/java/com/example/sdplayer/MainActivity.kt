package com.example.sdplayer

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sdplayer.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import android.os.Environment
import com.example.sdplayer.MainActivity.customAdapter
import android.widget.AdapterView.OnItemClickListener
import android.content.Intent
import android.view.*
import android.widget.*
import androidx.core.view.MenuItemCompat
import com.example.sdplayer.PlayerActivity
import com.karumi.dexter.listener.PermissionRequest
import java.io.File
import java.util.ArrayList
import java.util.Locale.filter

//import android.webkit.PermissionRequest;
class MainActivity : AppCompatActivity() {
    var listView: ListView? = null
    lateinit var items: Array<String?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById<View>(R.id.listView) as ListView
        runtimePermission()
    }

    fun runtimePermission() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    displaySong()
                }

                override fun onPermissionRationaleShouldBeShown(
                    list: List<PermissionRequest>,
                    permissionToken: PermissionToken
                ) {
                    permissionToken.continuePermissionRequest()
                }
            }).check()
    }

    fun findSong(file: File): ArrayList<File> {
        val arrayList = ArrayList<File>()
        val files = file.listFiles()
        for (singleFile in files) {
            if (singleFile.isDirectory && !singleFile.isHidden) {
                arrayList.addAll(findSong(singleFile))
            } else {
                if (singleFile.name.endsWith(".mp3") || singleFile.name.endsWith(".wav")) {
                    arrayList.add(singleFile)
                }
            }
        }
        return arrayList
    }

    fun displaySong() {
        val mySongs = findSong(Environment.getExternalStorageDirectory())
        items = arrayOfNulls(mySongs.size)
        for (i in mySongs.indices) {
            items[i] = mySongs[i].name.toString().replace(".mp3", "").replace(".wav", "")
        }
        val customAdapter = customAdapter()
        listView!!.adapter = customAdapter
        listView!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val songName = listView!!.getItemAtPosition(position) as? String //for type casting use as? and datatype
            startActivity(
                Intent(applicationContext, PlayerActivity::class.java)
                    .putExtra("songs", mySongs)
                    .putExtra("songname", songName)
                    .putExtra("pos", position)
            )
        }
    }

    internal inner class customAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return items.size
        }

        override fun getItem(position: Int):Any {
            //return null
            return 0
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            //var view:View? = null
            val view = layoutInflater.inflate(R.layout.list_item, null)
            val txtSong = view.findViewById<TextView>(R.id.txtSong)
            txtSong.isSelected = true
            txtSong.text = items[position]
            return view
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search, menu)

        /*val searchViewItem = menu.findItem(R.id.search_bar)
        val searchView = MenuItemCompat.getActionView(searchViewItem) as SearchView

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener{

                override fun onQueryTextSubmit(query: String?): Boolean {
                    if(items!!.contains(query)){
                        customAdapter.filter.filter(query)

                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    customAdapter!!.filter.filter(newText)
                    return false
                }
            }
        )*/


        return super.onCreateOptionsMenu(menu)
    }
}
