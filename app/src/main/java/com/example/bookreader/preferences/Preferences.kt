import android.content.Context
import android.net.Uri

class Preferences(context: Context) {

    private val prefs = context.getSharedPreferences("bookreader_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FOLDERS = "scanned_folders"
    }

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
}
