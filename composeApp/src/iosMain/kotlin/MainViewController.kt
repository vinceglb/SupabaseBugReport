import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import com.vinceglb.temp.shared.SupabaseKeyConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import kotlinx.coroutines.launch
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL

fun MainViewController() = ComposeUIViewController { IOSContent() }

@OptIn(ExperimentalForeignApi::class)
@Composable
fun IOSContent() {
    val supabase = remember {
        createSupabaseClient(
            supabaseUrl = SupabaseKeyConfig.SupabaseUrl,
            supabaseKey = SupabaseKeyConfig.SupabaseKey,
        ) {
            install(Storage)
        }
    }

    var showFilePicker by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column {
        Button(onClick = { showFilePicker = true }) {
            Text(text = "Pick a file")
        }
        if (loading) {
            CircularProgressIndicator()
        }

        FilePicker(show = showFilePicker, fileExtensions = listOf("jpg", "png")) { mpfile ->
            showFilePicker = false
            mpfile?.let {
                // Get the url
                val nsUrl = it.platformFile as NSURL

                // Get the bytes
                val bytes = NSData.dataWithContentsOfURL(nsUrl)?.let { nsData ->
                    nsData.bytes?.readBytes(nsData.length.toInt())
                } ?: byteArrayOf()

                // Create the upload file
                val bucketApi = supabase.storage["bug"]

                // Upload file
                coroutineScope.launch {
                    loading = true
                    bucketApi.upload(
                        path = "ios",
                        data = bytes,
                        upsert = true
                    )
                    loading = false
                }
            }
        }
    }
}
