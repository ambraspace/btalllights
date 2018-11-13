# btalllights
Background service that maintains all lights and their statuses for Bticino MyHome system

This was made for a customer that required seeing all lights in the house and their statuses. He had problems with kids leaving lights on.

This would be helpful for large houses.

This service runs in the background and periodically check each light point. Furthermore, it monitors the activity of controls and updates lights' statuses.

This service uses SQLite DB created by atMyHome Android app (https://play.google.com/store/apps/details?id=com.devq.atmyhome).
