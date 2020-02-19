package com.hypertrack.android.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.messaging.RemoteMessage
import com.hypertrack.android.KEY_EXTRA_DELIVERY_ID
import com.hypertrack.android.UpdateStatusModel
import com.hypertrack.android.ui.JobDetailActivity
import com.hypertrack.android.ui.ListActivity
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.DeliveryStatusViewModel
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.HyperTrackMessagingService
import org.greenrobot.eventbus.EventBus

// Firebase service for the getting token and values
class FirebaseService : HyperTrackMessagingService(), LifecycleOwner {

    private lateinit var deliveryStatusUpdate: DeliveryStatusViewModel

    override fun getLifecycle(): Lifecycle {
        return mDispatcher.lifecycle
    }

    private val mDispatcher = ServiceLifecycleDispatcher(this)


    // add 'id' as static variable adn increase every new incoming notifications
    companion object {
        var id = 1
    }

    // on create Firebase service
    override fun onCreate() {
        super.onCreate()

        mDispatcher.onServicePreSuperOnCreate()

        deliveryStatusUpdate = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(DeliveryStatusViewModel::class.java)

    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        println("The Token value is -> $p0")
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        val getPushData = p0.data

        println("Message Received in FCM ${p0.data}")

        getPushData.apply {
            // Bypass fcm message if not contain delivery id because these pushed are only for HyperTrack Sdk
            if(!contains("delivery_id"))
                return

            val getDeliveryId = get("delivery_id")
            val getDeliveryStatus = get("status")//geofence_enter
            val getDeliveryLabel = get("label").toString()
            val getDeliveryTitle = get("title").toString()
            val getDeliveryMessage = get("message").toString()

            createNotification(this@FirebaseService, getDeliveryTitle, getDeliveryMessage,getDeliveryId!!)

            if (getDeliveryStatus == "geofence_enter") {
                // Fire Event Bus Subscriber to update particular delivery in {ListActivity::class.java}
                EventBus.getDefault().post(UpdateStatusModel(getDeliveryStatus, getDeliveryId!!))
                return
            }

            if (getDeliveryStatus == "geofence_exit") {

                println("Change Delivery Status Success with ${MyApplication.activity}")
                if (MyApplication.activity is ListActivity) {
                    println("Change Delivery Status Success if")
                    EventBus.getDefault().post(UpdateStatusModel(getDeliveryStatus, getDeliveryId!!))
                } else {
                    println("Change Delivery Status Success else ")
                   // deliveryStatusUpdate.callStatusMethod(getDeliveryId!!, "visit")
                }
            }
        }

    }


    // create notification when condition matches
    private fun createNotification(context: Context, title: String, message: String,deliveryId : String) {

        createNotificationChannel(context)

        // Create an Intent for the activity you want to start
        val resultIntent = Intent(this, JobDetailActivity::class.java)
        resultIntent.putExtra(KEY_EXTRA_DELIVERY_ID, deliveryId)

        // Create the TaskStackBuilder
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addParentStack(ListActivity::class.java)

            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = NotificationCompat.Builder(context, context.getString(R.string.channel_id))
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(resultPendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(id++, builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)

            val id = context.getString(R.string.channel_id)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}