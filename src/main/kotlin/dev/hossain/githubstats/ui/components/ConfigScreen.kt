package dev.hossain.githubstats.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.hossain.githubstats.ui.model.ConfigUiModel
import dev.hossain.githubstats.ui.viewmodel.GitHubStatsViewModel
import java.time.LocalDate

/**
 * Configuration screen for the GitHub Stats app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(viewModel: GitHubStatsViewModel) {
    val config = viewModel.configUiModel
    var showToken by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "GitHub Configuration",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Token section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "GitHub Authentication",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = config.accessToken,
                        onValueChange = { 
                            viewModel.updateConfig(config.copy(accessToken = it))
                        },
                        label = { Text("GitHub Personal Access Token") },
                        visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        singleLine = true
                    )
                    
                    Switch(
                        checked = showToken,
                        onCheckedChange = { showToken = it },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    Button(
                        onClick = { viewModel.validateToken() },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Validate")
                        Spacer(Modifier.width(4.dp))
                        Text("Validate")
                    }
                }
                
                if (config.isTokenValid) {
                    Text(
                        "Token is valid!",
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        
        // Repository section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Repository Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = config.repoOwner,
                        onValueChange = { 
                            viewModel.updateConfig(config.copy(repoOwner = it))
                        },
                        label = { Text("Repository Owner") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = config.repoId,
                        onValueChange = { 
                            viewModel.updateConfig(config.copy(repoId = it))
                        },
                        label = { Text("Repository ID") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        singleLine = true
                    )
                }
            }
        }
        
        // User Configuration
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "User Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = config.authors,
                    onValueChange = { 
                        viewModel.updateConfig(config.copy(authors = it))
                    },
                    label = { Text("Authors (comma-separated)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = config.botUsers,
                    onValueChange = { 
                        viewModel.updateConfig(config.copy(botUsers = it))
                    },
                    label = { Text("Bot Users to Exclude (comma-separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        
        // Date Configuration
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Date Range Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    DatePickerField(
                        label = "Start Date",
                        date = config.dateAfter,
                        onDateChange = { 
                            viewModel.updateConfig(config.copy(dateAfter = it))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    
                    DatePickerField(
                        label = "End Date",
                        date = config.dateBefore,
                        onDateChange = { 
                            viewModel.updateConfig(config.copy(dateBefore = it))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }
            }
        }
        
        // Buttons
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.saveConfigToFile() },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
                Spacer(Modifier.width(4.dp))
                Text("Save Configuration")
            }
        }
    }
}

/**
 * Custom date picker field for selecting dates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = date.toString(),
        onValueChange = { /* We'll use the date picker instead */ },
        label = { Text(label) },
        modifier = modifier,
        readOnly = true,
        singleLine = true,
        onClick = { showDatePicker = true }
    )
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toEpochDay() * 24 * 60 * 60 * 1000
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onDateChange(newDate)
                    }
                    showDatePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}