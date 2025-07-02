import React, { useEffect, useState } from 'react';
import { Table, Input, Button, Alert, Card, Statistic, Modal, Form, InputNumber, Select, message, Popconfirm } from 'antd';
import { SearchOutlined, UserOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { db, auth } from '../../services/firebase';
import { collection, getDocs, query, where, updateDoc, doc, deleteDoc } from 'firebase/firestore';

interface PointsHistory {
  id: string;
  userId: string;
  points: number;
  type: string;
  timestamp: { seconds: number };
  reason?: string;
}

interface User {
  id: string;
  email: string;
  points: number;
  status?: string;
  createdAt?: { seconds: number };
}

const PointsHistoryPage: React.FC = () => {
  const [data, setData] = useState<PointsHistory[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [userSearchValue, setUserSearchValue] = useState('');
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [userModalVisible, setUserModalVisible] = useState(false);
  const [userForm] = Form.useForm();

  // 获取所有用户列表（用于搜索）
  const fetchUsers = async () => {
    try {
      const usersCol = collection(db, 'users');
      const userSnapshot = await getDocs(usersCol);
      const userList = userSnapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data(),
      })) as User[];
      setUsers(userList);
    } catch (error) {
      console.error('获取用户列表失败:', error);
    }
  };

  useEffect(() => {
    if (!auth.currentUser) return;
    fetchUsers();
  }, []);

  // 搜索用户
  const searchUsers = (searchValue: string) => {
    if (!searchValue.trim()) return [];
    return users.filter(user => 
      user.email.toLowerCase().includes(searchValue.toLowerCase()) ||
      user.id.toLowerCase().includes(searchValue.toLowerCase())
    );
  };

  const fetchData = async (uid?: string) => {
    setLoading(true);
    setError(null);
    
    try {
      let q;
      if (uid) {
        // 如果指定了用户ID，尝试查询该用户的积分流水
        q = query(collection(db, 'points_history'), where('userId', '==', uid));
      } else {
        // 如果没有指定用户ID，由于安全规则限制，我们无法获取所有数据
        setData([]);
        setError('由于安全规则限制，需要指定用户ID才能查看积分流水。');
        setLoading(false);
        return;
      }
      
      const snap = await getDocs(q);
      const historyData = snap.docs.map(doc => ({ id: doc.id, ...doc.data() })) as PointsHistory[];
      setData(historyData);
      
    } catch (err) {
      console.error('获取积分流水失败:', err);
      setError('获取数据失败，请检查用户ID是否正确');
      setData([]);
    } finally {
      setLoading(false);
    }
  };

  // 用户管理功能
  const handleUserEdit = async (values: any) => {
    if (!selectedUser) return;
    
    try {
      await updateDoc(doc(db, 'users', selectedUser.id), {
        points: values.points,
        status: values.status
      });
      
      message.success('用户信息更新成功');
      setUserModalVisible(false);
      fetchUsers(); // 刷新用户列表
      
      // 如果当前查看的是这个用户的积分流水，刷新数据
      if (selectedUser.id === userSearchValue) {
        fetchData(selectedUser.id);
      }
    } catch (error) {
      console.error('更新用户信息失败:', error);
      message.error('更新用户信息失败');
    }
  };

  const handleUserDelete = async () => {
    if (!selectedUser) return;
    
    try {
      await deleteDoc(doc(db, 'users', selectedUser.id));
      message.success('用户删除成功');
      setUserModalVisible(false);
      fetchUsers();
      
      // 如果删除的是当前查看的用户，清空数据
      if (selectedUser.id === userSearchValue) {
        setUserSearchValue('');
        setData([]);
        setSelectedUser(null);
      }
    } catch (error) {
      console.error('删除用户失败:', error);
      message.error('删除用户失败');
    }
  };

  const handleUserSelect = (userId: string) => {
    setUserSearchValue(userId);
    setSelectedUser(users.find(u => u.id === userId) || null);
    fetchData(userId);
  };

  const [error, setError] = useState<string | null>(null);

  return (
    <div>
      <h2>积分流水管理</h2>
      
      <Alert 
        message="使用说明" 
        description="您可以通过邮箱或用户ID搜索用户，查看其积分流水记录，并进行用户管理操作。" 
        type="info" 
        showIcon 
        style={{ marginBottom: 16 }}
      />
      
      <Card style={{ marginBottom: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
          <div style={{ minWidth: 240 }}>
            <Select
              showSearch
              placeholder="搜索用户（邮箱或用户ID）"
              value={userSearchValue}
              onChange={handleUserSelect}
              onSearch={(value) => setUserSearchValue(value)}
              filterOption={false}
              style={{ width: 240 }}
              notFoundContent={userSearchValue ? '未找到用户' : '请输入搜索关键词'}
            >
              {searchUsers(userSearchValue).map(user => (
                <Select.Option key={user.id} value={user.id}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span>{user.email}</span>
                    <span style={{ color: '#999', fontSize: '12px' }}>{user.id}</span>
                  </div>
                </Select.Option>
              ))}
            </Select>
          </div>
          <Button type="primary" icon={<SearchOutlined />} onClick={() => fetchData(userSearchValue)}>
            查询
          </Button>
          <Button onClick={() => { 
            setUserSearchValue(''); 
            setData([]); 
            setSelectedUser(null);
          }}>
            重置
          </Button>
          {selectedUser && (
            <Button 
              type="primary" 
              icon={<UserOutlined />}
              onClick={() => setUserModalVisible(true)}
            >
              管理用户
            </Button>
          )}
        </div>
      </Card>

      {error && (
        <Alert 
          message="提示" 
          description={error} 
          type="warning" 
          showIcon 
          style={{ marginBottom: 16 }}
        />
      )}

      {selectedUser && (
        <Card style={{ marginBottom: 16 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <h3>用户信息</h3>
              <p>邮箱: {selectedUser.email}</p>
              <p>用户ID: {selectedUser.id}</p>
              <p>当前积分: {selectedUser.points || 0}</p>
            </div>
          </div>
        </Card>
      )}

      <Table 
        dataSource={data} 
        rowKey="id" 
        loading={loading} 
        pagination={{ pageSize: 20 }}
        locale={{ emptyText: '请选择用户查看积分流水' }}
      >
        <Table.Column title="用户ID" dataIndex="userId" />
        <Table.Column 
          title="变动积分" 
          dataIndex="points" 
          render={(points: number) => (
            <span style={{ color: points > 0 ? '#3f8600' : '#cf1322' }}>
              {points > 0 ? '+' : ''}{points}
            </span>
          )}
        />
        <Table.Column title="类型" dataIndex="type" />
        <Table.Column
          title="时间"
          dataIndex="timestamp"
          render={(ts: any) => ts ? new Date(ts.seconds * 1000).toLocaleString() : '-'}
        />
        <Table.Column title="来源说明" dataIndex="reason" render={reason => reason || '-'} />
      </Table>

      {/* 用户管理弹窗 */}
      <Modal
        open={userModalVisible}
        title="用户管理"
        onCancel={() => setUserModalVisible(false)}
        footer={null}
        width={500}
      >
        {selectedUser && (
          <div>
            <Form
              form={userForm}
              layout="vertical"
              initialValues={{
                email: selectedUser.email,
                points: selectedUser.points || 0,
                status: selectedUser.status || 'active'
              }}
              onFinish={handleUserEdit}
            >
              <Form.Item label="邮箱">
                <Input value={selectedUser.email} disabled />
              </Form.Item>
              
              <Form.Item label="用户ID">
                <Input value={selectedUser.id} disabled />
              </Form.Item>
              
              <Form.Item 
                name="points" 
                label="积分" 
                rules={[{ required: true, message: '请输入积分' }]}
              >
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
              
              <Form.Item 
                name="status" 
                label="账号状态" 
                rules={[{ required: true, message: '请选择账号状态' }]}
              >
                <Select>
                  <Select.Option value="active">正常</Select.Option>
                  <Select.Option value="suspended">停用</Select.Option>
                </Select>
              </Form.Item>
              
              <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                <Popconfirm
                  title="确定要删除这个用户吗？"
                  description="删除后无法恢复，请谨慎操作！"
                  onConfirm={handleUserDelete}
                  okText="确定删除"
                  cancelText="取消"
                >
                  <Button type="primary" danger icon={<DeleteOutlined />}>
                    删除用户
                  </Button>
                </Popconfirm>
                <Button type="primary" htmlType="submit" icon={<EditOutlined />}>
                  保存修改
                </Button>
              </div>
            </Form>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default PointsHistoryPage; 