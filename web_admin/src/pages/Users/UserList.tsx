import React, { useEffect, useState, useCallback } from 'react';
import { Table, Button, Modal, Descriptions, Tabs, Input, Select, Tooltip, Form, InputNumber, DatePicker, message, Popconfirm, Upload } from 'antd';
import { db, auth, storage } from '../../services/firebase';
import { collection, getDocs, query, where, updateDoc, doc, addDoc, serverTimestamp } from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import { ReloadOutlined, EditOutlined, LockOutlined, UploadOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { updatePassword, getAuth } from 'firebase/auth';

interface User {
  id: string;
  email: string;
  points: number;
  streakDays: number;
  createdAt: { seconds: number } | null;
  status: string;
  sleepStatus: string;
  avatar?: string;
  nickname?: string;
  phone?: string;
  gender?: string;
  birthday?: string;
}

interface PointsHistory {
  id: string;
  points: number;
  type: string;
  timestamp: { seconds: number };
  reason?: string;
}

interface Redemption {
  id: string;
  productName: string;
  pointsCost: number;
  timestamp: { seconds: number };
}

interface SleepRecord {
  id: string;
  date: string;
  sleepTime: string;
  wakeTime: string;
  duration: number;
  isCompliant: boolean;
}

const UserList: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [pointsHistory, setPointsHistory] = useState<PointsHistory[]>([]);
  const [redemptions, setRedemptions] = useState<Redemption[]>([]);
  const [sleepRecords, setSleepRecords] = useState<SleepRecord[]>([]);
  const [tabKey, setTabKey] = useState('info');
  const [searchValue, setSearchValue] = useState('');
  const [sleepStatusFilter, setSleepStatusFilter] = useState('');
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [editForm] = Form.useForm();
  const [resetPwdModal, setResetPwdModal] = useState(false);
  const [resetPwdUser, setResetPwdUser] = useState<User | null>(null);
  const [newPwd, setNewPwd] = useState('');
  const [avatarUploading, setAvatarUploading] = useState(false);

  // 头像上传函数
  const handleAvatarUpload = async (file: any) => {
    setAvatarUploading(true);
    try {
      const storageRef = ref(storage, `avatars/${Date.now()}_${file.name}`);
      const snapshot = await uploadBytes(storageRef, file);
      const downloadURL = await getDownloadURL(snapshot.ref);

      // 更新表单中的头像URL
      editForm.setFieldsValue({ avatar: downloadURL });
      message.success('头像上传成功');
      return false; // 阻止默认上传行为
    } catch (error) {
      message.error('头像上传失败');
      console.error('Upload error:', error);
    } finally {
      setAvatarUploading(false);
    }
    return false;
  };

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    const usersCol = collection(db, 'users');
    const userSnapshot = await getDocs(usersCol);
    const userList = userSnapshot.docs.map(doc => {
      const data = doc.data();
      return {
        id: doc.id,
        email: data.email,
        points: data.points,
        streakDays: data.streakDays ?? 0,
        createdAt: data.createdAt || null,
        status: data.status || '启用',
        sleepStatus: data.sleepStatus || '未知',
        avatar: data.avatar || '',
        nickname: data.nickname || '',
        phone: data.phone || '',
        gender: data.gender || '',
        birthday: data.birthday || '',
      } as User;
    });
    setUsers(userList);
    setLoading(false);
  }, []);

  useEffect(() => {
    if (!auth.currentUser) return;
    fetchUsers();
  }, [fetchUsers]);

  const filteredUsers = users.filter((user: any) => {
    const matchSearch =
      user.email.toLowerCase().includes(searchValue.toLowerCase()) ||
      user.id.toLowerCase().includes(searchValue.toLowerCase());
    const matchSleep = sleepStatusFilter ? user.sleepStatus === sleepStatusFilter : true;
    return matchSearch && matchSleep;
  });

  const sleepStatusOptions = [
    { value: '', label: '全部' },
    { value: '睡觉中', label: '睡觉中' },
    { value: '未睡觉', label: '未睡觉' },
    { value: '未知', label: '未知' },
  ];

  const showUserDetail = async (user: User) => {
    setSelectedUser(user);
    setDetailVisible(true);
    setTabKey('info');
    try {
      const q = query(collection(db, 'points_history'), where('userId', '==', user.id));
      const snap = await getDocs(q);
      setPointsHistory(snap.docs.map(doc => ({ id: doc.id, ...doc.data() })) as PointsHistory[]);
    } catch (e) {
      setPointsHistory([]);
    }
    try {
      const q2 = query(collection(db, 'redemptions'), where('userId', '==', user.id));
      const snap2 = await getDocs(q2);
      setRedemptions(snap2.docs.map(doc => ({ id: doc.id, ...doc.data() })) as Redemption[]);
    } catch (e) {
      setRedemptions([]);
    }
    try {
      const q3 = query(collection(db, 'sleep_records'), where('userId', '==', user.id));
      const snap3 = await getDocs(q3);
      setSleepRecords(snap3.docs.map(doc => ({ id: doc.id, ...doc.data() })) as SleepRecord[]);
    } catch (e) {
      setSleepRecords([]);
    }
  };

  // 刷新所有用户的睡眠状态
  const refreshAllSleepStatus = async () => {
    setLoading(true);
    const usersCol = collection(db, 'users');
    const userSnapshot = await getDocs(usersCol);
    const userList = userSnapshot.docs.map(doc => {
      const data = doc.data();
      return {
        id: doc.id,
        email: data.email,
        points: data.points,
        streakDays: data.streakDays ?? 0,
        createdAt: data.createdAt || null,
        status: data.status || '启用',
        sleepStatus: data.sleepStatus === '睡觉中' ? '睡觉' : data.sleepStatus === '未睡觉' ? '清醒' : '未知',
        avatar: data.avatar || '',
        nickname: data.nickname || '',
        phone: data.phone || '',
        gender: data.gender || '',
        birthday: data.birthday || '',
      } as User;
    });
    setUsers(userList);
    setLoading(false);
  };

  // 刷新单个用户的睡眠状态
  const refreshUserSleepStatus = async (userId: string) => {
    setLoading(true);
    const usersCol = collection(db, 'users');
    const userSnapshot = await getDocs(usersCol);
    const userList = userSnapshot.docs.map(doc => {
      const data = doc.data();
      return {
        id: doc.id,
        email: data.email,
        points: data.points,
        streakDays: data.streakDays ?? 0,
        createdAt: data.createdAt || null,
        status: data.status || '启用',
        sleepStatus: data.sleepStatus === '睡觉中' ? '睡觉' : data.sleepStatus === '未睡觉' ? '清醒' : '未知',
        avatar: data.avatar || '',
        nickname: data.nickname || '',
        phone: data.phone || '',
        gender: data.gender || '',
        birthday: data.birthday || '',
      } as User;
    });
    setUsers(userList);
    // 更新详情弹窗中的用户状态
    const updatedUser = userList.find(u => u.id === userId);
    if (updatedUser && selectedUser) {
      setSelectedUser({ ...selectedUser, sleepStatus: updatedUser.sleepStatus });
    }
    setLoading(false);
  };

  // 编辑用户信息（积分、状态、头像、昵称、手机号、性别、生日）
  const handleEditUser = (user: User) => {
    setSelectedUser(user);
    editForm.setFieldsValue({
      points: user.points,
      status: user.status || '启用',
      sleepStatus: user.sleepStatus || '未知',
      avatar: user.avatar || '',
      nickname: user.nickname || '',
      phone: user.phone || '',
      gender: user.gender || '',
      birthday: user.birthday ? dayjs(user.birthday) : null,
    });
    setEditModalVisible(true);
  };

  const handleEditSubmit = async () => {
    if (!selectedUser) return;
    const values = await editForm.validateFields();
    try {
      console.log('开始更新用户信息:', selectedUser.id, values);

      // 计算积分变动
      const oldPoints = selectedUser.points || 0;
      const newPoints = values.points;
      const diff = newPoints - oldPoints;

      // 准备更新数据，使用正确的字段名称
      const updateData: any = {
        points: newPoints,
        updatedAt: serverTimestamp(),
      };

      // 只更新有值的字段
      if (values.status) updateData.status = values.status;
      if (values.sleepStatus) updateData.sleepStatus = values.sleepStatus;
      if (values.avatar) updateData.avatar = values.avatar;
      if (values.nickname) updateData.nickname = values.nickname;
      if (values.phone) updateData.phone = values.phone;
      if (values.gender) updateData.gender = values.gender;
      if (values.birthday) updateData.birthday = values.birthday.format('YYYY-MM-DD');

      console.log('更新数据:', updateData);

      // 更新用户信息
      await updateDoc(doc(db, 'users', selectedUser.id), updateData);
      console.log('用户信息更新成功');

      // 如果积分有变动，插入积分流水
      if (diff !== 0) {
        console.log('添加积分流水:', diff);
        await addDoc(collection(db, 'pointsHistory'), {
          userId: selectedUser.id,
          points: diff,
          type: 'manual',
          description: '管理员手动修改',
          operatorId: auth.currentUser?.uid || '',
          createdAt: serverTimestamp(),
        });
        console.log('积分流水添加成功');
      }

      message.success('用户信息已更新');
      setEditModalVisible(false);
      // 立即刷新用户列表，确保前端展示与数据库一致
      await fetchUsers();
    } catch (e) {
      console.error('更新失败:', e);
      message.error(`更新失败: ${e}`);
    }
  };

  // 管理员直接重置用户密码（需后端云函数支持）
  const handleResetPassword = (user: User) => {
    setResetPwdUser(user);
    setNewPwd('');
    setResetPwdModal(true);
  };

  const handleResetPwdSubmit = async () => {
    if (!resetPwdUser || !newPwd) return;
    try {
      // 这里应调用后端云函数（如Firebase Functions）实现管理员重置任意用户密码
      // 假设有一个云函数resetUserPassword({ uid, newPassword })
      const resp = await fetch('/api/resetUserPassword', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ uid: resetPwdUser.id, newPassword: newPwd })
      });
      const result = await resp.json();
      if (result.success) {
        message.success('密码已重置');
        setResetPwdModal(false);
      } else {
        message.error(result.message || '重置密码失败');
      }
    } catch (e) {
      message.error('重置密码失败，需后端支持');
    }
  };

  return (
    <div>
      <h2>用户管理</h2>
      <div style={{ display: 'flex', gap: 16, marginBottom: 16 }}>
        <Input
          placeholder="搜索用户ID或邮箱"
          value={searchValue}
          onChange={(e: any) => setSearchValue(e.target.value)}
          style={{ width: 240 }}
        />
        <Select
          value={sleepStatusFilter}
          onChange={setSleepStatusFilter}
          style={{ width: 120 }}
          options={sleepStatusOptions}
        />
        <Button icon={<ReloadOutlined />} onClick={refreshAllSleepStatus} loading={loading}>
          刷新全部用户状态
        </Button>
      </div>
      <Table
        dataSource={filteredUsers}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
      >
        <Table.Column title="用户ID" dataIndex="id" />
        <Table.Column title="邮箱" dataIndex="email" />
        <Table.Column title="当前积分" dataIndex="points" />
        <Table.Column title="连续天数" dataIndex="streakDays" />
        <Table.Column
          title="注册时间"
          dataIndex="createdAt"
          render={(createdAt: any) =>
            createdAt && createdAt.seconds
              ? new Date(createdAt.seconds * 1000).toLocaleString()
              : '-'
          }
        />
        <Table.Column
          title="当前睡眠状态"
          dataIndex="sleepStatus"
          render={status => status || '未知'}
        />
        <Table.Column
          title="操作"
          render={(_, user: User) => (
            <>
              <Button type="link" onClick={() => showUserDetail(user)}>
                查看详情
              </Button>
              <Button type="link" icon={<EditOutlined />} onClick={() => handleEditUser(user)}>
                编辑
              </Button>
              <Button type="link" icon={<LockOutlined />} onClick={() => handleResetPassword(user)}>
                重置密码
              </Button>
            </>
          )}
        />
      </Table>
      <Modal
        open={detailVisible}
        title="用户详情"
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={900}
      >
        <Tabs activeKey={tabKey} onChange={setTabKey}>
          <Tabs.TabPane tab="基本信息" key="info">
            {selectedUser && (
              <Descriptions bordered column={1} size="small">
                <Descriptions.Item label="用户ID">{selectedUser.id}</Descriptions.Item>
                <Descriptions.Item label="邮箱">{selectedUser.email}</Descriptions.Item>
                <Descriptions.Item label="头像">
                  {selectedUser.avatar ? <img src={selectedUser.avatar} alt="头像" style={{ width: 48, height: 48, borderRadius: '50%' }} /> : <span style={{ color: '#aaa' }}>无</span>}
                </Descriptions.Item>
                <Descriptions.Item label="昵称">{selectedUser.nickname || '-'}</Descriptions.Item>
                <Descriptions.Item label="手机号">{selectedUser.phone || '-'}</Descriptions.Item>
                <Descriptions.Item label="性别">{selectedUser.gender === 'male' ? '男' : selectedUser.gender === 'female' ? '女' : selectedUser.gender === 'other' ? '其他' : '-'}</Descriptions.Item>
                <Descriptions.Item label="生日">{selectedUser.birthday || '-'}</Descriptions.Item>
                <Descriptions.Item label="当前积分">{selectedUser.points}</Descriptions.Item>
                <Descriptions.Item label="连续天数">{selectedUser.streakDays}</Descriptions.Item>
                <Descriptions.Item label="注册时间">
                  {selectedUser.createdAt && selectedUser.createdAt.seconds
                    ? new Date(selectedUser.createdAt.seconds * 1000).toLocaleString()
                    : '-'}
                </Descriptions.Item>
                <Descriptions.Item label="当前睡眠状态">
                  {selectedUser.sleepStatus || '未知'}
                  <Tooltip title="刷新该用户状态">
                    <Button
                      type="link"
                      icon={<ReloadOutlined />}
                      size="small"
                      onClick={() => refreshUserSleepStatus(selectedUser.id)}
                      style={{ marginLeft: 8, padding: 0 }}
                    />
                  </Tooltip>
                </Descriptions.Item>
              </Descriptions>
            )}
          </Tabs.TabPane>
          <Tabs.TabPane tab="睡眠记录" key="sleep">
            <Table
              dataSource={sleepRecords}
              rowKey="id"
              size="small"
              pagination={false}
            >
              <Table.Column title="日期" dataIndex="date" />
              <Table.Column title="入睡时间" dataIndex="sleepTime" />
              <Table.Column title="起床时间" dataIndex="wakeTime" />
              <Table.Column title="睡眠时长(分钟)" dataIndex="duration" />
              <Table.Column title="是否达标" dataIndex="isCompliant" render={(val: boolean) => val ? '是' : '否'} />
            </Table>
          </Tabs.TabPane>
          <Tabs.TabPane tab="积分流水" key="points">
            {/* 查询条件 */}
            <div style={{ display: 'flex', gap: 16, marginBottom: 12 }}>
              <Select
                placeholder="类型"
                style={{ width: 120 }}
                allowClear
                // onChange={setPointsTypeFilter}
                options={[
                  { value: '', label: '全部' },
                  { value: 'sleep', label: '睡觉' },
                  { value: 'streak', label: '连续睡觉' },
                  { value: 'admin', label: '管理员增加' },
                ]}
              />
              <DatePicker.RangePicker style={{ width: 260 }} />
              <Button type="primary">查询</Button>
            </div>
            <Table
              dataSource={pointsHistory}
              rowKey="id"
              size="small"
              pagination={false}
            >
              <Table.Column title="变动积分" dataIndex="points" />
              <Table.Column title="类型" dataIndex="type" />
              <Table.Column title="来源说明" dataIndex="reason" render={reason => reason || '-'} />
              <Table.Column
                title="时间"
                dataIndex="timestamp"
                render={(ts: any) => ts ? new Date(ts.seconds * 1000).toLocaleString() : '-'}
              />
            </Table>
          </Tabs.TabPane>
          <Tabs.TabPane tab="兑换历史" key="redeem">
            <Table
              dataSource={redemptions}
              rowKey="id"
              size="small"
              pagination={false}
            >
              <Table.Column title="商品名称" dataIndex="productName" />
              <Table.Column title="消耗积分" dataIndex="pointsCost" />
              <Table.Column
                title="时间"
                dataIndex="timestamp"
                render={(ts: any) => ts ? new Date(ts.seconds * 1000).toLocaleString() : '-'}
              />
            </Table>
          </Tabs.TabPane>
        </Tabs>
      </Modal>
      <Modal
        open={editModalVisible}
        title="编辑用户信息"
        onCancel={() => setEditModalVisible(false)}
        onOk={handleEditSubmit}
        okText="保存"
        cancelText="取消"
      >
        <Form form={editForm} layout="vertical">
          <Form.Item label="邮箱" required>
            <Input value={selectedUser?.email} disabled />
          </Form.Item>
          <Form.Item name="avatar" label="头像">
            <div>
              <Input placeholder="请输入头像图片URL" style={{ marginBottom: 8 }} />
              <Upload
                beforeUpload={handleAvatarUpload}
                showUploadList={false}
                accept="image/*"
              >
                <Button icon={<UploadOutlined />} loading={avatarUploading}>
                  {avatarUploading ? '上传中...' : '上传头像'}
                </Button>
              </Upload>
              {/* 头像预览 */}
              {editForm.getFieldValue('avatar') && (
                <div style={{ marginTop: 8 }}>
                  <img
                    src={editForm.getFieldValue('avatar')}
                    alt="头像"
                    style={{ width: 64, height: 64, borderRadius: '50%', objectFit: 'cover' }}
                  />
                </div>
              )}
            </div>
          </Form.Item>
          <Form.Item name="nickname" label="昵称">
            <Input placeholder="请输入昵称（非必填）" maxLength={20} />
          </Form.Item>
          <Form.Item name="phone" label="手机号">
            <Input placeholder="请输入手机号（非必填）" maxLength={20} />
          </Form.Item>
          <Form.Item name="gender" label="性别">
            <Select
              allowClear
              placeholder="请选择性别"
              options={[
                { value: 'male', label: '男' },
                { value: 'female', label: '女' },
                { value: 'other', label: '其他' }
              ]}
            />
          </Form.Item>
          <Form.Item name="birthday" label="生日">
            <DatePicker style={{ width: '100%' }} placeholder="请选择生日（非必填）" />
          </Form.Item>
          <Form.Item name="points" label="当前积分" rules={[{ required: true, message: '请输入积分' }]}> <InputNumber min={0} style={{ width: '100%' }} /> </Form.Item>
          <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}> <Select options={[{ value: '启用', label: '启用' }, { value: '停用', label: '停用' }]} /> </Form.Item>
          <Form.Item name="sleepStatus" label="睡眠状态" rules={[{ required: true, message: '请选择睡眠状态' }]}> <Select options={[{ value: '睡觉中', label: '睡觉中' }, { value: '未睡觉', label: '未睡觉' }, { value: '未知', label: '未知' }]} /> </Form.Item>
        </Form>
      </Modal>
      <Modal open={resetPwdModal} title="重置用户密码" onCancel={()=>setResetPwdModal(false)} onOk={handleResetPwdSubmit} okText="保存" cancelText="取消">
        <Input.Password value={newPwd} onChange={(e: any)=>setNewPwd(e.target.value)} placeholder="请输入新密码" />
      </Modal>
    </div>
  );
};

export default UserList;