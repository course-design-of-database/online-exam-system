<template>
  <Layout>
    <div class="api-management">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">
          <i class="icon-api">🔌</i>
          接口管理
        </h1>
        <p class="page-description">题目答案判断API接口文档与测试工具</p>
      </div>

    <!-- 导航标签 -->
    <div class="nav-tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        :class="['tab-button', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- API文档 -->
    <div v-if="activeTab === 'docs'" class="tab-content">
      <div class="api-docs">
        <div class="doc-section">
          <h2>基础信息</h2>
          <div class="info-card">
            <div class="info-item">
              <span class="label">基础URL:</span>
              <code class="value">{{ baseUrl }}</code>
              <button @click="copyToClipboard(baseUrl)" class="copy-btn">复制</button>
            </div>
            <div class="info-item">
              <span class="label">内容类型:</span>
              <code class="value">application/json</code>
            </div>
            <div class="info-item">
              <span class="label">字符编码:</span>
              <code class="value">UTF-8</code>
            </div>
          </div>
        </div>

        <div class="doc-section" v-for="api in apiList" :key="api.path">
          <h3>{{ api.title }}</h3>
          <div class="api-card">
            <div class="api-header">
              <span :class="['method', api.method.toLowerCase()]">{{ api.method }}</span>
              <code class="path">{{ api.path }}</code>
            </div>
            <div class="api-description">{{ api.description }}</div>
            
            <div class="api-details">
              <h4>请求参数</h4>
              <pre class="code-block">{{ api.requestExample }}</pre>
              
              <h4>响应示例</h4>
              <pre class="code-block">{{ api.responseExample }}</pre>
              
              <div class="api-actions">
                <button @click="testApi(api)" class="test-btn">测试接口</button>
                <button @click="copyApiExample(api)" class="copy-btn">复制示例</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 接口测试 -->
    <div v-if="activeTab === 'test'" class="tab-content">
      <div class="api-test">
        <div class="test-form">
          <h3>接口测试工具</h3>
          
          <div class="form-group">
            <label>选择接口:</label>
            <select v-model="selectedApi" class="form-control">
              <option value="">请选择接口</option>
              <option v-for="api in apiList" :key="api.path" :value="api.path">
                {{ api.method }} {{ api.path }} - {{ api.title }}
              </option>
            </select>
          </div>

          <div v-if="selectedApi" class="form-group">
            <label>请求参数:</label>
            <textarea 
              v-model="testRequestBody" 
              class="form-control code-input"
              rows="8"
              placeholder="请输入JSON格式的请求参数"
            ></textarea>
          </div>

          <div v-if="selectedApi" class="form-actions">
            <button @click="sendTestRequest" :disabled="testing" class="test-btn">
              {{ testing ? '测试中...' : '发送请求' }}
            </button>
            <button @click="clearTestData" class="clear-btn">清空</button>
          </div>
        </div>

        <div v-if="testResponse" class="test-result">
          <h3>响应结果</h3>
          <div class="response-info">
            <span :class="['status', testResponse.success ? 'success' : 'error']">
              {{ testResponse.success ? '成功' : '失败' }}
            </span>
            <span class="time">响应时间: {{ testResponse.time }}ms</span>
          </div>
          <pre class="response-body">{{ JSON.stringify(testResponse.data, null, 2) }}</pre>
        </div>
      </div>
    </div>

    <!-- 使用示例 -->
    <div v-if="activeTab === 'examples'" class="tab-content">
      <div class="examples">
        <div class="example-section" v-for="example in codeExamples" :key="example.language">
          <h3>{{ example.title }}</h3>
          <div class="code-example">
            <div class="code-header">
              <span class="language">{{ example.language }}</span>
              <button @click="copyToClipboard(example.code)" class="copy-btn">复制代码</button>
            </div>
            <pre class="code-block">{{ example.code }}</pre>
          </div>
        </div>
      </div>
    </div>

    <!-- 复制成功提示 -->
    <div v-if="showCopyTip" class="copy-tip">
      复制成功！
    </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import Layout from '../components/Layout.vue'

// 响应式数据
const activeTab = ref('docs')
const selectedApi = ref('')
const testRequestBody = ref('')
const testResponse = ref(null)
const testing = ref(false)
const showCopyTip = ref(false)

// 基础URL
const baseUrl = computed(() => {
  return '/api/v1'
})

// 导航标签
const tabs = [
  { key: 'docs', label: 'API文档' },
  { key: 'test', label: '接口测试' },
  { key: 'examples', label: '使用示例' }
]

// API列表
const apiList = [
  {
    title: '单题答案判断',
    method: 'POST',
    path: '/check-answer',
    description: '判断单个题目的答案是否正确',
    requestExample: JSON.stringify({
      questionId: 1,
      userAnswer: 'A'
    }, null, 2),
    responseExample: JSON.stringify({
      success: true,
      message: '答案判断完成',
      data: {
        questionId: 1,
        correct: true,
        questionType: 'SINGLE_CHOICE',
        userAnswer: 'A'
      }
    }, null, 2)
  },
  {
    title: '批量答案判断',
    method: 'POST',
    path: '/check-answers',
    description: '批量判断多个题目的答案是否正确',
    requestExample: JSON.stringify({
      answers: [
        { questionId: 1, userAnswer: 'A' },
        { questionId: 2, userAnswer: 'true' }
      ]
    }, null, 2),
    responseExample: JSON.stringify({
      success: true,
      message: '批量答案判断完成',
      data: [
        {
          questionId: 1,
          correct: true,
          questionType: 'SINGLE_CHOICE',
          userAnswer: 'A'
        },
        {
          questionId: 2,
          correct: false,
          questionType: 'TRUE_FALSE',
          userAnswer: 'true',
          correctAnswer: 'false'
        }
      ]
    }, null, 2)
  },
  {
    title: '获取题目信息',
    method: 'GET',
    path: '/question/{questionId}',
    description: '获取题目信息（不包含答案）',
    requestExample: '路径参数: questionId (题目ID)',
    responseExample: JSON.stringify({
      success: true,
      message: '获取题目信息成功',
      data: {
        id: 1,
        title: 'Java基础知识',
        content: 'Java是哪种类型的编程语言？',
        type: 'SINGLE_CHOICE',
        difficulty: 'EASY',
        options: ['A. 编译型', 'B. 解释型', 'C. 混合型', 'D. 脚本型']
      }
    }, null, 2)
  }
]

// 代码示例
const codeExamples = [
  {
    language: 'JavaScript',
    title: 'JavaScript 调用示例',
    code: `// 单题答案判断
fetch('/api/v1/check-answer', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    questionId: 1,
    userAnswer: 'A'
  })
})
.then(response => response.json())
.then(data => console.log(data));

// 批量答案判断
fetch('/api/v1/check-answers', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    answers: [
      { questionId: 1, userAnswer: 'A' },
      { questionId: 2, userAnswer: 'true' }
    ]
  })
})
.then(response => response.json())
.then(data => console.log(data));`
  },
  {
    language: 'Python',
    title: 'Python 调用示例',
    code: `import requests
import json

# 单题答案判断
response = requests.post(
    '/api/v1/check-answer',
    headers={'Content-Type': 'application/json'},
    json={
        'questionId': 1,
        'userAnswer': 'A'
    }
)
print(response.json())

# 批量答案判断
response = requests.post(
    '/api/v1/check-answers',
    headers={'Content-Type': 'application/json'},
    json={
        'answers': [
            {'questionId': 1, 'userAnswer': 'A'},
            {'questionId': 2, 'userAnswer': 'true'}
        ]
    }
)
print(response.json())`
  },
  {
    language: 'cURL',
    title: 'cURL 调用示例',
    code: `# 单题答案判断
curl -X POST /api/v1/check-answer \
  -H "Content-Type: application/json" \
  -d '{
    "questionId": 1,
    "userAnswer": "A"
  }'

# 批量答案判断
curl -X POST /api/v1/check-answers \
  -H "Content-Type: application/json" \
  -d '{
    "answers": [
      {"questionId": 1, "userAnswer": "A"},
      {"questionId": 2, "userAnswer": "true"}
    ]
  }'

# 获取题目信息
curl -X GET /api/v1/question/1`
  }
]

// 方法
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    showCopyTip.value = true
    setTimeout(() => {
      showCopyTip.value = false
    }, 2000)
  } catch (err) {
    console.error('复制失败:', err)
  }
}

