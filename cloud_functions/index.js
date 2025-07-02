/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {setGlobalOptions} = require("firebase-functions");
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const serviceAccount = require("../config/serviceAccountKey.json");
admin.initializeApp();

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({maxInstances: 10});

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

exports.fixUsersSchema = functions.https.onRequest(async (req, res) => {
  const usersRef = admin.firestore().collection("users");
  const snapshot = await usersRef.get();
  const batch = admin.firestore().batch();

  snapshot.forEach((doc) => {
    const data = doc.data();
    const updateData = {};
    // 检查并补齐字段
    if (data.email === undefined) updateData.email = "";
    if (data.nickname === undefined) updateData.nickname = "";
    if (data.avatar === undefined) updateData.avatar = "";
    if (data.phone === undefined) updateData.phone = "";
    if (data.gender === undefined) updateData.gender = "";
    if (data.birthday === undefined) updateData.birthday = "";
    if (data.status === undefined) updateData.status = "启用";
    if (data.sleepStatus === undefined) updateData.sleepStatus = "未知";
    if (data.points === undefined) updateData.points = 0;
    if (data.sleepPointsToday === undefined) {
      updateData.sleepPointsToday = 0;
    }
    if (data.streak === undefined) updateData.streak = 0;
    if (data.registerTime === undefined) {
      updateData.registerTime = admin.firestore.FieldValue.serverTimestamp();
    }
    if (data.lastLoginTime === undefined) updateData.lastLoginTime = null;
    if (data.extraInfo === undefined) updateData.extraInfo = {};
    if (data.updateTime === undefined) {
      updateData.updateTime = admin.firestore.FieldValue.serverTimestamp();
    }
    if (Object.keys(updateData).length > 0) {
      batch.update(doc.ref, updateData);
    }
  });

  await batch.commit();
  res.send("users集合字段已补齐");
});
