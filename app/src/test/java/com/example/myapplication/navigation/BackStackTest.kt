package com.example.myapplication.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class BackStackTest {
    @Test fun popFromRemovesTheExpectedTopEntry() {
        val stack = mutableListOf("contracts", "settings")

        stack.popFrom("settings")

        assertEquals(listOf("contracts"), stack)
    }

    @Test fun popFromIgnoresAStaleCaller() {
        val stack = mutableListOf("contracts", "details")

        stack.popFrom("settings")

        assertEquals(listOf("contracts", "details"), stack)
    }

    @Test fun popFromWithZeroCountDoesNothing() {
        val stack = mutableListOf("contracts", "details")

        stack.popFrom("details", count = 0)

        assertEquals(listOf("contracts", "details"), stack)
    }

    @Test fun popFromAnEmptyStackDoesNothing() {
        val stack = mutableListOf<String>()

        stack.popFrom("settings")

        assertEquals(emptyList<String>(), stack)
    }

    @Test fun doubleBackFromSettingsRetainsRoot() {
        val stack = mutableListOf("contracts", "settings")
        repeat(2) { stack.popFrom("settings") }
        stack.popFrom("contracts")
        assertEquals(listOf("contracts"), stack)
    }

    @Test fun repeatedCallbackDoesNotPopParentScreen() {
        val stack = mutableListOf("contracts", "details", "bill")
        repeat(2) { stack.popFrom("bill") }
        assertEquals(listOf("contracts", "details"), stack)
    }

    @Test fun renewalCompletionRemovesTwoScreensOnlyOnce() {
        val stack = mutableListOf("contracts", "details", "renewal")
        repeat(2) { stack.popFrom("renewal", count = 2) }
        assertEquals(listOf("contracts"), stack)
    }

    @Test fun oversizedPopStillRetainsRoot() {
        val stack = mutableListOf("contracts", "settings")
        stack.popFrom("settings", count = 5)
        assertEquals(listOf("contracts"), stack)
    }
}
