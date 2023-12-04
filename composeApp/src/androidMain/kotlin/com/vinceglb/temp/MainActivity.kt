package com.vinceglb.temp

import App
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import com.vinceglb.temp.shared.SupabaseKeyConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supabase = createSupabaseClient(
            supabaseUrl = SupabaseKeyConfig.SupabaseUrl,
            supabaseKey = SupabaseKeyConfig.SupabaseKey,
        ) {
            install(Storage)
        }

        setContent {
            AndroidContent(supabase)
        }
    }
}

@OptIn(SupabaseExperimental::class)
@Composable
fun AndroidContent(supabase: SupabaseClient) {
    var showFilePicker by remember { mutableStateOf(false) }
    var showFilePickerCrash by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column {
        Button(onClick = { showFilePicker = true }) {
            Text(text = "Pick a file : ok")
        }
        Button(onClick = { showFilePickerCrash = true }) {
            Text(text = "Pick a file : crash")
        }

        if (loading) {
            CircularProgressIndicator()
        }

        FilePicker(show = showFilePicker, fileExtensions = listOf("jpg", "png")) { mpfile ->
            showFilePicker = false
            mpfile?.let {
                val uri = it.platformFile as Uri

                // Get bytes from uri
                val bytes = context.contentResolver.openInputStream(uri)?.use {
                    it.buffered().readBytes()
                } ?: return@FilePicker

                val bucketApi = supabase.storage["bug"]
                coroutineScope.launch {
                    loading = true
                    bucketApi.upload(
                        path = "android",
                        data = bytes,
                        upsert = true
                    )
                    loading = false
                }
            }
        }

        FilePicker(show = showFilePickerCrash, fileExtensions = listOf("jpg", "png")) { mpfile ->
            showFilePickerCrash = false
            mpfile?.let {
                val uri = it.platformFile as Uri

                val bucketApi = supabase.storage["bug"]
                coroutineScope.launch {
                    loading = true
                    bucketApi.upload(
                        path = "androidCrash",
                        uri = uri,
                        upsert = true
                    )
                    loading = false
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}