import React, { useState } from 'react';
import { Form, Input, Button, message, Card } from 'antd';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth, db } from '../../services/firebase';
import { doc, getDoc } from 'firebase/firestore';

const LoginPage: React.FC<{ onLogin: (uid: string) => void }> = ({ onLogin }) => {
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  const onFinish = async (values: any) => {
    console.log('表单提交的值', values); // 终极调试输出
    setLoading(true);
    try {
      console.log('开始登录...');
      const userCredential = await signInWithEmailAndPassword(auth, values.email, values.password);
      const uid = userCredential.user.uid;
      console.log('登录成功，UID:', uid);
      
      // 检查是否为管理员
      console.log('检查管理员权限...');
      const adminDoc = await getDoc(doc(db, 'admins', uid));
      console.log('管理员文档存在:', adminDoc.exists());
      
      if (adminDoc.exists()) {
        console.log('管理员权限验证成功，调用onLogin');
        message.success('登录成功');
        onLogin(uid);
      } else {
        console.log('管理员权限验证失败');
        message.error('无管理员权限');
        await auth.signOut();
      }
    } catch (e) {
      console.error('登录过程出错:', e);
      message.error('登录失败，请检查邮箱和密码');
    }
    setLoading(false);
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <Card title="后台管理员登录" style={{ width: 350 }}>
        <Form
          form={form}
          onFinish={onFinish}
          layout="vertical"
          autoComplete="off"
        >
          <Form.Item
            name="email"
            label="邮箱"
            rules={[{ required: true, message: '请输入邮箱' }]}
          >
            <Input autoComplete="new-email" />
          </Form.Item>
          <Form.Item
            name="password"
            label="密码"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password autoComplete="new-password" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default LoginPage; 