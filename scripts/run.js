const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.fixUsersSchema = functions.https.onRequest(async (req, res) => {
  const usersRef = admin.firestore().collection('users');
  const snapshot = await usersRef.get();
  const batch = admin.firestore().batch();

  snapshot.forEach(doc => {
    const data = doc.data();
    const updateData = {};

    // 检查并补齐字段
    if (data.email === undefined) updateData.email = '';
    if (data.nickname === undefined) updateData.nickname = '';
    if (data.avatar === undefined) updateData.avatar = '';
    if (data.phone === undefined) updateData.phone = '';
    if (data.gender === undefined) updateData.gender = '';
    if (data.birthday === undefined) updateData.birthday = '';
    if (data.status === undefined) updateData.status = '启用';
    if (data.sleepStatus === undefined) updateData.sleepStatus = '未知';
    if (data.points === undefined) updateData.points = 0;
    if (data.sleepPointsToday === undefined) updateData.sleepPointsToday = 0;
    if (data.streak === undefined) updateData.streak = 0;
    if (data.registerTime === undefined) updateData.registerTime = admin.firestore.FieldValue.serverTimestamp();
    if (data.lastLoginTime === undefined) updateData.lastLoginTime = null;
    if (data.extraInfo === undefined) updateData.extraInfo = {};
    if (data.updateTime === undefined) updateData.updateTime = admin.firestore.FieldValue.serverTimestamp();

    if (Object.keys(updateData).length > 0) {
      batch.update(doc.ref, updateData);
    }
  });

  await batch.commit();
  res.send('users集合字段已补齐');
});

