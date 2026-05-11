package dev.chuds.still.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRepositorySeedingTest {
    @Test
    fun empty_apps_emission_does_not_latch_first_launch() = runBlocking {
        val apps = MutableStateFlow<List<LaunchableApp>>(emptyList())
        val applied = mutableListOf<List<HomeSlot>>()
        var marked = false

        val deferred = async {
            seedFirstLaunchDefaults(
                settings = flowOf(LauncherSettings(firstLaunchCompleted = false)),
                apps = apps,
                resolve = { listOf(HomeSlot(index = 0, packageName = "p", className = "c")) },
                applyDefaults = { applied.add(it) },
                markComplete = { marked = true },
            )
        }

        val result = withTimeoutOrNull(50) { deferred.await() }

        assertNull(result)
        assertTrue(applied.isEmpty())
        assertFalse(marked)

        deferred.cancel()
    }

    @Test
    fun late_non_empty_apps_emission_applies_defaults() = runBlocking {
        val apps = MutableStateFlow<List<LaunchableApp>>(emptyList())
        val applied = mutableListOf<List<HomeSlot>>()
        var marked = false
        val expected = listOf(HomeSlot(index = 0, packageName = "p", className = "c"))
        val started = CompletableDeferred<Unit>()

        val deferred = async {
            started.complete(Unit)
            seedFirstLaunchDefaults(
                settings = flowOf(LauncherSettings(firstLaunchCompleted = false)),
                apps = apps,
                resolve = { expected },
                applyDefaults = { applied.add(it) },
                markComplete = { marked = true },
            )
        }

        started.await()
        // Let the seed coroutine subscribe to the empty StateFlow before we flip it.
        yield()
        apps.value = listOf(LaunchableApp(label = "x", packageName = "p", className = "c"))

        assertNotNull(withTimeoutOrNull(1000) { deferred.await() })
        assertEquals(listOf(expected), applied)
        assertFalse(marked)
    }

    @Test
    fun non_empty_apps_with_empty_defaults_marks_first_launch_complete() = runBlocking {
        val apps = MutableStateFlow(
            listOf(LaunchableApp(label = "x", packageName = "p", className = "c")),
        )
        val applied = mutableListOf<List<HomeSlot>>()
        var marked = false

        seedFirstLaunchDefaults(
            settings = flowOf(LauncherSettings(firstLaunchCompleted = false)),
            apps = apps,
            resolve = { emptyList() },
            applyDefaults = { applied.add(it) },
            markComplete = { marked = true },
        )

        assertTrue(applied.isEmpty())
        assertTrue(marked)
    }

    @Test
    fun already_completed_first_launch_is_a_no_op() = runBlocking {
        val apps = MutableStateFlow<List<LaunchableApp>>(emptyList())
        val applied = mutableListOf<List<HomeSlot>>()
        var marked = false
        var resolveCalls = 0

        seedFirstLaunchDefaults(
            settings = flowOf(LauncherSettings(firstLaunchCompleted = true)),
            apps = apps,
            resolve = {
                resolveCalls += 1
                emptyList()
            },
            applyDefaults = { applied.add(it) },
            markComplete = { marked = true },
        )

        assertEquals(0, resolveCalls)
        assertTrue(applied.isEmpty())
        assertFalse(marked)
    }
}