const copyApiExample = (api) => {
  const example = `${api.method} ${baseUrl.value}${api.path}\n\n请求示例:\n${api.requestExample}\n\n响应示例:\n${api.responseExample}`
  copyToClipboard(example)
}

const testApi = (api) => {
  activeTab.value = 'test'
  selectedApi.value = api.path
  testRequestBody.value = api.requestExample
}

const sendTestRequest = async () => {
  if (!selectedApi.value || !testRequestBody.value) {
    alert('请选择接口并输入请求参数')
    return
  }

  testing.value = true
  const startTime = Date.now()

  try {
    let requestData
    try {
      requestData = JSON.parse(testRequestBody.value)
    } catch (e) {
      throw new Error('请求参数格式错误，请输入有效的JSON')
    }

    const url = `${baseUrl.value}${selectedApi.value}`
    let response

    if (selectedApi.value.includes('/question/{questionId}')) {
      // GET请求 - 获取题目信息
      const questionId = requestData.questionId || 1
      const finalUrl = url.replace('{questionId}', questionId)
      console.log('GET请求URL:', finalUrl)
      response = await axios.get(finalUrl)
    } else {
      // POST请求 - 答案判断
      console.log('POST请求URL:', url)
      console.log('POST请求数据:', requestData)
      response = await axios.post(url, requestData, {
        headers: {
          'Content-Type': 'application/json'
        }
      })
    }

    const endTime = Date.now()
    testResponse.value = {
      success: true,
      time: endTime - startTime,
      data: response.data
    }
  } catch (error) {
    const endTime = Date.now()
    testResponse.value = {
      success: false,
      time: endTime - startTime,
      data: {
        error: error.message,
        details: error.response?.data || null
      }
    }
  } finally {
    testing.value = false
  }
}

