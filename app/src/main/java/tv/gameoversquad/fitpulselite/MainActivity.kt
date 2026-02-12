package tv.gameoversquad.fitpulselite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import tv.gameoversquad.fitpulselite.ui.FitPulseLiteApp
import tv.gameoversquad.fitpulselite.ui.theme.FitPulseLiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitPulseLiteTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    FitPulseLiteApp()
                }
            }
        }
    }
}
