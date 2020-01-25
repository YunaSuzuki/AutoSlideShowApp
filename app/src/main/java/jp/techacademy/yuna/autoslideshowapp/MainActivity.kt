package jp.techacademy.yuna.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.os.Handler
import android.view.View
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100

    //タイマー用
    private var mTimer: Timer? = null
    private var mHandler = Handler()


    //cursor定義
    private var cursor: Cursor? = null

    //初回ユーザーのパーミッション許可求め
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else{
                    next_button.isClickable = false
                    previous_button.isClickable = false
                    slideshow_button.isClickable = false
                }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                //データベース上の検索結果を格納するのがcorsor
                // 画像の情報を取得する
                val resolver = contentResolver //データを参照するためのクラス
                cursor = resolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類。外部ストレージの画像を指定
                    null, // 項目(null = 全項目)
                    null, // フィルタ条件(null = フィルタなし)
                    null, // フィルタ用パラメータ
                    null // ソート (null ソートなし)
                )
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)

            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }


        //イベントリスナー登録
        previous_button.setOnClickListener(this)
        next_button.setOnClickListener(this)
        //スライドショー
        slideshow_button.setOnClickListener(this)

    }



    override fun onClick(v: View) {


        if (v.id == R.id.previous_button) {
            if (cursor!!.moveToPrevious()) {

                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView1.setImageURI(imageUri)
            } else if (cursor!!.moveToLast()){
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView1.setImageURI(imageUri)
            }

        } else if(v.id == R.id.next_button){

            if(cursor!!.moveToNext()){
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView1.setImageURI(imageUri)
            } else if(cursor!!.moveToFirst()){
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView1.setImageURI(imageUri)
            }

        } else if(v.id == R.id.slideshow_button){
            //他のボタンをタップ不可にする
            previous_button.isClickable = false
            next_button.isClickable = false

            //タイマー
            if(mTimer == null){
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        //2秒ループの中での処理
                        if(cursor!!.moveToNext()){
                            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                            val id = cursor!!.getLong(fieldIndex)
                            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                            mHandler.post {
                                imageView1.setImageURI(imageUri)
                                slideshow_button.text = "停止"
                            }
                        } else if(cursor!!.moveToFirst()){
                            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                            val id = cursor!!.getLong(fieldIndex)
                            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                            mHandler.post {
                                imageView1.setImageURI(imageUri)
                            }
                        }
                    }
                }, 100, 2000)
            }
            else if (mTimer != null) {
                mTimer!!.cancel()
                mTimer = null
                slideshow_button.text = "再生"
                //他のボタンをタップ可に戻す
                previous_button.isClickable = true
                next_button.isClickable = true
            }

        }
    }


        //初期１回のみ
    private fun getContentsInfo() {

        val resolver = contentResolver //データを参照するためのクラス
         cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類。外部ストレージの画像を指定
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor!!.moveToFirst()) {

            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor!!.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            imageView1.setImageURI(imageUri)
        } else if(cursor!!.moveToNext()){
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor!!.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            imageView1.setImageURI(imageUri)
        }


    }




}
