import android.content.Context
import android.net.Uri


class Preferences(context: Context) {

    private val prefs = context.getSharedPreferences("bookreader_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FOLDERS = "scanned_folders"
        private const val KEY_FIRST_SCAN_DONE = "first_scan_done"
        private const val KEY_CONTINUE_READING = "continue_reading"
        private const val KEY_HISTORY = "reading_history"
        private const val KEY_SCANNED_BOOKS = "scanned_books"
        private const val KEY_LAST_SCAN_TIME = "last_scan_time"
    }

    /* ------------------------------
       Folder Handling
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
       First Install Scan
    ------------------------------ */

    fun isFirstScanDone(): Boolean {
        return prefs.getBoolean(KEY_FIRST_SCAN_DONE, false)
    }

    fun setFirstScanDone(done: Boolean) {
        prefs.edit().putBoolean(KEY_FIRST_SCAN_DONE, done).apply()
    }

    /* ------------------------------
        Continue Reading
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
        Reading Progress
    ------------------------------ */

    fun saveProgress(uriString: String, progress: Int) {
        prefs.edit().putInt("progress_$uriString", progress).apply()
    }

    fun getProgress(uriString: String): Int {
        return prefs.getInt("progress_$uriString", 0)
    }

    fun addToHistory(uriString: String, timestamp: Long) {
        val saved = prefs.getStringSet(KEY_HISTORY, emptySet())?.toMutableSet() ?: mutableSetOf()
        saved.add("$uriString|$timestamp")
        prefs.edit().putStringSet(KEY_HISTORY, saved).apply()
    }

    fun getHistory(): List<Pair<String, Long>> {
        val saved = prefs.getStringSet(KEY_HISTORY, emptySet()) ?: emptySet()
        return saved.mapNotNull {
            val parts = it.split("|")
            if (parts.size == 2) {
                parts[0] to (parts[1].toLongOrNull() ?: 0L)
            } else null
        }
    }

    fun saveGoal(goal: Int) {
        prefs.edit().putInt("reading_goal", goal).apply()
    }

    fun getGoal(): Int {
        return prefs.getInt("reading_goal", 10)
    }

    fun setFirstLaunchDone() {
        prefs.edit().putBoolean("first_launch_done", true).apply()
    }

    fun isFirstLaunch(): Boolean {
        return !prefs.getBoolean("first_launch_done", false)
    }

    // Save scanned book URIs persistently
    fun saveScannedBooks(uris: Set<String>) {
        prefs.edit().putStringSet(KEY_SCANNED_BOOKS, uris).apply()
    }

    // Get scanned book URIs
    fun getScannedBooks(): Set<String> {
        return prefs.getStringSet(KEY_SCANNED_BOOKS, emptySet()) ?: emptySet()
    }

    // Save last scan timestamp
    fun saveLastScanTime(time: Long) {
        prefs.edit().putLong(KEY_LAST_SCAN_TIME, time).apply()
    }

    // Get last scan timestamp
    fun getLastScanTime(): Long {
        return prefs.getLong(KEY_LAST_SCAN_TIME, 0L)
    }



}
