import { useState } from 'react';
import { Form, Input, Button, Card, message, Tabs } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { login, register } from '../api/auth';

export default function Login({ onLogin }) {
  const [loading, setLoading] = useState(false);

  const handleLogin = async (values) => {
    setLoading(true);
    try {
      const { data } = await login(values.username, values.password);
      localStorage.setItem('token', data.token);
      onLogin();
    } catch {
      message.error('Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (values) => {
    setLoading(true);
    try {
      const { data } = await register(values.username, values.password, values.fullName);
      localStorage.setItem('token', data.token);
      onLogin();
    } catch (err) {
      message.error(err.response?.data?.error || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  const items = [
    {
      key: 'login',
      label: 'Login',
      children: (
        <Form onFinish={handleLogin} layout="vertical">
          <Form.Item name="username" rules={[{ required: true }]}>
            <Input prefix={<UserOutlined />} placeholder="Username" size="large" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="Password" size="large" />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} block size="large">Login</Button>
        </Form>
      ),
    },
    {
      key: 'register',
      label: 'Register',
      children: (
        <Form onFinish={handleRegister} layout="vertical">
          <Form.Item name="fullName" rules={[{ required: true }]}>
            <Input placeholder="Full Name" size="large" />
          </Form.Item>
          <Form.Item name="username" rules={[{ required: true }]}>
            <Input prefix={<UserOutlined />} placeholder="Username" size="large" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, min: 6 }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="Password" size="large" />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} block size="large">Register</Button>
        </Form>
      ),
    },
  ];

  return (
    <div style={{
      display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh',
      background: 'linear-gradient(135deg, #1A1F71 0%, #2d3494 50%, #1A1F71 100%)',
    }}>
      <Card style={{ width: 420, borderTop: '4px solid #ED1C24', borderRadius: 8 }}>
        <div style={{ textAlign: 'center', marginBottom: 28 }}>
          <div style={{
            width: 56, height: 56, borderRadius: '50%', background: '#ED1C24',
            display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 22, fontWeight: 'bold', color: '#fff', marginBottom: 12,
          }}>TE</div>
          <h2 style={{ margin: 0, color: '#1A1F71', fontSize: 22, fontWeight: 700 }}>TotalEnergies</h2>
          <p style={{ margin: '4px 0 0', color: '#888', fontSize: 13 }}>Warehouse Management System</p>
        </div>
        <Tabs items={items} centered />
      </Card>
    </div>
  );
}
