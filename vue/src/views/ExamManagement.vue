<template>
  <Layout>
    <div class="content-header">
      <h2>考试管理</h2>
      <p class="subtitle">管理系统中的所有考试</p>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <h3>快速操作</h3>
      <div class="action-buttons">
        <button class="action-btn primary" @click="showCreateExamModal = true">
          <i class="icon">📝</i>
          <span>创建考试</span>
        </button>
        <button class="action-btn secondary" @click="loadExams">
          <i class="icon">🔄</i>
          <span>刷新列表</span>
        </button>
      </div>
    </div>

    <!-- 考试管理区域 -->
    <div class="exam-management">
      <div class="exam-header">
        <h3>考试列表</h3>
        <div class="exam-filters">
          <select v-model="statusFilter" @change="loadExams">
            <option value="">全部状态</option>
            <option value="pending">待开始</option>
            <option value="active">进行中</option>
            <option value="completed">已结束</option>
          </select>
          <select v-model="categoryFilter" @change="loadExams">
            <option value="">全部分类</option>
            <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
          </select>
          <input 
            type="text" 
            v-model="searchTitle" 
            @input="loadExams" 
            placeholder="搜索考试标题..."
            class="search-input"
          >
        </div>
      </div>

      <div class="exam-list">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="exams.length === 0" class="no-data">暂无考试数据</div>
        <div v-else>
          <div v-for="exam in sortedExams" :key="exam.id" class="exam-item" :class="getExamStatus(exam)">
            <div class="exam-info">
              <h4>{{ exam.title }}</h4>
              <div v-if="exam.category" class="exam-category">{{ exam.category }}</div>
              <p class="exam-desc">{{ exam.description }}</p>
              <div class="exam-meta">
                <span>开始时间: {{ formatDateTime(exam.startTime) }}</span>
                <span>结束时间: {{ formatDateTime(exam.endTime) }}</span>
                <span>时长: {{ exam.duration }}分钟</span>
                <span>总分: {{ exam.totalScore }}分</span>
                <span class="exam-status" :class="getExamStatus(exam)">{{ getStatusText(exam) }}</span>
              </div>
              <!-- 倒计时显示 -->
              <div v-if="getCountdown(exam)" class="exam-countdown">
                <span class="countdown-label">{{ getCountdownPrefix(exam) }}</span>
                <span class="countdown-time" :class="getExamStatus(exam)">{{ getCountdown(exam) }}</span>
              </div>
            </div>
            <div class="exam-actions">
              <button @click="editExam(exam)" class="btn secondary">编辑</button>
              <button @click="manageQuestions(exam)" class="btn primary">题目管理</button>
              <button @click="viewStatistics(exam)" class="btn secondary">统计</button>
              <button @click="deleteExam(exam.id)" class="btn danger">删除</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <button 
          @click="currentPage = Math.max(1, currentPage - 1)" 
          :disabled="currentPage === 1"
          class="btn secondary"
        >
          上一页
        </button>
        <span class="page-info">第 {{ currentPage }} 页，共 {{ totalPages }} 页</span>
        <button 
          @click="currentPage = Math.min(totalPages, currentPage + 1)" 
          :disabled="currentPage === totalPages"
          class="btn secondary"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 创建/编辑考试模态框 -->
    <div v-if="showCreateExamModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ editingExam ? '编辑考试' : '创建考试' }}</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveExam">
            <div class="form-row">
              <div class="form-group">
                <label>考试标题</label>
                <input type="text" v-model="examForm.title" required>
              </div>
              <div class="form-group">
                <label>考试时长(分钟)</label>
                <input type="number" v-model="examForm.duration" min="1" required>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>考试分类</label>
                <div class="category-input-group">
                  <select v-model="examForm.category" required>
                    <option value="">请选择分类</option>
                    <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
                  </select>
                  <button type="button" @click="showAddCategoryModal = true" class="btn secondary small">添加分类</button>
                </div>
              </div>
            </div>
            <div class="form-group">
              <label>考试描述</label>
              <textarea v-model="examForm.description" rows="3"></textarea>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>开始时间</label>
                <input type="datetime-local" v-model="examForm.startTime" required>
              </div>
              <div class="form-group">
                <label>结束时间</label>
                <input type="datetime-local" v-model="examForm.endTime" required>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>总分</label>
                <input type="number" v-model="examForm.totalScore" min="1" required>
              </div>
              <div class="form-group">
                <label>及格分</label>
                <input type="number" v-model="examForm.passScore" min="1" required>
              </div>
            </div>
            <div class="form-actions">
              <button type="button" @click="closeModal" class="btn secondary">取消</button>
              <button type="submit" class="btn primary">{{ editingExam ? '更新' : '创建' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 统计模态框 -->
    <div v-if="showStatisticsModal" class="modal-overlay" @click="showStatisticsModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>考试统计</h3>
          <button @click="showStatisticsModal = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="statistics-content" v-if="statistics">
            <div class="stats-overview">
              <div class="stat-item">
                <span class="stat-label">参与人数</span>
                <span class="stat-value">{{ statistics.totalParticipants }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">完成人数</span>
                <span class="stat-value">{{ statistics.completedCount }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">完成率</span>
                <span class="stat-value">{{ statistics.completionRate.toFixed(1) }}%</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">通过率</span>
                <span class="stat-value">{{ statistics.passRate.toFixed(1) }}%</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">平均分</span>
                <span class="stat-value">{{ statistics.averageScore || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加分类模态框 -->
    <div v-if="showAddCategoryModal" class="modal-overlay" @click="showAddCategoryModal = false">
      <div class="modal-content small-modal" @click.stop>
        <div class="modal-header">
          <h3>添加新分类</h3>
          <button @click="showAddCategoryModal = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>分类名称</label>
            <input type="text" v-model="newCategory" placeholder="请输入分类名称" maxlength="20">
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showAddCategoryModal = false" class="btn secondary">取消</button>
          <button @click="addNewCategory" class="btn primary">添加</button>
        </div>
      </div>
    </div>

    <!-- 题目管理模态框 -->
    <div v-if="showQuestionManagementModal" class="modal-overlay" @click="closeQuestionManagementModal">
      <div class="modal-content large-modal" @click.stop>
        <div class="modal-header">
          <h3>题目管理 - {{ currentExam?.title }}</h3>
          <button @click="closeQuestionManagementModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="question-management-header">
            <h4>考试题目列表</h4>
            <button @click="openAddQuestionModal" class="btn primary">添加题目</button>
          </div>
          
          <div class="question-list" v-if="!loadingQuestions">
            <div v-if="examQuestions.length === 0" class="no-data">暂无题目</div>
            <div v-else>
              <div v-for="(examQuestion, index) in examQuestions" :key="examQuestion.id" class="question-item">
                <div class="question-info">
                  <div class="question-header">
                    <span class="question-number">{{ index + 1 }}.</span>
                    <span class="question-type">{{ getQuestionTypeText(examQuestion.questionType) }}</span>
                    <span class="question-difficulty">{{ getDifficultyText(examQuestion.questionDifficulty) }}</span>
                  </div>
                  <div class="question-content">{{ examQuestion.questionContent }}</div>
                  <div class="question-meta">
                    <span>分值: {{ examQuestion.score }}分</span>
                    <span>顺序: {{ examQuestion.questionOrder }}</span>
                  </div>
                </div>
                <div class="question-actions">
                  <button @click="editQuestionScore(examQuestion)" class="btn secondary small">编辑分值</button>
                  <button @click="removeQuestionFromExam(examQuestion.questionId)" class="btn danger small">删除</button>
                </div>
              </div>
            </div>
          </div>
          
          <div v-if="loadingQuestions" class="loading">加载中...</div>
        </div>
      </div>
    </div>

    <!-- 添加题目模态框 -->
    <div v-if="showAddQuestionModal" class="modal-overlay" @click="showAddQuestionModal = false">
      <div class="modal-content large-modal question-selection-modal" @click.stop>
        <div class="modal-header">
          <h3>添加题目到考试</h3>
          <button @click="showAddQuestionModal = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="question-selection-container">
            <!-- 左侧：筛选和操作区域 -->
            <div class="selection-sidebar">
              <div class="selection-header">
                <h4>题目筛选</h4>
              </div>
              
              <!-- 筛选条件 -->
              <div class="filter-section">
                <div class="form-group">
                  <label>题目类型</label>
                  <select v-model="questionFilter.type" @change="filterQuestions">
                    <option value="">全部类型</option>
                    <option value="single">单选题</option>
                    <option value="multiple">多选题</option>
                    <option value="judge">判断题</option>
                    <option value="fill">填空题</option>
                    <option value="essay">问答题</option>
                  </select>
                </div>
                
                <div class="form-group">
                  <label>难度等级</label>
                  <select v-model="questionFilter.difficulty" @change="filterQuestions">
                    <option value="">全部难度</option>
                    <option value="easy">简单</option>
                    <option value="medium">中等</option>
                    <option value="hard">困难</option>
                  </select>
                </div>
                
                <div class="form-group">
                  <label>搜索题目</label>
                  <input 
                    type="text" 
                    v-model="questionFilter.keyword" 
                    @input="filterQuestions"
                    placeholder="输入关键词搜索..."
                    class="search-input"
                  >
                </div>
              </div>
              
              <!-- 已选题目信息 -->
              <div class="selected-info">
                <h5>已选择题目 ({{ selectedQuestionIds.length }})</h5>
                <div class="selected-questions" v-if="selectedQuestionIds.length > 0">
                  <div v-for="questionId in selectedQuestionIds" :key="questionId" class="selected-item">
                    <span>{{ getSelectedQuestionTitle(questionId) }}</span>
                    <button @click="removeFromSelection(questionId)" class="remove-btn">×</button>
                  </div>
                </div>
                <div v-else class="no-selection">暂未选择题目</div>
              </div>
              
              <!-- 批量操作 -->
              <div class="batch-actions">
                <div class="form-group">
                  <label>默认分值</label>
                  <input type="number" v-model="batchScore" min="1" max="100" class="score-input">
                </div>
                <button @click="clearSelection" class="btn secondary small">清空选择</button>
                <button @click="selectAllFiltered" class="btn secondary small">全选当前</button>
              </div>
            </div>
            
            <!-- 右侧：题目列表 -->
            <div class="question-list-area">
              <div class="list-header">
                <h4>题目列表 ({{ filteredQuestions.length }})</h4>
                <div class="list-actions">
                  <button @click="loadAvailableQuestions" class="btn secondary small">刷新</button>
                </div>
              </div>
              
              <div class="question-grid" v-if="!loadingQuestions">
                <div v-if="filteredQuestions.length === 0" class="no-data">没有找到符合条件的题目</div>
                <div v-else>
                  <div 
                    v-for="question in paginatedQuestions" 
                    :key="question.id" 
                    class="question-card"
                    :class="{ 'selected': selectedQuestionIds.includes(question.id) }"
                    @click="toggleQuestionSelection(question.id)"
                  >
                    <div class="question-card-header">
                      <div class="question-meta">
                        <span class="question-type-badge" :class="question.type.toLowerCase()">
                          {{ getQuestionTypeText(question.type) }}
                        </span>
                        <span class="difficulty-badge" :class="question.difficulty.toLowerCase()">
                          {{ getDifficultyText(question.difficulty) }}
                        </span>
                      </div>
                      <div class="selection-indicator">
                        <input 
                          type="checkbox" 
                          :checked="selectedQuestionIds.includes(question.id)"
                          @click.stop
                          @change="toggleQuestionSelection(question.id)"
                        >
                      </div>
                    </div>
                    
                    <div class="question-content-preview">
                      {{ question.content.length > 100 ? question.content.substring(0, 100) + '...' : question.content }}
                    </div>
                    
                    <div class="question-card-footer">
                      <span class="question-id">ID: {{ question.id }}</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div v-if="loadingQuestions" class="loading">加载题目中...</div>
              
              <!-- 分页 -->
              <div class="pagination" v-if="totalQuestionPages > 1">
                <button 
                  @click="currentQuestionPage = Math.max(1, currentQuestionPage - 1)" 
                  :disabled="currentQuestionPage === 1"
                  class="btn secondary small"
                >
                  上一页
                </button>
                <span class="page-info">{{ currentQuestionPage }} / {{ totalQuestionPages }}</span>
                <button 
                  @click="currentQuestionPage = Math.min(totalQuestionPages, currentQuestionPage + 1)" 
                  :disabled="currentQuestionPage === totalQuestionPages"
                  class="btn secondary small"
                >
                  下一页
                </button>
              </div>
            </div>
          </div>
          
          <!-- 底部操作按钮 -->
          <div class="modal-footer">
            <div class="footer-info">
              <span>已选择 {{ selectedQuestionIds.length }} 个题目</span>
            </div>
            <div class="footer-actions">
              <button type="button" @click="showAddQuestionModal = false" class="btn secondary">取消</button>
              <button 
                @click="batchAddQuestionsToExam" 
                :disabled="selectedQuestionIds.length === 0"
                class="btn primary"
              >
                批量添加 ({{ selectedQuestionIds.length }})
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import axios from 'axios'

export default {
  name: 'ExamManagement',
  components: {
    Layout
  },
  data() {
    return {
      // 考试管理相关
      showCreateExamModal: false,
      showStatisticsModal: false,
      exams: [],
      loading: false,
      statusFilter: '',
      categoryFilter: '',
      searchTitle: '',
      currentPage: 1,
      totalPages: 1,
      pageSize: 10,
      
      // 考试表单
      editingExam: null,
      examForm: {
        title: '',
        description: '',
        category: '',
        startTime: '',
        endTime: '',
        duration: 120,
        totalScore: 100,
        passScore: 60
      },
      
      // 分类相关
      categories: ['期中考试', '期末考试', '随堂测验', '模拟考试', '补考'],
      showAddCategoryModal: false,
      newCategory: '',
      
      // 统计数据
      statistics: null,
      
      // 题目管理
      showQuestionManagementModal: false,
      currentExam: null,
      examQuestions: [],
      availableQuestions: [],
      loadingQuestions: false,
      showAddQuestionModal: false,
      selectedQuestionIds: [],
      questionForm: {
        questionId: null,
        score: 10,
        order: 1
      },
      
      // 题目筛选和分页
      questionFilter: {
        type: '',
        difficulty: '',
        keyword: ''
      },
      filteredQuestions: [],
      currentQuestionPage: 1,
      questionPageSize: 12,
      batchScore: 10,
      
      // 倒计时定时器
      countdownTimer: null,
      // 当前时间，用于响应式更新
      currentTime: new Date().getTime()
    }
  },
  computed: {
    // 按状态排序和分类筛选的考试列表（待开始>考试中>已结束）
    sortedExams() {
      let filteredExams = [...this.exams]
      
      // 按分类筛选
      if (this.categoryFilter) {
        filteredExams = filteredExams.filter(exam => exam.category === this.categoryFilter)
      }
      
      // 按状态筛选（基于前端时间计算）
      if (this.statusFilter) {
        filteredExams = filteredExams.filter(exam => {
          const examStatus = this.getExamStatus(exam)
          return examStatus === this.statusFilter
        })
      }
      
      // 按状态排序
      return filteredExams.sort((a, b) => {
        const statusA = this.getExamStatus(a)
        const statusB = this.getExamStatus(b)
        
        // 定义状态优先级：pending(0) > active(1) > completed(2)
        const statusPriority = {
          'pending': 0,
          'active': 1, 
          'completed': 2
        }
        
        const priorityA = statusPriority[statusA] || 3
        const priorityB = statusPriority[statusB] || 3
        
        // 按优先级排序，如果优先级相同则按开始时间排序
        if (priorityA !== priorityB) {
          return priorityA - priorityB
        }
        
        // 同状态下按开始时间排序
        return new Date(a.startTime) - new Date(b.startTime)
      })
    },
    
    // 分页后的题目列表
    paginatedQuestions() {
      const start = (this.currentQuestionPage - 1) * this.questionPageSize
      const end = start + this.questionPageSize
      return this.filteredQuestions.slice(start, end)
    },
    
    // 题目总页数
    totalQuestionPages() {
      return Math.ceil(this.filteredQuestions.length / this.questionPageSize)
    }
  },
  mounted() {
    this.loadExams()
    // 启动倒计时定时器，每秒更新一次
    this.countdownTimer = setInterval(() => {
      this.currentTime = new Date().getTime() // 更新响应式数据
    }, 1000)
  },
  beforeDestroy() {
    // 清理定时器
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
      this.countdownTimer = null
    }
  },
  methods: {
    // 加载考试列表
    async loadExams() {
      console.log('开始加载考试列表...')
      this.loading = true
      try {
        const params = {
          page: this.currentPage - 1,
          size: this.pageSize
        }
        
        // 只保留标题搜索的后端筛选，状态筛选改为前端处理
        if (this.searchTitle) params.title = this.searchTitle
        
        console.log('请求参数:', params)
        const response = await axios.get('/api/v1/exams', { params })
        console.log('API响应:', response.data)
        
        if (response.data && response.data.code === 200) {
          this.exams = response.data.data.list || []
          this.totalPages = response.data.data.pages || 1
          console.log('成功加载考试列表，数量:', this.exams.length)
        } else {
          console.error('API返回失败:', response.data)
          alert('获取考试列表失败: ' + (response.data.message || '未知错误'))
        }
      } catch (error) {
        console.error('加载考试列表失败:', error)
        console.error('错误详情:', error.response?.data)
        alert('加载考试列表失败: ' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    
    // 创建考试
    async createExam() {
      console.log('开始创建考试:', this.examForm)
      try {
        const response = await axios.post('/api/v1/exams', this.examForm)
        console.log('创建考试响应:', response.data)
        
        if (response.data && response.data.code === 200) {
          alert('考试创建成功')
          this.closeModal()
          this.loadExams()
        } else {
          console.error('创建考试失败:', response.data)
          alert('创建考试失败: ' + (response.data.message || '未知错误'))
        }
      } catch (error) {
        console.error('创建考试失败:', error)
        console.error('错误详情:', error.response?.data)
        alert('创建考试失败: ' + (error.response?.data?.message || error.message))
      }
    },
    
    // 更新考试
    async updateExam() {
      console.log('开始更新考试:', this.examForm)
      console.log('考试ID:', this.editingExam.id)
      try {
        // 确保时间格式正确
        const examData = {
          ...this.examForm,
          startTime: this.examForm.startTime ? new Date(this.examForm.startTime).toISOString() : null,
          endTime: this.examForm.endTime ? new Date(this.examForm.endTime).toISOString() : null
        }
        console.log('发送的考试数据:', examData)
        
        const response = await axios.put(`/api/v1/exams/${this.editingExam.id}`, examData)
        console.log('更新考试响应:', response.data)
        
        if (response.data && response.data.code === 200) {
          alert('考试更新成功')
          this.closeModal()
          this.loadExams()
        } else {
          console.error('更新考试失败:', response.data)
          alert('更新考试失败: ' + (response.data.message || '未知错误'))
        }
      } catch (error) {
        console.error('更新考试失败:', error)
        console.error('错误详情:', error.response?.data)
        alert('更新考试失败: ' + (error.response?.data?.message || error.message))
      }
    },
    
    // 删除考试
    async deleteExam(examId) {
      if (!confirm('确定要删除这个考试吗？')) return
      
      try {
        await axios.delete(`/api/v1/exams/${examId}`)
        alert('考试删除成功')
        this.loadExams()
      } catch (error) {
        console.error('删除考试失败:', error)
        alert('删除考试失败')
      }
    },
    
    // 编辑考试
    editExam(exam) {
      this.editingExam = exam
      this.examForm = {
        title: exam.title,
        description: exam.description,
        category: exam.category,
        startTime: this.formatDateTimeForInput(exam.startTime),
        endTime: this.formatDateTimeForInput(exam.endTime),
        duration: exam.duration,
        totalScore: exam.totalScore,
        passScore: exam.passScore
      }
      this.showCreateExamModal = true
    },
    
    // 查看统计
    async viewStatistics(exam) {
      try {
        const response = await axios.get(`/api/v1/exams/${exam.id}/statistics`)
        if (response.data && response.data.code === 200) {
          this.statistics = response.data.data
          this.showStatisticsModal = true
        } else {
          console.error('获取统计数据失败:', response.data)
          alert('获取统计数据失败: ' + (response.data.message || '未知错误'))
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
        alert('获取统计数据失败: ' + (error.response?.data?.message || error.message))
      }
    },
    
    // 保存考试
    saveExam() {
      if (this.editingExam) {
        this.updateExam()
      } else {
        this.createExam()
      }
    },
    
    // 关闭模态框
    closeModal() {
      this.showCreateExamModal = false
      this.editingExam = null
      this.examForm = {
        title: '',
        description: '',
        category: '',
        startTime: '',
        endTime: '',
        duration: 120,
        totalScore: 100,
        passScore: 60
      }
    },
    
    // 添加新分类
    addNewCategory() {
      if (!this.newCategory.trim()) {
        alert('请输入分类名称')
        return
      }
      if (this.categories.includes(this.newCategory.trim())) {
        alert('该分类已存在')
        return
      }
      this.categories.push(this.newCategory.trim())
      this.examForm.category = this.newCategory.trim()
      this.newCategory = ''
      this.showAddCategoryModal = false
    },
    
    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      return new Date(dateTime).toLocaleString('zh-CN')
    },
    
    // 格式化日期时间用于输入框
    formatDateTimeForInput(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      // 直接格式化为本地时间，不进行时区转换
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
    },
    
    // 获取考试状态（基于时间动态判断）
    getExamStatus(exam) {
      // 使用响应式的 currentTime 而不是直接获取当前时间
      const now = this.currentTime
      const startTime = new Date(exam.startTime).getTime()
      const endTime = new Date(exam.endTime).getTime()
      
      if (now < startTime) {
        return 'pending' // 待考试
      } else if (now >= startTime && now <= endTime) {
        return 'active' // 考试中
      } else {
        return 'completed' // 已结束
      }
    },
    
    // 获取状态文本
    getStatusText(exam) {
      const status = this.getExamStatus(exam)
      const statusMap = {
        pending: '待考试',
        active: '考试中',
        completed: '已结束'
      }
      return statusMap[status] || status
    },
    
    // 计算倒计时
    getCountdown(exam) {
      // 使用响应式的 currentTime 而不是直接获取当前时间
      const now = this.currentTime
      const startTime = new Date(exam.startTime).getTime()
      const endTime = new Date(exam.endTime).getTime()
      const status = this.getExamStatus(exam)
      
      let targetTime
      if (status === 'pending') {
        targetTime = startTime // 距离开始时间
      } else if (status === 'active') {
        targetTime = endTime // 距离结束时间
      } else {
        return null // 已结束，无倒计时
      }
      
      const diff = targetTime - now
      if (diff <= 0) return null
      
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      const seconds = Math.floor((diff % (1000 * 60)) / 1000)
      
      if (days > 0) {
        return `${days}天 ${hours}时 ${minutes}分 ${seconds}秒`
      } else if (hours > 0) {
        return `${hours}时 ${minutes}分 ${seconds}秒`
      } else if (minutes > 0) {
        return `${minutes}分 ${seconds}秒`
      } else {
        return `${seconds}秒`
      }
    },
    
    // 获取倒计时前缀文本
    getCountdownPrefix(exam) {
      const status = this.getExamStatus(exam)
      if (status === 'pending') {
        return '距离开始：'
      } else if (status === 'active') {
        return '距离结束：'
      }
      return ''
    },
    
    // 题目管理
    async manageQuestions(exam) {
      this.currentExam = exam
      this.showQuestionManagementModal = true
      await this.loadExamQuestions()
    },
    
    // 加载考试题目
    async loadExamQuestions() {
      this.loadingQuestions = true
      try {
        const response = await axios.get(`/api/v1/exams/${this.currentExam.id}/questions`)
        if (response.data && response.data.code === 200) {
          this.examQuestions = response.data.data || []
        } else {
          console.error('获取考试题目失败:', response.data)
          alert('获取考试题目失败')
        }
      } catch (error) {
        console.error('获取考试题目失败:', error)
        alert('获取考试题目失败')
      } finally {
        this.loadingQuestions = false
      }
    },
    
    // 加载可用题目
    async loadAvailableQuestions() {
      this.loadingQuestions = true
      try {
        const response = await axios.get('/api/questions', {
          params: { page: 0, size: 1000 } // 加载更多题目以支持筛选
        })
        if (response.data && response.data.code === 200) {
          this.availableQuestions = response.data.data.list || []
          this.filterQuestions() // 初始化筛选
        }
      } catch (error) {
        console.error('获取可用题目失败:', error)
        alert('获取可用题目失败')
      } finally {
        this.loadingQuestions = false
      }
    },
    
    // 添加题目到考试
    async addQuestionToExam() {
      if (!this.questionForm.questionId) {
        alert('请选择题目')
        return
      }
      
      try {
        const response = await axios.post(`/api/v1/exams/${this.currentExam.id}/questions`, {
          questionId: this.questionForm.questionId,
          score: this.questionForm.score,
          order: this.questionForm.order
        })
        
        if (response.data && response.data.code === 200) {
          alert('添加题目成功')
          this.showAddQuestionModal = false
          this.questionForm = { questionId: null, score: 10, order: 1 }
          await this.loadExamQuestions()
        } else {
          alert('添加题目失败: ' + (response.data.message || '未知错误'))
        }
      } catch (error) {
        console.error('添加题目失败:', error)
        alert('添加题目失败')
      }
    },
    
    // 从考试中删除题目
    async removeQuestionFromExam(questionId) {
      if (!confirm('确定要从考试中删除这个题目吗？')) return
      
      try {
        const response = await axios.delete(`/api/v1/exams/${this.currentExam.id}/questions/${questionId}`)
        if (response.data && response.data.code === 200) {
          alert('删除题目成功')
          await this.loadExamQuestions()
        } else {
          alert('删除题目失败')
        }
      } catch (error) {
        console.error('删除题目失败:', error)
        alert('删除题目失败')
      }
    },
    
    // 更新题目分数
    async updateQuestionScore(questionId, newScore) {
      try {
        const response = await axios.put(`/api/v1/exams/${this.currentExam.id}/questions/${questionId}/score`, {
          score: newScore
        })
        
        if (response.data && response.data.code === 200) {
          alert('更新分数成功')
          await this.loadExamQuestions()
        } else {
          alert('更新分数失败')
        }
      } catch (error) {
        console.error('更新分数失败:', error)
        alert('更新分数失败')
      }
    },
    
    // 打开添加题目模态框
    async openAddQuestionModal() {
      this.showAddQuestionModal = true
      await this.loadAvailableQuestions()
    },
    
    // 关闭题目管理模态框
    closeQuestionManagementModal() {
      this.showQuestionManagementModal = false
      this.currentExam = null
      this.examQuestions = []
      this.availableQuestions = []
    },
    
    // 获取题目类型文本
    getQuestionTypeText(type) {
      const typeMap = {
        SINGLE_CHOICE: '单选题',
        MULTIPLE_CHOICE: '多选题',
        TRUE_FALSE: '判断题',
        FILL_BLANK: '填空题',
        SHORT_ANSWER: '简答题',
        ESSAY: '论述题'
      }
      return typeMap[type] || type
    },
    
    // 获取难度文本
    getDifficultyText(difficulty) {
      const difficultyMap = {
        EASY: '简单',
        MEDIUM: '中等',
        HARD: '困难'
      }
      return difficultyMap[difficulty] || difficulty
    },
    
    // 编辑题目分值
    editQuestionScore(examQuestion) {
      const newScore = prompt('请输入新的分值:', examQuestion.score)
      if (newScore && !isNaN(newScore) && newScore > 0) {
        this.updateQuestionScore(examQuestion.questionId, parseFloat(newScore))
      }
    },
    
    // 题目筛选
    filterQuestions() {
      let filtered = [...this.availableQuestions]
      
      // 按类型筛选
      if (this.questionFilter.type) {
        filtered = filtered.filter(q => q.type === this.questionFilter.type)
      }
      
      // 按难度筛选
      if (this.questionFilter.difficulty) {
        filtered = filtered.filter(q => q.difficulty === this.questionFilter.difficulty)
      }
      
      // 按关键词搜索
      if (this.questionFilter.keyword) {
        const keyword = this.questionFilter.keyword.toLowerCase()
        filtered = filtered.filter(q => 
          q.content.toLowerCase().includes(keyword) ||
          q.title?.toLowerCase().includes(keyword)
        )
      }
      
      this.filteredQuestions = filtered
      this.currentQuestionPage = 1 // 重置到第一页
    },
    
    // 切换题目选择状态
    toggleQuestionSelection(questionId) {
      const index = this.selectedQuestionIds.indexOf(questionId)
      if (index > -1) {
        this.selectedQuestionIds.splice(index, 1)
      } else {
        this.selectedQuestionIds.push(questionId)
      }
    },
    
    // 从选择中移除题目
    removeFromSelection(questionId) {
      const index = this.selectedQuestionIds.indexOf(questionId)
      if (index > -1) {
        this.selectedQuestionIds.splice(index, 1)
      }
    },
    
    // 清空选择
    clearSelection() {
      this.selectedQuestionIds = []
    },
    
    // 全选当前筛选结果
    selectAllFiltered() {
      const currentPageIds = this.paginatedQuestions.map(q => q.id)
      currentPageIds.forEach(id => {
        if (!this.selectedQuestionIds.includes(id)) {
          this.selectedQuestionIds.push(id)
        }
      })
    },
    
    // 获取选中题目的标题
    getSelectedQuestionTitle(questionId) {
      const question = this.availableQuestions.find(q => q.id === questionId)
      if (question) {
        const title = question.content.substring(0, 30) + (question.content.length > 30 ? '...' : '')
        return `[${this.getQuestionTypeText(question.type)}] ${title}`
      }
      return `题目 ${questionId}`
    },
    
    // 批量添加题目到考试
    async batchAddQuestionsToExam() {
      if (this.selectedQuestionIds.length === 0) {
        alert('请先选择题目')
        return
      }
      
      try {
        const promises = this.selectedQuestionIds.map((questionId, index) => {
          return axios.post(`/api/v1/exams/${this.currentExam.id}/questions`, {
            questionId: questionId,
            score: this.batchScore,
            order: this.examQuestions.length + index + 1
          })
        })
        
        await Promise.all(promises)
        
        alert(`成功添加 ${this.selectedQuestionIds.length} 个题目到考试`)
        this.showAddQuestionModal = false
        this.selectedQuestionIds = []
        this.questionFilter = { type: '', difficulty: '', keyword: '' }
        await this.loadExamQuestions()
      } catch (error) {
        console.error('批量添加题目失败:', error)
        alert('批量添加题目失败: ' + (error.response?.data?.message || error.message))
      }
    }
  }
}
</script>

<style scoped>
/* 页面头部 */
.content-header {
  margin-bottom: 2rem;
}

.content-header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 0.5rem 0;
}

.subtitle {
  color: #64748b;
  font-size: 1rem;
  margin: 0;
}

/* 快速操作 */
.quick-actions {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}

.quick-actions h3 {
  margin: 0 0 1rem 0;
  color: #1e293b;
  font-size: 1.2rem;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
  text-decoration: none;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.action-btn.secondary {
  background: #e2e8f0;
  color: #475569;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-btn .icon {
  font-size: 1.1rem;
}

/* 考试管理区域 */
.exam-management {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.exam-header h3 {
  margin: 0;
  color: #1e293b;
  font-size: 1.2rem;
}

.exam-filters {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.exam-filters select,
.search-input {
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
}

.search-input {
  min-width: 200px;
}

.exam-list {
  margin-bottom: 1.5rem;
}

.loading,
.no-data {
  text-align: center;
  padding: 2rem;
  color: #64748b;
  font-size: 1rem;
}

.exam-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.5rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 1rem;
  transition: all 0.3s ease;
}

.exam-item:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.exam-info {
  flex: 1;
}

.exam-info h4 {
  margin: 0 0 0.5rem 0;
  color: #1e293b;
  font-size: 1.1rem;
}

.exam-desc {
  margin: 0 0 1rem 0;
  color: #64748b;
  font-size: 0.9rem;
  line-height: 1.4;
}

.exam-meta {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  font-size: 0.85rem;
}

.exam-meta span {
  color: #64748b;
}

.exam-status {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-weight: 500;
}

.exam-status.pending {
  background: #fef3c7;
  color: #92400e;
}

.exam-status.active {
  background: #dcfce7;
  color: #166534;
}

.exam-status.completed {
  background: #e0e7ff;
  color: #3730a3;
}

.exam-actions {
  display: flex;
  gap: 0.5rem;
  flex-shrink: 0;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 1.5rem;
}

.page-info {
  color: #64748b;
  font-size: 0.9rem;
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
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
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
  font-size: 1.2rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #64748b;
  padding: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 1.5rem;
}

/* 表单样式 */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #374151;
  font-weight: 500;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: border-color 0.3s ease;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #667eea;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn.primary {
  background: #667eea;
  color: white;
}

.btn.secondary {
  background: #e5e7eb;
  color: #374151;
}

.btn.danger {
  background: #ef4444;
  color: white;
}

.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 统计内容样式 */
.statistics-content {
  padding: 1.5rem;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
}

.stat-item {
  text-align: center;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 8px;
}

.stat-label {
  display: block;
  color: #64748b;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

.stat-value {
  display: block;
  color: #1e293b;
  font-size: 1.5rem;
  font-weight: 700;
}

/* 题目管理模态框样式 */
.large-modal {
  max-width: 900px;
  width: 90vw;
  max-height: 80vh;
  overflow-y: auto;
}

/* 题目选择模态框样式 */
.question-selection-modal {
  max-width: 1200px;
  width: 95%;
}

.question-selection-container {
  display: flex;
  gap: 1.5rem;
  height: 70vh;
}

/* 左侧筛选区域 */
.selection-sidebar {
  width: 300px;
  background: #f8fafc;
  border-radius: 8px;
  padding: 1rem;
  overflow-y: auto;
}

.selection-header h4 {
  margin: 0 0 1rem 0;
  color: #1e293b;
  font-size: 1.1rem;
}

.filter-section {
  margin-bottom: 1.5rem;
}

.filter-section .form-group {
  margin-bottom: 1rem;
}

.filter-section label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
  font-size: 0.9rem;
}

.filter-section select,
.filter-section .search-input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
}

/* 已选题目信息 */
.selected-info {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: white;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.selected-info h5 {
  margin: 0 0 0.5rem 0;
  color: #1e293b;
  font-size: 0.95rem;
}

.selected-questions {
  max-height: 150px;
  overflow-y: auto;
}

.selected-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  margin-bottom: 0.5rem;
  background: #f3f4f6;
  border-radius: 4px;
  font-size: 0.85rem;
}

.selected-item span {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-btn {
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  cursor: pointer;
  font-size: 0.8rem;
  margin-left: 0.5rem;
}

.no-selection {
  color: #6b7280;
  font-size: 0.85rem;
  text-align: center;
  padding: 1rem;
}

/* 批量操作 */
.batch-actions {
  padding: 1rem;
  background: white;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.batch-actions .form-group {
  margin-bottom: 1rem;
}

.batch-actions label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
  font-size: 0.9rem;
}

.score-input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
}

.batch-actions button {
  width: 100%;
  margin-bottom: 0.5rem;
}

/* 右侧题目列表区域 */
.question-list-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.list-header h4 {
  margin: 0;
  color: #1e293b;
  font-size: 1.1rem;
}

.question-grid {
  flex: 1;
  overflow-y: auto;
  padding-right: 0.5rem;
}

/* 题目卡片样式 */
.question-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
}

.question-card:hover {
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
}

.question-card.selected {
  border-color: #3b82f6;
  background: #eff6ff;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
}

.question-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.question-meta {
  display: flex;
  gap: 0.5rem;
}

.question-type-badge,
.difficulty-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}

.question-type-badge.single_choice {
  background: #dbeafe;
  color: #1e40af;
}

.question-type-badge.multiple_choice {
  background: #fef3c7;
  color: #92400e;
}

.question-type-badge.true_false {
  background: #d1fae5;
  color: #065f46;
}

.question-type-badge.fill_blank {
  background: #fce7f3;
  color: #be185d;
}

.question-type-badge.short_answer {
  background: #e0e7ff;
  color: #3730a3;
}

.question-type-badge.essay {
  background: #f3e8ff;
  color: #6b21a8;
}

.difficulty-badge.easy {
  background: #dcfce7;
  color: #166534;
}

.difficulty-badge.medium {
  background: #fef3c7;
  color: #92400e;
}

.difficulty-badge.hard {
  background: #fee2e2;
  color: #991b1b;
}

.selection-indicator input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.question-content-preview {
  color: #374151;
  font-size: 0.9rem;
  line-height: 1.4;
  margin-bottom: 0.75rem;
}

.question-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #6b7280;
}

/* 模态框底部 */
.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
  margin-top: 1rem;
}

.footer-info {
  color: #6b7280;
  font-size: 0.9rem;
}

.footer-actions {
  display: flex;
  gap: 0.75rem;
}

/* 分页样式优化 */
.question-grid .pagination {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
}

.question-grid .pagination .page-info {
  font-size: 0.9rem;
  color: #6b7280;
}

.question-management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e2e8f0;
}

.question-management-header h4 {
  margin: 0;
  color: #1e293b;
  font-size: 1.1rem;
}

.question-list {
  max-height: 400px;
  overflow-y: auto;
}

.question-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 1rem;
  background: #f8fafc;
}

.question-info {
  flex: 1;
  margin-right: 1rem;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.question-number {
  font-weight: 600;
  color: #1e293b;
}

.question-type,
.question-difficulty {
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
}

/* 考试状态和倒计时样式 */
.exam-status {
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.exam-status.pending {
  background: #dbeafe;
  color: #1e40af;
  border: 1px solid #3b82f6;
}

.exam-status.active {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #22c55e;
  animation: pulse-active 2s infinite;
}

.exam-status.completed {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #9ca3af;
}

@keyframes pulse-active {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.exam-countdown {
  margin-top: 0.75rem;
  padding: 0.75rem;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 4px solid #3b82f6;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.countdown-label {
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 500;
}

.countdown-time {
  font-size: 1.1rem;
  font-weight: 700;
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
}

.countdown-time.pending {
  color: #1e40af;
}

.countdown-time.active {
  color: #dc2626;
  animation: blink-urgent 1s infinite;
}

@keyframes blink-urgent {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0.6;
  }
}

/* 考试项目整体样式优化 */
.exam-item {
  transition: all 0.3s ease;
  position: relative;
}

.exam-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.exam-item.pending {
  border-left: 4px solid #3b82f6;
}

.exam-item.active {
  border-left: 4px solid #22c55e;
  background: linear-gradient(135deg, #f0fdf4 0%, #ffffff 100%);
}

.exam-item.completed {
  border-left: 4px solid #9ca3af;
  opacity: 0.8;
}

.question-type {
  background: #dbeafe;
  color: #1e40af;
  font-size: 0.8rem;
  font-weight: 500;
}

.question-difficulty {
  background: #fef3c7;
  color: #92400e;
  font-size: 0.8rem;
  font-weight: 500;
}

.question-content {
  color: #374151;
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.question-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.9rem;
  color: #64748b;
}

.question-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.btn.small {
  padding: 0.4rem 0.8rem;
  font-size: 0.8rem;
  min-width: 80px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .action-buttons {
    flex-direction: column;
  }
  
  .exam-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .exam-filters {
    flex-direction: column;
  }
  
  .search-input {
    min-width: auto;
  }
  
  .exam-item {
    flex-direction: column;
    gap: 1rem;
  }
  
  .exam-actions {
    justify-content: flex-start;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .modal-content {
    width: 95%;
    margin: 1rem;
  }
  
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 分类相关样式 */
.category-input-group {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.category-input-group select {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
}

.category-input-group .btn.small {
  padding: 0.5rem 0.75rem;
  font-size: 0.8rem;
  white-space: nowrap;
}

.small-modal {
  max-width: 400px;
}

.exam-category {
  display: inline-block;
  background: #e0f2fe;
  color: #0277bd;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
}
</style>