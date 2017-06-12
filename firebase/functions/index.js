var functions = require('firebase-functions');
var admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

var topic = 'locations';

exports.notifyLocation = functions.database.ref('/locations/{key}')
  .onWrite(event => {
    if (event.data.previous.exists()) {
      return;
    }
    var key = event.data.key;
    var user = event.data.val().user;
    var payload = {
      notification: {
        title: 'Somebody shared a location',
        body: 'Tap to view'
      },
      data: {
        locationKey: key,
        user: user
      }
    };
    var options = {
      priority: 'high',
      timeToLive: 60*60
    };
    return admin.messaging().sendToTopic(topic, payload, options);
  });
