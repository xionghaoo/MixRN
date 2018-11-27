package xh.zero.mixproj2.Utils

import android.content.Context
import android.os.Environment
import android.widget.TextView
import java.io.*



class FileHelper {
    companion object {
        fun readAssetsFile(file: String, context: Context): String {
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

        fun writeFile(file: String, content: String) {
            var fileWriter: FileWriter? = null
            var bufferedWriter: BufferedWriter? = null
            try {
                fileWriter = FileWriter(file)
                bufferedWriter = BufferedWriter(fileWriter)
                bufferedWriter.write(content)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                bufferedWriter?.close()
                fileWriter?.close()
            }
        }

        fun readFile(filename: String): String {
            // Read a file from disk and return the text contents.
            val sb = StringBuilder()
            var input: FileReader? = null
            var bufRead: BufferedReader? = null
            try {
                input = FileReader(filename)
                bufRead = BufferedReader(input)
                var line = bufRead.readLine()
                while (line != null) {
                    sb.append(line).append('\n')
                    line = bufRead.readLine()
                }
            } finally {
                bufRead?.close()
                input?.close()
            }
            return sb.toString()
        }


        fun getCacheDir(context: Context) : File {
            val externalCachePath = context.externalCacheDir
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