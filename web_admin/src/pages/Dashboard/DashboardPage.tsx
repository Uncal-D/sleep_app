import React, { useEffect, useState } from 'react';
import { Card, Row, Col, Statistic, Alert } from 'antd';
import { db, auth } from '../../services/firebase';
import { collection, getDocs, doc, getDoc } from 'firebase/firestore';

const DashboardPage: React.FC = () => {
  const [userCount, setUserCount] = useState(0);
  const [totalPoints, setTotalPoints] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    const loadDashboardData = async () => {
      setLoading(true);
      setError(null);
      try {
        // 1. 检查是否已登录
        if (!auth.currentUser) {
          setError('请先登录');
          setLoading(false);
          return;
        }
        // 2. 检查是否为管理员
        const uid = auth.currentUser.uid;
        const adminDoc = await getDoc(doc(db, 'admins', uid));
        if (!adminDoc.exists()) {
          setError('无管理员权限，禁止访问后台数据统计');
          setIsAdmin(false);
          setLoading(false);
          return;
        }
        setIsAdmin(true);
        // 3. 管理员，加载统计数据
        const usersSnapshot = await getDocs(collection(db, 'users'));
        setUserCount(usersSnapshot.size);
        let total = 0;
        usersSnapshot.docs.forEach(doc => {
          const data = doc.data();
          if (typeof data.points === 'number') {
            total += data.points;
          }
        });
        setTotalPoints(total);
      } catch (err) {
        console.error('加载仪表板数据失败:', err);
        setError('加载数据失败，请检查权限设置或网络');
      } finally {
        setLoading(false);
      }
    };
    loadDashboardData();
  }, []);

  if (loading) {
    return <div>加载中...</div>;
  }

  if (error) {
    return <Alert message="错误" description={error} type="error" showIcon />;
  }

  if (!isAdmin) {
    return <Alert message="无权限" description="只有管理员可以访问数据统计页面。" type="error" showIcon />;
  }

  return (
    <div>
      <h2>数据统计看板</h2>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic title="用户总数" value={userCount} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="当前总积分" value={totalPoints} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="当前管理员" value={auth.currentUser?.email || '未知'} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="登录时间" value={new Date().toLocaleString()} />
          </Card>
        </Col>
      </Row>
      <Card title="系统状态">
        <p>✅ 管理员登录状态正常</p>
        <p>✅ 权限校验已生效</p>
        <p>✅ 数据统计功能正常</p>
      </Card>
    </div>
  );
};

export default DashboardPage;