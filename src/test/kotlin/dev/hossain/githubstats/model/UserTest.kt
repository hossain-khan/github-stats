package dev.hossain.githubstats.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class UserTest {
    @Test
    fun `User should store all provided values`() {
        val user = User(
            login = "testuser",
            type = "User",
            url = "https://api.github.com/users/testuser",
            html_url = "https://github.com/testuser",
            avatar_url = "https://avatars.githubusercontent.com/u/12345?v=4",
            id = 12345L,
            repos_url = "https://api.github.com/users/testuser/repos"
        )

        assertThat(user.login).isEqualTo("testuser")
        assertThat(user.type).isEqualTo("User")
        assertThat(user.url).isEqualTo("https://api.github.com/users/testuser")
        assertThat(user.html_url).isEqualTo("https://github.com/testuser")
        assertThat(user.avatar_url).isEqualTo("https://avatars.githubusercontent.com/u/12345?v=4")
        assertThat(user.id).isEqualTo(12345L)
        assertThat(user.repos_url).isEqualTo("https://api.github.com/users/testuser/repos")
    }

    @Test
    fun `User toString should return expected format`() {
        val user = User(
            login = "testuser",
            type = "User",
            url = "https://api.github.com/users/testuser",
            html_url = "https://github.com/testuser",
            avatar_url = "https://avatars.githubusercontent.com/u/12345?v=4",
            id = 12345L,
            repos_url = "https://api.github.com/users/testuser/repos"
        )
        assertThat(user.toString()).isEqualTo("User: testuser")
    }

    @Test
    fun `User should handle null values for optional fields`() {
        val user = User(
            login = "anotheruser",
            type = null,
            url = null,
            html_url = null,
            avatar_url = null,
            id = 67890L,
            repos_url = null
        )

        assertThat(user.login).isEqualTo("anotheruser")
        assertThat(user.type).isNull()
        assertThat(user.url).isNull()
        assertThat(user.html_url).isNull()
        assertThat(user.avatar_url).isNull()
        assertThat(user.id).isEqualTo(67890L)
        assertThat(user.repos_url).isNull()
    }
}
