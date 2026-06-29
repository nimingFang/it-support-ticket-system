import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getTicket, assignTicket, updateStatus } from '../api/tickets';
import { getRole, getUsername } from '../utils/jwt';
import { STATUS_LABEL, PRIORITY_LABEL, CATEGORY_LABEL, STATUS_COLOR } from '../utils/constants';

export default function TicketDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const role = getRole();
  const currentUser = getUsername();

  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const res = await getTicket(id);
      setTicket(res.data);
    } catch (err) {
      setError(err.response?.data?.message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [id]);

  const handleAction = async (fn, ...args) => {
    setActionError('');
    try {
      await fn(id, ...args);
      load(); // 重新加载最新数据
    } catch (err) {
      setActionError(err.response?.data?.message || '操作失败');
    }
  };

  if (loading) return <p>加载中...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;
  if (!ticket) return null;

  const isCreator = ticket.creatorUsername === currentUser;
  const isAssignee = ticket.assigneeUsername === currentUser;
  const isIT = role === 'IT_SUPPORT' || role === 'ADMIN';

  return (
    <div>
      <button className="btn-secondary" onClick={() => navigate('/')}>← 返回列表</button>
      <h2>工单 #{ticket.id}</h2>
      {actionError && <div className="form-error">{actionError}</div>}

      <div className="ticket-detail">
        <div className="detail-field"><strong>标题：</strong>{ticket.title}</div>
        <div className="detail-field">
          <strong>状态：</strong>
          <span className="badge" style={{ background: STATUS_COLOR[ticket.status] }}>
            {STATUS_LABEL[ticket.status]}
          </span>
        </div>
        <div className="detail-field"><strong>优先级：</strong>{PRIORITY_LABEL[ticket.priority]}</div>
        <div className="detail-field"><strong>分类：</strong>{CATEGORY_LABEL[ticket.category]}</div>
        <div className="detail-field"><strong>创建人：</strong>{ticket.creatorUsername}</div>
        <div className="detail-field"><strong>负责人：</strong>{ticket.assigneeUsername || '-'}</div>
        <div className="detail-field"><strong>创建时间：</strong>{ticket.creationDate}</div>
        <div className="detail-field">
          <strong>描述：</strong>
          <p className="detail-desc">{ticket.description}</p>
        </div>
      </div>

      {/* 操作按钮组——仅 UI 优化，真实权限校验在后端 FSM */}
      <div className="action-bar">
        {isIT && ticket.status === 'NEW' && (
          <button onClick={() => handleAction(assignTicket)}>接单</button>
        )}
        {isAssignee && ticket.status === 'ASSIGNED' && (
          <button onClick={() => handleAction(updateStatus, 'IN_PROGRESS')}>开始处理</button>
        )}
        {isAssignee && ticket.status === 'IN_PROGRESS' && (
          <button onClick={() => handleAction(updateStatus, 'RESOLVED')}>标记解决</button>
        )}
        {isCreator && ticket.status === 'RESOLVED' && (
          <button onClick={() => handleAction(updateStatus, 'CLOSED')}>验收关闭</button>
        )}
      </div>
    </div>
  );
}
