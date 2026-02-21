import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bookreader.R
import com.example.bookreader.presentation.home.HomeScreen
import com.example.bookreader.presentation.home.HomeViewModel
import com.example.bookreader.presentation.navigator.Screen
import com.example.bookreader.presentation.setting.SettingScreen

object GoalScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(onNavigate: ((Screen) -> Unit)) {

        val viewModel: HomeViewModel = viewModel()
        var goalInput
        by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(onClick = { onNavigate?.invoke(SettingScreen) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    },
                    windowInsets = WindowInsets(0)
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Set Your Reading Goal",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "How many books do you want to read?",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it },
                    label = { Text("Enter number of books") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Set Goal Button
                Button(
                    onClick = {
                        val newGoal = goalInput.toIntOrNull() ?: 0
                        viewModel.updateGoal(newGoal)
                        viewModel.markFirstLaunchDone()
                        onNavigate?.invoke(HomeScreen)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Set Goal")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Skip Button
                TextButton(
                    onClick = {
                        viewModel.markFirstLaunchDone()
                        onNavigate?.invoke(HomeScreen)
                    }
                ) {
                    Text("I'll set my goal later")
                }
            }
        }
    }
}
