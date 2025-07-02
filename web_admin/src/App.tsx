import React, { useState, useEffect } from 'react';
import UserList from './pages/Users/UserList';
import ShopList from './pages/Shop/ShopList';
import PointsHistoryPage from './pages/Points/PointsHistoryPage';
import DashboardPage from './pages/Dashboard/DashboardPage';
import LoginPage from './pages/Auth/LoginPage';
import { Tabs, Modal, List, Button, Avatar, Dropdown, Menu, Space } from 'antd';
import { auth, db } from './services/firebase';
import { collection, getDocs, getDoc, doc } from 'firebase/firestore';
import 'antd/dist/reset.css';
import { UserOutlined, LoginOutlined, LogoutOutlined } from '@ant-design/icons';

function App() {
  // 优先从localStorage读取adminUid，保证刷新后不退出
  const [adminUid, setAdminUidState] = useState<string | null>(() => localStorage.getItem('adminUid'));
  const [tab, setTab] = useState('dashboard');
  const [debugModalVisible, setDebugModalVisible] = useState(false);
  const [debugInfo, setDebugInfo] = useState({
    currentUid: '',
    adminUids: [] as string[],
    requestAuthUid: '',
    loginTime: '',
    authState: ''
  });

  // 封装setAdminUid，持久化到localStorage
  const setAdminUid = (uid: string | null) => {
    setAdminUidState(uid);
    if (uid) {
      localStorage.setItem('adminUid', uid);
    } else {
      localStorage.removeItem('adminUid');
    }
  };

  // 获取调试信息
  const getDebugInfo = async () => {
    try {
      console.log('开始获取调试信息...');
      
      const currentUser = auth.currentUser;
      console.log('当前用户:', currentUser);
      
      const currentUid = currentUser?.uid || '未获取到';
      const requestAuthUid = currentUser?.uid || '未获取到';
      const loginTime = new Date().toLocaleString();
      const authState = currentUser ? '已认证' : '未认证';
      
      // 由于安全规则限制，我们无法读取整个admins集合
      // 只能显示当前用户的信息
      let adminUids: string[] = [];
      try {
        console.log('检查当前用户是否为管理员...');
        // 只检查当前用户的管理员状态
        const adminDoc = await getDoc(doc(db, 'admins', currentUid));
        if (adminDoc.exists()) {
          adminUids = [currentUid + ' (当前用户 - 管理员)'];
          console.log('当前用户是管理员');
        } else {
          adminUids = ['当前用户不是管理员'];
          console.log('当前用户不是管理员');
        }
      } catch (error) {
        console.error('检查管理员状态失败:', error);
        adminUids = ['检查失败 - 权限不足'];
      }
      
      const newDebugInfo = {
        currentUid,
        adminUids,
        requestAuthUid,
        loginTime,
        authState
      };
      
      console.log('设置调试信息:', newDebugInfo);
      setDebugInfo(newDebugInfo);
      
      // 显示弹窗
      console.log('显示调试弹窗');
      setDebugModalVisible(true);
    } catch (error) {
      console.error('获取调试信息失败:', error);
      // 即使出错也显示弹窗，显示错误信息
      setDebugInfo({
        currentUid: '获取失败',
        adminUids: ['获取失败'],
        requestAuthUid: '获取失败',
        loginTime: new Date().toLocaleString(),
        authState: '错误'
      });
      setDebugModalVisible(true);
    }
  };

  // 当用户登录后，显示调试信息
  useEffect(() => {
    console.log('adminUid变化:', adminUid);
    if (adminUid) {
      console.log('用户已登录，准备显示调试信息...');
      // 延迟一点时间确保登录状态完全更新
      setTimeout(() => {
        console.log('延迟结束，开始获取调试信息');
        getDebugInfo();
      }, 1000); // 增加延迟时间
    }
  }, [adminUid]);

  // 新增：右上角用户信息展示组件
  const UserInfo = () => {
    // 取当前用户信息
    const user = auth.currentUser;
    const email = user?.email || '';
    const isLoggedIn = !!user;
    const handleLogout = async () => {
      await auth.signOut();
      setAdminUid(null);
    };
    const menu = (
      <Menu>
        {isLoggedIn && <Menu.Item key="logout" icon={<LogoutOutlined />} onClick={handleLogout}>退出登录</Menu.Item>}
      </Menu>
    );
    return (
      <div style={{ position: 'absolute', top: 16, right: 32, zIndex: 100 }}>
        <Dropdown overlay={menu} placement="bottomRight" trigger={["click"]}>
          <Space style={{ cursor: 'pointer' }}>
            <Avatar size={32} icon={<UserOutlined />} />
            <span style={{ fontWeight: 500, color: '#333' }}>
              {isLoggedIn ? (email ? email : '已登录') : '登录'}
            </span>
          </Space>
        </Dropdown>
      </div>
    );
  };

  if (!adminUid) {
    return <LoginPage onLogin={setAdminUid} />;
  }

  return (
    <div style={{ padding: 24, position: 'relative' }}>
      <UserInfo />
      <Tabs activeKey={tab} onChange={setTab}>
        <Tabs.TabPane tab="数据统计" key="dashboard">
          <DashboardPage />
        </Tabs.TabPane>
        <Tabs.TabPane tab="用户管理" key="users">
          <UserList />
        </Tabs.TabPane>
        <Tabs.TabPane tab="商品管理" key="shop">
          <ShopList />
        </Tabs.TabPane>
        <Tabs.TabPane tab="积分流水" key="points">
          <PointsHistoryPage />
        </Tabs.TabPane>
      </Tabs>

      {/* 调试信息弹窗 */}
      <Modal
        title="登录调试信息"
        open={debugModalVisible}
        onOk={() => setDebugModalVisible(false)}
        onCancel={() => setDebugModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setDebugModalVisible(false)}>
            关闭
          </Button>
        ]}
        width={600}
      >
        <List
          size="small"
          bordered
          dataSource={[
            {
              title: '当前登录UID',
              content: debugInfo.currentUid
            },
            {
              title: '管理员UID列表',
              content: debugInfo.adminUids.length > 0 ? debugInfo.adminUids.join('; ') : '无管理员'
            },
            {
              title: 'request.auth.uid',
              content: debugInfo.requestAuthUid
            },
            {
              title: '登录时间',
              content: debugInfo.loginTime
            },
            {
              title: '认证状态',
              content: debugInfo.authState
            }
          ]}
          renderItem={(item) => (
            <List.Item>
              <div style={{ width: '100%' }}>
                <div style={{ fontWeight: 'bold', marginBottom: 4 }}>{item.title}:</div>
                <div style={{ wordBreak: 'break-all', color: item.content.includes('失败') ? 'red' : 'black' }}>
                  {item.content}
                </div>
              </div>
            </List.Item>
          )}
        />
      </Modal>
    </div>
  );
}

export default App; 