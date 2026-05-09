import { useEffect, useState } from 'react';
import {
  Card, Table, Button, Modal, Form, Input, InputNumber, Select, DatePicker,
  Tag, Space, message, Tabs, Timeline, Spin, Popconfirm, TreeSelect,
} from 'antd';
import {
  PlusOutlined, EditOutlined, DeleteOutlined, SwapOutlined,
  HistoryOutlined, AimOutlined,
} from '@ant-design/icons';
import {
  getStockItems, createStockItem, adjustStock, moveStock, getStockTracking,
  getStockLocations, createStockLocation, updateStockLocation, deleteStockLocation,
} from '../api/inventory';
import { getProducts, getProductsBySku } from '../api/products';
import BarcodeScanner from '../components/BarcodeScanner';
import dayjs from 'dayjs';

const statusColors = { IN_STOCK: 'green', ALLOCATED: 'blue', SHIPPED: 'orange', DEPLETED: 'red' };

export default function Inventory() {
  const [stockItems, setStockItems] = useState([]);
  const [locations, setLocations] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [itemModal, setItemModal] = useState(false);
  const [adjustModal, setAdjustModal] = useState(null);
  const [moveModal, setMoveModal] = useState(null);
  const [trackingModal, setTrackingModal] = useState(null);
  const [tracking, setTracking] = useState([]);
  const [locModal, setLocModal] = useState(false);
  const [editLoc, setEditLoc] = useState(null);
  const [itemForm] = Form.useForm();
  const [adjustForm] = Form.useForm();
  const [moveForm] = Form.useForm();
  const [locForm] = Form.useForm();

  const load = () => {
    setLoading(true);
    Promise.all([getStockItems(), getStockLocations(), getProducts()])
      .then(([si, locs, prods]) => {
        setStockItems(si.data);
        setLocations(locs.data);
        setProducts(prods.data);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleCreateItem = async (values) => {
    const payload = {
      ...values,
      expiryDate: values.expiryDate?.format('YYYY-MM-DD'),
      productionDate: values.productionDate?.format('YYYY-MM-DD'),
    };
    await createStockItem(payload);
    message.success('Stock item created');
    setItemModal(false);
    itemForm.resetFields();
    load();
  };

  const handleBarcodeScan = async (sku) => {
    try {
      const { data } = await getProductsBySku(sku);
      if (data.length === 1) {
        itemForm.setFieldsValue({ productId: data[0].id });
        setItemModal(true);
        message.success(`Found: ${data[0].name} (${data[0].packaging})`);
      } else if (data.length > 1) {
        setItemModal(true);
        message.info(`Found ${data.length} packaging variants — please select one`);
      }
    } catch {
      message.error(`No product found for SKU: ${sku}`);
    }
  };

  const handleAdjust = async (values) => {
    await adjustStock(adjustModal.id, values);
    message.success('Stock adjusted');
    setAdjustModal(null);
    adjustForm.resetFields();
    load();
  };

  const handleMove = async (values) => {
    await moveStock(moveModal.id, values);
    message.success('Stock moved');
    setMoveModal(null);
    moveForm.resetFields();
    load();
  };

  const showTracking = async (record) => {
    setTrackingModal(record);
    const { data } = await getStockTracking(record.id);
    setTracking(data);
  };

  const handleCreateLoc = async (values) => {
    if (editLoc) {
      await updateStockLocation(editLoc.id, values);
      message.success('Location updated');
    } else {
      await createStockLocation(values);
      message.success('Location created');
    }
    setLocModal(false);
    setEditLoc(null);
    locForm.resetFields();
    load();
  };

  const handleDeleteLoc = async (id) => {
    await deleteStockLocation(id);
    message.success('Location deleted');
    load();
  };

  const buildLocationTree = (locs) => {
    const map = {};
    locs.forEach((l) => { map[l.id] = { ...l, title: l.name, value: l.id, children: [] }; });
    const roots = [];
    locs.forEach((l) => {
      if (l.parent?.id && map[l.parent.id]) map[l.parent.id].children.push(map[l.id]);
      else roots.push(map[l.id]);
    });
    return roots;
  };

  const locationTree = buildLocationTree(locations);

  const stockColumns = [
    { title: 'Product', dataIndex: ['product', 'name'], key: 'product', render: (_, r) => `${r.product.name} (${r.product.packaging})` },
    { title: 'SKU', dataIndex: ['product', 'sku'], key: 'sku', width: 180 },
    { title: 'Location', dataIndex: ['location', 'name'], key: 'location', width: 140, render: (v) => v || '—' },
    { title: 'Qty', dataIndex: 'quantity', key: 'qty', width: 80, align: 'right', render: (v) => Number(v).toFixed(0) },
    { title: 'Purchase Price', dataIndex: 'purchasePrice', key: 'pp', width: 120, align: 'right', render: (v) => v ? `$${Number(v).toFixed(2)}` : '—' },
    { title: 'Batch', dataIndex: 'batch', key: 'batch', width: 100, render: (v) => v || '—' },
    { title: 'Status', dataIndex: 'status', key: 'status', width: 100, render: (v) => <Tag color={statusColors[v]}>{v}</Tag> },
    {
      title: 'Actions', key: 'actions', width: 180, render: (_, record) => (
        <Space size="small">
          <Button size="small" icon={<EditOutlined />} onClick={() => { setAdjustModal(record); adjustForm.setFieldsValue({ newQuantity: record.quantity }); }}>Adjust</Button>
          <Button size="small" icon={<SwapOutlined />} onClick={() => setMoveModal(record)}>Move</Button>
          <Button size="small" icon={<HistoryOutlined />} onClick={() => showTracking(record)} />
        </Space>
      ),
    },
  ];

  const locColumns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Description', dataIndex: 'description', key: 'desc', render: (v) => v || '—' },
    { title: 'Parent', dataIndex: ['parent', 'name'], key: 'parent', render: (v) => v || '—' },
    {
      title: 'Actions', key: 'actions', width: 140, render: (_, record) => (
        <Space size="small">
          <Button size="small" icon={<EditOutlined />} onClick={() => {
            setEditLoc(record);
            locForm.setFieldsValue({ name: record.name, description: record.description, parentId: record.parent?.id });
            setLocModal(true);
          }} />
          <Popconfirm title="Delete this location?" onConfirm={() => handleDeleteLoc(record.id)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const tabItems = [
    {
      key: 'items',
      label: 'Stock Items',
      children: (
        <>
          <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
            <span style={{ fontSize: 14, color: '#888' }}>{stockItems.length} items</span>
            <Space>
              <BarcodeScanner onScan={handleBarcodeScan} buttonText="Scan to Add" buttonProps={{ type: 'default' }} />
              <Button type="primary" icon={<PlusOutlined />} onClick={() => setItemModal(true)}>Add Stock</Button>
            </Space>
          </div>
          <Table dataSource={stockItems} columns={stockColumns} rowKey="id" size="small"
            pagination={{ pageSize: 15, showSizeChanger: true }} scroll={{ x: 1000 }} loading={loading} />
        </>
      ),
    },
    {
      key: 'locations',
      label: 'Locations',
      children: (
        <>
          <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end' }}>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditLoc(null); locForm.resetFields(); setLocModal(true); }}>
              Add Location
            </Button>
          </div>
          <Table dataSource={locations} columns={locColumns} rowKey="id" size="small" pagination={false} loading={loading} />
        </>
      ),
    },
  ];

  return (
    <div>
      <h2 style={{ marginTop: 0, marginBottom: 20 }}>Inventory</h2>
      <Card size="small">
        <Tabs items={tabItems} />
      </Card>

      {/* Add Stock Item Modal */}
      <Modal title="Add Stock Item" open={itemModal} onCancel={() => setItemModal(false)} onOk={() => itemForm.submit()} width={520}>
        <Form form={itemForm} layout="vertical" onFinish={handleCreateItem}>
          <Form.Item name="productId" label="Product" rules={[{ required: true }]}>
            <Select showSearch optionFilterProp="label" placeholder="Select product"
              options={products.map((p) => ({ value: p.id, label: `${p.name} — ${p.packaging}` }))} />
          </Form.Item>
          <Form.Item name="locationId" label="Location">
            <TreeSelect treeData={locationTree} placeholder="Select location" allowClear />
          </Form.Item>
          <Form.Item name="quantity" label="Quantity" rules={[{ required: true }]}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="purchasePrice" label="Purchase Price">
            <InputNumber min={0} prefix="$" style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="batch" label="Batch"><Input /></Form.Item>
          <Form.Item name="productionDate" label="Production Date"><DatePicker style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="expiryDate" label="Expiry Date"><DatePicker style={{ width: '100%' }} /></Form.Item>
        </Form>
      </Modal>

      {/* Adjust Stock Modal */}
      <Modal title={`Adjust Stock — ${adjustModal?.product?.name || ''}`} open={!!adjustModal}
        onCancel={() => setAdjustModal(null)} onOk={() => adjustForm.submit()}>
        <Form form={adjustForm} layout="vertical" onFinish={handleAdjust}>
          <Form.Item name="newQuantity" label="New Quantity" rules={[{ required: true }]}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="notes" label="Reason"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>

      {/* Move Stock Modal */}
      <Modal title={`Move Stock — ${moveModal?.product?.name || ''}`} open={!!moveModal}
        onCancel={() => setMoveModal(null)} onOk={() => moveForm.submit()}>
        <Form form={moveForm} layout="vertical" onFinish={handleMove}>
          <Form.Item name="newLocationId" label="New Location" rules={[{ required: true }]}>
            <TreeSelect treeData={locationTree} placeholder="Select location" />
          </Form.Item>
          <Form.Item name="notes" label="Notes"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>

      {/* Tracking History Modal */}
      <Modal title={`Tracking History — ${trackingModal?.product?.name || ''}`} open={!!trackingModal}
        onCancel={() => { setTrackingModal(null); setTracking([]); }} footer={null} width={520}>
        {tracking.length > 0 ? (
          <Timeline items={tracking.map((t) => ({
            color: t.trackingType === 'RECEIVED' ? 'green' : t.trackingType === 'ADJUSTED' ? 'orange' : 'blue',
            children: (
              <div>
                <Tag>{t.trackingType}</Tag>
                <span style={{ fontWeight: 600 }}>{Number(t.quantityDelta) > 0 ? '+' : ''}{Number(t.quantityDelta).toFixed(0)}</span>
                {t.notes && <div style={{ color: '#888', fontSize: 12 }}>{t.notes}</div>}
                <div style={{ color: '#aaa', fontSize: 11 }}>{dayjs(t.createdAt).format('YYYY-MM-DD HH:mm')}</div>
              </div>
            ),
          }))} />
        ) : <Spin />}
      </Modal>
    </div>
  );
}
