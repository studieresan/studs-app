package se.studieresan.studs.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// TODO, this needs to be redone or removed
class MessagingService: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
//        val notification = remoteMessage?.notification ?: return
//        val data = remoteMessage.data
//        if (data["user"] == user?.uid) {
//            return
//        }
//        val intent = Intent(this, MainActivity::class.java)
//                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                .putExtra("locationKey", data["locationKey"])
//
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
//
//        val notificationBuilder = NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_location_on_black_24dp)
//                .setContentTitle(notification.title)
//                .setContentText(notification.body)
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                .setContentIntent(pendingIntent)
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

}
