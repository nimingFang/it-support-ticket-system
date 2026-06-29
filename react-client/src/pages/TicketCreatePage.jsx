import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createTicket } from '../api/tickets';
import { PRIORITY, CATEGORY, PRIORITY_LABEL, CATEGORY_LABEL } from '../utils/constants';

export default function TicketCreatePage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ title: '', description: '', category: 'NETWORK', priority: 'MEDIUM' });
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!form.title.trim() || !form.description.trim()) {
      setError('标题和描述不能为空');
      return;
    }
    try {
      await createTicket(form);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || '创建失败');
    }
  };

  return (
    <div>
      <h2>新建工单</h2>
      <button className="btn-secondary" onClick={() => navigate('/')}>← 返回列表</button>

      <form className="ticket-form" onSubmit={handleSubmit}>
        {error && <div className="form-error">{error}</div>}

        <label>标题 *</label>
        <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />

        <label>描述 *</label>
        <textarea rows={4} value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })} />

        <label>分类</label>
        <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
          {CATEGORY.map((c) => <option key={c} value={c}>{CATEGORY_LABEL[c]}</option>)}
        </select>

        <label>优先级</label>
        <select value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}>
          {PRIORITY.map((p) => <option key={p} value={p}>{PRIORITY_LABEL[p]}</option>)}
        </select>

        <button type="submit">提交</button>
      </form>
    </div>
  );
}
