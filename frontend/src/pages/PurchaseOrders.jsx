import { useEffect, useState } from 'react';
import {
  Card, Table, Button, Modal, Form, Select, Input, InputNumber, DatePicker,
  Tag, Space, message, Descriptions, Popconfirm,
} from 'antd';
import { PlusOutlined, SendOutlined, StopOutlined, DownloadOutlined } from '@ant-design/icons';
import { getPurchaseOrders, createPurchaseOrder, placePurchaseOrder, cancelPurchaseOrder, receiveGoods } from '../api/purchaseOrders';
import { getCompanies } from '../api/companies';
import { getProducts } from '../api/products';
import { getStockLocations } from '../api/inventory';

const statusColors = { PENDING: 'gold', PLACED: 'blue', COMPLETE: 'green', CANCELLED: 'red' };

export default function PurchaseOrders() {
  const [orders, setOrders] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [products, setProducts] = useState([]);
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [createModal, setCreateModal] = useState(false);
  const [viewOrder, setViewOrder] = useState(null);
  const [receiveModal, setReceiveModal] = useState(null);
  const [form] = Form.useForm();
  const [receiveForm] = Form.useForm();

  const load = () => {
    setLoading(true);
    Promise.all([getPurchaseOrders(), getCompanies(), getProducts(), getStockLocations()])
      .then(([po, comp, prod, locs]) => {
        setOrders(po.data);
        setSuppliers(comp.data.filter(c => c.supplier));
        setProducts(prod.data);
        setLocations(locs.data);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async (values) => {
    const payload = {
      supplierId: values.supplierId,
      orderDate: values.orderDate?.format('YYYY-MM-DD'),
      expectedDate: values.expectedDate?.format('YYYY-MM-DD'),
      notes: values.notes,
      lines: values.lines,
    };
    await createPurchaseOrder(payload);
    message.success('Purchase Order created');
    setCreateModal(false);
    form.resetFields();
    load();
  };

  const handlePlace = async (id) => {
    await placePurchaseOrder(id);
    message.success('PO placed');
    load();
  };

  const handleCancel = async (id) => {
    await cancelPurchaseOrder(id);
    message.success('PO cancelled');
    load();
  };

  const openReceive = (order) => {
    setReceiveModal(order);
    receiveForm.setFieldsValue({
      lines: order.lines.map(l => ({
        poLineId: l.id,
        quantityReceived: l.quantity - l.receivedQty,
        locationId: null,
      })),
    });
  };

  const handleReceive = async (values) => {
    await receiveGoods(receiveModal.id, { receiptDate: values.receiptDate?.format('YYYY-MM-DD'), notes: values.notes, lines: values.lines });
    message.success('Goods received');
    setReceiveModal(null);
    receiveForm.resetFields();
    load();
  };

  const columns = [
    { title: 'PO #', dataIndex: 'id', key: 'id', width: 70 },
    { title: 'Supplier', dataIndex: ['supplier', 'name'], key: 'supplier' },
    { title: 'Date', dataIndex: 'orderDate', key: 'date', width: 110 },
    { title: 'Expected', dataIndex: 'expectedDate', key: 'exp', width: 110, render: v => v || '—' },
    { title: 'Items', key: 'items', width: 70, render: (_, r) => r.lines?.length || 0 },
    { title: 'Total', key: 'total', width: 110, align: 'right', render: (_, r) => '$' + (r.lines?.reduce((s, l) => s + l.quantity * l.unitPrice, 0) || 0).toLocaleString() },
    { title: 'Status', dataIndex: 'status', key: 'status', width: 100, render: v => <Tag color={statusColors[v]}>{v}</Tag> },
    {
      title: 'Actions', key: 'actions', width: 220, render: (_, r) => (
        <Space size="small">
          <Button size="small" onClick={() => setViewOrder(r)}>View</Button>
          {r.status === 'PENDING' && <Button size="small" icon={<SendOutlined />} onClick={() => handlePlace(r.id)}>Place</Button>}
          {r.status === 'PLACED' && <Button size="small" type="primary" icon={<DownloadOutlined />} onClick={() => openReceive(r)}>Receive</Button>}
          {r.status !== 'COMPLETE' && r.status !== 'CANCELLED' && (
            <Popconfirm title="Cancel this PO?" onConfirm={() => handleCancel(r.id)}>
              <Button size="small" danger icon={<StopOutlined />} />
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h2 style={{ marginTop: 0, marginBottom: 20 }}>Purchase Orders</h2>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end' }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModal(true)}>New Purchase Order</Button>
      </div>
      <Card size="small">
        <Table dataSource={orders} columns={columns} rowKey="id" size="small" loading={loading}
          pagination={{ pageSize: 15, showSizeChanger: true }} />
      </Card>

      <Modal title="New Purchase Order" open={createModal} onCancel={() => setCreateModal(false)} onOk={() => form.submit()} width={700} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="supplierId" label="Supplier" rules={[{ required: true }]}>
            <Select showSearch optionFilterProp="label" options={suppliers.map(s => ({ value: s.id, label: s.name }))} />
          </Form.Item>
          <Space>
            <Form.Item name="orderDate" label="Order Date"><DatePicker /></Form.Item>
            <Form.Item name="expectedDate" label="Expected Date"><DatePicker /></Form.Item>
          </Space>
          <Form.Item name="notes" label="Notes"><Input.TextArea rows={2} /></Form.Item>
          <Form.List name="lines" initialValue={[{}]}>
            {(fields, { add, remove }) => (
              <>
                {fields.map(({ key, name }) => (
                  <Space key={key} align="start" style={{ display: 'flex', marginBottom: 8 }}>
                    <Form.Item name={[name, 'productId']} rules={[{ required: true }]}>
                      <Select style={{ width: 250 }} showSearch optionFilterProp="label" placeholder="Product"
                        options={products.map(p => ({ value: p.id, label: `${p.name} — ${p.packaging}` }))} />
                    </Form.Item>
                    <Form.Item name={[name, 'quantity']} rules={[{ required: true }]}>
                      <InputNumber placeholder="Qty" min={1} />
                    </Form.Item>
                    <Form.Item name={[name, 'unitPrice']} rules={[{ required: true }]}>
                      <InputNumber placeholder="Unit Price" min={0} prefix="$" />
                    </Form.Item>
                    {fields.length > 1 && <Button danger onClick={() => remove(name)}>X</Button>}
                  </Space>
                ))}
                <Button type="dashed" onClick={() => add()} block icon={<PlusOutlined />}>Add Line</Button>
              </>
            )}
          </Form.List>
        </Form>
      </Modal>

      <Modal title={`Purchase Order #${viewOrder?.id}`} open={!!viewOrder} onCancel={() => setViewOrder(null)} footer={null} width={700}>
        {viewOrder && (
          <>
            <Descriptions size="small" column={2} bordered>
              <Descriptions.Item label="Supplier">{viewOrder.supplier?.name}</Descriptions.Item>
              <Descriptions.Item label="Status"><Tag color={statusColors[viewOrder.status]}>{viewOrder.status}</Tag></Descriptions.Item>
              <Descriptions.Item label="Order Date">{viewOrder.orderDate}</Descriptions.Item>
              <Descriptions.Item label="Expected">{viewOrder.expectedDate || '—'}</Descriptions.Item>
              <Descriptions.Item label="Notes" span={2}>{viewOrder.notes || '—'}</Descriptions.Item>
            </Descriptions>
            <Table dataSource={viewOrder.lines} rowKey="id" size="small" pagination={false} style={{ marginTop: 16 }}
              columns={[
                { title: 'Product', dataIndex: ['product', 'name'], render: (_, l) => `${l.product.name} (${l.product.packaging})` },
                { title: 'Qty', dataIndex: 'quantity', width: 80, align: 'right' },
                { title: 'Unit Price', dataIndex: 'unitPrice', width: 100, align: 'right', render: v => `$${v}` },
                { title: 'Total', key: 'total', width: 100, align: 'right', render: (_, l) => `$${(l.quantity * l.unitPrice).toLocaleString()}` },
                { title: 'Received', dataIndex: 'receivedQty', width: 90, align: 'right' },
              ]} />
          </>
        )}
      </Modal>

      <Modal title={`Receive Goods — PO #${receiveModal?.id}`} open={!!receiveModal}
        onCancel={() => setReceiveModal(null)} onOk={() => receiveForm.submit()} width={600} destroyOnClose>
        <Form form={receiveForm} layout="vertical" onFinish={handleReceive}>
          <Form.Item name="receiptDate" label="Receipt Date"><DatePicker /></Form.Item>
          <Form.Item name="notes" label="Notes"><Input.TextArea rows={2} /></Form.Item>
          <Form.List name="lines">
            {(fields) => fields.map(({ key, name }) => {
              const line = receiveModal?.lines?.[name];
              return (
                <Card size="small" key={key} style={{ marginBottom: 8 }} title={`${line?.product?.name} (${line?.product?.packaging})`}>
                  <Form.Item name={[name, 'poLineId']} hidden><Input /></Form.Item>
                  <Space>
                    <Form.Item name={[name, 'quantityReceived']} label="Qty Received" rules={[{ required: true }]}>
                      <InputNumber min={0} />
                    </Form.Item>
                    <Form.Item name={[name, 'locationId']} label="Location">
                      <Select style={{ width: 200 }} allowClear placeholder="Location"
                        options={locations.map(l => ({ value: l.id, label: l.name }))} />
                    </Form.Item>
                  </Space>
                </Card>
              );
            })}
          </Form.List>
        </Form>
      </Modal>
    </div>
  );
}
