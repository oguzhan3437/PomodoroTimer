package com.oguzhancetin.pomodoro.util.Time

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.oguzhancetin.pomodoro.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


class TimerWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private var timer: CountDownTimer? = null
    }

    private var left = 0L

    override suspend fun doWork(): Result {
        val startTime = inputData.getLong("Time", 0)
        val leftTime = inputData.getLong("Left", 0)
        left = leftTime
        setForeground(createForegroundInfo("Pomodoro Running"))
        withContext(Dispatchers.IO) {
            repeat((leftTime / 1000).toInt()) {
                delay(1000)
                left -= 1000
                Log.e("kalan", "${leftTime / 60000} : ${(left % 60000) / 1000}")
                setProgress(workDataOf("Left" to left.toFloat() / startTime.toFloat()))
            }
        }

        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return super.getForegroundInfo()
    }

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val id = "chanel1"
        val title = "title1"
        val cancel = "cancel"
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())


        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }


        //tab intent
        val mainActivityIntent = Intent(applicationContext, MainActivity::class.java).also {
            it.action = Intent.ACTION_MAIN;
            it.addCategory(Intent.CATEGORY_LAUNCHER);
        }


        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setSilent(true)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setAutoCancel(false)
            .setSmallIcon(android.R.drawable.arrow_down_float)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .setContentIntent(pendingIntent)
            .build()

        return ForegroundInfo(123, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val name = "chanel1"
        val descriptionText = "chanel1"
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel("chanel1", name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }


}