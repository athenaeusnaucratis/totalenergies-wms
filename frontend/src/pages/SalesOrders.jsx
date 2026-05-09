import { useEffect, useState } from 'react';
import {
  Card, Table, Button, Modal, Form, Select, Input, InputNumber, DatePicker,
  Tag, Space, message, Descriptions, Popconfirm,
} from 'antd';
import { PlusOutlined, CheckOutlined, SendOutlined, StopOutlined, CarOutlined } from '@ant-design/icons';
import { getSalesOrders, createSalesOrder, confirmSalesOrder, allocateSalesOrder, shipSalesOrder, completeSalesOrder, cancelSalesOrder } from '../api/salesOrders';
import { getCompanies } from '../api/companies';
import { getProducts } from '../api/products';
import { getStockItems } from '../api/inventory';

const statusColors = { PENDING: 'gold', CONFIRMED: 'cyan', ALLOCATED: 'blue', SHIPPED: 'orange', COMPLETE: 'green', CANCELLED: 'red' };

export default function SalesOrders() {
  const [orders, setOrders] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [stockItems, setStockItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [createModal, setCreateModal] = useState(false);
  const [viewOrder, setViewOrder] = useState(null);
  const [allocateModal, setAllocateModal] = useState(null);
  const [shipModal, setShipModal] = useState(null);
  const [form] = Form.useForm();
  const [allocForm] = Form.useForm();
  const [shipForm] = Form.useForm();

  const load = () => {
    setLoading(true);
    Promise.all([getSalesOrders(), getCompanies(), getProducts(), getStockItems({ status: 'IN_STOCK' })])
      .then(([so, comp, prod, si]) => {
        setOrders(so.data);
        setCustomers(comp.data.filter(c => c.customer));
        setProducts(prod.data);
        setStockItems(si.data);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async (values) => {
    await createSalesOrder({
      customerId: values.customerId,
      orderDate: values.orderDate?.format('YYYY-MM-DD'),
      notes: values.notes,
      lines: values.lines,
    });
    message.success('Sales Order created');
    setCreateModal(false);
    form.resetFields();
    load();
  };

  const handleAllocate = async (values) => {
    await allocateSalesOrder(allocateModal.id, values.allocations);
    message.success('Stock allocated');
    setAllocateModal(null);
    allocForm.resetFields();
    load();
  };

  const handleShip = async (values) => {
    await shipSalesOrder(shipModal.id, {
      shipmentDate: values.shipmentDate?.format('YYYY-MM-DD'),
      trackingNumber: values.trackingNumber,
      notes: values.notes,
    });
    message.success('Order shipped & invoice generated');
    setShipModal(null);
    shipForm.resetFields();
    load();
  };

  const columns = [
    { title: 'SO #', dataIndex: 'id', key: 'id', width: 70 },
    { title: 'Customer', dataIndex: ['customer', 'name'], key: 'customer' },
    { title: 'Date', dataIndex: 'orderDate', key: 'date', width: 110 },
    { title: 'Items', key: 'items', width: 70, render: (_, r) => r.lines?.length || 0 },
    { title: 'Total', key: 'total', width: 110, align: 'right', render: (_, r) => '$' + (r.lines?.reduce((s, l) => s + l.quantity * l.unitPrice, 0) || 0).toLocaleString() },
    { title: 'Status', dataIndex: 'status', key: 'status', width: 110, render: v => <Tag color={statusColors[v]}>{v}</Tag> },
    {
      title: 'Actions', key: 'actions', width: 280, render: (_, r) => (
        <Space size="small">
          <Button size="small" onClick={() => setViewOrder(r)}>View</Button>
          {r.status === 'PENDING' && <Button size="small" icon={<CheckOutlined />} onClick={() => confirmSalesOrder(r.id).then(() => { message.success('Confirmed'); load(); })}>Confirm</Button>}
          {(r.status === 'CONFIRMED' || r.status === 'ALLOCATED') && <Button size="small" icon={<SendOutlined />} onClick={() => { setAllocateModal(r); allocForm.resetFields(); }}>Allocate</Button>}
          {r.status === 'ALLOCATED' && <Button size="small" type="primary" icon={<CarOutlined />} onClick={() => setShipModal(r)}>Ship</Button>}
          {r.status === 'SHIPPED' && <Button size="small" onClick={() => completeSalesOrder(r.id).then(() => { message.success('Completed'); load(); })}>Complete</Button>}
          {!['SHIPPED', 'COMPLETE', 'CANCELLED'].includes(r.status) && (
            <Popconfirm title="Cancel?" onConfirm={() => cancelSalesOrder(r.id).then(() => { message.success('Cancelled'); load(); })}>
              <Button size="small" danger icon={<StopOutlined />} />
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h2 style={{ marginTop: 0, marginBottom: 20 }}>Sales Orders</h2>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end' }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModal(true)}>New Sales Order</Button>
      </div>
      <Card size="small">
        <Table dataSource={orders} columns={columns} rowKey="id" size="small" loading={loading}
          pagination={{ pageSize: 15, showSizeChanger: true }} />
      </Card>

      <Modal title="New Sales Order" open={createModal} onCancel={() => setCreateModal(false)} onOk={() => form.submit()} width={700} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="customerId" label="Customer" rules={[{ required: true }]}>
            <Select showSearch optionFilterProp="label" options={customers.map(c => ({ value: c.id, label: c.name }))} />
          </Form.Item>
          <Space>
            <Form.Item name="orderDate" label="Order Date"><DatePicker /></Form.Item>
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

      <Modal title={`Sales Order #${viewOrder?.id}`} open={!!viewOrder} onCancel={() => setViewOrder(null)} footer={null} width={700}>
        {viewOrder && (
          <>
            <Descriptions size="small" column={2} bordered>
              <Descriptions.Item label="Customer">{viewOrder.customer?.name}</Descriptions.Item>
              <Descriptions.Item label="Status"><Tag color={statusColors[viewOrder.status]}>{viewOrder.status}</Tag></Descriptions.Item>
              <Descriptions.Item label="Order Date">{viewOrder.orderDate}</Descriptions.Item>
              <Descriptions.Item label="Notes">{viewOrder.notes || '—'}</Descriptions.Item>
            </Descriptions>
            <Table dataSource={viewOrder.lines} rowKey="id" size="small" pagination={false} style={{ marginTop: 16 }}
              columns={[
                { title: 'Product', dataIndex: ['product', 'name'], render: (_, l) => `${l.product.name} (${l.product.packaging})` },
                { title: 'Qty', dataIndex: 'quantity', width: 80, align: 'right' },
                { title: 'Price', dataIndex: 'unitPrice', width: 100, align: 'right', render: v => `$${v}` },
                { title: 'Total', key: 'total', width: 100, align: 'right', render: (_, l) => `$${(l.quantity * l.unitPrice).toLocaleString()}` },
                { title: 'Allocated', dataIndex: 'allocatedQty', width: 90, align: 'right' },
                { title: 'Shipped', dataIndex: 'shippedQty', width: 90, align: 'right' },
              ]} />
          </>
        )}
      </Modal>

      <Modal title={`Allocate Stock — SO #${allocateModal?.id}`} open={!!allocateModal}
        onCancel={() => setAllocateModal(null)} onOk={() => allocForm.submit()} width={600} destroyOnClose>
        <Form form={allocForm} layout="vertical" onFinish={handleAllocate}>
          <Form.List name="allocations" initialValue={allocateModal?.lines?.map(l => ({ soLineId: l.id })) || []}>
            {(fields) => fields.map(({ key, name }) => {
              const line = allocateModal?.lines?.[name];
              const available = stockItems.filter(si => si.product?.id === line?.product?.id);
              return (
                <Card size="small" key={key} style={{ marginBottom: 8 }} title={`${line?.product?.name} (${line?.product?.packaging}) — need ${line?.quantity - line?.allocatedQty}`}>
                  <Form.Item name={[name, 'soLineId']} hidden><Input /></Form.Item>
                  <Space>
                    <Form.Item name={[name, 'stockItemId']} label="Stock Item" rules={[{ required: true }]}>
                      <Select style={{ width: 250 }} placeholder="Select stock"
                        options={available.map(si => ({ value: si.id, label: `#${si.id} — Qty: ${si.quantity} @ ${si.location?.name || 'No loc'}` }))} />
                    </Form.Item>
                    <Form.Item name={[name, 'quantity']} label="Qty" rules={[{ required: true }]}>
                      <InputNumber min={1} />
                    </Form.Item>
                  </Space>
                </Card>
              );
            })}
          </Form.List>
        </Form>
      </Modal>

      <Modal title={`Ship — SO #${shipModal?.id}`} open={!!shipModal}
        onCancel={() => setShipModal(null)} onOk={() => shipForm.submit()} destroyOnClose>
        <Form form={shipForm} layout="vertical" onFinish={handleShip}>
          <Form.Item name="shipmentDate" label="Shipment Date"><DatePicker /></Form.Item>
          <Form.Item name="trackingNumber" label="Tracking Number"><Input /></Form.Item>
          <Form.Item name="notes" label="Notes"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
