import React from 'react';
import ReactDOM from 'react-dom/client';
import { ConfigProvider } from 'antd';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ConfigProvider theme={{
      token: {
        colorPrimary: '#ED1C24',
        colorLink: '#1A1F71',
        borderRadius: 6,
        fontFamily: "'Segoe UI', Roboto, -apple-system, BlinkMacSystemFont, sans-serif",
      },
    }}>
      <App />
    </ConfigProvider>
  </React.StrictMode>
);
