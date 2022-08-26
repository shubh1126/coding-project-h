package utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.companionObject

private fun <T : Any> logger(forClass: Class<T>): Logger {

    fun Logger.debug(s: () -> String) {
        if (isDebugEnabled) debug(s())
    }

    return LoggerFactory.getLogger(unwrapCompanionClass(forClass).name)
}

private fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return if (ofClass.enclosingClass != null && ofClass.enclosingClass.kotlin.companionObject?.java == ofClass) {
        ofClass.enclosingClass
    } else {
        ofClass
    }
}

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { logger(this.javaClass) }
}
