var functions = require('firebase-functions');
var admin = require('firebase-admin');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

var topic = 'locations';

exports.notifyLocation = functions.database.ref('/locations')
  .onWrite(event => {
    if (event.data.previous.exists()) {
      return;
    }
    var key = event.data.key;
    var payload = {
      notification: {
        title: 'Somebody shared a location',
        body: 'Tap to view',
        clickAction: 'location'
      },
      data: {
        locationKey: key
      }
    };
    return admin.messaging().sendToTopic(topic, payload);
  });
