<template>
  <Layout>
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">题目管理</h1>
        <p class="page-subtitle">管理系统中的所有题目</p>
      </div>
      <div class="header-right">
        <button @click="downloadTemplate" class="btn btn-outline" :disabled="loading">
          <i class="icon">📋</i>
          <span>下载模板</span>
        </button>
        <div class="upload-container">
          <input 
            ref="fileInput" 
            type="file" 
            accept=".xlsx,.xls" 
            @change="handleFileUpload" 
            style="display: none"
          >
          <button @click="$refs.fileInput.click()" class="btn btn-outline" :disabled="loading || importing">
            <i class="icon">📤</i>
            <span>{{ importing ? '导入中...' : '导入Excel' }}</span>
          </button>
        </div>
        <button @click="exportQuestions" class="btn btn-secondary" :disabled="loading">
          <i class="icon">📊</i>
          <span>导出Excel</span>
        </button>
        <button @click="resetForm(); showAddModal = true" class="btn btn-primary">
          <i class="icon">➕</i>
          <span>添加题目</span>
        </button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-section">
      <div class="search-box">
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="搜索题目内容、标签或ID..."
          class="search-input"
          @input="handleSearch"
        >
        <i class="search-icon">🔍</i>
      </div>
      
      <div class="filter-controls">
        <select v-model="selectedDifficulty" @change="handleFilter" class="filter-select">
          <option value="">所有难度</option>
          <option value="easy">简单</option>
          <option value="medium">中等</option>
          <option value="hard">困难</option>
        </select>
        
        <select v-model="selectedType" @change="handleFilter" class="filter-select">
          <option value="">所有类型</option>
          <option value="single">单选题</option>
          <option value="multiple">多选题</option>
          <option value="judge">判断题</option>
          <option value="fill">填空题</option>
          <option value="essay">问答题</option>
        </select>
        
        <button @click="resetFilters" class="btn btn-secondary">
          <i class="icon">🔄</i>
          <span>重置</span>
        </button>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-label">总题目数：</span>
        <span class="stat-value">{{ totalQuestions }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">已筛选：</span>
        <span class="stat-value">{{ questions.length }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">每页显示：</span>
        <select v-model="pageSize" @change="currentPage = 1; loadQuestions()" class="page-size-select">
          <option value="10">10条</option>
          <option value="20">20条</option>
          <option value="50">50条</option>
          <option value="100">100条</option>
        </select>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="question-list">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-if="error && !loading" class="error-state">
        <div class="error-icon">⚠️</div>
        <h3>加载失败</h3>
        <p>{{ error }}</p>
        <button @click="error = ''; loadQuestions()" class="btn btn-primary">重试</button>
      </div>
      
      <div v-else-if="questions.length === 0 && !error" class="empty-state">
        <div class="empty-icon">📝</div>
        <h3>暂无题目</h3>
        <p>{{ searchQuery ? '没有找到匹配的题目' : '还没有添加任何题目，点击上方按钮开始添加' }}</p>
      </div>
      
      <div v-else class="question-cards">
        <div 
          v-for="question in paginatedQuestions" 
          :key="question.id" 
          class="question-card"
        >
          <!-- 题目头部 -->
          <div class="card-header">
            <div class="question-meta">
              <span class="question-id">#{{ question.id }}</span>
              <span :class="['difficulty-badge', question.difficulty]">{{ getDifficultyText(question.difficulty) }}</span>
              <span :class="['type-badge', question.type]">{{ getTypeText(question.type) }}</span>
            </div>
            <div class="card-actions">
              <button @click="editQuestion(question)" class="action-btn edit" title="编辑">
                <i class="icon">✏️</i>
              </button>
              <button @click="deleteQuestion(question.id)" class="action-btn delete" title="删除">
                <i class="icon">🗑️</i>
              </button>
            </div>
          </div>
          
          <!-- 题目内容 -->
          <div class="card-content">
            <h3 class="question-title">{{ question.title }}</h3>
            <div class="question-content" v-html="question.content"></div>
            
            <!-- 选项（如果是选择题） -->
            <div v-if="question.options && question.options.length > 0" class="question-options">
              <div 
                v-for="(option, index) in question.options" 
                :key="index" 
                :class="['option-item', { 'correct': isCorrectOption(question, index) }]"
              >
                <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
                <span class="option-text">{{ option }}</span>
                <i v-if="isCorrectOption(question, index)" class="correct-icon">✓</i>
              </div>
            </div>
            
            <!-- 正确答案（非选择题） -->
            <div v-else-if="question.correctAnswer" class="correct-answer">
              <strong>正确答案：</strong>{{ question.correctAnswer }}
            </div>
          </div>
          
          <!-- 题目标签 -->
          <div v-if="question.tags && question.tags.length > 0" class="card-tags">
            <span v-for="tag in question.tags" :key="tag" class="tag">{{ tag }}</span>
          </div>
          
          <!-- 题目底部信息 -->
          <div class="card-footer">
            <span class="create-time">创建时间：{{ formatDate(question.createTime) }}</span>
            <span class="update-time">更新时间：{{ formatDate(question.updateTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页信息 -->
    <div v-if="!loading && totalQuestions > 0" class="pagination-info">
      <span>共 {{ totalQuestions }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页</span>
    </div>

    <!-- 分页 -->
    <div v-if="!loading && questions.length > 0" class="pagination">
      <button 
        @click="currentPage = 1" 
        :disabled="currentPage === 1"
        class="page-btn"
      >
        首页
      </button>
      <button 
        @click="currentPage--" 
        :disabled="currentPage === 1"
        class="page-btn"
      >
        上一页
      </button>
      
      <div class="page-numbers">
        <button 
          v-for="page in visiblePages" 
          :key="page"
          @click="currentPage = page"
          :class="['page-number', { active: currentPage === page }]"
        >
          {{ page }}
        </button>
      </div>
      
      <button 
        @click="currentPage++" 
        :disabled="currentPage === totalPages"
        class="page-btn"
      >
        下一页
      </button>
      <button 
        @click="currentPage = totalPages" 
        :disabled="currentPage === totalPages"
        class="page-btn"
      >
        末页
      </button>
    </div>

    <!-- 添加/编辑题目模态框 (Teleport 到 body 避免层叠上下文问题) -->
    <Teleport to="body">
    <div v-if="showAddModal || showEditModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ showAddModal ? '添加题目' : '编辑题目' }}</h2>
          <button @click="closeModal" class="close-btn">×</button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="saveQuestion">
            <!-- 题目标题 -->
            <div class="form-group">
              <label>题目标题 *</label>
              <input 
                v-model="questionForm.title" 
                type="text" 
                placeholder="请输入题目标题"
                class="form-input"
                required
              >
            </div>
            
            <!-- 题目内容 -->
            <div class="form-group">
              <label>题目内容 *</label>
              <textarea 
                v-model="questionForm.content" 
                placeholder="请输入题目内容"
                class="form-textarea"
                rows="4"
                required
              ></textarea>
            </div>
            
            <!-- 题目类型 -->
            <div class="form-group">
              <label>题目类型 *</label>
              <select v-model="questionForm.type" @change="handleTypeChange" class="form-select" required>
                <option value="">请选择题目类型</option>
                <option value="single">单选题</option>
                <option value="multiple">多选题</option>
                <option value="judge">判断题</option>
                <option value="fill">填空题</option>
                <option value="essay">问答题</option>
              </select>
            </div>
            
            <!-- 难度等级 -->
            <div class="form-group">
              <label>难度等级 *</label>
              <select v-model="questionForm.difficulty" class="form-select" required>
                <option value="">请选择难度等级</option>
                <option value="easy">简单</option>
                <option value="medium">中等</option>
                <option value="hard">困难</option>
              </select>
            </div>
            
            <!-- 选项设置（选择题和判断题） -->
            <div v-if="['single', 'multiple', 'judge'].includes(questionForm.type)" class="form-group">
              <label>选项设置 *</label>
              <div class="options-container">
                <div 
                  v-for="(option, index) in questionForm.options" 
                  :key="index" 
                  class="option-input-group"
                >
                  <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
                  <input 
                    v-model="questionForm.options[index]" 
                    type="text" 
                    :placeholder="`选项 ${String.fromCharCode(65 + index)}`"
                    class="option-input"
                    :readonly="questionForm.type === 'judge'"
                    required
                  >
                  <label class="checkbox-wrapper">
                    <input 
                      type="checkbox" 
                      :checked="questionForm.correctAnswers.includes(index)"
                      @change="toggleCorrectAnswer(index)"
                    >
                    <span class="checkmark"></span>
                    <span class="checkbox-label">正确</span>
                  </label>
                  <button 
                    v-if="questionForm.options.length > 2 && questionForm.type !== 'judge'" 
                    @click="removeOption(index)" 
                    type="button" 
                    class="remove-option-btn"
                  >
                    ×
                  </button>
                </div>
                
                <button 
                  v-if="questionForm.options.length < 6 && questionForm.type !== 'judge'" 
                  @click="addOption" 
                  type="button" 
                  class="add-option-btn"
                >
                  + 添加选项
                </button>
                
                <!-- 判断题提示 -->
                <div v-if="questionForm.type === 'judge'" class="judge-tip">
                  <i class="tip-icon">💡</i>
                  <span>判断题选项已自动设置为"正确"和"错误"</span>
                </div>
              </div>
            </div>
            
            <!-- 正确答案（填空题和问答题） -->
            <div v-else-if="['fill', 'essay'].includes(questionForm.type)" class="form-group">
              <label>正确答案 *</label>
              <textarea 
                v-model="questionForm.correctAnswer" 
                placeholder="请输入正确答案"
                class="form-textarea"
                rows="3"
                required
              ></textarea>
            </div>
            
            <!-- 题目标签 -->
            <div class="form-group">
              <label>题目标签</label>
              <input 
                v-model="tagInput" 
                type="text" 
                placeholder="输入标签后按回车添加"
                class="form-input"
                @keyup.enter="addTag"
              >
              <div v-if="questionForm.tags.length > 0" class="tags-display">
                <span 
                  v-for="(tag, index) in questionForm.tags" 
                  :key="index" 
                  class="tag removable"
                >
                  {{ tag }}
                  <button @click="removeTag(index)" type="button" class="remove-tag-btn">×</button>
                </span>
              </div>
            </div>
            
            <!-- 表单按钮 -->
            <div class="form-actions">
              <button type="button" @click="closeModal" class="btn btn-secondary">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="saving">
                <span v-if="saving">保存中...</span>
                <span v-else>{{ showAddModal ? '添加题目' : '保存修改' }}</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
    </Teleport>
  </Layout>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import Layout from '../components/Layout.vue'
import { questionApi } from '../services/questionApi.js'

export default {
  name: 'QuestionManagement',
  components: {
    Layout
  },
  setup() {
    // 响应式数据
    const questions = ref([])
    const loading = ref(false)
    const error = ref('')
    const searchQuery = ref('')
    const selectedDifficulty = ref('')
    const selectedType = ref('')
    const showAddModal = ref(false)
    const showEditModal = ref(false)
    const saving = ref(false)
    const tagInput = ref('')
    
    // 分页相关
    const currentPage = ref(1)
    const pageSize = ref(50)  // 增加每页显示数量，让用户能看到更多题目
    const totalQuestions = ref(0)
    const totalPages = ref(0)
    
    // 题目表单数据
    const questionForm = ref({
      id: null,
      title: '',
      content: '',
      type: '',
      difficulty: '',
      options: ['', ''],
      correctAnswers: [],
      correctAnswer: '',
      tags: []
    })
    
    // 计算属性
    
    // 直接使用后端返回的分页数据，不进行前端二次分页
    const paginatedQuestions = computed(() => {
      return questions.value
    })
    
    const visiblePages = computed(() => {
      const pages = []
      const total = totalPages.value
      const current = currentPage.value
      
      if (total <= 7) {
        for (let i = 1; i <= total; i++) {
          pages.push(i)
        }
      } else {
        if (current <= 4) {
          for (let i = 1; i <= 5; i++) {
            pages.push(i)
          }
          pages.push('...', total)
        } else if (current >= total - 3) {
          pages.push(1, '...')
          for (let i = total - 4; i <= total; i++) {
            pages.push(i)
          }
        } else {
          pages.push(1, '...')
          for (let i = current - 1; i <= current + 1; i++) {
            pages.push(i)
          }
          pages.push('...', total)
        }
      }
      
      return pages
    })
    
    // 方法
    const loadQuestions = async () => {
      console.log('=== 开始加载题目数据 ===')
      console.log('当前页码:', currentPage.value)
      console.log('每页大小:', pageSize.value)
      console.log('搜索关键词:', searchQuery.value)
      console.log('选择类型:', selectedType.value)
      console.log('选择难度:', selectedDifficulty.value)
      
      loading.value = true
      error.value = ''
      try {
        // 构建查询参数
        const params = {
          page: currentPage.value - 1, // 后端页码从0开始
          size: pageSize.value
        }
        
        // 添加搜索关键词
        if (searchQuery.value.trim()) {
          params.keyword = searchQuery.value.trim()
          console.log('添加搜索关键词:', params.keyword)
        }
        
        // 添加筛选条件
        if (selectedType.value) {
          // 将前端类型映射到后端枚举（小写）
          const typeMapping = {
            'single': 'single',
            'multiple': 'multiple', 
            'judge': 'judge',
            'fill': 'fill',
            'essay': 'essay'
          }
          params.type = typeMapping[selectedType.value]
          console.log('添加类型筛选:', params.type)
        }
        
        if (selectedDifficulty.value) {
          // 将前端难度映射到后端枚举（小写）
          const difficultyMapping = {
            'easy': 'easy',
            'medium': 'medium',
            'hard': 'hard'
          }
          params.difficulty = difficultyMapping[selectedDifficulty.value]
          console.log('添加难度筛选:', params.difficulty)
        }
        
        console.log('最终请求参数:', JSON.stringify(params, null, 2))
        console.log('准备调用API: questionApi.getQuestions')
        
        // 调用API获取题目数据
        const startTime = Date.now()
        const response = await questionApi.getQuestions(params)
        const endTime = Date.now()
        
        console.log('API调用耗时:', (endTime - startTime) + 'ms')
        console.log('API响应状态:', response?.status || 'undefined')
        console.log('API响应代码:', response?.code || 'undefined')
        console.log('API响应消息:', response?.message || 'undefined')
        console.log('API完整响应:', JSON.stringify(response, null, 2))
        
        if (response.code === 200) {
          console.log('API调用成功，开始处理数据')
          const pageData = response.data
          console.log('分页数据:', {
            totalElements: pageData?.total,
            totalPages: pageData?.pages,
            size: pageData?.pageSize,
            number: pageData?.pageNum,
            contentLength: pageData?.list?.length
          })
          
          if (!pageData || !pageData.list) {
            console.error('响应数据格式错误: pageData或content为空')
            questions.value = []
            error.value = '服务器返回数据格式错误'
            return
          }
          
          console.log('原始题目数据:', pageData.list)
          
          // 转换后端数据格式为前端格式
          questions.value = pageData.list.map((item, index) => {
            console.log(`处理第${index + 1}个题目:`, item)
            // 类型映射：后端枚举 -> 前端类型（小写）
            const typeMapping = {
              'single': 'single',
              'multiple': 'multiple',
              'judge': 'judge',
              'fill': 'fill',
              'essay': 'essay'
            }
            
            // 解析JSON字符串字段
            let options = []
            let correctAnswers = []
            let tags = []
            
            // 处理选项数据 - 支持JSON格式和逗号分隔字符串格式
            if (item.options) {
              if (typeof item.options === 'string') {
                try {
                  // 尝试解析JSON格式
                  options = JSON.parse(item.options)
                } catch (e) {
                  // 如果不是JSON，按逗号分隔处理
                  options = item.options.split(',').map(opt => opt.trim()).filter(opt => opt)
                }
              } else if (Array.isArray(item.options)) {
                options = item.options
              }
            }
            
            // 处理正确答案数据
            if (item.correctAnswers) {
              if (typeof item.correctAnswers === 'string') {
                try {
                  correctAnswers = JSON.parse(item.correctAnswers)
                } catch (e) {
                  correctAnswers = item.correctAnswers.split(',').map(ans => ans.trim()).filter(ans => ans)
                }
              } else if (Array.isArray(item.correctAnswers)) {
                correctAnswers = item.correctAnswers
              }
            }
            
            // 处理标签数据
            if (item.tags) {
              if (typeof item.tags === 'string') {
                try {
                  tags = JSON.parse(item.tags)
                } catch (e) {
                  tags = item.tags.split(',').map(tag => tag.trim()).filter(tag => tag)
                }
              } else if (Array.isArray(item.tags)) {
                tags = item.tags
              }
            }
            
            return {
              id: item.id,
              title: item.title,
              content: item.content,
              type: typeMapping[item.type] || item.type.toLowerCase(),
              difficulty: item.difficulty.toLowerCase(),
              options: options,
              correctAnswers: correctAnswers,
              correctAnswer: item.correctAnswer,
              tags: tags,
              createTime: item.createdTime,
              updateTime: item.updatedTime
            }
          })
          
          console.log('数据转换完成，转换后的题目数据:', questions.value)
          
          // 更新分页信息
          totalQuestions.value = pageData.total
          totalPages.value = pageData.pages
          
          console.log('分页信息更新:')
          console.log('- 总题目数:', totalQuestions.value)
          console.log('- 总页数:', totalPages.value)
          console.log('- 当前页题目数:', questions.value.length)
          console.log('=== 题目加载成功 ===')
        } else {
          console.error('API返回错误:')
          console.error('- 错误代码:', response.code)
          console.error('- 错误消息:', response.message)
          console.error('- 完整响应:', response)
          questions.value = []
          error.value = `获取题目失败: ${response.message || '未知错误'}`
        }
      } catch (err) {
        console.error('=== 加载题目异常 ===')
        console.error('异常类型:', err.name)
        console.error('异常消息:', err.message)
        console.error('异常堆栈:', err.stack)
        console.error('完整异常对象:', err)
        
        if (err.response) {
          console.error('HTTP响应错误:')
          console.error('- 状态码:', err.response.status)
          console.error('- 状态文本:', err.response.statusText)
          console.error('- 响应头:', err.response.headers)
          console.error('- 响应数据:', err.response.data)
        } else if (err.request) {
          console.error('网络请求错误:')
          console.error('- 请求对象:', err.request)
          console.error('- 可能是网络连接问题或服务器未响应')
        } else {
          console.error('其他错误:', err.message)
        }
        
        questions.value = []
        error.value = `加载题目失败: ${err.message || '网络连接错误，请检查后端服务是否启动'}`
      } finally {
        loading.value = false
        console.log('=== 加载题目流程结束 ===')
      }
    }
    
    const handleSearch = async () => {
      currentPage.value = 1
      await loadQuestions()
    }
    
    const handleFilter = async () => {
      currentPage.value = 1
      await loadQuestions()
    }
    
    const resetFilters = async () => {
      searchQuery.value = ''
      selectedDifficulty.value = ''
      selectedType.value = ''
      currentPage.value = 1
      await loadQuestions()
    }
    
    const editQuestion = (question) => {
      questionForm.value = {
        ...question,
        options: [...(question.options || ['', ''])],
        correctAnswers: [...(question.correctAnswers || [])],
        tags: [...(question.tags || [])]
      }
      showEditModal.value = true
    }
    
    const deleteQuestion = async (id) => {
      if (confirm('确定要删除这个题目吗？')) {
        try {
          console.log('删除题目ID:', id)
          
          // 调用API删除题目
          const response = await questionApi.deleteQuestion(id)
          
          if (response.code === 200) {
            console.log('题目删除成功')
            // 重新加载题目列表
            await loadQuestions()
            alert('题目删除成功')
          } else {
            console.error('删除题目失败:', response.message)
            alert('删除失败: ' + response.message)
          }
        } catch (error) {
          console.error('删除题目失败：', error)
          alert('删除失败，请检查网络连接或稍后重试')
        }
      }
    }
    
    const closeModal = () => {
      showAddModal.value = false
      showEditModal.value = false
      resetForm()
    }
    
    const resetForm = () => {
      questionForm.value = {
        id: null,
        title: '',
        content: '',
        type: '',
        difficulty: '',
        options: ['', ''],
        correctAnswers: [],
        correctAnswer: '',
        tags: []
      }
      tagInput.value = ''
    }
    
    // 监听题目类型变化，自动设置判断题选项
    const handleTypeChange = () => {
      if (questionForm.value.type === 'judge') {
        questionForm.value.options = ['正确', '错误']
        questionForm.value.correctAnswers = []
      } else if (['single', 'multiple'].includes(questionForm.value.type)) {
        if (questionForm.value.options.length < 2) {
          questionForm.value.options = ['', '']
        }
        questionForm.value.correctAnswers = []
      } else {
        questionForm.value.correctAnswer = ''
      }
    }
    
    // 表单验证函数
    const validateForm = () => {
      const errors = []
      
      // 验证必填字段
      if (!questionForm.value.title.trim()) {
        errors.push('题目标题不能为空')
      }
      
      if (!questionForm.value.content.trim()) {
        errors.push('题目内容不能为空')
      }
      
      if (!questionForm.value.type) {
        errors.push('请选择题目类型')
      }
      
      if (!questionForm.value.difficulty) {
        errors.push('请选择难度等级')
      }
      
      // 验证选择题和判断题的选项
      if (['single', 'multiple', 'judge'].includes(questionForm.value.type)) {
        const validOptions = questionForm.value.options.filter(opt => opt.trim())
        
        if (validOptions.length < 2) {
          errors.push('选择题至少需要2个选项')
        }
        
        if (questionForm.value.correctAnswers.length === 0) {
          errors.push('请选择正确答案')
        }
        
        // 单选题只能有一个正确答案
        if (questionForm.value.type === 'single' && questionForm.value.correctAnswers.length > 1) {
          errors.push('单选题只能有一个正确答案')
        }
        
        // 判断题必须是2个选项
        if (questionForm.value.type === 'judge' && validOptions.length !== 2) {
          errors.push('判断题必须有且仅有2个选项')
        }
      }
      
      // 验证填空题和问答题的答案
      if (['fill', 'essay'].includes(questionForm.value.type)) {
        if (!questionForm.value.correctAnswer.trim()) {
          errors.push('请输入正确答案')
        }
      }
      
      return errors
    }
    
    const saveQuestion = async () => {
      // 表单验证
      const validationErrors = validateForm()
      if (validationErrors.length > 0) {
        alert('请修正以下错误：\n' + validationErrors.join('\n'))
        return
      }
      
      saving.value = true
      try {
        // 准备提交的数据
        const submitData = {
          title: questionForm.value.title.trim(),
          content: questionForm.value.content.trim(),
          difficulty: questionForm.value.difficulty.toLowerCase(),
          status: 1  // 后端期望Integer类型，1表示ACTIVE
        }
        
        // 类型映射：前端类型 -> 后端枚举（匹配后端枚举定义）
        const typeMapping = {
          'single': 'single',
          'multiple': 'multiple', 
          'judge': 'judge',
          'fill': 'fill',
          'essay': 'essay'
        }
        submitData.type = typeMapping[questionForm.value.type] || questionForm.value.type
        
        // 处理选项（选择题和判断题）
        if (['single', 'multiple', 'judge'].includes(questionForm.value.type)) {
          submitData.options = questionForm.value.options.filter(opt => opt.trim())
          
          // 验证选项数量
          if (submitData.options.length < 2) {
            throw new Error('选择题至少需要2个有效选项')
          }
          
          // 验证正确答案索引的有效性
          const validIndexes = questionForm.value.correctAnswers.filter(index => 
            index >= 0 && index < submitData.options.length
          )
          
          if (validIndexes.length === 0) {
            throw new Error('请选择有效的正确答案')
          }
          
          submitData.correctAnswers = validIndexes
        } else {
          // 填空题和问答题：使用correctAnswer字段
          submitData.correctAnswer = questionForm.value.correctAnswer.trim()
          
          if (!submitData.correctAnswer) {
            throw new Error('正确答案不能为空')
          }
        }
        
        // 处理标签
        if (questionForm.value.tags && questionForm.value.tags.length > 0) {
          submitData.tags = questionForm.value.tags
        }
        
        console.log('提交数据:', JSON.stringify(submitData, null, 2))
        
        let response
        if (showAddModal.value) {
          // 创建新题目
          console.log('调用创建题目API...')
          response = await questionApi.createQuestion(submitData)
        } else {
          // 更新现有题目
          console.log('调用更新题目API...')
          response = await questionApi.updateQuestion(questionForm.value.id, submitData)
        }
        
        console.log('API响应:', response)
        
        if (response.code === 200) {
          console.log('题目保存成功')
          // 重新加载题目列表
          await loadQuestions()
          closeModal()
          alert(showAddModal.value ? '题目创建成功' : '题目更新成功')
        } else {
          console.error('保存题目失败:', response.message)
          alert('保存失败: ' + response.message)
        }
      } catch (error) {
        console.error('保存题目失败：', error)
        alert('保存失败，请检查网络连接或稍后重试')
      } finally {
        saving.value = false
      }
    }
    
    const addOption = () => {
      if (questionForm.value.options.length < 6) {
        questionForm.value.options.push('')
      }
    }
    
    const removeOption = (index) => {
      if (questionForm.value.options.length > 2) {
        questionForm.value.options.splice(index, 1)
        // 更新正确答案索引
        questionForm.value.correctAnswers = questionForm.value.correctAnswers
          .filter(i => i !== index)
          .map(i => i > index ? i - 1 : i)
      }
    }
    
    // 导出Excel功能
    const exportQuestions = async () => {
      try {
        console.log('开始导出Excel...')
        
        // 构建查询参数
        const params = new URLSearchParams()
        
        if (searchQuery.value) {
          params.append('keyword', searchQuery.value)
        }
        
        if (selectedType.value) {
          params.append('type', selectedType.value)
        }
        
        if (selectedDifficulty.value) {
          params.append('difficulty', selectedDifficulty.value)
        }
        
        // 构建完整的导出URL
        const exportUrl = `/api/questions/export?${params.toString()}`
        console.log('导出URL:', exportUrl)
        
        // 创建一个临时的a标签来触发下载
        const link = document.createElement('a')
        link.href = exportUrl
        link.download = 'questions.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        
        console.log('Excel导出完成')
        alert('Excel文件导出成功！')
      } catch (error) {
        console.error('导出Excel失败：', error)
        alert('导出失败，请检查网络连接或稍后重试')
      }
    }
    
    // 导入相关状态
    const importing = ref(false)
    
    // 下载Excel模板
    const downloadTemplate = async () => {
      try {
        console.log('开始下载Excel模板...')
        
        const templateUrl = '/api/questions/template'
        
        // 创建一个临时的a标签来触发下载
        const link = document.createElement('a')
        link.href = templateUrl
        link.download = 'question_import_template.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        
        console.log('Excel模板下载完成')
      } catch (error) {
        console.error('下载模板失败:', error)
        alert('下载模板失败，请稍后重试')
      }
    }
    
    // 处理文件上传
    const handleFileUpload = async (event) => {
      const file = event.target.files[0]
      if (!file) return
      
      // 验证文件类型
      if (!file.name.endsWith('.xlsx') && !file.name.endsWith('.xls')) {
        alert('请选择Excel文件(.xlsx或.xls格式)')
        return
      }
      
      importing.value = true
      
      try {
        console.log('开始导入Excel文件:', file.name)
        
        const formData = new FormData()
        formData.append('file', file)
        
        const response = await fetch('/api/questions/import', {
          method: 'POST',
          body: formData
        })
        
        const result = await response.json()
        
        if (result.code === 200) {
          const data = result.data
          const message = `导入完成！\n成功导入: ${data.successCount} 条\n失败: ${data.errorCount} 条\n总计: ${data.totalCount} 条`
          
          if (data.errors && data.errors.length > 0) {
            const errorMessage = data.errors.slice(0, 5).join('\n')
            alert(message + '\n\n错误详情:\n' + errorMessage + (data.errors.length > 5 ? '\n...' : ''))
          } else {
            alert(message)
          }
          
          // 刷新题目列表
          await loadQuestions()
        } else {
          alert('导入失败: ' + result.message)
        }
      } catch (error) {
        console.error('导入Excel失败:', error)
        alert('导入失败，请检查文件格式或稍后重试')
      } finally {
        importing.value = false
        // 清空文件输入
        event.target.value = ''
      }
    }
    
    const toggleCorrectAnswer = (index) => {
      const correctAnswers = questionForm.value.correctAnswers
      const existingIndex = correctAnswers.indexOf(index)
      
      if (existingIndex > -1) {
        correctAnswers.splice(existingIndex, 1)
      } else {
        if (questionForm.value.type === 'single') {
          // 单选题只能有一个正确答案
          questionForm.value.correctAnswers = [index]
        } else {
          // 多选题可以有多个正确答案
          correctAnswers.push(index)
        }
      }
    }
    
    const addTag = () => {
      const tag = tagInput.value.trim()
      if (tag && !questionForm.value.tags.includes(tag)) {
        questionForm.value.tags.push(tag)
        tagInput.value = ''
      }
    }
    
    const removeTag = (index) => {
      questionForm.value.tags.splice(index, 1)
    }
    
    const isCorrectOption = (question, optionIndex) => {
      console.log('检查正确答案:', {
        questionId: question.id,
        optionIndex,
        type: question.type,
        correctAnswers: question.correctAnswers,
        correctAnswer: question.correctAnswer
      })
      
      // 对于选择题（单选、多选），检查correctAnswers数组
      if (['single', 'multiple'].includes(question.type)) {
        if (!question.correctAnswers || !Array.isArray(question.correctAnswers) || question.correctAnswers.length === 0) {
          return false
        }
        
        return question.correctAnswers.some(answer => {
          if (typeof answer === 'string') {
            // 处理字符串格式的答案，如 "B", "A"
            if (answer.length === 1 && answer >= 'A' && answer <= 'Z') {
              const letterIndex = answer.charCodeAt(0) - 65
              return letterIndex === optionIndex
            }
            // 如果是数字字符串，转换为数字
            const numAnswer = parseInt(answer)
            if (!isNaN(numAnswer)) {
              return numAnswer === optionIndex
            }
          } else if (typeof answer === 'number') {
            // 直接比较数字索引
            return answer === optionIndex
          }
          return false
        })
      }
      
      // 对于判断题，检查correctAnswer字段或correctAnswers数组
      if (question.type === 'judge') {
        // 首先检查correctAnswer字段（修复后的数据）
        if (question.correctAnswer) {
          const correctAnswerText = question.correctAnswer.trim()
          const optionText = question.options && question.options[optionIndex] ? question.options[optionIndex].trim() : ''
          return correctAnswerText === optionText
        }
        
        // 兼容旧数据：检查correctAnswers数组
        if (question.correctAnswers && Array.isArray(question.correctAnswers) && question.correctAnswers.length > 0) {
          return question.correctAnswers.some(answer => {
            if (typeof answer === 'string') {
              if (answer.length === 1 && answer >= 'A' && answer <= 'Z') {
                const letterIndex = answer.charCodeAt(0) - 65
                return letterIndex === optionIndex
              }
              // 直接比较答案文本
              const optionText = question.options && question.options[optionIndex] ? question.options[optionIndex].trim() : ''
              return answer.trim() === optionText
            } else if (typeof answer === 'number') {
              return answer === optionIndex
            }
            return false
          })
        }
      }
      
      return false
    }

    const getDifficultyText = (difficulty) => {
      const map = {
        easy: '简单',
        medium: '中等',
        hard: '困难'
      }
      return map[difficulty] || difficulty
    }
    
    const getTypeText = (type) => {
      const map = {
        single: '单选题',
        multiple: '多选题',
        judge: '判断题',
        fill: '填空题',
        essay: '问答题'
      }
      return map[type] || type
    }
    
    const formatDate = (dateString) => {
      return dateString || '未知'
    }
    
    // 监听分页变化
    watch(currentPage, async () => {
      await loadQuestions()
    })
    
    // 监听搜索和筛选条件变化
    watch([searchQuery, selectedDifficulty, selectedType], async () => {
      currentPage.value = 1 // 重置到第一页
      await loadQuestions()
    })
    
    // 生命周期
    onMounted(() => {
      loadQuestions()
    })
    
    return {
      questions,
      loading,
      error,
      searchQuery,
      selectedDifficulty,
      selectedType,
      currentPage,
      pageSize,
      showAddModal,
      showEditModal,
      saving,
      tagInput,
      questionForm,
      totalQuestions,
      totalPages,
      paginatedQuestions,
      visiblePages,
      importing,
      loadQuestions,
      handleSearch,
      handleFilter,
      resetFilters,
      editQuestion,
      deleteQuestion,
      closeModal,
      resetForm,
      handleTypeChange,
      saveQuestion,
      addOption,
      removeOption,
      exportQuestions,
      downloadTemplate,
      handleFileUpload,
      toggleCorrectAnswer,
      addTag,
      removeTag,
      isCorrectOption,
      getDifficultyText,
      getTypeText,
      formatDate
    }
  }
}
</script>

<style scoped>
/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e2e8f0;
}

.header-left .page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 0.5rem 0;
}

.header-left .page-subtitle {
  font-size: 1.1rem;
  color: #64748b;
  margin: 0;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.btn-secondary {
  background: #e2e8f0;
  color: #475569;
}

.btn-secondary:hover {
  background: #cbd5e1;
}

.btn-outline {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-outline:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

.upload-container {
  position: relative;
  display: inline-block;
}

/* 搜索和筛选区域 */
.search-filter-section {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  margin-bottom: 1.5rem;
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 300px;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 3rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.2rem;
  color: #64748b;
}

.filter-controls {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.filter-select {
  padding: 0.75rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  background: white;
  cursor: pointer;
}

.filter-select:focus {
  outline: none;
  border-color: #667eea;
}

/* 统计信息 */
.stats-bar {
  display: flex;
  gap: 2rem;
  margin-bottom: 1.5rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.stat-label {
  color: #64748b;
  font-size: 0.9rem;
}

.stat-value {
  color: #1a202c;
  font-weight: 600;
  font-size: 1.1rem;
}

.page-size-select {
  padding: 0.25rem 0.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  background: white;
  font-size: 0.9rem;
  color: #374151;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.page-size-select:hover {
  border-color: #667eea;
}

.page-size-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* 加载和空状态 */
.loading-state, .empty-state, .error-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.error-state {
  border: 2px solid #fee2e2;
}

.error-state .error-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.error-state h3 {
  color: #dc2626;
  margin-bottom: 0.5rem;
}

.error-state p {
  color: #64748b;
  margin-bottom: 1.5rem;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  color: #1a202c;
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: #64748b;
}

/* 题目卡片 */
.question-cards {
  display: grid;
  gap: 1.5rem;
}

.question-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: all 0.3s ease;
}

.question-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.question-meta {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.question-id {
  font-weight: 600;
  color: #64748b;
  font-size: 0.9rem;
}

.difficulty-badge, .type-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
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

.type-badge {
  background: #e0e7ff;
  color: #3730a3;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  padding: 0.5rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
}

.action-btn.edit {
  background: #e0f2fe;
  color: #0369a1;
}

.action-btn.edit:hover {
  background: #bae6fd;
}

.action-btn.delete {
  background: #fee2e2;
  color: #dc2626;
}

.action-btn.delete:hover {
  background: #fecaca;
}

.card-content {
  padding: 1.5rem;
}

.question-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 1rem 0;
}

.question-content {
  color: #374151;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.question-options {
  margin: 1rem 0;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  margin-bottom: 0.5rem;
  background: #f8fafc;
  border-radius: 8px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.option-item.correct {
  background: #dcfce7;
  border-color: #22c55e;
}

.option-label {
  font-weight: 600;
  color: #64748b;
  min-width: 1.5rem;
}

.option-text {
  flex: 1;
  color: #374151;
}

.correct-icon {
  color: #22c55e;
  font-weight: bold;
}

.correct-answer {
  padding: 1rem;
  background: #dcfce7;
  border-radius: 8px;
  margin: 1rem 0;
}

.card-tags {
  padding: 0 1.5rem 1rem;
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.tag {
  padding: 0.25rem 0.75rem;
  background: #e0e7ff;
  color: #3730a3;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.tag.removable {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #f3f4f6;
  color: #374151;
}

.remove-tag-btn {
  background: none;
  border: none;
  color: #ef4444;
  cursor: pointer;
  font-size: 1rem;
  padding: 0;
  width: 1rem;
  height: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-footer {
  padding: 1rem 1.5rem;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #64748b;
}

/* 分页信息 */
.pagination-info {
  text-align: center;
  margin-top: 1.5rem;
  color: #64748b;
  font-size: 0.9rem;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  margin-top: 2rem;
}

.page-btn, .page-number {
  padding: 0.5rem 1rem;
  border: 1px solid #e2e8f0;
  background: white;
  color: #374151;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled), .page-number:hover {
  background: #f8fafc;
  border-color: #667eea;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-number.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
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
  padding: 1rem;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h2 {
  margin: 0;
  color: #1a202c;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #64748b;
  padding: 0.5rem;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: #f1f5f9;
  color: #1a202c;
}

.modal-body {
  padding: 1.5rem;
}

/* 表单样式 */
.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
}

.form-input, .form-textarea, .form-select {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-input:focus, .form-textarea:focus, .form-select:focus {
  outline: none;
  border-color: #667eea;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.options-container {
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  padding: 1rem;
}

.option-input-group {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.judge-tip {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #f0f9ff;
  border: 1px solid #0ea5e9;
  border-radius: 6px;
  color: #0369a1;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.tip-icon {
  font-size: 1.1rem;
}

.option-input-group:last-child {
  margin-bottom: 0;
}

.option-input {
  flex: 1;
  margin: 0;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.checkbox-wrapper input[type="checkbox"] {
  width: auto;
  margin: 0;
}

.checkbox-label {
  font-size: 0.9rem;
  color: #374151;
}

.remove-option-btn {
  background: #fee2e2;
  color: #dc2626;
  border: none;
  border-radius: 50%;
  width: 2rem;
  height: 2rem;
  cursor: pointer;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.remove-option-btn:hover {
  background: #fecaca;
}

.add-option-btn {
  background: #e0f2fe;
  color: #0369a1;
  border: 2px dashed #0369a1;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  width: 100%;
  margin-top: 0.5rem;
}

.add-option-btn:hover {
  background: #bae6fd;
}

.tags-display {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-top: 0.5rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .search-filter-section {
    flex-direction: column;
    gap: 1rem;
  }
  
  .filter-controls {
    flex-wrap: wrap;
  }
  
  .question-cards {
    grid-template-columns: 1fr;
  }
  
  .pagination {
    flex-wrap: wrap;
  }
}
</style>