package com.navin.glitterwall.activities

import android.Manifest
import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.navin.glitterwall.databinding.ActivityShowImageBinding
import com.navin.glitterwall.util.Font
import java.io.File
import java.io.IOException

class ShowImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowImageBinding
    private lateinit var bundle: Bundle
    private lateinit var wallpaper: String
    private lateinit var id: String
    private lateinit var wallUrl: String
    private lateinit var title: String

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            Font.showImageActivity(applicationContext, binding)
            bundle = intent.extras!!
            wallpaper = intent.getStringExtra("wall").toString()
            id = intent.getStringExtra("id").toString()
            wallUrl = intent.getStringExtra("wallUrl").toString()
            title = intent.getStringExtra("title").toString()

            Log.e("", "")

            Glide.with(applicationContext).load(wallpaper).into(img)

            btnDownload.setOnClickListener {
                downloadImg()
            }

            btnSetWallpaper.setOnClickListener {
                setWallpaper()
            }
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun downloadImg() {
        /** روش دانلود برای نسخه 13 به بعد اندروید*/
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, storagePermissions33, 1)
            } else {
                Toast.makeText(applicationContext, "شروع دانلود", Toast.LENGTH_LONG).show()
                Glide.with(this)
                    .asBitmap()
                    .load(wallUrl)
                    .into(object : CustomTarget<Bitmap>() { override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            val resolver = applicationContext.contentResolver
                            val contentValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, "$id.jpg")
                                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GlitterWall")
                            }
                            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                            val outputStream = resolver.openOutputStream(uri!!)
                            if (outputStream != null) {
                                resource.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            }
                            outputStream?.close()
                            Toast.makeText(applicationContext, "اتمام دانلود", Toast.LENGTH_LONG).show()
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
            }

        } else {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, storagePermissions, 1)
            } else {
                /** روش دانلود برای نسخه 10 اندروید*/
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    val request = DownloadManager.Request(Uri.parse(wallUrl))
                        .setTitle(title)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_PICTURES, "$id.jpg")
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)
                    val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    downloadManager.enqueue(request)
                } else {
                    /** روش دانلود برای دیگر نسخه های اندروید*/
                    val request = DownloadManager.Request(Uri.parse(wallUrl))
                        .setTitle(title)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "/GlitterWall/$id.jpg")
                    val downloadManager = applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    downloadManager.enqueue(request)
                }
            }
        }
    }

    private fun setWallpaper() {
        /**اگر اندروید بالاتر از 12 بود */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            /**ایجاد SharedPreferences برای ذخیره وضعیت بودن عکس در والپیپر */
            val prefs = getSharedPreferences("wallpaper", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            /**گرفتن آدرس عکس دانلود شده ی موجود در حافظه */
            val filePath = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/GlitterWall/" + id + ".jpg"
            val file = File(filePath)
            /**چک کردن موجودی همچین فایلی در حاغظه */
            if (file.exists()) {
                /**دادن فایل به متد decodeFile در صورت بودن */
                val bitmap = BitmapFactory.decodeFile(filePath)
                /**اگر عکسه دانلود شده به درستی و بدون مشکل دانلود شده بود و وجود داشت و قابل خواندن توسط bitmap بود(برابر با نال نبود)  */
                if (bitmap != null) {
                    /**ایجاد یک متغیر ازWallpaperManager */
                    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                    /**انجام بده */
                    try {
                        /**چک کردن وضعیت بودن عکس در والپیپر */
                        val currentWallpaperId = prefs.getString("currentWallpaperId", "")
                        /**اگر عکس در والپیپر بود */
                        if (currentWallpaperId == id) {
                            Toast.makeText(applicationContext, "این عکس هم اکنون در والپیپر شما ست است", Toast.LENGTH_SHORT).show()
                        } else {
                            /**اگر عکس در والپیپر نبود آن را ست کند */
                            wallpaperManager.setBitmap(bitmap)
                            Toast.makeText(applicationContext, "ست شد", Toast.LENGTH_LONG).show()
                            /**ذخیره وضعیت عکس موجود در والپیپر */
                            editor.putString("currentWallpaperId", id)
                            editor.apply()
                        }
                    } catch (e: Exception) {
                        /**مشکل در ست کردن : میتواند به هر دلیلی باشد(خطاا توسط متغیر e چک شود) */
                        Toast.makeText(applicationContext, "مشکل در ست: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("", "")
                    }
                } else {
                    /**اگر عکسه مورد نظر به درستی دانلود نشده بود و وجود نداشت و یا قابل خواندن توسط bitmap نبود(برابر با نال بود)  */
                    Toast.makeText(applicationContext, "عکسی یافت نشد مجدد دانلود کنید", Toast.LENGTH_SHORT).show()
                }
            } else {
                /**چک کردن موجودی همچین فایلی در حاغظه ->> اگر وجود نداشت */
                Toast.makeText(applicationContext, "ابتدا عکس دانلود شود سپس ست شود", Toast.LENGTH_LONG).show()
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            /**اگر اندروید برابر با 10 بود */
            /**ایجاد SharedPreferences برای ذخیره وضعیت بودن عکس در والپیپر */
            val prefs = getSharedPreferences("wallpaper", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            /**گرفتن آدرس عکس دانلود شده ی موجود در حافظه */
            val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "$id.jpg")
            /**چک کردن موجودی همچین فایلی در حاغظه */
            if (file.exists()) {
                /**دادن فایل به متد decodeFile در صورت بودن */
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                /**اگر عکسه دانلود شده به درستی و بدون مشکل دانلود شده بود و وجود داشت و قابل خواندن توسط bitmap بود(برابر با نال نبود)  */
                if (bitmap != null) {
                    /**ایجاد یک متغیر ازWallpaperManager */
                    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                    /**انجام بده */
                    try {
                        /**چک کردن وضعیت بودن عکس در والپیپر */
                        val currentWallpaperId = prefs.getString("currentWallpaperId", "")
                        /**اگر عکس در والپیپر بود */
                        if (currentWallpaperId == id) {
                            Toast.makeText(applicationContext, "این عکس هم اکنون در والپیپر شما ست است", Toast.LENGTH_SHORT).show()
                        } else {
                            /**اگر عکس در والپیپر نبود آن را ست کند */
                            wallpaperManager.setBitmap(bitmap)
                            Toast.makeText(applicationContext, "ست شد", Toast.LENGTH_SHORT).show()
                            /**ذخیره وضعیت عکس موجود در والپیپر */
                            editor.putString("currentWallpaperId", id)
                            editor.apply()
                        }
                    } catch (ex: IOException) {
                        /**مشکل در ست کردن : میتواند به هر دلیلی باشد(خطاا توسط متغیر e چک شود) */
                        ex.printStackTrace()
                        Log.e("", "")
                    }
                } else {
                    /**اگر عکسه مورد نظر به درستی دانلود نشده بود و وجود نداشت و یا قابل خواندن توسط bitmap نبود(برابر با نال بود)  */
                    Toast.makeText(applicationContext, "عکسی یافت نشد مجدد دانلود کنید", Toast.LENGTH_SHORT).show()
                }
            } else {
                /**چک کردن موجودی همچین فایلی در حاغظه ->> اگر وجود نداشت */
                Toast.makeText(applicationContext, "ابتدا عکس دانلود شود سپس ست شود", Toast.LENGTH_SHORT).show()
            }
        } else {
            /**ست برای دیگر نسخه های اندروید */
            /**ایجاد SharedPreferences برای ذخیره وضعیت بودن عکس در والپیپر */
            val prefs = getSharedPreferences("wallpaper", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            /**گرفتن آدرس عکس دانلود شده ی موجود در حافظه */
            val filePath =
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/GlitterWall/" + id + ".jpg"
            val file = File(filePath)
            /**چک کردن موجودی همچین فایلی در حاغظه */
            if (file.exists()) {
                /**دادن فایل به متد decodeFile در صورت بودن */
                val bitmap = BitmapFactory.decodeFile(filePath)
                /**اگر عکسه دانلود شده به درستی و بدون مشکل دانلود شده بود و وجود داشت و قابل خواندن توسط bitmap بود(برابر با نال نبود)  */
                if (bitmap != null) {
                    /**ایجاد یک متغیر ازWallpaperManager */
                    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                    /**انجام بده */
                    try {
                        /**چک کردن وضعیت بودن عکس در والپیپر */
                        val currentWallpaperId = prefs.getString("currentWallpaperId", "")
                        /**اگر عکس در والپیپر بود */
                        if (currentWallpaperId == id) {
                            Toast.makeText(applicationContext, "این عکس هم اکنون در والپیپر شما ست است", Toast.LENGTH_SHORT).show()
                        } else {
                            /**اگر عکس در والپیپر نبود آن را ست کند */
                            wallpaperManager.setBitmap(bitmap)
                            Toast.makeText(applicationContext, "ست شد", Toast.LENGTH_LONG).show()
                            /**ذخیره وضعیت عکس موجود در والپیپر */
                            editor.putString("currentWallpaperId", id)
                            editor.apply()
                        }

                    } catch (e: Exception) {
                        /**مشکل در ست کردن : میتواند به هر دلیلی باشد(خطاا توسط متغیر e چک شود) */
                        Toast.makeText(applicationContext, "مشکل در ست: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("", "")
                    }
                } else {
                    /**اگر عکسه مورد نظر به درستی دانلود نشده بود و وجود نداشت و یا قابل خواندن توسط bitmap نبود(برابر با نال بود)  */
                    Toast.makeText(applicationContext, "عکسی یافت نشد مجدد دانلود کنید", Toast.LENGTH_SHORT).show()
                }
            } else {
                /**چک کردن موجودی همچین فایلی در حاغظه ->> اگر وجود نداشت */
                Toast.makeText(applicationContext, "ابتدا عکس دانلود شود سپس ست شود", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
private var storagePermissions33 = arrayOf(
    Manifest.permission.READ_MEDIA_IMAGES)

private var storagePermissions = arrayOf(
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE)