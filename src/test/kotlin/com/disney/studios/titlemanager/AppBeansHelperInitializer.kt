package com.disney.studios.titlemanager

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext

class AppBeansHelperInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(applicationContext: GenericApplicationContext?) {
        appBeans().initialize(applicationContext!!)
    }

}