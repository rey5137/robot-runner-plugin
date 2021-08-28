package com.github.rey5137.robotrunnerplugin.http

import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Semaphore

/**
 * @author Gong Zhang
 */
internal class TimeoutExecutor(private val timeout: Long,
                               private val baseExecutor: Executor?) : Executor {
    @Throws(TimeoutException::class)
    override fun execute(task: Runnable) {
        val semaphore = Semaphore(0)
        val isTimeout = booleanArrayOf(false)
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                isTimeout[0] = true
                semaphore.release()
            }
        }, timeout)
        val runnable = Runnable {
            try {
                task.run()
            } finally {
                semaphore.release()
            }
        }
        if (baseExecutor != null) {
            baseExecutor.execute(runnable)
        } else {
            Thread(runnable).start()
        }
        try {
            semaphore.acquire()
            if (isTimeout[0]) {
                throw TimeoutException()
            }
        } catch (ignored: InterruptedException) {
        } finally {
            timer.cancel()
        }
    }
}