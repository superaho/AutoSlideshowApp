package jp.techacademy.taiki.hamaguchi.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100

    var mTimer: Timer? = null
    //var cursor: Cursor? = null

    // タイマー用の時間のための変数
    var index = 0

    var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                    getContentsInfo(index)
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo(index)
        }

        /*if (cursor != null) {
            Log.d("count", cursor!!.count.toString())
        }*/

        startstop_button.setOnClickListener {
            if (mTimer == null){
                // タイマーの作成
                mTimer = Timer()
                mTimer!!.schedule(0,2000) {
                    index ++
                    if (index >= getCorsorCount()) {
                        index = 0
                    }

                    mHandler.post {
                        getContentsInfo(index)
                        startstop_button.text = "停止"
                        next_button.isClickable = false
                        back_button.isClickable = false
                    }
                }
            } else {
                mTimer!!.cancel()
                mTimer = null
                startstop_button.text = "再生"
                next_button.isClickable = true
                back_button.isClickable = true
            }
        }

        next_button.setOnClickListener {
            index ++
            if (index >= getCorsorCount()) {
                index = 0
            }
            getContentsInfo(index)
        }

        back_button.setOnClickListener {
            if (getCorsorCount() != 0) {
                if (index == 0) {
                    index = getCorsorCount() - 1
                } else {
                    index --
                }
                getContentsInfo(index)
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo(index)
                } else {
                    startstop_button.isClickable = false
                    next_button.isClickable = false
                    back_button.isClickable = false
                }
        }
    }

    private fun getCorsorCount(): Int {
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        val count = cursor!!.count
        cursor.close()
        return  count
    }

    private fun getContentsInfo(index: Int) {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        if (cursor!!.count != 0 ) {
            cursor!!.moveToPosition(index)
            //if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            imageView.setImageURI(imageUri)
            //}
        }
        cursor.close()
    }
}