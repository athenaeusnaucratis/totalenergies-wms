import { useEffect, useRef, useState } from 'react';
import { Html5Qrcode } from 'html5-qrcode';
import { Modal, Button, message } from 'antd';
import { ScanOutlined } from '@ant-design/icons';

export default function BarcodeScanner({ onScan, buttonText = 'Scan Barcode', buttonProps = {} }) {
  const [open, setOpen] = useState(false);
  const [scanning, setScanning] = useState(false);
  const scannerRef = useRef(null);
  const containerRef = useRef('barcode-scanner-' + Math.random().toString(36).slice(2));

  const startScanner = async () => {
    try {
      const scanner = new Html5Qrcode(containerRef.current);
      scannerRef.current = scanner;
      await scanner.start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: { width: 250, height: 150 } },
        (decodedText) => {
          onScan(decodedText);
          message.success(`Scanned: ${decodedText}`);
          stopScanner();
          setOpen(false);
        },
        () => {}
      );
      setScanning(true);
    } catch (err) {
      message.error('Camera access denied or not available');
      console.error(err);
    }
  };

  const stopScanner = async () => {
    if (scannerRef.current && scanning) {
      try {
        await scannerRef.current.stop();
        scannerRef.current.clear();
      } catch {}
      setScanning(false);
    }
  };

  useEffect(() => {
    if (open) {
      setTimeout(startScanner, 300);
    }
    return () => { stopScanner(); };
  }, [open]);

  const handleClose = () => {
    stopScanner();
    setOpen(false);
  };

  return (
    <>
      <Button icon={<ScanOutlined />} onClick={() => setOpen(true)} {...buttonProps}>
        {buttonText}
      </Button>
      <Modal
        title="Scan Barcode / QR Code"
        open={open}
        onCancel={handleClose}
        footer={null}
        width={400}
        destroyOnClose
      >
        <div id={containerRef.current} style={{ width: '100%', minHeight: 300 }} />
        <p style={{ textAlign: 'center', color: '#888', marginTop: 12, fontSize: 13 }}>
          Point your camera at a barcode or QR code containing the product SKU
        </p>
      </Modal>
    </>
  );
}
