import client from './client';

export const getSalesOrders = (status) => client.get('/sales-orders', { params: status ? { status } : {} });
export const getSalesOrder = (id) => client.get(`/sales-orders/${id}`);
export const createSalesOrder = (data) => client.post('/sales-orders', data);
export const confirmSalesOrder = (id) => client.post(`/sales-orders/${id}/confirm`);
export const allocateSalesOrder = (id, data) => client.post(`/sales-orders/${id}/allocate`, data);
export const shipSalesOrder = (id, data) => client.post(`/sales-orders/${id}/ship`, data);
export const completeSalesOrder = (id) => client.post(`/sales-orders/${id}/complete`);
export const cancelSalesOrder = (id) => client.post(`/sales-orders/${id}/cancel`);
