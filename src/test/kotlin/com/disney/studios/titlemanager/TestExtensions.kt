package com.disney.studios.titlemanager

import org.junit.jupiter.api.DynamicTest
import java.text.SimpleDateFormat

fun should(name: String, test: () -> Unit) = DynamicTest.dynamicTest("should $name", test)
fun date(str: String) = SimpleDateFormat("yyyy-MM-dd").parse(str)