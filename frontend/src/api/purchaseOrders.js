import client from './client';

export const getPurchaseOrders = (status) => client.get('/purchase-orders', { params: status ? { status } : {} });
export const getPurchaseOrder = (id) => client.get(`/purchase-orders/${id}`);
export const createPurchaseOrder = (data) => client.post('/purchase-orders', data);
export const placePurchaseOrder = (id) => client.post(`/purchase-orders/${id}/place`);
export const cancelPurchaseOrder = (id) => client.post(`/purchase-orders/${id}/cancel`);
export const receiveGoods = (id, data) => client.post(`/purchase-orders/${id}/receive`, data);
