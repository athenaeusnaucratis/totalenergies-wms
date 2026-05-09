import { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Checkbox, message, Popconfirm, Space, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { getCompanies, createCompany, updateCompany, deleteCompany } from '../api/companies';

export default function Companies() {
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form] = Form.useForm();

  const load = async () => {
    setLoading(true);
    try {
      const { data } = await getCompanies();
      setCompanies(data);
    } catch {
      message.error('Failed to load');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleSubmit = async (values) => {
    try {
      if (editing) {
        await updateCompany(editing.id, values);
      } else {
        await createCompany(values);
      }
      setModalOpen(false);
      form.resetFields();
      setEditing(null);
      load();
    } catch (err) {
      message.error(err.response?.data?.error || 'Failed');
    }
  };

  const handleEdit = (record) => {
    setEditing(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'Phone', dataIndex: 'phone', key: 'phone' },
    {
      title: 'Type', key: 'type',
      render: (_, r) => (
        <Space>
          {r.supplier && <Tag color="blue">Supplier</Tag>}
          {r.customer && <Tag color="green">Customer</Tag>}
        </Space>
      ),
    },
    {
      title: 'Actions', key: 'actions',
      render: (_, record) => (
        <Space>
          <Button icon={<EditOutlined />} size="small" onClick={() => handleEdit(record)} />
          <Popconfirm title="Delete?" onConfirm={async () => { await deleteCompany(record.id); load(); }}>
            <Button icon={<DeleteOutlined />} size="small" danger />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h2>Companies</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditing(null); form.resetFields(); setModalOpen(true); }}>
          Add Company
        </Button>
      </div>
      <Table dataSource={companies} columns={columns} rowKey="id" loading={loading} />
      <Modal title={editing ? 'Edit Company' : 'New Company'} open={modalOpen} onCancel={() => setModalOpen(false)} onOk={() => form.submit()} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="email" label="Email"><Input /></Form.Item>
          <Form.Item name="phone" label="Phone"><Input /></Form.Item>
          <Form.Item name="address" label="Address"><Input.TextArea rows={2} /></Form.Item>
          <Form.Item name="supplier" valuePropName="checked"><Checkbox>Supplier</Checkbox></Form.Item>
          <Form.Item name="customer" valuePropName="checked"><Checkbox>Customer</Checkbox></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
