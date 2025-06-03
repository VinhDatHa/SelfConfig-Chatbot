package io.curri.dictionary.chatbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.curri.dictionary.chatbot.app.App
import io.curri.dictionary.chatbot.components.ui.FormItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}


@Preview(showBackground = true)
@Composable
private fun FormItemPreview() {
    FormItem(
        label = { Text("Label") },
        content = {
            OutlinedTextField(
                value = "",
                onValueChange = {}
            )
        }
    )
}