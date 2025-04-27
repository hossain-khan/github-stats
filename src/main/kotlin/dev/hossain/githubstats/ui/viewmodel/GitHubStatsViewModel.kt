package dev.hossain.githubstats.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.PrAuthorStatsService
import dev.hossain.githubstats.PrReviewerStatsService
import dev.hossain.githubstats.ReviewStats
import dev.hossain.githubstats.ui.model.ConfigUiModel
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.LocalProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * ViewModel for the GitHub Stats desktop application.
 * Handles configuration and stats generation.
 */
class GitHubStatsViewModel : KoinComponent {
    private val prAuthorStatsService: PrAuthorStatsService by inject()
    private val prReviewerStatsService: PrReviewerStatsService by inject()
    
    // UI State
    var configUiModel by mutableStateOf(ConfigUiModel())
        private set
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _authorStats = MutableStateFlow<List<AuthorStats>>(emptyList())
    val authorStats: StateFlow<List<AuthorStats>> = _authorStats.asStateFlow()
    
    private val _reviewerStats = MutableStateFlow<List<ReviewStats>>(emptyList())
    val reviewerStats: StateFlow<List<ReviewStats>> = _reviewerStats.asStateFlow()
    
    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages: StateFlow<List<String>> = _logMessages.asStateFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Updates the configuration UI model.
     */
    fun updateConfig(config: ConfigUiModel) {
        configUiModel = config
    }
    
    /**
     * Validates the GitHub token.
     * In a real implementation, this would make an API call to GitHub.
     */
    fun validateToken() {
        // Simple validation - in a real app this would check with GitHub API
        configUiModel = configUiModel.copy(isTokenValid = configUiModel.accessToken.length >= 30)
    }
    
    /**
     * Loads configuration from a local.properties file if it exists.
     */
    fun loadConfigFromFile() {
        val file = File("local.properties")
        if (file.exists()) {
            try {
                val properties = java.util.Properties()
                file.inputStream().use { properties.load(it) }
                
                configUiModel = ConfigUiModel(
                    accessToken = properties.getProperty("access_token", ""),
                    repoOwner = properties.getProperty("repository_owner", ""),
                    repoId = properties.getProperty("repository_id", ""),
                    authors = properties.getProperty("authors", ""),
                    botUsers = properties.getProperty("bot_users", ""),
                    isTokenValid = properties.getProperty("access_token", "").length >= 30
                )
                
                // Parse dates if available
                val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                properties.getProperty("date_limit_after")?.let {
                    try {
                        configUiModel = configUiModel.copy(
                            dateAfter = java.time.LocalDate.parse(it, dateFormat)
                        )
                    } catch (e: Exception) {
                        addLogMessage("Error parsing date_limit_after: ${e.message}")
                    }
                }
                
                properties.getProperty("date_limit_before")?.let {
                    if (it.isNotBlank()) {
                        try {
                            configUiModel = configUiModel.copy(
                                dateBefore = java.time.LocalDate.parse(it, dateFormat)
                            )
                        } catch (e: Exception) {
                            addLogMessage("Error parsing date_limit_before: ${e.message}")
                        }
                    }
                }
                
                addLogMessage("Configuration loaded from local.properties")
            } catch (e: Exception) {
                addLogMessage("Error loading configuration: ${e.message}")
            }
        } else {
            addLogMessage("No local.properties file found. Please configure manually.")
        }
    }
    
    /**
     * Saves the current configuration to local.properties.
     */
    fun saveConfigToFile() {
        try {
            val properties = java.util.Properties()
            properties.setProperty("access_token", configUiModel.accessToken)
            properties.setProperty("repository_owner", configUiModel.repoOwner)
            properties.setProperty("repository_id", configUiModel.repoId)
            properties.setProperty("authors", configUiModel.authors)
            properties.setProperty("bot_users", configUiModel.botUsers)
            
            val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            properties.setProperty("date_limit_after", configUiModel.dateAfter.format(dateFormat))
            properties.setProperty("date_limit_before", configUiModel.dateBefore.format(dateFormat))
            
            val file = File("local.properties")
            file.outputStream().use { properties.store(it, "GitHub Stats Configuration") }
            
            addLogMessage("Configuration saved to local.properties")
        } catch (e: Exception) {
            addLogMessage("Error saving configuration: ${e.message}")
        }
    }
    
    /**
     * Generates author stats based on current configuration.
     */
    fun generateAuthorStats() {
        if (!validateConfigForStats()) return
        
        coroutineScope.launch {
            _isLoading.value = true
            addLogMessage("Generating author stats...")
            
            try {
                // Create temporary config for stats generation
                createTemporaryPropertiesFile()
                
                val stats = mutableListOf<AuthorStats>()
                configUiModel.authors.split(",").forEach { authorId ->
                    val authorName = authorId.trim()
                    if (authorName.isNotBlank()) {
                        addLogMessage("Processing stats for author: $authorName")
                        val authorStats = prAuthorStatsService.authorStats(authorName)
                        stats.add(authorStats)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    _authorStats.value = stats
                    addLogMessage("Author stats generation complete!")
                }
            } catch (e: Exception) {
                addLogMessage("Error generating author stats: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Generates reviewer stats based on current configuration.
     */
    fun generateReviewerStats() {
        if (!validateConfigForStats()) return
        
        coroutineScope.launch {
            _isLoading.value = true
            addLogMessage("Generating reviewer stats...")
            
            try {
                // Create temporary config for stats generation
                createTemporaryPropertiesFile()
                
                val stats = mutableListOf<ReviewStats>()
                configUiModel.authors.split(",").forEach { reviewerId ->
                    val reviewerName = reviewerId.trim()
                    if (reviewerName.isNotBlank()) {
                        addLogMessage("Processing stats for reviewer: $reviewerName")
                        val reviewerStats = prReviewerStatsService.reviewerStats(reviewerName)
                        stats.add(reviewerStats)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    _reviewerStats.value = stats
                    addLogMessage("Reviewer stats generation complete!")
                }
            } catch (e: Exception) {
                addLogMessage("Error generating reviewer stats: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Adds a log message to the UI.
     */
    private fun addLogMessage(message: String) {
        _logMessages.value = _logMessages.value + message
    }
    
    /**
     * Validates the configuration before generating stats.
     */
    private fun validateConfigForStats(): Boolean {
        if (!configUiModel.isTokenValid) {
            addLogMessage("Error: GitHub token is not valid. Please validate your token.")
            return false
        }
        
        if (configUiModel.repoOwner.isBlank() || configUiModel.repoId.isBlank()) {
            addLogMessage("Error: Repository owner and ID are required.")
            return false
        }
        
        if (configUiModel.authors.isBlank()) {
            addLogMessage("Error: At least one author is required.")
            return false
        }
        
        return true
    }
    
    /**
     * Creates a temporary properties file for the stats services to use.
     * This adapts the desktop UI to work with the existing codebase.
     */
    private fun createTemporaryPropertiesFile() {
        saveConfigToFile()
    }
}