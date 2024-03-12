const admin = require('firebase-admin');

// Initialize Firebase Admin with your project credentials
const serviceAccount = require('../cytocheck-push-service-firebase-adminsdk-q3plv-f5d4c4defc.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

function sendFirebaseNotification(token, title, body) {
  const message = {
    notification: {
      title: title,
      body: body
    },
    token: token
  };

  admin.messaging().send(message)
    .then((response) => {
      console.log('Successfully sent message:', response);
    })
    .catch((error) => {
      console.log('Error sending message:', error);
    });
}

module.exports = {
  sendFirebaseNotification
}