import client from './client';

export const getProducts = (categoryId) =>
  client.get('/products', { params: categoryId ? { categoryId } : {} });

export const getProduct = (id) => client.get(`/products/${id}`);
export const getProductsBySku = (sku) => client.get(`/products/sku/${sku}`);
export const createProduct = (data) => client.post('/products', data);
export const updateProduct = (id, data) => client.put(`/products/${id}`, data);
export const deleteProduct = (id) => client.delete(`/products/${id}`);

export const getCategories = () => client.get('/product-categories');
export const createCategory = (data) => client.post('/product-categories', data);
export const updateCategory = (id, data) => client.put(`/product-categories/${id}`, data);
export const deleteCategory = (id) => client.delete(`/product-categories/${id}`);
