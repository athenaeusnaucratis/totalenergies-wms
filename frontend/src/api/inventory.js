import client from './client';

export const getStockLocations = () => client.get('/stock-locations');
export const getRootLocations = () => client.get('/stock-locations/roots');
export const createStockLocation = (data) => client.post('/stock-locations', data);
export const updateStockLocation = (id, data) => client.put(`/stock-locations/${id}`, data);
export const deleteStockLocation = (id) => client.delete(`/stock-locations/${id}`);

export const getStockItems = (params) => client.get('/stock-items', { params });
export const getStockItem = (id) => client.get(`/stock-items/${id}`);
export const createStockItem = (data) => client.post('/stock-items', data);
export const adjustStock = (id, data) => client.post(`/stock-items/${id}/adjust`, data);
export const moveStock = (id, data) => client.post(`/stock-items/${id}/move`, data);
export const getStockTracking = (id) => client.get(`/stock-items/${id}/tracking`);
