import { useEffect, useState } from 'react';
import { Card, Table, Tabs, Spin, Statistic, Row, Col } from 'antd';
import { getTrialBalance, getProfitLoss, getProfitPerItem, getInventoryValuation } from '../api/accounting';

export default function Reports() {
  const [trialBalance, setTrialBalance] = useState([]);
  const [pl, setPl] = useState(null);
  const [profitPerItem, setProfitPerItem] = useState([]);
  const [valuation, setValuation] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getTrialBalance(), getProfitLoss(), getProfitPerItem(), getInventoryValuation()])
      .then(([tb, p, ppi, v]) => {
        setTrialBalance(tb.data);
        setPl(p.data);
        setProfitPerItem(ppi.data);
        setValuation(v.data);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spin size="large" style={{ display: 'block', marginTop: 100 }} />;

  const tbColumns = [
    { title: 'Code', dataIndex: 'account_code', width: 80 },
    { title: 'Account', dataIndex: 'name' },
    { title: 'Type', dataIndex: 'account_type', width: 100 },
    { title: 'Debit', dataIndex: 'total_debit', width: 120, align: 'right', render: v => `$${Number(v).toLocaleString()}` },
    { title: 'Credit', dataIndex: 'total_credit', width: 120, align: 'right', render: v => `$${Number(v).toLocaleString()}` },
    { title: 'Balance', dataIndex: 'balance', width: 120, align: 'right', render: v => <span style={{ color: v < 0 ? 'red' : 'inherit' }}>${Number(v).toLocaleString()}</span> },
  ];

  const ppiColumns = [
    { title: 'Product', dataIndex: 'name' },
    { title: 'Packaging', dataIndex: 'packaging', width: 100 },
    { title: 'Revenue', dataIndex: 'revenue', width: 120, align: 'right', render: v => `$${Number(v).toLocaleString()}` },
    { title: 'COGS', dataIndex: 'cogs', width: 120, align: 'right', render: v => `$${Number(v).toLocaleString()}` },
    { title: 'Profit', dataIndex: 'profit', width: 120, align: 'right', render: v => <span style={{ color: v < 0 ? 'red' : '#52c41a', fontWeight: 600 }}>${Number(v).toLocaleString()}</span> },
  ];

  const valColumns = [
    { title: 'Product', dataIndex: 'name' },
    { title: 'Packaging', dataIndex: 'packaging', width: 100 },
    { title: 'Avg Cost', dataIndex: 'avg_cost', width: 120, align: 'right', render: v => `$${Number(v).toFixed(2)}` },
    { title: 'Qty', dataIndex: 'qty', width: 80, align: 'right', render: v => Number(v).toFixed(0) },
    { title: 'Value', dataIndex: 'value', width: 120, align: 'right', render: v => `$${Number(v).toLocaleString()}` },
  ];

  const tabItems = [
    {
      key: 'pl', label: 'Profit & Loss', children: (
        <div>
          <Row gutter={16} style={{ marginBottom: 24 }}>
            <Col span={8}><Card><Statistic title="Revenue" value={pl?.revenue || 0} prefix="$" valueStyle={{ color: '#52c41a' }} /></Card></Col>
            <Col span={8}><Card><Statistic title="COGS" value={pl?.cogs || 0} prefix="$" valueStyle={{ color: '#ED1C24' }} /></Card></Col>
            <Col span={8}><Card><Statistic title="Gross Profit" value={pl?.grossProfit || 0} prefix="$" valueStyle={{ color: (pl?.grossProfit || 0) >= 0 ? '#52c41a' : '#ED1C24' }} /></Card></Col>
          </Row>
        </div>
      ),
    },
    { key: 'ppi', label: 'Profit per Item', children: <Table dataSource={profitPerItem} columns={ppiColumns} rowKey="sku" size="small" pagination={{ pageSize: 20 }} /> },
    { key: 'tb', label: 'Trial Balance', children: <Table dataSource={trialBalance} columns={tbColumns} rowKey="account_code" size="small" pagination={false} /> },
    { key: 'val', label: 'Inventory Valuation', children: <Table dataSource={valuation} columns={valColumns} rowKey="sku" size="small" pagination={{ pageSize: 20 }} /> },
  ];

  return (
    <div>
      <h2 style={{ marginTop: 0, marginBottom: 20 }}>Reports</h2>
      <Card size="small"><Tabs items={tabItems} /></Card>
    </div>
  );
}
