package com.oguzhancetin.pomodoro.util.Time

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.oguzhancetin.pomodoro.util.Times
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


object WorkUtil {

    var timerIsRunning = MutableStateFlow(false)
    var runningTimeType: MutableStateFlow<Times> = MutableStateFlow(Times.Pomodoro())

    /**
     * Time is cached when timer is stop, if timer type change or restart same time type
     * cached will be cleared
     */
    var cachedTime: Long? = runningTimeType.value.time

    /**
     * Get percentage version of cached time
     * Ex 5_000 / 15_000 => 0.3f
     */
    private val cachedTimePercentage
        get() = cachedTime?.toFloat()?.div(runningTimeType.value.time.toFloat())


    /**
     * Hold progress between (0.0f - 1.0f)
     */
    var progress = MediatorLiveData(1f)


    private var request: OneTimeWorkRequest? = null
    private val workRequestBuilder = OneTimeWorkRequestBuilder<TimerWorker>()


    fun stopTimer(context: Application) {
        cachedTime = (progress.value?.times(runningTimeType.value.time))?.toLong()
        request?.id?.let {
            WorkManager.getInstance(context).cancelAllWork()
        }
        timerIsRunning.value = false
    }

    fun restart(context: Application) {
        cachedTime = runningTimeType.value.time
        request?.id?.let {
            WorkManager.getInstance(context).cancelAllWork()
        }
        startTime(context = context)
    }

    /**
     * Start timer according to selected timer
     */
    fun startTime(context: Application) {
        timerIsRunning.value = true
        request = workRequestBuilder.setInputData(
            workDataOf(
                "Time" to runningTimeType.value.time,
                "Left" to (cachedTime ?: runningTimeType.value.time)
            )
        ).build()
        request?.let { request ->

            WorkManager.getInstance(context).enqueue(request)

            val translatedProgress: LiveData<Float> = Transformations.switchMap(
                WorkManager.getInstance(context).getWorkInfoByIdLiveData(request.id)
            ) { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> {
                        timerIsRunning.value = true
                        return@switchMap MutableLiveData(cachedTimePercentage)
                    }
                    WorkInfo.State.RUNNING -> {

                        val progress = workInfo?.progress?.getFloat(
                            "Left",
                            cachedTimePercentage ?: 1f

                        )

                        return@switchMap MutableLiveData(progress)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        val progress = workInfo?.progress?.getFloat(
                            "Left", 1f
                        ) ?: -1f
                        timerIsRunning.value = false
                        showFinishNotification()
                        return@switchMap MutableLiveData(progress)
                    }
                    else -> {
                        return@switchMap MutableLiveData(
                            cachedTimePercentage
                        )
                    }
                }
            }

            progress.addSource(translatedProgress) {
                Log.e("Time", it.toString())
                progress.value = it
            }
        }
    }

    private fun showFinishNotification() {

    }

    fun changeCurrentTime(time: Times, context: Application) {
        stopTimer(context)
        runningTimeType.value = time
        cachedTime = runningTimeType.value.time
    }

    fun Long.getMinute(): Long {
        return this / 60000
    }

}