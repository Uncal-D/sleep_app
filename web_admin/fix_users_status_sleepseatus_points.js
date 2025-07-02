const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: 'sleep-app-uncald',
});

const db = admin.firestore();

const BATCH_LIMIT = 400; // Firestore批量操作上限500，预留安全空间

async function fixAllUsers() {
  const usersRef = db.collection('users');
  const pointsHistoryRef = db.collection('points_history');
  const snapshot = await usersRef.get();
  const users = snapshot.docs;
  let success = 0, fail = 0;

  for (let i = 0; i < users.length; i += BATCH_LIMIT) {
    const batch = db.batch();
    const batchUsers = users.slice(i, i + BATCH_LIMIT);
    for (const doc of batchUsers) {
      const ref = usersRef.doc(doc.id);
      batch.update(ref, {
        status: '启用',
        sleepStatus: '未知',
        totalPoints: admin.firestore.FieldValue.delete(),
        updateTime: admin.firestore.FieldValue.serverTimestamp(),
      });
      // 插入积分流水
      const historyRef = pointsHistoryRef.doc();
      batch.set(historyRef, {
        userId: doc.id,
        points: 0,
        reason: '系统批量修正',
        type: 'system',
        createTime: admin.firestore.FieldValue.serverTimestamp(),
        operatorId: 'system',
        extraInfo: {},
      });
    }
    try {
      await batch.commit();
      success += batchUsers.length;
      console.log(`成功处理${batchUsers.length}个用户`);
    } catch (e) {
      fail += batchUsers.length;
      console.error('批量提交失败：', e);
    }
  }
  console.log(`操作完成，成功：${success}，失败：${fail}`);
}

fixAllUsers();