import android.content.Context
import android.net.Uri

class Preferences(context: Context) {

    private val prefs = context.getSharedPreferences("bookreader_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FOLDERS = "scanned_folders"
        private const val KEY_FIRST_SCAN_DONE = "first_scan_done"
        private const val KEY_CONTINUE_READING = "continue_reading"
    }

    /* ------------------------------
       📂 Folder Handling
    ------------------------------ */

    fun addFolder(uri: Uri) {
        val savedUris = prefs.getStringSet(KEY_FOLDERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        savedUris.add(uri.toString())
        prefs.edit().putStringSet(KEY_FOLDERS, savedUris).apply()
    }

    fun removeFolder(uriString: String) {
        val savedUris = prefs.getStringSet(KEY_FOLDERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        savedUris.remove(uriString)
        prefs.edit().putStringSet(KEY_FOLDERS, savedUris).apply()
    }

    fun getFolders(): List<Uri> {
        val savedUris = prefs.getStringSet(KEY_FOLDERS, emptySet()) ?: emptySet()
        return savedUris.map { Uri.parse(it) }
    }

    fun clearFolders() {
        prefs.edit().remove(KEY_FOLDERS).apply()
    }

    /* ------------------------------
       🚀 First Install Scan
    ------------------------------ */

    fun isFirstScanDone(): Boolean {
        return prefs.getBoolean(KEY_FIRST_SCAN_DONE, false)
    }

    fun setFirstScanDone(done: Boolean) {
        prefs.edit().putBoolean(KEY_FIRST_SCAN_DONE, done).apply()
    }

    /* ------------------------------
       📖 Continue Reading
    ------------------------------ */

    fun addToContinueReading(uriString: String) {
        val saved = prefs.getStringSet(KEY_CONTINUE_READING, emptySet())?.toMutableSet() ?: mutableSetOf()
        saved.add(uriString)
        prefs.edit().putStringSet(KEY_CONTINUE_READING, saved).apply()
    }

    fun getContinueReading(): List<String> {
        return prefs.getStringSet(KEY_CONTINUE_READING, emptySet())?.toList() ?: emptyList()
    }

    /* ------------------------------
       📊 Reading Progress
    ------------------------------ */

    fun saveProgress(uriString: String, progress: Int) {
        prefs.edit().putInt("progress_$uriString", progress).apply()
    }

    fun getProgress(uriString: String): Int {
        return prefs.getInt("progress_$uriString", 0)
    }


}
