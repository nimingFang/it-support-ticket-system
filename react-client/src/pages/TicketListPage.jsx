import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchTickets } from '../api/tickets';
import { getRole } from '../utils/jwt';
import { STATUS, PRIORITY, CATEGORY, STATUS_LABEL, PRIORITY_LABEL, STATUS_COLOR } from '../utils/constants';

export default function TicketListPage() {
  const navigate = useNavigate();
  const role = getRole();

  const [tickets, setTickets] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [filter, setFilter] = useState({ status: '', priority: '', keyword: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const load = async (p) => {
    setLoading(true);
    setError('');
    try {
      const params = { page: p, size: 10 };
      if (filter.status) params.status = filter.status;
      if (filter.priority) params.priority = filter.priority;
      if (filter.keyword) params.keyword = filter.keyword;
      const res = await fetchTickets(params);
      setTickets(res.data.content);
      setTotalPages(res.data.totalPages);
    } catch (err) {
      setError(err.response?.data?.message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(page); }, [page]);

  const handleSearch = () => { setPage(0); load(0); };
  const handleReset = () => {
    setFilter({ status: '', priority: '', keyword: '' });
    setPage(0);
  };
  // Reset 后需要重新加载，用 useEffect 不够，用单独的 load call
  useEffect(() => { load(0); }, [filter.status, filter.priority, filter.keyword]);

  return (
    <div>
      <div className="toolbar">
        <h2>工单列表</h2>
        {role === 'EMPLOYEE' && (
          <button onClick={() => navigate('/tickets/new')}>+ 新建工单</button>
        )}
      </div>

      <div className="filter-bar">
        <select value={filter.status} onChange={(e) => setFilter({ ...filter, status: e.target.value })}>
          <option value="">全部状态</option>
          {STATUS.map((s) => <option key={s} value={s}>{STATUS_LABEL[s]}</option>)}
        </select>
        <select value={filter.priority} onChange={(e) => setFilter({ ...filter, priority: e.target.value })}>
          <option value="">全部优先级</option>
          {PRIORITY.map((p) => <option key={p} value={p}>{PRIORITY_LABEL[p]}</option>)}
        </select>
        <input
          placeholder="搜索标题/描述"
          value={filter.keyword}
          onChange={(e) => setFilter({ ...filter, keyword: e.target.value })}
        />
        <button onClick={handleSearch}>搜索</button>
        <button className="btn-secondary" onClick={handleReset}>重置</button>
      </div>

      {error && <div className="form-error">{error}</div>}

      {loading ? <p>加载中...</p> : (
        <>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>标题</th>
                <th>状态</th>
                <th>优先级</th>
                <th>分类</th>
                <th>创建人</th>
                <th>负责人</th>
              </tr>
            </thead>
            <tbody>
              {tickets.map((t) => (
                <tr key={t.id} onClick={() => navigate(`/tickets/${t.id}`)} className="clickable-row">
                  <td>{t.id}</td>
                  <td>{t.title}</td>
                  <td>
                    <span className="badge" style={{ background: STATUS_COLOR[t.status] }}>
                      {STATUS_LABEL[t.status]}
                    </span>
                  </td>
                  <td>{PRIORITY_LABEL[t.priority]}</td>
                  <td>{t.category}</td>
                  <td>{t.creatorUsername}</td>
                  <td>{t.assigneeUsername || '-'}</td>
                </tr>
              ))}
              {tickets.length === 0 && (
                <tr><td colSpan={7} style={{ textAlign: 'center', color: '#999' }}>暂无工单</td></tr>
              )}
            </tbody>
          </table>

          <div className="pagination">
            <button disabled={page === 0} onClick={() => setPage(page - 1)}>上一页</button>
            <span>第 {page + 1} / {totalPages} 页</span>
            <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>下一页</button>
          </div>
        </>
      )}
    </div>
  );
}
