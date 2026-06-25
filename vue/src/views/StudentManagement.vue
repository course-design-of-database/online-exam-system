<template>
  <Layout>
    <div class="content-header">
      <div class="header-content">
        <div>
          <h2>学生管理</h2>
          <p class="subtitle">管理系统中的所有学生信息</p>
        </div>
        <div class="header-actions">
          <button @click="refreshStudents" :disabled="loading" class="refresh-btn">
            <span v-if="loading">🔄 刷新中...</span>
            <span v-else>🔄 刷新数据</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <h3>学生总数</h3>
          <p class="stat-number">{{ statistics.totalStudents || 0 }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <h3>活跃学生</h3>
          <p class="stat-number">{{ statistics.activeStudents || 0 }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📚</div>
        <div class="stat-info">
          <h3>班级数量</h3>
          <p class="stat-number">{{ statistics.totalClasses || 0 }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🎓</div>
        <div class="stat-info">
          <h3>专业数量</h3>
          <p class="stat-number">{{ statistics.totalMajors || 0 }}</p>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-filter-section">
      <div class="search-box">
        <input 
          v-model="searchKeyword" 
          @input="handleSearch"
          type="text" 
          placeholder="搜索学生姓名、学号或邮箱..."
          class="search-input"
        >
        <button @click="handleSearch" class="search-btn">🔍</button>
      </div>
      
      <div class="filter-controls">
        <select v-model="filterStatus" @change="handleFilter" class="filter-select">
          <option value="">全部状态</option>
          <option value="ACTIVE">活跃</option>
          <option value="INACTIVE">非活跃</option>
          <option value="SUSPENDED">暂停</option>
        </select>
        
        <select v-model="filterClassName" @change="handleFilter" class="filter-select">
          <option value="">全部班级</option>
          <option v-for="className in classNames" :key="className" :value="className">
            {{ className }}
          </option>
        </select>
        
        <select v-model="filterMajor" @change="handleFilter" class="filter-select">
          <option value="">全部专业</option>
          <option v-for="major in majors" :key="major" :value="major">
            {{ major }}
          </option>
        </select>
        
        <select v-model="filterGrade" @change="handleFilter" class="filter-select">
          <option value="">全部年级</option>
          <option v-for="grade in grades" :key="grade" :value="grade">
            {{ grade }}
          </option>
        </select>
        
        <button @click="clearFilters" class="clear-filters-btn">清除筛选</button>
      </div>
    </div>

    <!-- 学生列表 -->
    <div class="students-section">
      <div class="section-header">
        <h3>学生列表</h3>
        <div class="batch-actions">
          <button 
            @click="toggleSelectAll" 
            :class="['select-all-btn', { active: isAllSelected }]"
          >
            {{ isAllSelected ? '取消全选' : '全选' }}
          </button>
          <button 
            @click="batchUpdateStatus" 
            :disabled="selectedStudents.length === 0"
            class="batch-btn"
          >
            批量操作 ({{ selectedStudents.length }})
          </button>
        </div>
      </div>

      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="students.length === 0" class="no-data">
        <p>暂无学生数据</p>
      </div>

      <div v-else class="students-table">
        <table>
          <thead>
            <tr>
              <th width="50">
                <input 
                  type="checkbox" 
                  :checked="isAllSelected"
                  @change="toggleSelectAll"
                >
              </th>
              <th>学号</th>
              <th>姓名</th>
              <th>班级</th>
              <th>专业</th>
              <th>年级</th>
              <th>邮箱</th>
              <th>电话</th>
              <th>状态</th>
              <th>注册时间</th>
              <th width="150">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="student in students" :key="student.id" class="student-row">
              <td>
                <input 
                  type="checkbox" 
                  :value="student.id"
                  v-model="selectedStudents"
                >
              </td>
              <td class="student-number">{{ student.studentNumber }}</td>
              <td class="student-name">{{ student.name }}</td>
              <td>{{ student.className }}</td>
              <td>{{ student.major }}</td>
              <td>{{ student.grade }}</td>
              <td>{{ student.email }}</td>
              <td>{{ student.phone || '-' }}</td>
              <td>
                <span :class="['status-badge', getStatusClass(student.status)]">
                  {{ getStatusText(student.status) }}
                </span>
              </td>
              <td>{{ formatDate(student.createdAt) }}</td>
              <td class="actions">
                <button @click="viewStudent(student)" class="btn-small primary">查看</button>
                <button @click="editStudent(student)" class="btn-small secondary">编辑</button>
                <button @click="deleteStudent(student)" class="btn-small danger">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="pagination">
        <button 
          @click="changePage(currentPage - 1)" 
          :disabled="currentPage === 1"
          class="page-btn"
        >
          上一页
        </button>
        
        <button 
          v-for="page in visiblePages" 
          :key="page"
          @click="changePage(page)"
          :class="['page-btn', { active: page === currentPage }]"
        >
          {{ page }}
        </button>
        
        <button 
          @click="changePage(currentPage + 1)" 
          :disabled="currentPage === totalPages"
          class="page-btn"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 学生详情模态框 -->
    <div v-if="showStudentModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ modalMode === 'view' ? '学生详情' : '编辑学生' }}</h3>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>
        
        <div class="modal-body">
          <div v-if="modalMode === 'view'" class="student-details">
            <div class="detail-row">
              <label>学号：</label>
              <span>{{ selectedStudent.studentNumber }}</span>
            </div>
            <div class="detail-row">
              <label>姓名：</label>
              <span>{{ selectedStudent.name }}</span>
            </div>
            <div class="detail-row">
              <label>班级：</label>
              <span>{{ selectedStudent.className }}</span>
            </div>
            <div class="detail-row">
              <label>专业：</label>
              <span>{{ selectedStudent.major }}</span>
            </div>
            <div class="detail-row">
              <label>年级：</label>
              <span>{{ selectedStudent.grade }}</span>
            </div>
            <div class="detail-row">
              <label>邮箱：</label>
              <span>{{ selectedStudent.email }}</span>
            </div>
            <div class="detail-row">
              <label>电话：</label>
              <span>{{ selectedStudent.phone || '-' }}</span>
            </div>
            <div class="detail-row">
              <label>状态：</label>
              <span :class="['status-badge', getStatusClass(selectedStudent.status)]">
                {{ getStatusText(selectedStudent.status) }}
              </span>
            </div>
            <div class="detail-row">
              <label>注册时间：</label>
              <span>{{ formatDate(selectedStudent.createdAt) }}</span>
            </div>
            <div class="detail-row">
              <label>最后更新：</label>
              <span>{{ formatDate(selectedStudent.updatedAt) }}</span>
            </div>
          </div>
          
          <div v-else class="student-form">
            <div class="form-row">
              <label>学号：</label>
              <input v-model="editForm.studentNumber" type="text" readonly class="form-input disabled">
            </div>
            <div class="form-row">
              <label>姓名：</label>
              <input v-model="editForm.name" type="text" class="form-input">
            </div>
            <div class="form-row">
              <label>班级：</label>
              <input v-model="editForm.className" type="text" class="form-input">
            </div>
            <div class="form-row">
              <label>专业：</label>
              <input v-model="editForm.major" type="text" class="form-input">
            </div>
            <div class="form-row">
              <label>年级：</label>
              <input v-model="editForm.grade" type="text" class="form-input">
            </div>
            <div class="form-row">
              <label>邮箱：</label>
              <input v-model="editForm.email" type="email" class="form-input">
            </div>
            <div class="form-row">
              <label>电话：</label>
              <input v-model="editForm.phone" type="text" class="form-input">
            </div>
            <div class="form-row">
              <label>状态：</label>
              <select v-model="editForm.status" class="form-input">
                <option value="ACTIVE">活跃</option>
                <option value="INACTIVE">非活跃</option>
                <option value="SUSPENDED">暂停</option>
              </select>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeModal" class="btn secondary">取消</button>
          <button v-if="modalMode === 'edit'" @click="saveStudent" class="btn primary">保存</button>
        </div>
      </div>
    </div>

    <!-- 批量操作模态框 -->
    <div v-if="showBatchModal" class="modal-overlay" @click="closeBatchModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>批量操作</h3>
          <button @click="closeBatchModal" class="close-btn">&times;</button>
        </div>
        
        <div class="modal-body">
          <p>已选择 {{ selectedStudents.length }} 个学生</p>
          <div class="batch-options">
            <button @click="batchChangeStatus('ACTIVE')" class="batch-option-btn active">设为活跃</button>
            <button @click="batchChangeStatus('INACTIVE')" class="batch-option-btn inactive">设为非活跃</button>
            <button @click="batchChangeStatus('SUSPENDED')" class="batch-option-btn suspended">设为暂停</button>
            <button @click="batchDelete" class="batch-option-btn danger">批量删除</button>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeBatchModal" class="btn secondary">取消</button>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import axios from 'axios'

export default {
  name: 'StudentManagement',
  components: {
    Layout
  },
  data() {
    return {
      loading: false,
      students: [],
      statistics: {},
      classNames: [],
      majors: [],
      grades: [],
      
      // 搜索和筛选
      searchKeyword: '',
      filterStatus: '',
      filterClassName: '',
      filterMajor: '',
      filterGrade: '',
      
      // 分页
      currentPage: 1,
      pageSize: 10,
      totalPages: 0,
      totalElements: 0,
      
      // 选择
      selectedStudents: [],
      
      // 模态框
      showStudentModal: false,
      showBatchModal: false,
      modalMode: 'view', // 'view' or 'edit'
      selectedStudent: {},
      editForm: {}
    }
  },
  computed: {
    isAllSelected() {
      return this.students.length > 0 && this.selectedStudents.length === this.students.length
    },
    visiblePages() {
      const pages = []
      const start = Math.max(1, this.currentPage - 2)
      const end = Math.min(this.totalPages, this.currentPage + 2)
      
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      return pages
    }
  },
  async mounted() {
    await this.loadInitialData()
  },
  methods: {
    async loadInitialData() {
      await Promise.all([
        this.loadStudents(),
        this.loadStatistics(),
        this.loadFilterOptions()
      ])
    },
    
    async loadStudents() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage - 1,
          size: this.pageSize
        }
        
        if (this.searchKeyword) {
          params.keyword = this.searchKeyword
        }
        if (this.filterStatus) {
          params.status = this.filterStatus
        }
        if (this.filterClassName) {
          params.className = this.filterClassName
        }
        if (this.filterMajor) {
          params.major = this.filterMajor
        }
        if (this.filterGrade) {
          params.grade = this.filterGrade
        }
        
        const response = await axios.get('/api/admin/students', { params })
        
        if (response.data.code === 200) {
          const data = response.data.data
          this.students = data.list
          this.totalPages = data.pages
          this.totalElements = data.total
        }
      } catch (error) {
        console.error('加载学生列表失败:', error)
        this.$message?.error('加载学生列表失败')
      } finally {
        this.loading = false
      }
    },
    
    async loadStatistics() {
      try {
        const response = await axios.get('/api/admin/students/statistics')
        if (response.data.code === 200) {
          this.statistics = response.data.data
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },
    
    async loadFilterOptions() {
      try {
        const [classResponse, majorResponse, gradeResponse] = await Promise.all([
          axios.get('/api/admin/students/classes'),
          axios.get('/api/admin/students/majors'),
          axios.get('/api/admin/students/grades')
        ])
        
        if (classResponse.data.code === 200) {
          this.classNames = classResponse.data.data
        }
        if (majorResponse.data.code === 200) {
          this.majors = majorResponse.data.data
        }
        if (gradeResponse.data.code === 200) {
          this.grades = gradeResponse.data.data
        }
      } catch (error) {
        console.error('加载筛选选项失败:', error)
      }
    },
    
    handleSearch() {
      this.currentPage = 1
      this.loadStudents()
    },
    
    handleFilter() {
      this.currentPage = 1
      this.loadStudents()
    },
    
    clearFilters() {
      this.searchKeyword = ''
      this.filterStatus = ''
      this.filterClassName = ''
      this.filterMajor = ''
      this.filterGrade = ''
      this.currentPage = 1
      this.loadStudents()
    },
    
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page
        this.loadStudents()
      }
    },
    
    toggleSelectAll() {
      if (this.isAllSelected) {
        this.selectedStudents = []
      } else {
        this.selectedStudents = this.students.map(s => s.id)
      }
    },
    
    viewStudent(student) {
      this.selectedStudent = { ...student }
      this.modalMode = 'view'
      this.showStudentModal = true
    },
    
    editStudent(student) {
      this.selectedStudent = { ...student }
      this.editForm = { ...student }
      this.modalMode = 'edit'
      this.showStudentModal = true
    },
    
    async saveStudent() {
      try {
        const response = await axios.put(`/api/admin/students/${this.editForm.id}`, this.editForm)
        if (response.data.code === 200) {
          this.$message?.success('学生信息更新成功')
          this.closeModal()
          this.loadStudents()
        }
      } catch (error) {
        console.error('更新学生信息失败:', error)
        this.$message?.error('更新学生信息失败')
      }
    },
    
    async deleteStudent(student) {
      if (confirm(`确定要删除学生 ${student.name} 吗？`)) {
        try {
          const response = await axios.delete(`/api/admin/students/${student.id}`)
          if (response.data.code === 200) {
            this.$message?.success('删除成功')
            this.loadStudents()
          }
        } catch (error) {
          console.error('删除学生失败:', error)
          this.$message?.error('删除学生失败')
        }
      }
    },
    
    batchUpdateStatus() {
      if (this.selectedStudents.length === 0) {
        this.$message?.warning('请先选择学生')
        return
      }
      this.showBatchModal = true
    },
    
    async batchChangeStatus(status) {
      try {
        const response = await axios.put('/api/admin/students/batch-status', {
          studentIds: this.selectedStudents,
          status: status
        })
        if (response.data.code === 200) {
          this.$message?.success('批量更新状态成功')
          this.closeBatchModal()
          this.selectedStudents = []
          this.loadStudents()
        }
      } catch (error) {
        console.error('批量更新状态失败:', error)
        this.$message?.error('批量更新状态失败')
      }
    },
    
    async batchDelete() {
      if (confirm(`确定要删除选中的 ${this.selectedStudents.length} 个学生吗？`)) {
        try {
          const response = await axios.delete('/api/admin/students/batch', {
            data: { studentIds: this.selectedStudents }
          })
          if (response.data.code === 200) {
            this.$message?.success('批量删除成功')
            this.closeBatchModal()
            this.selectedStudents = []
            this.loadStudents()
          }
        } catch (error) {
          console.error('批量删除失败:', error)
          this.$message?.error('批量删除失败')
        }
      }
    },
    
    closeModal() {
      this.showStudentModal = false
      this.selectedStudent = {}
      this.editForm = {}
    },
    
    closeBatchModal() {
      this.showBatchModal = false
    },
    
    refreshStudents() {
      this.loadInitialData()
    },
    
    getStatusText(status) {
      const statusMap = {
        1: '活跃',
        0: '非活跃',
        2: '暂停'
      }
      return statusMap[status] || '未知'
    },
    
    getStatusClass(status) {
      const classMap = {
        1: 'active',
        0: 'inactive', 
        2: 'suspended'
      }
      return classMap[status] || 'unknown'
    },
    
    formatDate(dateString) {
      if (!dateString) return '-'
      return new Date(dateString).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
/* 基础样式 */
.content-header {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
  color: #1e293b;
  font-size: 1.8rem;
}

.subtitle {
  margin: 0.5rem 0 0 0;
  color: #64748b;
  font-size: 1rem;
}

.refresh-btn {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.refresh-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
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

.stat-info h3 {
  margin: 0 0 0.5rem 0;
  color: #64748b;
  font-size: 0.9rem;
  font-weight: 500;
}

.stat-number {
  margin: 0;
  color: #1e293b;
  font-size: 2rem;
  font-weight: 700;
}

/* 搜索和筛选 */
.search-filter-section {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}

.search-box {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.search-input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
}

.search-btn {
  padding: 0.75rem 1rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.filter-controls {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  align-items: center;
}

.filter-select {
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
}

.clear-filters-btn {
  padding: 0.5rem 1rem;
  background: #64748b;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

/* 学生列表 */
.students-section {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.section-header h3 {
  margin: 0;
  color: #1e293b;
  font-size: 1.2rem;
}

.batch-actions {
  display: flex;
  gap: 0.5rem;
}

.select-all-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #667eea;
  background: white;
  color: #667eea;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.select-all-btn.active {
  background: #667eea;
  color: white;
}

.batch-btn {
  padding: 0.5rem 1rem;
  background: #f59e0b;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.batch-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 表格样式 */
.students-table {
  overflow-x: auto;
}

.students-table table {
  width: 100%;
  border-collapse: collapse;
}

.students-table th,
.students-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.students-table th {
  background: #f8fafc;
  font-weight: 600;
  color: #374151;
  font-size: 0.9rem;
}

.student-row:hover {
  background: #f8fafc;
}

.student-number {
  font-weight: 600;
  color: #667eea;
}

.student-name {
  font-weight: 500;
  color: #1e293b;
}

.status-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-badge.active {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.inactive {
  background: #fee2e2;
  color: #991b1b;
}

.status-badge.suspended {
  background: #fef3c7;
  color: #92400e;
}

.actions {
  display: flex;
  gap: 0.5rem;
}

.btn-small {
  padding: 0.4rem 0.8rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-small.primary {
  background: #667eea;
  color: white;
}

.btn-small.secondary {
  background: #64748b;
  color: white;
}

.btn-small.danger {
  background: #ef4444;
  color: white;
}

.btn-small:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* 加载和空数据 */
.loading,
.no-data {
  text-align: center;
  padding: 3rem;
  color: #64748b;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  padding: 1.5rem;
}

.page-btn {
  padding: 0.5rem 0.75rem;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: #f3f4f6;
}

.page-btn.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 模态框 */
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
  box-shadow: 0 20px 25px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 90vw;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
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
  color: #9ca3af;
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

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1rem 1.5rem;
  border-top: 1px solid #e5e7eb;
}

/* 学生详情 */
.student-details {
  display: grid;
  gap: 1rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f3f4f6;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row label {
  font-weight: 600;
  color: #374151;
  min-width: 100px;
}

/* 表单 */
.student-form {
  display: grid;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 1rem;
  align-items: center;
}

.form-row label {
  font-weight: 600;
  color: #374151;
}

.form-input {
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input.disabled {
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
}

/* 按钮 */
.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn.primary {
  background: #667eea;
  color: white;
}

.btn.secondary {
  background: #64748b;
  color: white;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 批量操作 */
.batch-options {
  display: grid;
  gap: 1rem;
  margin-top: 1rem;
}

.batch-option-btn {
  padding: 1rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.batch-option-btn.active {
  background: #10b981;
  color: white;
}

.batch-option-btn.inactive {
  background: #ef4444;
  color: white;
}

.batch-option-btn.suspended {
  background: #f59e0b;
  color: white;
}

.batch-option-btn.danger {
  background: #dc2626;
  color: white;
}

.batch-option-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .section-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .students-table {
    font-size: 0.8rem;
  }
  
  .students-table th,
  .students-table td {
    padding: 0.5rem;
  }
  
  .actions {
    flex-direction: column;
  }
  
  .modal-content {
    width: 95%;
    margin: 1rem;
  }
  
  .form-row {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }
}
</style>