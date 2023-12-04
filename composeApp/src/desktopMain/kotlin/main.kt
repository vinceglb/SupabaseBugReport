import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import com.vinceglb.temp.shared.SupabaseKeyConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.launch
import java.io.File

fun main() = application {
    val supabase = createSupabaseClient(
        supabaseUrl = SupabaseKeyConfig.SupabaseUrl,
        supabaseKey = SupabaseKeyConfig.SupabaseKey,
    ) {
        install(Storage)
    }

    Window(onCloseRequest = ::exitApplication) {
        DesktopContent(supabase)
    }
}

@Composable
fun DesktopContent(supabase: SupabaseClient) {
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
                val file = it.platformFile as File
                val bucketApi = supabase.storage["bug"]
                coroutineScope.launch {
                    loading = true
                    bucketApi.upload(
                        path = "desktop",
                        file = file,
                        upsert = true
                    )
                    loading = false
                }
            }
        }
    }
}
