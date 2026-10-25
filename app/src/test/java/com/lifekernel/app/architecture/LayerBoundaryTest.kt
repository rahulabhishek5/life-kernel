package com.lifekernel.app.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Architectural invariant test (resolving question AQ-06).
 *
 * Invariant: The domain layer imports nothing from android.*, Compose, Room, or DataStore.
 * Domain logic must remain pure Kotlin and java.time only.
 */
class LayerBoundaryTest {

    companion object {
        val FORBIDDEN_IMPORT_PREFIXES = listOf(
            "import android.",
            "import androidx.compose.",
            "import androidx.room.",
            "import androidx.datastore.",
            "import androidx.lifecycle.",
            "import androidx.activity.",
            "import androidx.core.",
            "import androidx."
        )

        fun findViolationsInContent(fileName: String, content: String): List<String> {
            val violations = mutableListOf<String>()
            val lines = content.lines()
            for ((index, line) in lines.withIndex()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("import ")) {
                    val isForbidden = FORBIDDEN_IMPORT_PREFIXES.any { trimmed.startsWith(it) }
                    if (isForbidden) {
                        violations.add("$fileName:${index + 1}: $trimmed")
                    }
                }
            }
            return violations
        }
    }

    @Test
    fun domainLayer_hasNoForbiddenImports() {
        val domainDir = findDomainDirectory()
        assertTrue(
            "Domain source directory must exist (checked path: ${domainDir.path})",
            domainDir.exists()
        )

        val kotlinFiles = domainDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()

        val allViolations = mutableListOf<String>()
        for (file in kotlinFiles) {
            val violations = findViolationsInContent(file.name, file.readText())
            allViolations.addAll(violations)
        }

        assertTrue(
            "Domain layer invariant violated: domain/ must not import android.* or androidx.* (Compose, Room, DataStore, etc.). Violations found:\n" +
                allViolations.joinToString("\n"),
            allViolations.isEmpty()
        )
    }

    @Test
    fun boundaryChecker_flagsForbiddenImportsCorrectly() {
        val sampleWithViolations = """
            package com.lifekernel.app.domain.sample
            
            import android.os.Bundle
            import androidx.compose.runtime.Composable
            import androidx.room.Entity
            import androidx.datastore.preferences.core.Preferences
            import java.time.Instant
            import java.time.LocalDate
            
            class SampleModel(val date: LocalDate)
        """.trimIndent()

        val violations = findViolationsInContent("SampleModel.kt", sampleWithViolations)

        assertEquals(4, violations.size)
        assertTrue(violations.any { it.contains("android.os.Bundle") })
        assertTrue(violations.any { it.contains("androidx.compose.runtime.Composable") })
        assertTrue(violations.any { it.contains("androidx.room.Entity") })
        assertTrue(violations.any { it.contains("androidx.datastore.preferences.core.Preferences") })
    }

    @Test
    fun boundaryChecker_permitsPureKotlinAndJavaImports() {
        val sampleClean = """
            package com.lifekernel.app.domain.sample
            
            import java.time.Instant
            import java.time.LocalDate
            import kotlin.math.max
            
            data class PureModel(val timestamp: Instant, val day: LocalDate)
        """.trimIndent()

        val violations = findViolationsInContent("PureModel.kt", sampleClean)
        assertTrue(violations.isEmpty())
    }

    private fun findDomainDirectory(): File {
        val candidates = listOf(
            File("src/main/java/com/lifekernel/app/domain"),
            File("app/src/main/java/com/lifekernel/app/domain"),
            File("../app/src/main/java/com/lifekernel/app/domain")
        )
        val existing = candidates.map { it.canonicalFile }.firstOrNull { it.exists() }
        if (existing != null) return existing

        val mainJava = listOf(
            File("src/main/java/com/lifekernel/app"),
            File("app/src/main/java/com/lifekernel/app")
        ).map { it.canonicalFile }.firstOrNull { it.exists() }
            ?: File("app/src/main/java/com/lifekernel/app").canonicalFile

        val domain = File(mainJava, "domain")
        domain.mkdirs()
        return domain
    }
}
