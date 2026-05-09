import client from './client';

export const getGlAccounts = () => client.get('/gl-accounts');
export const getJournalEntries = () => client.get('/journal-entries');
export const getInvoices = (type) => client.get('/invoices', { params: type ? { type } : {} });
export const getInvoice = (id) => client.get(`/invoices/${id}`);
export const payInvoice = (id) => client.post(`/invoices/${id}/pay`);
export const getTrialBalance = () => client.get('/reports/trial-balance');
export const getProfitLoss = () => client.get('/reports/profit-loss');
export const getProfitPerItem = () => client.get('/reports/profit-per-item');
export const getInventoryValuation = () => client.get('/reports/inventory-valuation');
