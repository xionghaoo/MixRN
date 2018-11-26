package xh.zero.mixproj2.Utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ZipHelper {
    companion object {
        private const val BUFFER_SIZE = 1024

        //https://oss.jinzhucaifu.com/others/app/config/homeIcom.zip
        fun unzip(zipUrl: String, context: Context, callback: UnzipCallback) {
            val iconPath = File(context.filesDir, "icons")
            if (!iconPath.exists()) {
                iconPath.mkdir()
            }

            val zipDownloadPath = File(context.cacheDir, "")
            if (!zipDownloadPath.exists()) {
                zipDownloadPath.mkdir()
            }

            Logger.d("iconPath: " + iconPath.absolutePath)

            Thread {
                try {
                    unzipFile(File(downloadZipFile(zipUrl, zipDownloadPath.absolutePath)).absolutePath, iconPath.absolutePath)
                    callback.unzipComplete()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }

        private fun downloadZipFile(fileUrl: String, saveDir: String) : String {
            var saveFilePath: String = ""
            val url = URL(fileUrl)
            val httpConn = url.openConnection() as HttpURLConnection
            val responseCode = httpConn.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                var fileName: String = ""
                val disposition = httpConn.getHeaderField("Content-Disposition")
                val contentType = httpConn.contentLength



                if (disposition != null) {
                    val index = disposition.indexOf("filename=")
                    if (index > 0) {
                        fileName = disposition.substring(index + 9, disposition.length)
                    }
                } else {
                    fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1, fileUrl.length)
                }

                val inputStream = httpConn.inputStream
                saveFilePath = saveDir + File.separator + fileName

                Logger.d("saveFilePath: " + saveFilePath)

                val os = FileOutputStream(saveFilePath)

                var bytesRead = -1
                val buffer = ByteArray(BUFFER_SIZE)
                bytesRead = inputStream.read(buffer)
                while (bytesRead != -1) {
                    os.write(buffer, 0, bytesRead)
                    bytesRead = inputStream.read(buffer)
                }

                inputStream.close()
                os.close()
            }
            httpConn.disconnect()

            return saveFilePath
        }

        private fun unzipFile(zipFile: String, location: String) {
            try {
                val fin = FileInputStream(zipFile)
                val zin = ZipInputStream(fin)
                var ze: ZipEntry? = zin.nextEntry
                while (ze != null) {
                    if (ze.isDirectory) {
                        dirChecker(location, ze.name)
                    } else {
                        val filePath = File(location + File.separator + ze.name)

                        val fout = FileOutputStream(filePath)

                        val buffer = ByteArray(8192)
                        var len: Int = zin.read(buffer)
                        while (len != -1) {
                            fout.write(buffer, 0, len)
                            len = zin.read(buffer)
                        }
                        fout.close()
                        zin.closeEntry()
                    }
                    ze = zin.nextEntry
                }
                zin.close()
            } catch (e: Exception) {
                Log.e("Decompress", "unzip", e)
            }

        }

        private fun dirChecker(location: String, dir: String) {
            val f = File(location + File.separator + dir)
            if (!f.isDirectory) {
                f.mkdirs()
            }
        }

    }

    interface UnzipCallback {
        fun unzipComplete()
    }
}