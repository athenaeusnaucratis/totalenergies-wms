import { useEffect, useState } from 'react';
import { Card, Col, Row, Statistic, Table, Tag, Spin, Empty } from 'antd';
import {
  ShoppingCartOutlined, InboxOutlined, DollarOutlined, TeamOutlined,
  WarningOutlined, StockOutlined,
} from '@ant-design/icons';
import { getDashboardStats, getStockByProduct, getLowStock } from '../api/dashboard';

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [stockByProduct, setStockByProduct] = useState([]);
  const [lowStock, setLowStock] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getDashboardStats(), getStockByProduct(), getLowStock()])
      .then(([s, sp, ls]) => {
        setStats(s.data);
        setStockByProduct(sp.data);
        setLowStock(ls.data);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spin size="large" style={{ display: 'block', marginTop: 100 }} />;

  const kpis = [
    { title: 'Total Products', value: stats?.totalProducts || 0, icon: <InboxOutlined />, color: '#1A1F71' },
    { title: 'Companies', value: stats?.totalCompanies || 0, icon: <TeamOutlined />, color: '#1A1F71' },
    { title: 'Open POs', value: stats?.openPurchaseOrders || 0, icon: <ShoppingCartOutlined />, color: '#ED1C24' },
    { title: 'Open SOs', value: stats?.openSalesOrders || 0, icon: <DollarOutlined />, color: '#ED1C24' },
    { title: 'Stock Value (Cost)', value: stats?.stockCostValue || 0, icon: <StockOutlined />, color: '#1A1F71', prefix: '$', formatter: true },
    { title: 'Stock Value (Sell)', value: stats?.stockSellingValue || 0, icon: <DollarOutlined />, color: '#52c41a', prefix: '$', formatter: true },
  ];

  const stockColumns = [
    { title: 'Product', dataIndex: 'name', key: 'name' },
    { title: 'Packaging', dataIndex: 'packaging', key: 'packaging', width: 110 },
    { title: 'Stock', dataIndex: 'stock', key: 'stock', width: 80, align: 'right', render: (v) => Number(v).toFixed(0) },
    { title: 'C.P', dataIndex: 'cost_price', key: 'cp', width: 80, align: 'right', render: (v) => v > 0 ? `$${Number(v).toFixed(0)}` : '-' },
    { title: 'S.P', dataIndex: 'selling_price', key: 'sp', width: 80, align: 'right', render: (v) => `$${Number(v).toFixed(0)}` },
    { title: 'Stock Value (Cost)', dataIndex: 'stock_cost_value', key: 'scv', width: 140, align: 'right', render: (v) => `$${Number(v).toLocaleString()}` },
    { title: 'Stock Value (Sell)', dataIndex: 'stock_selling_value', key: 'ssv', width: 140, align: 'right', render: (v) => `$${Number(v).toLocaleString()}` },
  ];

  const lowStockColumns = [
    { title: 'Product', dataIndex: 'name', key: 'name' },
    { title: 'Packaging', dataIndex: 'packaging', key: 'packaging', width: 110 },
    { title: 'Current Stock', dataIndex: 'current_stock', key: 'current', width: 120, align: 'right', render: (v) => <Tag color="red">{Number(v).toFixed(0)}</Tag> },
    { title: 'Minimum Required', dataIndex: 'minimum_stock', key: 'min', width: 140, align: 'right' },
  ];

  return (
    <div>
      <h2 style={{ marginTop: 0, marginBottom: 20 }}>Dashboard</h2>

      <Row gutter={[16, 16]}>
        {kpis.map((kpi, i) => (
          <Col xs={12} sm={8} lg={4} key={i}>
            <Card className="te-stat-card" size="small">
              <Statistic
                title={kpi.title}
                value={kpi.value}
                prefix={kpi.icon}
                valueStyle={{ color: kpi.color, fontSize: 24 }}
                formatter={kpi.formatter ? (v) => `${kpi.prefix}${Number(v).toLocaleString()}` : undefined}
              />
            </Card>
          </Col>
        ))}
      </Row>

      {lowStock.length > 0 && (
        <Card
          title={<span><WarningOutlined style={{ color: '#ED1C24', marginRight: 8 }} />Low Stock Alerts ({lowStock.length})</span>}
          style={{ marginTop: 24 }}
          size="small"
        >
          <Table dataSource={lowStock} columns={lowStockColumns} rowKey="sku" size="small" pagination={false} />
        </Card>
      )}

      <Card title="Stock on Hand by Product" style={{ marginTop: 24 }} size="small">
        {stockByProduct.length > 0 ? (
          <Table
            dataSource={stockByProduct}
            columns={stockColumns}
            rowKey="sku"
            size="small"
            pagination={{ pageSize: 15, showSizeChanger: true }}
            scroll={{ x: 800 }}
          />
        ) : (
          <Empty description="No stock data yet — receive goods from a Purchase Order to see stock" />
        )}
      </Card>
    </div>
  );
}
