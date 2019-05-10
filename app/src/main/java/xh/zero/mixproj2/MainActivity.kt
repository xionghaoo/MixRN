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
        startActivity(intent)
    }

    fun startTaskPage(v: View) {
        startActivity(Intent(this@MainActivity, RNTaskActivity::class.java))
    }

    fun startDetailPage(v: View) {
        startActivity(Intent(this@MainActivity, DetailActivity::class.java))
    }

    fun download(v: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showPermissionRequestWithPermissionCheck()
        } else {
            downloadFile()
        }
    }

    private fun downloadFile() {
        val url = "https://oss.jinzhucaifu.com/others/app/config/homeIcom.zip"

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
                    //读取assets中的bundle，只在第一次更新时
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
            FileHelper.writeFile(filesDir.toString() + "/index.android.bundle", newBundle)
        } else {
            Logger.e("更新文件合并失败")
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

    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onPermissionDenied() {
//        JUtil.showToast(this@MainActivity, "未授予外部存储权限")
    }
    //6.0以上权限申请 end-----------------------------------------------------------------------
}
