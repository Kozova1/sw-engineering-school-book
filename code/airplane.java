public class AirplaneModeDuolingoNotificationSender
        extends BroadcastReceiver {
    private final Random rng = new Random();
    private final static String CHANNEL_ID = 
        "net.vogman.learnprogramming.airplaneModeNag";
    private final static String[] AIRPLANE_MESSAGES =
        new String[]{
            // Omitted because not relevant here
            ...
        };
    private boolean isInitialized = false;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // when airplane mode triggered
        boolean isAirplaneModeOn = intent
            .getExtras()
            .getBoolean("state");
        if (isAirplaneModeOn) {
            nagUser(context);
        }
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Airplane Mode Reminder";
            // de-indented to save space
            String description = 
    "Reminds the user to program every time they fly on an airplane";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            // Create Notification Channel
            // Required on Android O and above
            NotificationChannel channel =
                new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManagerCompat manager =
                NotificationManagerCompat.from(context);
            manager.createNotificationChannel(channel);
        }
        isInitialized = true;
    }
    
    private void nagUser(Context context) {
        // create if not created yet
        if (!isInitialized) {
            createNotificationChannel(context);
        }
        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(context, CHANNEL_ID);
        // If clicked, go to main activity
        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
        // Select random message
        String airplaneRandomMessage =
            AIRPLANE_MESSAGES[rng.nextInt(AIRPLANE_MESSAGES.length)];
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Airplane Mode Detected")
                .setContentText(airplaneRandomMessage)
                .setStyle(
                        new NotificationCompat
                        .BigTextStyle()
                        .bigText(airplaneRandomMessage)
                        )
                .setAutoCancel(true) // auto close if clicked
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // finally, send notification
        NotificationManagerCompat.from(context).notify(1, builder.build());
    }
}
