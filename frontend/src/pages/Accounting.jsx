import { useEffect, useState } from 'react';
import { Card, Table, Tag, Tabs, Spin, Descriptions, Modal, Button, message } from 'antd';
import { getGlAccounts, getJournalEntries, getInvoices, payInvoice } from '../api/accounting';

const typeColors = { ASSET: 'blue', LIABILITY: 'red', EQUITY: 'purple', REVENUE: 'green', EXPENSE: 'orange' };
const invStatusColors = { OPEN: 'gold', PAID: 'green', CANCELLED: 'red' };

export default function Accounting() {
  const [accounts, setAccounts] = useState([]);
  const [journals, setJournals] = useState([]);
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [viewJournal, setViewJournal] = useState(null);
  const [viewInvoice, setViewInvoice] = useState(null);

  const load = () => {
    setLoading(true);
    Promise.all([getGlAccounts(), getJournalEntries(), getInvoices()])
      .then(([ga, je, inv]) => {
        setAccounts(ga.data);
        setJournals(je.data);
        setInvoices(inv.data);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handlePay = async (id) => {
    await payInvoice(id);
    message.success('Invoice marked as paid');
    load();
  };

  if (loading) return <Spin size="large" style={{ display: 'block', marginTop: 100 }} />;

  const coaColumns = [
    { title: 'Code', dataIndex: 'accountCode', key: 'code', width: 80 },
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Type', dataIndex: 'accountType', key: 'type', width: 120, render: v => <Tag color={typeColors[v]}>{v}</Tag> },
    { title: 'Description', dataIndex: 'description', key: 'desc', render: v => v || '—' },
  ];

  const journalColumns = [
    { title: '#', dataIndex: 'id', key: 'id', width: 60 },
    { title: 'Date', dataIndex: 'transDate', key: 'date', width: 110 },
    { title: 'Description', dataIndex: 'description', key: 'desc' },
    { title: 'Source', dataIndex: 'sourceType', key: 'src', width: 140, render: (v, r) => v ? `${v} #${r.sourceId}` : '—' },
    { title: 'Entries', key: 'entries', width: 70, render: (_, r) => r.entries?.length || 0 },
    { title: 'Posted', dataIndex: 'posted', key: 'posted', width: 80, render: v => <Tag color={v ? 'green' : 'gold'}>{v ? 'Yes' : 'No'}</Tag> },
    { title: '', key: 'view', width: 60, render: (_, r) => <Button size="small" onClick={() => setViewJournal(r)}>View</Button> },
  ];

  const invoiceColumns = [
    { title: '#', dataIndex: 'id', key: 'id', width: 60 },
    { title: 'Type', dataIndex: 'invoiceType', key: 'type', width: 90, render: v => <Tag color={v === 'SALES' ? 'blue' : 'orange'}>{v}</Tag> },
    { title: 'Company', dataIndex: ['company', 'name'], key: 'company' },
    { title: 'Date', dataIndex: 'invoiceDate', key: 'date', width: 110 },
    { title: 'Amount', dataIndex: 'totalAmount', key: 'amount', width: 120, align: 'right', render: v => `$${Number(v).toLocaleString()}` },
    { title: 'Status', dataIndex: 'status', key: 'status', width: 90, render: v => <Tag color={invStatusColors[v]}>{v}</Tag> },
    {
      title: 'Actions', key: 'actions', width: 130, render: (_, r) => (
        <>
          <Button size="small" onClick={() => setViewInvoice(r)} style={{ marginRight: 4 }}>View</Button>
          {r.status === 'OPEN' && <Button size="small" type="primary" onClick={() => handlePay(r.id)}>Pay</Button>}
        </>
      ),
    },
  ];

  const tabItems = [
    { key: 'coa', label: 'Chart of Accounts', children: <Table dataSource={accounts} columns={coaColumns} rowKey="id" size="small" pagination={false} /> },
    { key: 'journals', label: 'Journal Entries', children: <Table dataSource={journals} columns={journalColumns} rowKey="id" size="small" pagination={{ pageSize: 20 }} /> },
    { key: 'invoices', label: 'Invoices', children: <Table dataSource={invoices} columns={invoiceColumns} rowKey="id" size="small" pagination={{ pageSize: 20 }} /> },
  ];

  return (
    <div>
      <h2 style={{ marginTop: 0, marginBottom: 20 }}>Accounting</h2>
      <Card size="small"><Tabs items={tabItems} /></Card>

      <Modal title={`Journal Entry #${viewJournal?.id}`} open={!!viewJournal} onCancel={() => setViewJournal(null)} footer={null} width={600}>
        {viewJournal && (
          <>
            <Descriptions size="small" column={2} bordered>
              <Descriptions.Item label="Date">{viewJournal.transDate}</Descriptions.Item>
              <Descriptions.Item label="Source">{viewJournal.sourceType} #{viewJournal.sourceId}</Descriptions.Item>
              <Descriptions.Item label="Description" span={2}>{viewJournal.description}</Descriptions.Item>
            </Descriptions>
            <Table dataSource={viewJournal.entries} rowKey="id" size="small" pagination={false} style={{ marginTop: 16 }}
              columns={[
                { title: 'Account', dataIndex: ['glAccount', 'name'], render: (v, r) => `${r.glAccount?.accountCode} — ${v}` },
                { title: 'Debit', dataIndex: 'debitAmount', width: 120, align: 'right', render: v => v > 0 ? `$${Number(v).toLocaleString()}` : '' },
                { title: 'Credit', dataIndex: 'creditAmount', width: 120, align: 'right', render: v => v > 0 ? `$${Number(v).toLocaleString()}` : '' },
                { title: 'Description', dataIndex: 'description' },
              ]} />
          </>
        )}
      </Modal>

      <Modal title={`Invoice #${viewInvoice?.id}`} open={!!viewInvoice} onCancel={() => setViewInvoice(null)} footer={null} width={600}>
        {viewInvoice && (
          <>
            <Descriptions size="small" column={2} bordered>
              <Descriptions.Item label="Type"><Tag>{viewInvoice.invoiceType}</Tag></Descriptions.Item>
              <Descriptions.Item label="Status"><Tag color={invStatusColors[viewInvoice.status]}>{viewInvoice.status}</Tag></Descriptions.Item>
              <Descriptions.Item label="Company">{viewInvoice.company?.name}</Descriptions.Item>
              <Descriptions.Item label="Total">${Number(viewInvoice.totalAmount).toLocaleString()}</Descriptions.Item>
            </Descriptions>
            <Table dataSource={viewInvoice.items} rowKey="id" size="small" pagination={false} style={{ marginTop: 16 }}
              columns={[
                { title: 'Product ID', dataIndex: 'productId', width: 100 },
                { title: 'Qty', dataIndex: 'quantity', width: 80, align: 'right' },
                { title: 'Unit Price', dataIndex: 'unitPrice', width: 100, align: 'right', render: v => `$${v}` },
                { title: 'Amount', dataIndex: 'amount', width: 120, align: 'right', render: v => `$${Number(v).toLocaleString()}` },
              ]} />
          </>
        )}
      </Modal>
    </div>
  );
}
