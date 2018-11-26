package xh.zero.mixproj2.Utils

import android.content.Context
import android.os.Environment
import android.widget.TextView
import java.io.*

class FileHelper {
    companion object {
        fun readConfiguration(file: String, context: Context): String {
//            var inS = inputStream
//            val inputStream = FileInputStream(file)
            val result = StringBuffer()
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(InputStreamReader(context.assets.open(file)))
                var line = reader.readLine()
                while (line != null) {
                    result.append(line).append("\n")
                    line = reader.readLine()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    if (reader != null) {
                        reader.close()
                    }
//                    if (inputStream != null) {
//                        inputStream.close()
//                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            return result.toString()
        }

        fun getDownloadPath(context: Context) : File {
            val externalCachePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val innerCachePath = context.cacheDir
            return if (externalCachePath == null) innerCachePath else externalCachePath
        }

        fun getCacheFolderSize(context: Context) : Long {
            val cacheFile = File(context.cacheDir, ".")
            val outCacheFile = File(context.externalCacheDir, ".")

            val innerCache = getFileSize(cacheFile)
            val outerCache = getFileSize(outCacheFile)

            return innerCache + outerCache
        }

        private fun getFileSize(file: File) : Long {
            var size: Long = 0
            if (file.isDirectory) {

                for (f in file.listFiles()) {
                    size += getFileSize(f)
                }
            } else {
                size += file.length()
            }

            return size
        }

        fun clearCacheFolder(context: Context) {
            val cacheFile = File(context.cacheDir, ".")
            val outCacheFile = File(context.externalCacheDir, ".")
            deleteFile(cacheFile)
            deleteFile(outCacheFile)
        }

        private fun deleteFile(file: File) {
            if (file.isDirectory) {
                for (f in file.listFiles()) {
                    deleteFile(f)
                }
            }
            file.delete()
        }
    }
}