const clearTestData = () => {
  selectedApi.value = ''
  testRequestBody.value = ''
  testResponse.value = null
}

// 生命周期
onMounted(() => {
  // 页面加载时的初始化逻辑
})
</script>

<style scoped>
.api-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 3rem;
  padding: 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.icon-api {
  font-size: 2rem;
}

.page-description {
  font-size: 1.1rem;
  margin: 0;
  opacity: 0.9;
}

/* 导航标签 */
.nav-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 2px solid #e2e8f0;
}

.tab-button {
  padding: 0.75rem 1.5rem;
  border: none;
  background: none;
  font-size: 1rem;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
}

.tab-button:hover {
  color: #667eea;
  background: #f8fafc;
}

.tab-button.active {
  color: #667eea;
  border-bottom-color: #667eea;
  background: #f8fafc;
}

/* 标签内容 */
.tab-content {
  min-height: 500px;
}

/* API文档样式 */
.doc-section {
  margin-bottom: 3rem;
}

.doc-section h2 {
  font-size: 1.8rem;
  color: #1e293b;
  margin-bottom: 1rem;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 0.5rem;
}

.doc-section h3 {
  font-size: 1.4rem;
  color: #334155;
  margin-bottom: 1rem;
}

.info-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1.5rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  font-weight: 600;
  color: #475569;
  min-width: 80px;
}

.value {
  background: #e2e8f0;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  flex: 1;
}

.api-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.api-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.method {
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.875rem;
  text-transform: uppercase;
}

.method.get {
  background: #dcfce7;
  color: #166534;
}

.method.post {
  background: #dbeafe;
  color: #1d4ed8;
}

.path {
  background: #f1f5f9;
  padding: 0.5rem;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  flex: 1;
}

.api-description {
  color: #64748b;
  margin-bottom: 1.5rem;
}

.api-details h4 {
  font-size: 1.1rem;
  color: #374151;
  margin: 1.5rem 0 0.5rem 0;
}

.code-block {
  background: #1e293b;
  color: #e2e8f0;
  padding: 1rem;
  border-radius: 6px;
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
  line-height: 1.5;
  overflow-x: auto;
  margin-bottom: 1rem;
}

.api-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

/* 接口测试样式 */
.api-test {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.test-form {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1.5rem;
}

.test-result {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
}

.form-control {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
  transition: border-color 0.3s ease;
}

.form-control:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.code-input {
  font-family: 'Courier New', monospace;
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 0.5rem;
}

.response-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  padding: 0.75rem;
  background: #f8fafc;
  border-radius: 6px;
}

.status {
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.875rem;
}

.status.success {
  background: #dcfce7;
  color: #166534;
}

.status.error {
  background: #fee2e2;
  color: #dc2626;
}

.time {
  color: #64748b;
  font-size: 0.875rem;
}

.response-body {
  background: #1e293b;
  color: #e2e8f0;
  padding: 1rem;
  border-radius: 6px;
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
  line-height: 1.5;
  overflow-x: auto;
  max-height: 400px;
  overflow-y: auto;
}

/* 使用示例样式 */
.examples {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.example-section {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.example-section h3 {
  background: #f8fafc;
  padding: 1rem 1.5rem;
  margin: 0;
  border-bottom: 1px solid #e2e8f0;
}

.code-example {
  position: relative;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: #374151;
  color: white;
}

.language {
  font-size: 0.875rem;
  font-weight: 600;
}

/* 按钮样式 */
.copy-btn, .test-btn, .clear-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.copy-btn {
  background: #f3f4f6;
  color: #374151;
}

.copy-btn:hover {
  background: #e5e7eb;
}

.test-btn {
  background: #667eea;
  color: white;
}

.test-btn:hover:not(:disabled) {
  background: #5a67d8;
}

.test-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.clear-btn {
  background: #ef4444;
  color: white;
}

.clear-btn:hover {
  background: #dc2626;
}

/* 复制提示 */
.copy-tip {
  position: fixed;
  top: 2rem;
  right: 2rem;
  background: #10b981;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .api-management {
    padding: 1rem;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .api-test {
    grid-template-columns: 1fr;
  }
  
  .nav-tabs {
    flex-wrap: wrap;
  }
  
  .tab-button {
    flex: 1;
    min-width: 120px;
  }
}
</style>