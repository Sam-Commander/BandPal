const functions = require('firebase-functions');

const admin = require('firebase-admin');

admin.initializeApp();

exports.sendNotification = functions.database.ref('/Notification/{notification_id}')
    .onCreate((snapshot, context) => {

        const text = snapshot.val().text;
        const receiver_id = snapshot.val().receiver_id;
        const title = snapshot.val().title;

        return admin.database().ref('/tokens/' + receiver_id).once('value').then(function (snapshot) {
            const receiver_token = snapshot.val();

            const payload = {
                notification: {
                    title: title,
                    body: text,
                    icon: 'default',
                    click_action: 'com.sam.tippy'
                }
            };

            return admin.messaging().sendToDevice(receiver_token, payload);
        });
    });
