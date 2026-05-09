import { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, message, Popconfirm, Space, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { getProducts, createProduct, updateProduct, deleteProduct, getCategories } from '../api/products';

export default function Products() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form] = Form.useForm();

  const load = async () => {
    setLoading(true);
    try {
      const [prodRes, catRes] = await Promise.all([getProducts(), getCategories()]);
      setProducts(prodRes.data);
      setCategories(catRes.data);
    } catch {
      message.error('Failed to load products');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleSubmit = async (values) => {
    try {
      if (editing) {
        await updateProduct(editing.id, values);
        message.success('Product updated');
      } else {
        await createProduct(values);
        message.success('Product created');
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
    form.setFieldsValue({ ...record, categoryId: record.category?.id });
    setModalOpen(true);
  };

  const handleDelete = async (id) => {
    await deleteProduct(id);
    message.success('Deleted');
    load();
  };

  const columns = [
    { title: 'Code', dataIndex: 'sku', key: 'sku', width: 180, ellipsis: true },
    { title: 'Product', dataIndex: 'name', key: 'name' },
    { title: 'Packaging', dataIndex: 'packaging', key: 'packaging', width: 120 },
    {
      title: 'Category', key: 'category', width: 140,
      render: (_, r) => r.category ? <Tag color="blue">{r.category.name}</Tag> : '-',
    },
    {
      title: 'C.P', dataIndex: 'costPrice', key: 'costPrice', width: 90, align: 'right',
      render: (v) => v ? `$${Number(v).toFixed(0)}` : '-',
    },
    {
      title: 'S.P', dataIndex: 'sellingPrice', key: 'sellingPrice', width: 90, align: 'right',
      render: (v) => v ? `$${Number(v).toFixed(0)}` : '-',
    },
    {
      title: 'P.Diff', dataIndex: 'priceDifference', key: 'priceDifference', width: 90, align: 'right',
      render: (v) => {
        const n = Number(v);
        return <span style={{ color: n > 0 ? '#52c41a' : n < 0 ? '#ff4d4f' : undefined }}>${n.toFixed(0)}</span>;
      },
    },
    {
      title: '%', dataIndex: 'marginPercent', key: 'marginPercent', width: 70, align: 'right',
      render: (v) => `${Number(v).toFixed(1)}%`,
    },
    { title: 'Min Stock', dataIndex: 'minimumStock', key: 'minimumStock', width: 90, align: 'center' },
    {
      title: 'Actions', key: 'actions', width: 100, align: 'center',
      render: (_, record) => (
        <Space>
          <Button icon={<EditOutlined />} size="small" onClick={() => handleEdit(record)} />
          <Popconfirm title="Delete?" onConfirm={() => handleDelete(record.id)}>
            <Button icon={<DeleteOutlined />} size="small" danger />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>Products</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditing(null); form.resetFields(); setModalOpen(true); }}>
          Add Product
        </Button>
      </div>
      <Table
        dataSource={products}
        columns={columns}
        rowKey="id"
        loading={loading}
        size="middle"
        scroll={{ x: 1100 }}
        pagination={{ pageSize: 25, showSizeChanger: true, showTotal: (t) => `${t} products` }}
      />
      <Modal
        title={editing ? 'Edit Product' : 'New Product'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        destroyOnClose
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="name" label="Product Name" rules={[{ required: true }]}><Input /></Form.Item>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <Form.Item name="sku" label="Code / SKU" rules={[{ required: true }]}><Input /></Form.Item>
            <Form.Item name="packaging" label="Packaging" rules={[{ required: true }]}>
              <Input placeholder="e.g. 3X4L, 208L, 12X1L" />
            </Form.Item>
          </div>
          <Form.Item name="description" label="Description"><Input.TextArea rows={2} /></Form.Item>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 16 }}>
            <Form.Item name="costPrice" label="Cost Price ($)">
              <InputNumber min={0} precision={2} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="sellingPrice" label="Selling Price ($)">
              <InputNumber min={0} precision={2} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="categoryId" label="Category">
              <Select allowClear placeholder="Select" options={categories.map(c => ({ label: c.name, value: c.id }))} />
            </Form.Item>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 16 }}>
            <Form.Item name="unit" label="Unit" initialValue="EACH">
              <Select options={['EACH', 'KG', 'L', 'M', 'BOX'].map(u => ({ label: u, value: u }))} />
            </Form.Item>
            <Form.Item name="minimumStock" label="Min Stock" initialValue={0}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
          </div>
        </Form>
      </Modal>
    </div>
  );
}
