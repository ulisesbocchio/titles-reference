package com.disney.studios.titlemanager

import java.text.SimpleDateFormat

fun should(name: String, test: () -> Unit) = dynamicTest(name, test)
fun date(str: String) = SimpleDateFormat("yyyy-MM-dd").parse(str)