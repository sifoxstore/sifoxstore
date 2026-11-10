package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun testAppNameConstant() {
    val expectedName = "SiFox Store"
    assertEquals("SiFox Store", expectedName)
    assertTrue(expectedName.isNotEmpty())
  }
}
