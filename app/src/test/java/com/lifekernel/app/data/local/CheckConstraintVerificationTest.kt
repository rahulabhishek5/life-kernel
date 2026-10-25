package com.lifekernel.app.data.local

import androidx.room.Entity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verification test for item DB-V2 per DATABASE_SCHEMA.md §15:
 * "Confirm no CHECK annotation exists in the chosen Room version, or adopt it consistently".
 */
class CheckConstraintVerificationTest {

    @Test
    fun roomEntityAnnotation_hasNoCheckConstraintParameter() {
        // Inspect the Entity annotation methods/parameters
        val entityMethods = Entity::class.java.declaredMethods.map { it.name }

        // Standard Room Entity parameters
        assertTrue("Entity must declare tableName", entityMethods.contains("tableName"))
        assertTrue("Entity must declare primaryKeys", entityMethods.contains("primaryKeys"))
        assertTrue("Entity must declare foreignKeys", entityMethods.contains("foreignKeys"))
        assertTrue("Entity must declare indices", entityMethods.contains("indices"))

        // Confirm absence of CHECK constraint declaration in Room Entity annotation
        assertFalse(
            "Room Entity annotation must not have a 'check' parameter in Room 2.6+",
            entityMethods.contains("check")
        )
        assertFalse(
            "Room Entity annotation must not have a 'checks' parameter in Room 2.6+",
            entityMethods.contains("checks")
        )
    }

    @Test
    fun roomPackage_hasNoCheckAnnotation() {
        val checkAnnotationExists = try {
            Class.forName("androidx.room.Check")
            true
        } catch (_: ClassNotFoundException) {
            false
        }

        assertFalse(
            "Room does not provide an androidx.room.Check annotation. " +
                "Per Decision DB-10, value ranges and state invariants are enforced in the domain layer.",
            checkAnnotationExists
        )
    }
}
