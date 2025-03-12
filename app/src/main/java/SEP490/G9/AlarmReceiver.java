//package SEP490.G9;
//
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.os.Build;
//
//import androidx.core.app.NotificationCompat;
//
//public class AlarmReceiver extends BroadcastReceiver {
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        // Thực hiện hành động khi báo thức kích hoạt, ví dụ: tạo thông báo
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        String channelId = "channel_id";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(channelId, "My Channel", NotificationManager.IMPORTANCE_HIGH);
//            notificationManager.createNotificationChannel(channel);
//        }
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
//                .setSmallIcon(R.drawable.ic_notification)
//                .setContentTitle("Lịch chơi Pickleball")
//                .setContentText("Sắp tới giờ chơi của bạn sau 30 phút nữa!")
//                .setPriority(NotificationCompat.PRIORITY_HIGH);
//
//        notificationManager.notify(1, builder.build());
//    }
//}
//
