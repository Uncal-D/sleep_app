import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { getAuth } from 'firebase/auth';

const firebaseConfig = {
  apiKey: 'AIzaSyC4tjZDrts8-KiKBv4LymzSLOvRH05z8g0',
  authDomain: 'sleep-app-uncald.firebaseapp.com',
  projectId: 'sleep-app-uncald',
  storageBucket: 'sleep-app-uncald.appspot.com',
  messagingSenderId: '653970785329',
  appId: '1:653970785329:android:285eaf4583c7b72a088110',
};

const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
export const auth = getAuth(app); 