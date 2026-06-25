<template>
  <Layout>
    <div class="user-management">
      <div class="page-header">
        <h1>用户管理</h1>
        <div class="header-actions">
          <button class="btn btn-primary" @click="refreshData">
            <i class="icon">🔄</i>
            刷新
          </button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">👥</div>
          <div class="stat-content">
            <div class="stat-number">{{ statistics.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">✅</div>
          <div class="stat-content">
            <div class="stat-number">{{ statistics.activeUsers || 0 }}</div>
            <div class="stat-label">活跃用户</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">❌</div>
          <div class="stat-content">
            <div class="stat-number">{{ statistics.inactiveUsers || 0 }}</div>
            <div class="stat-label">禁用用户</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🚫</div>
          <div class="stat-content">
            <div class="stat-number">{{ statistics.neverLoggedInUsers || 0 }}</div>
            <div class="stat-label">从未登录</div>
          </div>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <div class="search-filters">
        <div class="search-box">
          <input 
            v-model="searchKeyword" 
            type="text" 
            placeholder="搜索用户名、邮箱、昵称..."
            @keyup.enter="searchUsers"
            class="search-input"
          >
          <button @click="searchUsers" class="search-btn">
            <i class="icon">🔍</i>
          </button>
        </div>
        
        <div class="filters">
          <select v-model="statusFilter" @change="filterUsers" class="filter-select">
            <option value="">全部状态</option>
            <option value="1">正常</option>
            <option value="0">禁用</option>
          </select>
          
          <button @click="clearFilters" class="btn btn-secondary">
            <i class="icon">🗑️</i>
            清除筛选
          </button>
        </div>
      </div>

      <!-- 用户列表 -->
      <div class="table-container">
        <table class="user-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>邮箱</th>
              <th>昵称</th>
              <th>电话</th>
              <th>状态</th>
              <th>最后登录</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.email }}</td>
              <td>{{ user.nickname || '-' }}</td>
              <td>{{ user.phone || '-' }}</td>
              <td>
                <span :class="['status-badge', getStatusClass(user.status)]">
                  {{ getStatusText(user.status) }}
                </span>
              </td>
              <td>{{ formatDate(user.lastLoginTime) }}</td>
              <td>{{ formatDate(user.createdTime) }}</td>
              <td>
                <div class="action-buttons">
                  <button @click="viewUser(user)" class="btn btn-sm btn-info">
                    <i class="icon">👁️</i>
                    查看
                  </button>
                  <button 
                    @click="toggleUserStatus(user)" 
                    :class="['btn', 'btn-sm', user.status === 1 ? 'btn-warning' : 'btn-success']"
                  >
                    <i class="icon">{{ user.status === 1 ? '🚫' : '✅' }}</i>
                    {{ user.status === 1 ? '禁用' : '启用' }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        
        <div v-if="users.length === 0" class="empty-state">
          <div class="empty-icon">📭</div>
          <div class="empty-text">暂无用户数据</div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <button 
          @click="changePage(currentPage - 1)" 
          :disabled="currentPage === 0"
          class="btn btn-secondary"
        >
          上一页
        </button>
        
        <span class="page-info">
          第 {{ currentPage + 1 }} 页，共 {{ totalPages }} 页，总计 {{ totalElements }} 条记录
        </span>
        
        <button 
          @click="changePage(currentPage + 1)" 
          :disabled="currentPage >= totalPages - 1"
          class="btn btn-secondary"
        >
          下一页
        </button>
      </div>

      <!-- 用户详情弹窗 -->
      <div v-if="showUserModal" class="modal-overlay" @click="closeUserModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>用户详情</h3>
            <button @click="closeUserModal" class="close-btn">&times;</button>
          </div>
          
          <div class="modal-body">
            <div v-if="selectedUser" class="user-details">
              <div class="detail-row">
                <label>用户ID：</label>
                <span>{{ selectedUser.id }}</span>
              </div>
              <div class="detail-row">
                <label>用户名：</label>
                <span>{{ selectedUser.username }}</span>
              </div>
              <div class="detail-row">
                <label>邮箱：</label>
                <span>{{ selectedUser.email }}</span>
              </div>
              <div class="detail-row">
                <label>昵称：</label>
                <span>{{ selectedUser.nickname || '-' }}</span>
              </div>
              <div class="detail-row">
                <label>电话：</label>
                <span>{{ selectedUser.phone || '-' }}</span>
              </div>
              <div class="detail-row">
                <label>头像：</label>
                <span>{{ selectedUser.avatar || '-' }}</span>
              </div>
              <div class="detail-row">
                <label>状态：</label>
                <span :class="['status-badge', getStatusClass(selectedUser.status)]">
                  {{ getStatusText(selectedUser.status) }}
                </span>
              </div>
              <div class="detail-row">
                <label>最后登录：</label>
                <span>{{ formatDate(selectedUser.lastLoginTime) }}</span>
              </div>
              <div class="detail-row">
                <label>创建时间：</label>
                <span>{{ formatDate(selectedUser.createdTime) }}</span>
              </div>
              <div class="detail-row">
                <label>更新时间：</label>
                <span>{{ formatDate(selectedUser.updatedTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
import axios from 'axios'
import Layout from '../components/Layout.vue'

export default {
  name: 'UserManagement',
  components: {
    Layout
  },
  data() {
    return {
      users: [],
      statistics: {},
      searchKeyword: '',
      statusFilter: '',
      currentPage: 0,
      pageSize: 10,
      totalPages: 0,
      totalElements: 0,
      loading: false,
      showUserModal: false,
      selectedUser: null
    }
  },
  mounted() {
    this.loadUsers()
    this.loadStatistics()
  },
  methods: {
    async loadUsers() {
      try {
        this.loading = true
        const params = {
          page: this.currentPage,
          size: this.pageSize,
          sortBy: 'createdTime',
          sortDir: 'desc'
        }
        
        let url = '/api/admin/users'
        
        if (this.searchKeyword) {
          url = '/api/admin/users/search'
          params.keyword = this.searchKeyword
        } else if (this.statusFilter !== '') {
          url = `/api/admin/users/status/${this.statusFilter}`
        }
        
        const response = await axios.get(url, { params })
        
        if (response.data.code === 200) {
          const data = response.data.data
          this.users = data.list || []
          this.totalPages = data.pages || 0
          this.totalElements = data.total || 0
        } else {
           console.error('获取用户列表失败:', response.data.message || '获取用户列表失败')
           alert(response.data.message || '获取用户列表失败')
         }
       } catch (error) {
         console.error('获取用户列表失败:', error)
         alert('获取用户列表失败')
       } finally {
        this.loading = false
      }
    },
    
    async loadStatistics() {
      try {
        const response = await axios.get('/api/admin/users/statistics')
        if (response.data.code === 200) {
          this.statistics = response.data.data || {}
        }
      } catch (error) {
        console.error('获取用户统计失败:', error)
      }
    },
    
    searchUsers() {
      this.currentPage = 0
      this.loadUsers()
    },
    
    filterUsers() {
      this.currentPage = 0
      this.loadUsers()
    },
    
    clearFilters() {
      this.searchKeyword = ''
      this.statusFilter = ''
      this.currentPage = 0
      this.loadUsers()
    },
    
    changePage(page) {
      if (page >= 0 && page < this.totalPages) {
        this.currentPage = page
        this.loadUsers()
      }
    },
    
    refreshData() {
      this.loadUsers()
      this.loadStatistics()
    },
    
    viewUser(user) {
      this.selectedUser = user
      this.showUserModal = true
    },
    
    closeUserModal() {
      this.showUserModal = false
      this.selectedUser = null
    },
    
    async toggleUserStatus(user) {
      try {
        const newStatus = user.status === 1 ? 0 : 1
        const response = await axios.put(`/api/admin/users/${user.id}/status`, {
          status: newStatus
        })
        
        if (response.data.code === 200) {
           user.status = newStatus
           alert(`用户状态已${newStatus === 1 ? '启用' : '禁用'}`)
           this.loadStatistics() // 重新加载统计数据
         } else {
           console.error('更新用户状态失败:', response.data.message || '更新用户状态失败')
           alert(response.data.message || '更新用户状态失败')
         }
       } catch (error) {
         console.error('更新用户状态失败:', error)
         alert('更新用户状态失败')
       }
    },
    
    getStatusClass(status) {
      return status === 1 ? 'active' : 'inactive'
    },
    
    getStatusText(status) {
      return status === 1 ? '正常' : '禁用'
    },
    
    formatDate(dateString) {
      if (!dateString) return '-'
      return new Date(dateString).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.user-management {
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  color: #1e293b;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2.5rem;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #1e293b;
  margin-bottom: 0.25rem;
}

.stat-label {
  color: #64748b;
  font-size: 0.875rem;
}

/* 搜索和筛选 */
.search-filters {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 1.5rem;
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  flex: 1;
  min-width: 300px;
}

.search-input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px 0 0 8px;
  font-size: 0.875rem;
  outline: none;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  border-color: #667eea;
}

.search-btn {
  padding: 0.75rem 1rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 0 8px 8px 0;
  cursor: pointer;
  transition: background 0.3s ease;
}

.search-btn:hover {
  background: #5a67d8;
}

.filters {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.filter-select {
  padding: 0.75rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
  outline: none;
  cursor: pointer;
}

/* 表格 */
.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 1.5rem;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th,
.user-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

.user-table th {
  background: #f8fafc;
  font-weight: 600;
  color: #374151;
}

.user-table tbody tr:hover {
  background: #f8fafc;
}

/* 状态标签 */
.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
}

.status-badge.active {
  background: #dcfce7;
  color: #166534;
}

.status-badge.inactive {
  background: #fee2e2;
  color: #dc2626;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 0.5rem;
}

/* 按钮样式 */
.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5a67d8;
}

.btn-secondary {
  background: #e2e8f0;
  color: #374151;
}

.btn-secondary:hover {
  background: #cbd5e1;
}

.btn-info {
  background: #0ea5e9;
  color: white;
}

.btn-info:hover {
  background: #0284c7;
}

.btn-warning {
  background: #f59e0b;
  color: white;
}

.btn-warning:hover {
  background: #d97706;
}

.btn-success {
  background: #10b981;
  color: white;
}

.btn-success:hover {
  background: #059669;
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.75rem;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.page-info {
  color: #64748b;
  font-size: 0.875rem;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 3rem;
  color: #64748b;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-text {
  font-size: 1.125rem;
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  margin: 0;
  color: #1e293b;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #64748b;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #374151;
}

.modal-body {
  padding: 1.5rem;
}

.user-details {
  display: grid;
  gap: 1rem;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.detail-row label {
  font-weight: 600;
  color: #374151;
  min-width: 100px;
}

.detail-row span {
  color: #64748b;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-management {
    padding: 1rem;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .search-filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    min-width: auto;
  }
  
  .filters {
    justify-content: center;
  }
  
  .user-table {
    font-size: 0.75rem;
  }
  
  .user-table th,
  .user-table td {
    padding: 0.5rem;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>