import React, { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, message, Popconfirm, Tag, Upload, Card, Alert, Select } from 'antd';
import { UploadOutlined, PlusOutlined, ReloadOutlined, EditOutlined, DeleteOutlined, ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';
import { db, auth } from '../../services/firebase';
import { collection, getDocs, addDoc, updateDoc, doc, deleteDoc, writeBatch } from 'firebase/firestore';

interface Product {
  id: string;
  name: string;
  description: string;
  imageUrl: string;
  price: number;
  status?: string;
}

const ShopList: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [form] = Form.useForm();
  const [uploading, setUploading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [searchName, setSearchName] = useState('');
  const [searchStatus, setSearchStatus] = useState('');
  const [sorter, setSorter] = useState<{field: string, order: 'ascend'|'descend'|null}>({field: '', order: null});
  const [batchPriceModal, setBatchPriceModal] = useState(false);
  const [batchPrice, setBatchPrice] = useState<number>(0);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const productsCol = collection(db, 'products');
      const productSnapshot = await getDocs(productsCol);
      const productList = productSnapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data(),
      })) as Product[];
      setProducts(productList);
    } catch (error) {
      console.error('获取商品列表失败:', error);
      message.error('获取商品列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!auth.currentUser) return;
    fetchProducts();
  }, []);

  const handleAdd = () => {
    setEditingProduct(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (product: Product) => {
    setEditingProduct(product);
    form.setFieldsValue(product);
    setModalVisible(true);
  };

  const handleDelete = async (productId: string) => {
    try {
      await updateDoc(doc(db, 'products', productId), { status: 'deleted' });
      message.success('商品已标记为删除');
      fetchProducts();
    } catch (error) {
      console.error('删除商品失败:', error);
      message.error('删除商品失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      console.log('表单提交的值:', values); // 调试输出
      
      if (editingProduct) {
        // 编辑
        await updateDoc(doc(db, 'products', editingProduct.id), values);
        message.success('商品更新成功');
      } else {
        // 新增
        await addDoc(collection(db, 'products'), { 
          ...values, 
          status: 'on',
          createdAt: new Date()
        });
        message.success('商品添加成功');
      }
      setModalVisible(false);
      fetchProducts();
    } catch (error: any) {
      console.error('保存商品失败:', error);
      if (error.errorFields) {
        // 表单验证错误
        message.error('请检查表单填写是否正确');
      } else {
        message.error('操作失败，请重试');
      }
    }
  };

  const handleUpload = async (file: File) => {
    setUploading(true);
    try {
      // 这里应该上传到Firebase Storage，暂时使用本地URL
      const reader = new FileReader();
      reader.onload = (e) => {
        const result = e.target?.result as string;
        form.setFieldsValue({ imageUrl: result });
        message.success('图片上传成功');
      };
      reader.readAsDataURL(file);
    } catch (error) {
      console.error('上传失败:', error);
      message.error('图片上传失败');
    } finally {
      setUploading(false);
    }
    return false; // 阻止默认上传行为
  };

  const uploadProps = {
    beforeUpload: handleUpload,
    showUploadList: false,
    accept: 'image/*',
  };

  // 查询和排序过滤
  const filteredProducts = products.filter(p => {
    const matchName = searchName ? (p.name?.includes(searchName) || p.id?.includes(searchName)) : true;
    const matchStatus = searchStatus ? p.status === searchStatus : p.status !== 'deleted';
    return matchName && matchStatus;
  });
  const sortedProducts = [...filteredProducts].sort((a, b) => {
    if (!sorter.field || !sorter.order) return 0;
    const valA = a[sorter.field as keyof Product];
    const valB = b[sorter.field as keyof Product];
    if (valA === undefined || valB === undefined) return 0;
    if (typeof valA === 'number' && typeof valB === 'number') {
      return sorter.order === 'ascend' ? valA - valB : valB - valA;
    }
    return sorter.order === 'ascend' ? String(valA).localeCompare(String(valB)) : String(valB).localeCompare(String(valA));
  });

  // 批量操作
  const handleBatchStatus = async (status: string) => {
    if (selectedRowKeys.length === 0) return;
    const batch = writeBatch(db);
    selectedRowKeys.forEach(id => {
      batch.update(doc(db, 'products', String(id)), { status });
    });
    await batch.commit();
    message.success(`批量${status === 'on' ? '上架' : '下架'}成功`);
    fetchProducts();
  };
  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) return;
    const batch = writeBatch(db);
    selectedRowKeys.forEach(id => {
      batch.update(doc(db, 'products', String(id)), { status: 'deleted' });
    });
    await batch.commit();
    message.success('批量删除成功');
    fetchProducts();
  };
  const handleBatchPrice = async () => {
    if (selectedRowKeys.length === 0) return;
    if (!batchPrice || batchPrice <= 0) {
      message.error('请输入有效的积分');
      return;
    }
    const batch = writeBatch(db);
    selectedRowKeys.forEach(id => {
      batch.update(doc(db, 'products', String(id)), { price: batchPrice });
    });
    await batch.commit();
    message.success('批量修改积分成功');
    setBatchPriceModal(false);
    fetchProducts();
  };

  return (
    <div>
      <h2>商品管理</h2>
      
      <Alert 
        message="使用说明" 
        description="您可以添加、编辑和删除商品。支持拖拽上传图片，图片会自动转换为base64格式。" 
        type="info" 
        showIcon 
        style={{ marginBottom: 16 }}
      />
      
      {/* 查询和批量操作区 */}
      <div style={{ display: 'flex', gap: 12, marginBottom: 12, flexWrap: 'wrap' }}>
        <Input placeholder="商品名/ID" value={searchName} onChange={e => setSearchName(e.target.value)} style={{ width: 180 }} />
        <Select value={searchStatus} onChange={setSearchStatus} style={{ width: 120 }} allowClear placeholder="状态"
          options={[
            { value: '', label: '全部' },
            { value: 'on', label: '上架' },
            { value: 'off', label: '下架' },
            { value: 'deleted', label: '已删除' },
          ]} />
        <Button icon={<ReloadOutlined />} onClick={fetchProducts}>刷新</Button>
        <Button type="primary" onClick={handleAdd}>添加商品</Button>
        <Popconfirm title="确定批量上架？" onConfirm={() => handleBatchStatus('on')} okText="确定" cancelText="取消">
          <Button disabled={selectedRowKeys.length === 0}>批量上架</Button>
        </Popconfirm>
        <Popconfirm title="确定批量下架？" onConfirm={() => handleBatchStatus('off')} okText="确定" cancelText="取消">
          <Button disabled={selectedRowKeys.length === 0}>批量下架</Button>
        </Popconfirm>
        <Popconfirm title="确定批量删除？" onConfirm={handleBatchDelete} okText="确定" cancelText="取消">
          <Button danger disabled={selectedRowKeys.length === 0}>批量删除</Button>
        </Popconfirm>
        <Popconfirm title="确定批量修改积分？" onConfirm={() => setBatchPriceModal(true)} okText="确定" cancelText="取消">
          <Button disabled={selectedRowKeys.length === 0}>批量改积分</Button>
        </Popconfirm>
        <span style={{ color: '#888', marginLeft: 8 }}>已选 {selectedRowKeys.length} 项</span>
      </div>
      
      <Table
        dataSource={sortedProducts}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        rowSelection={{ selectedRowKeys, onChange: setSelectedRowKeys }}
      >
        <Table.Column
          title={<span>商品名称 <ArrowUpOutlined style={{ fontSize: 12, color: sorter.field==='name'&&sorter.order==='ascend'?'#1890ff':'#ccc', cursor:'pointer' }} onClick={()=>setSorter({field:'name',order:'ascend'})}/> <ArrowDownOutlined style={{ fontSize: 12, color: sorter.field==='name'&&sorter.order==='descend'?'#1890ff':'#ccc', cursor:'pointer' }} onClick={()=>setSorter({field:'name',order:'descend'})}/></span>}
          dataIndex="name"
        />
        <Table.Column title="简介" dataIndex="description" ellipsis />
        <Table.Column 
          title="图片" 
          dataIndex="imageUrl" 
          render={url => url ? (
            <img src={url} alt="商品" style={{ width: 60, height: 60, objectFit: 'cover' }} />
          ) : (
            <div style={{ width: 60, height: 60, background: '#f0f0f0', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              无图片
            </div>
          )} 
        />
        <Table.Column
          title={<span>所需积分 <ArrowUpOutlined style={{ fontSize: 12, color: sorter.field==='price'&&sorter.order==='ascend'?'#1890ff':'#ccc', cursor:'pointer' }} onClick={()=>setSorter({field:'price',order:'ascend'})}/> <ArrowDownOutlined style={{ fontSize: 12, color: sorter.field==='price'&&sorter.order==='descend'?'#1890ff':'#ccc', cursor:'pointer' }} onClick={()=>setSorter({field:'price',order:'descend'})}/></span>}
          dataIndex="price"
        />
        <Table.Column
          title={<span>状态 <ArrowUpOutlined style={{ fontSize: 12, color: sorter.field==='status'&&sorter.order==='ascend'?'#1890ff':'#ccc', cursor:'pointer' }} onClick={()=>setSorter({field:'status',order:'ascend'})}/> <ArrowDownOutlined style={{ fontSize: 12, color: sorter.field==='status'&&sorter.order==='descend'?'#1890ff':'#ccc', cursor:'pointer' }} onClick={()=>setSorter({field:'status',order:'descend'})}/></span>}
          dataIndex="status"
          render={status => (
            <Tag color={status === 'on' ? 'green' : status === 'off' ? 'red' : 'gray'}>
              {status === 'on' ? '上架' : status === 'off' ? '下架' : '已删除'}
            </Tag>
          )}
        />
        <Table.Column
          title="操作"
          render={(_, product: Product) => (
            <>
              <Button type="link" onClick={() => handleEdit(product)}>编辑</Button>
              <Popconfirm
                title="确定要删除这个商品吗？"
                onConfirm={() => handleDelete(product.id)}
                okText="确定"
                cancelText="取消"
              >
                <Button type="link" danger>删除</Button>
              </Popconfirm>
            </>
          )}
        />
      </Table>
      
      <Modal
        open={modalVisible}
        title={editingProduct ? '编辑商品' : '添加商品'}
        onCancel={() => setModalVisible(false)}
        onOk={handleSubmit}
        okText="保存"
        cancelText="取消"
        width={600}
        confirmLoading={uploading}
      >
        <Form form={form} layout="vertical">
          <Form.Item 
            name="name" 
            label="商品名称" 
            rules={[{ required: true, message: '请输入商品名称' }]}
          >
            <Input placeholder="请输入商品名称" />
          </Form.Item>
          
          <Form.Item 
            name="description" 
            label="商品简介" 
            rules={[{ required: true, message: '请输入商品简介' }]}
          >
            <Input.TextArea rows={3} placeholder="请输入商品简介" />
          </Form.Item>
          
          <Form.Item 
            name="imageUrl" 
            label="商品图片" 
            rules={[{ required: true, message: '请上传商品图片' }]}
          >
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <Upload {...uploadProps}>
                <Button icon={<UploadOutlined />} loading={uploading}>
                  点击上传
                </Button>
              </Upload>
              <span style={{ color: '#999' }}>或拖拽图片到此处</span>
            </div>
          </Form.Item>
          
          <Form.Item 
            name="price" 
            label="所需积分" 
            rules={[{ required: true, message: '请输入所需积分' }]}
          >
            <InputNumber 
              min={1} 
              style={{ width: '100%' }} 
              placeholder="请输入所需积分"
            />
          </Form.Item>
        </Form>
      </Modal>
      <Modal open={batchPriceModal} title="批量修改积分" onCancel={()=>setBatchPriceModal(false)} onOk={handleBatchPrice} okText="保存" cancelText="取消">
        <InputNumber min={1} value={batchPrice} onChange={v=>setBatchPrice(Number(v))} style={{width:200}} placeholder="请输入新积分" />
      </Modal>
    </div>
  );
};

export default ShopList; 