import { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Button, theme } from 'antd';
import {
  DashboardOutlined, InboxOutlined, ShoppingCartOutlined, ShoppingOutlined,
  TeamOutlined, DollarOutlined, BarChartOutlined, LogoutOutlined,
  BankOutlined,
} from '@ant-design/icons';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import Companies from './pages/Companies';
import Inventory from './pages/Inventory';
import PurchaseOrders from './pages/PurchaseOrders';
import SalesOrders from './pages/SalesOrders';
import Accounting from './pages/Accounting';
import Reports from './pages/Reports';

const { Sider, Content, Header } = Layout;

const menuItems = [
  { key: '/', icon: <DashboardOutlined />, label: 'Dashboard' },
  { key: '/products', icon: <InboxOutlined />, label: 'Products' },
  { key: '/companies', icon: <TeamOutlined />, label: 'Companies' },
  { key: '/inventory', icon: <ShoppingOutlined />, label: 'Inventory' },
  { key: '/purchase-orders', icon: <ShoppingCartOutlined />, label: 'Purchase Orders' },
  { key: '/sales-orders', icon: <DollarOutlined />, label: 'Sales Orders' },
  { key: '/accounting', icon: <BankOutlined />, label: 'Accounting' },
  { key: '/reports', icon: <BarChartOutlined />, label: 'Reports' },
];

function AppLayout({ onLogout }) {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { token: { colorBgContainer, borderRadiusLG } } = theme.useToken();

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed} className="te-sider" width={230}>
        <div style={{
          height: 64, margin: '0', padding: '12px 16px', display: 'flex', alignItems: 'center', gap: 10,
          borderBottom: '2px solid #ED1C24',
        }}>
          <div style={{
            width: 36, height: 36, borderRadius: '50%', background: '#ED1C24',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 16, fontWeight: 'bold', color: '#fff', flexShrink: 0,
          }}>TE</div>
          {!collapsed && (
            <div style={{ color: '#fff', lineHeight: 1.2 }}>
              <div style={{ fontSize: 14, fontWeight: 700, letterSpacing: 0.5 }}>TotalEnergies</div>
              <div style={{ fontSize: 11, opacity: 0.7 }}>Warehouse Management</div>
            </div>
          )}
        </div>
        <Menu theme="dark" selectedKeys={[location.pathname]} items={menuItems} onClick={({ key }) => navigate(key)} />
      </Sider>
      <Layout>
        <Header className="te-header" style={{
          padding: '0 24px', background: colorBgContainer,
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
          height: 64,
        }}>
          <span style={{ fontSize: 16, fontWeight: 600, color: '#1A1F71' }}>
            TotalEnergies WMS — Turkmenistan
          </span>
          <Button icon={<LogoutOutlined />} onClick={onLogout}>Logout</Button>
        </Header>
        <Content style={{ margin: 16, padding: 24, background: colorBgContainer, borderRadius: borderRadiusLG, minHeight: 'calc(100vh - 112px)' }}>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/products" element={<Products />} />
            <Route path="/companies" element={<Companies />} />
            <Route path="/inventory" element={<Inventory />} />
            <Route path="/purchase-orders" element={<PurchaseOrders />} />
            <Route path="/sales-orders" element={<SalesOrders />} />
            <Route path="/accounting" element={<Accounting />} />
            <Route path="/reports" element={<Reports />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
}

export default function App() {
  const [authed, setAuthed] = useState(!!localStorage.getItem('token'));

  const handleLogout = () => {
    localStorage.removeItem('token');
    setAuthed(false);
  };

  return (
    <BrowserRouter>
      {authed ? (
        <AppLayout onLogout={handleLogout} />
      ) : (
        <Login onLogin={() => setAuthed(true)} />
      )}
    </BrowserRouter>
  );
}
