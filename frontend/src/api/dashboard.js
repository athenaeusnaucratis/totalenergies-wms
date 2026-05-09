import client from './client';

export const getDashboardStats = () => client.get('/dashboard');
export const getStockByProduct = () => client.get('/dashboard/stock-by-product');
export const getLowStock = () => client.get('/dashboard/low-stock');
