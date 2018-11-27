package xh.zero.mixproj2

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import permissions.dispatcher.*
import xh.zero.mixproj2.Utils.FileHelper
import xh.zero.mixproj2.Utils.Logger
import xh.zero.mixproj2.Utils.ZipHelper
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

@RuntimePermissions
class MainActivity : AppCompatActivity() {

    companion object {
        const val COMPLETE_ID = 1
    }

    private val OVERLAY_PERMISSION_REQ_CODE = 1  // Choose any value
    private var mDownloadId: Long = 0
    private var hasNewBundle: Boolean = false

    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val competeId = intent?.getLongExtra(DownloadManager.ACTION_DOWNLOAD_COMPLETE, -1)
            Log.d("MPage", "received")
            if (competeId == mDownloadId) {
                Toast.makeText(this@MainActivity, "下载完成", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
            }
        }

        PRDownloader.initialize(applicationContext)

        registerReceiver(localReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onDestroy() {
        unregisterReceiver(localReceiver)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted
                }
            }
        }
    }

    fun start(v: View) {
        val intent = Intent(this@MainActivity, MyRNActivity::class.java)
        intent.putExtra(MyRNActivity.EXTRA_HAS_UPDATE_BUNDLE, hasNewBundle)
        startActivity(intent)
    }

    fun download(v: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showPermissionRequestWithPermissionCheck()
        } else {
            downloadFile()
        }
    }

    private fun downloadFile() {
//        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        val request = DownloadManager.Request(Uri.parse("https://oss.jinzhucaifu.com/others/app/config/homeIcom.zip"))
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
//        request.setTitle("test.zip")
//        request.setDescription("desc")
        val destFileName = externalCacheDir.toString() + File.separator + "test.zip"
//        request.setDestinationInExternalFilesDir(this@MainActivity, Environment.DIRECTORY_DOWNLOADS, "t")
//        mDownloadId = downloadManager.enqueue(request)
//        Log.d("MPage", "file: " + destFileName)
        val url = "https://oss.jinzhucaifu.com/others/app/config/homeIcom.zip"
//        DownloadTask().execute(url, destFileName)

        val updatePackagePath = File(FileHelper.getCacheDir(this@MainActivity), "update")
        val cachePath = FileHelper.getCacheDir(this@MainActivity).toString()
        if (!updatePackagePath.exists()) {
            updatePackagePath.mkdir()
        }

        PRDownloader.download(url, updatePackagePath.toString(), "test1.zip")
            .build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    Toast.makeText(this@MainActivity, "下载完成", Toast.LENGTH_SHORT).show()
                    //解压补丁包
                    ZipHelper.unzipFile(cachePath + "/patch.zip", updatePackagePath.toString())
                    //读取assets中的bundle
                    val oldBundle = FileHelper.readAssetsFile("index.android.bundle", this@MainActivity)
                    //读取解压完成的patch文件
                    val patch: String = FileHelper.readFile(updatePackagePath.toString() + "/_patch")
                    //生成新的bundle
                    merge(oldBundle, patch)
                }

                override fun onError(error: Error?) {
                    Toast.makeText(this@MainActivity, "下载失败", Toast.LENGTH_SHORT).show()
                }
            })

    }

    fun merge(oldBundle: String, patch: String) {
        val dmp = diff_match_patch()
        val patches = dmp.patch_fromText(patch) as LinkedList<diff_match_patch.Patch>
        val objs = dmp.patch_apply(patches, oldBundle)
        val newBundle = objs[0] as String
        val success = objs[1] as BooleanArray
        if (success[0]) {
            FileHelper.writeFile(FileHelper.getCacheDir(this@MainActivity).toString() + "/update/index.android.bundle", newBundle)
            hasNewBundle = true
        } else {
            Logger.e("更新文件合并失败")
            hasNewBundle = false
        }
    }

    class DownloadTask : AsyncTask<String, Int, Int>() {

        override fun doInBackground(vararg params: String?): Int {
            val url = params[0]
            val fileName = params[1]

            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.doOutput = false
                conn.setRequestProperty("Connection", "Keep-Alive")
                conn.setRequestProperty("Content-Type", "application/zip")
                conn.connect()

                val length = conn.contentLength
                Log.d("MPage", "start download, file length = " + length)
                val file = File(fileName)

                val inStream = conn.inputStream
                val fout = FileOutputStream(File(fileName))

                val buffer = ByteArray(1024)
                var progress = 0
                var byteSum = 0
                var byteRead = inStream.read()

                while (byteRead != -1) {
                    byteSum += byteRead
                    progress = (byteSum * 100L / length).toInt()
                    publishProgress(progress)
                    fout.write(buffer, 0, byteRead)
                    fout.flush()

                    byteRead = inStream.read()
                }
                conn.disconnect()
                inStream.close()
                fout.close()
                return 1
            } catch (e: Exception) {
                e.printStackTrace()
                return 0
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            Log.d("MPage", "download " + values[0] + "%")
        }

        override fun onPostExecute(result: Int) {
            if (result == 0) {
                Log.d("MPage", "download complete")
            } else {
                Log.d("MPage", "download failure")
            }
        }
    }

    //6.0以上权限申请 start-----------------------------------------------------------------------
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE)
    fun showRationaleForPermission(request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setMessage("尊敬的用户，为保证App的正常使用， 需要您授予相关权限。")
            .setPositiveButton("授予权限") { dialog, which ->
                request.proceed()
            }
            .setNegativeButton("取消") { dialog, which ->
                request.cancel()
            }
            .show()
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE)
    fun showPermissionRequest() {
       downloadFile()
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onPermissionNeverAskAgain() {
//        AlertDialog.Builder(this)
//                .setMessage("检测到您禁止了外部存储权限，为了正常使用App，请开启。")
//                .setPositiveButton("授予权限") { dialog, which ->
//                    request.proceed()
//                }
//                .setNegativeButton("取消") { dialog, which ->
//                    request.cancel()
//                }
//                .show()
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onPermissionDenied() {
//        JUtil.showToast(this@MainActivity, "未授予外部存储权限")
    }
    //6.0以上权限申请 end-----------------------------------------------------------------------
}
