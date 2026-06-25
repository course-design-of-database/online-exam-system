<template>
  <div class="student-dashboard">
    <!-- 顶部导航栏 -->
    <header class="dashboard-header">
      <div class="header-content">
        <div class="logo-section">
          <h1 class="logo">🎓 学生系统</h1>
        </div>
        <div class="user-section">
          <div class="user-info">
            <span class="welcome-text">欢迎，{{ studentInfo.name }}</span>
            <span class="student-number">学号：{{ studentInfo.studentNumber }}</span>
          </div>
          <button @click="logout" class="logout-btn">
            <svg viewBox="0 0 24 24" class="logout-icon">
              <path d="M16,17V14H9V10H16V7L21,12L16,17M14,2A2,2 0 0,1 16,4V6H14V4H5V20H14V18H16V20A2,2 0 0,1 14,22H5A2,2 0 0,1 3,20V4A2,2 0 0,1 5,2H14Z"/>
            </svg>
            退出登录
          </button>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="dashboard-main">
      <div class="dashboard-container">
        <!-- 欢迎卡片 -->
        <div class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-text">
              <h2>欢迎回来，{{ studentInfo.name }}！</h2>
              <p>今天是 {{ currentDate }}，祝您学习愉快！</p>
            </div>
            <div class="welcome-graphic">
              <svg viewBox="0 0 200 200" class="welcome-svg">
                <defs>
                  <linearGradient id="welcomeGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#4facfe;stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#00f2fe;stop-opacity:1" />
                  </linearGradient>
                </defs>
                <circle cx="100" cy="100" r="80" fill="url(#welcomeGradient)" opacity="0.8">
                  <animate attributeName="r" values="80;85;80" dur="3s" repeatCount="indefinite"/>
                </circle>
                <text x="100" y="110" text-anchor="middle" fill="white" font-size="40">📚</text>
              </svg>
            </div>
          </div>
        </div>

        <!-- 功能卡片网格 -->
        <div class="feature-grid">
          <!-- 个人信息卡片 -->
          <div class="feature-card">
            <div class="card-header">
              <div class="card-icon personal-info">
                <svg viewBox="0 0 24 24">
                  <path d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z"/>
                </svg>
              </div>
              <h3>个人信息</h3>
            </div>
            <div class="card-content">
              <div class="info-item">
                <span class="info-label">姓名：</span>
                <span class="info-value">{{ studentInfo.name }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">学号：</span>
                <span class="info-value">{{ studentInfo.studentNumber }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">班级：</span>
                <span class="info-value">{{ studentInfo.className || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">专业：</span>
                <span class="info-value">{{ studentInfo.major || '未设置' }}</span>
              </div>
            </div>
          </div>

          <!-- 考试记录卡片 -->
          <div class="feature-card">
            <div class="card-header">
              <div class="card-icon exam-record">
                <svg viewBox="0 0 24 24">
                  <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z"/>
                </svg>
              </div>
              <h3>考试记录</h3>
            </div>
            <div class="card-content">
              <div class="stats-grid">
                <div class="stat-item">
                  <div class="stat-number">{{ examStats.total }}</div>
                  <div class="stat-label">总考试次数</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ examStats.passed }}</div>
                  <div class="stat-label">通过次数</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ examStats.average }}%</div>
                  <div class="stat-label">平均分数</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 最近考试卡片 -->
          <div class="feature-card recent-exams">
            <div class="card-header">
              <div class="card-icon recent">
                <svg viewBox="0 0 24 24">
                  <path d="M12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22C6.47,22 2,17.5 2,12A10,10 0 0,1 12,2M12.5,7V12.25L17,14.92L16.25,16.15L11,13V7H12.5Z"/>
                </svg>
              </div>
              <h3>最近考试</h3>
            </div>
            <div class="card-content">
              <div v-if="recentExams.length > 0" class="exam-list">
                <div v-for="exam in recentExams" :key="exam.id" class="exam-item">
                  <div class="exam-info">
                    <div class="exam-name">{{ exam.name }}</div>
                    <div class="exam-date">{{ formatDate(exam.date) }}</div>
                  </div>
                  <div class="exam-score" :class="getScoreClass(exam.score)">
                    {{ exam.score }}分
                  </div>
                </div>
              </div>
              <div v-else class="no-data">
                <svg viewBox="0 0 24 24" class="no-data-icon">
                  <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4M11,16.5L6.5,12L7.91,10.59L11,13.67L16.59,8.09L18,9.5L11,16.5Z"/>
                </svg>
                <p>暂无考试记录</p>
              </div>
            </div>
          </div>

          <!-- 快速操作卡片 -->
          <div class="feature-card">
            <div class="card-header">
              <div class="card-icon quick-actions">
                <svg viewBox="0 0 24 24">
                  <path d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z"/>
                </svg>
              </div>
              <h3>快速操作</h3>
            </div>
            <div class="card-content">
              <div class="action-buttons">
                <button @click="showExamModal" class="action-btn start-exam">
                  <svg viewBox="0 0 24 24">
                    <path d="M8.5,13.5L11,16.5L16.5,9.5L15.09,8.09L11,13.17L9.91,12.09L8.5,13.5M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4Z"/>
                  </svg>
                  开始考试
                </button>
                <button @click="viewResults" class="action-btn view-results">
                  <svg viewBox="0 0 24 24">
                    <path d="M9,5V9H21V7.5H10.5V5H9M9,19H21V17.5H9V19M3,17H7V15H3V17M3,13H7V11H3V13M3,9H7V7H3V9Z"/>
                  </svg>
                  查看成绩
                </button>
                <button @click="viewWrongQuestions" class="action-btn wrong-questions">
                  <svg viewBox="0 0 24 24">
                    <path d="M5,3H19A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5A2,2 0 0,1 3,19V5A2,2 0 0,1 5,3M7,7H17V9H7V7M7,11H17V13H7V11M7,15H13V17H7V15Z"/>
                  </svg>
                  错题本
                </button>
                <button class="action-btn profile-settings">
                  <svg viewBox="0 0 24 24">
                    <path d="M12,15.5A3.5,3.5 0 0,1 8.5,12A3.5,3.5 0 0,1 12,8.5A3.5,3.5 0 0,1 15.5,12A3.5,3.5 0 0,1 12,15.5M19.43,12.97C19.47,12.65 19.5,12.33 19.5,12C19.5,11.67 19.47,11.34 19.43,11L21.54,9.37C21.73,9.22 21.78,8.95 21.66,8.73L19.66,5.27C19.54,5.05 19.27,4.96 19.05,5.05L16.56,6.05C16.04,5.66 15.5,5.32 14.87,5.07L14.5,2.42C14.46,2.18 14.25,2 14,2H10C9.75,2 9.54,2.18 9.5,2.42L9.13,5.07C8.5,5.32 7.96,5.66 7.44,6.05L4.95,5.05C4.73,4.96 4.46,5.05 4.34,5.27L2.34,8.73C2.22,8.95 2.27,9.22 2.46,9.37L4.57,11C4.53,11.34 4.5,11.67 4.5,12C4.5,12.33 4.53,12.65 4.57,12.97L2.46,14.63C2.27,14.78 2.22,15.05 2.34,15.27L4.34,18.73C4.46,18.95 4.73,19.03 4.95,18.95L7.44,17.94C7.96,18.34 8.5,18.68 9.13,18.93L9.5,21.58C9.54,21.82 9.75,22 10,22H14C14.25,22 14.46,21.82 14.5,21.58L14.87,18.93C15.5,18.68 16.04,18.34 16.56,17.94L19.05,18.95C19.27,19.03 19.54,18.95 19.66,18.73L21.66,15.27C21.78,15.05 21.73,14.78 21.54,14.63L19.43,12.97Z"/>
                  </svg>
                  个人设置
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 考试弹窗 -->
    <div v-if="showExamList" class="exam-modal-overlay" @click="closeExamModal">
      <div class="exam-modal" @click.stop>
        <div class="modal-header">
          <h3>可参加的考试</h3>
          <button @click="closeExamModal" class="close-btn">
            <svg viewBox="0 0 24 24">
              <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
            </svg>
          </button>
        </div>
        <div class="modal-content">
          <!-- 未开始的考试 -->
          <div v-if="pendingExams.length > 0" class="exam-section">
            <h4 class="section-title">
              <svg viewBox="0 0 24 24" class="section-icon">
                <path d="M12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22C6.47,22 2,17.5 2,12A10,10 0 0,1 12,2M12.5,7V12.25L17,14.92L16.25,16.15L11,13V7H12.5Z"/>
              </svg>
              即将开始的考试
            </h4>
            <div class="exam-list">
              <div v-for="exam in pendingExams" :key="exam.id" class="exam-item pending">
                <div class="exam-info">
                  <div class="exam-title">{{ exam.title }}</div>
                  <div class="exam-details">
                    <span class="exam-time">
                      <svg viewBox="0 0 24 24" class="detail-icon">
                        <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4M12.5,7V12.25L17,14.92L16.25,16.15L11,13V7H12.5Z"/>
                      </svg>
                      {{ formatExamTime(exam.startTime) }} - {{ formatExamTime(exam.endTime) }}
                    </span>
                    <span class="exam-duration">
                      <svg viewBox="0 0 24 24" class="detail-icon">
                        <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4M12.5,7V12.25L17,14.92L16.25,16.15L11,13V7H12.5Z"/>
                      </svg>
                      {{ exam.duration }}分钟
                    </span>
                  </div>
                  <div class="exam-description">{{ exam.description }}</div>
                </div>
                <div class="exam-actions">
                  <div class="countdown">{{ getCountdown(exam.startTime) }}</div>
                  <button class="exam-btn waiting" disabled>
                    等待开始
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 正在进行的考试 -->
          <div v-if="activeExams.length > 0" class="exam-section">
            <h4 class="section-title">
              <svg viewBox="0 0 24 24" class="section-icon">
                <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4M15.5,7L14.09,8.41L16.67,11H7V13H16.67L14.09,15.59L15.5,17L20.5,12L15.5,7Z"/>
              </svg>
              正在进行的考试
            </h4>
            <div class="exam-list">
              <div v-for="exam in activeExams" :key="exam.id" class="exam-item active">
                <div class="exam-info">
                  <div class="exam-title">{{ exam.title }}</div>
                  <div class="exam-details">
                    <span class="exam-time">
                      <svg viewBox="0 0 24 24" class="detail-icon">
                        <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4M12.5,7V12.25L17,14.92L16.25,16.15L11,13V7H12.5Z"/>
                      </svg>
                      {{ formatExamTime(exam.startTime) }} - {{ formatExamTime(exam.endTime) }}
                    </span>
                    <span class="exam-duration">
                      <svg viewBox="0 0 24 24" class="detail-icon">
                        <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4M12.5,7V12.25L17,14.92L16.25,16.15L11,13V7H12.5Z"/>
                      </svg>
                      {{ exam.duration }}分钟
                    </span>
                  </div>
                  <div class="exam-description">{{ exam.description }}</div>
                </div>
                <div class="exam-actions">
                  <div class="countdown active">{{ getCountdown(exam.endTime) }}</div>
                  <button @click="startExam(exam)" class="exam-btn start">
                    开始考试
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 无可用考试 -->
          <div v-if="pendingExams.length === 0 && activeExams.length === 0" class="no-exams">
            <svg viewBox="0 0 24 24" class="no-exam-icon">
              <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4M11,16.5L6.5,12L7.91,10.59L11,13.67L16.59,8.09L18,9.5L11,16.5Z"/>
            </svg>
            <h4>暂无可参加的考试</h4>
            <p>目前没有未开始或正在进行的考试</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'StudentDashboard',
  data() {
    return {
      studentInfo: {
        name: '',
        studentNumber: '',
        className: '',
        major: ''
      },
      currentDate: '',
      examStats: {
        total: 0,
        passed: 0,
        average: 0
      },
      recentExams: [], // 从API获取的最近考试记录
      // 考试弹窗相关数据
      showExamList: false,
      availableExams: [] // 从API获取的考试数据
    }
  },
  computed: {
    // 未开始的考试
    pendingExams() {
      const now = new Date()
      return this.availableExams.filter(exam => {
        const startTime = new Date(exam.startTime)
        return startTime > now
      })
    },
    // 正在进行的考试
    activeExams() {
      const now = new Date()
      return this.availableExams.filter(exam => {
        const startTime = new Date(exam.startTime)
        const endTime = new Date(exam.endTime)
        return startTime <= now && now <= endTime
      })
    }
  },
  async mounted() {
    this.loadStudentInfo()
    this.updateCurrentDate()
    await this.loadRecentExams()
    this.loadExamStats()
    await this.loadAvailableExams()
  },
  methods: {
    loadStudentInfo() {
      const studentInfo = localStorage.getItem('studentInfo')
      if (studentInfo) {
        this.studentInfo = JSON.parse(studentInfo)
        console.log('学生信息加载成功:', this.studentInfo)
      }
    },
    updateCurrentDate() {
      const now = new Date()
      const options = { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric',
        weekday: 'long'
      }
      this.currentDate = now.toLocaleDateString('zh-CN', options)
    },
    async loadRecentExams() {
      try {
        // 从localStorage获取学生信息
        const studentInfo = JSON.parse(localStorage.getItem('studentInfo') || '{}')
        if (!studentInfo.id) {
          console.error('未找到学生ID')
          alert('请重新登录')
          return
        }

        // 调用API获取学生最近的考试记录
        const response = await axios.get('/api/v1/student/exam-records/recent', {
          params: {
            studentId: studentInfo.id,
            limit: 5
          }
        })
        
        if (response.data && response.data.code === 200) {
          // 转换数据格式以适配前端显示
           this.recentExams = (response.data.data || []).map(record => ({
             id: record.id,
             name: record.exam ? record.exam.title : '未知考试',
             date: record.submitTime ? new Date(record.submitTime).toISOString().split('T')[0] : '未完成',
             score: record.score ? Math.round(record.score) : 0
           }))
          console.log('成功加载最近考试记录:', this.recentExams)
        } else {
          console.error('API返回错误:', response.data)
          alert(response.data.message || '获取考试记录失败')
        }
      } catch (error) {
        console.error('加载考试记录失败:', error)
        if (error.response) {
          alert(error.response.data.message || '获取考试记录失败')
        } else {
          alert('网络错误，请检查连接')
        }
      }
    },
    loadExamStats() {
      // 基于真实的考试记录计算统计数据
      this.examStats = {
        total: this.recentExams.length,
        passed: this.recentExams.filter(exam => exam.score >= 60).length,
        average: Math.round(this.recentExams.reduce((sum, exam) => sum + exam.score, 0) / this.recentExams.length) || 0
      }
    },
    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        month: 'short',
        day: 'numeric'
      })
    },
    getScoreClass(score) {
      if (score >= 90) return 'excellent'
      if (score >= 80) return 'good'
      if (score >= 60) return 'pass'
      return 'fail'
    },
    logout() {
      console.log('学生退出登录')
      // 清除学生登录信息
      localStorage.removeItem('studentToken')
      localStorage.removeItem('studentInfo')
      localStorage.removeItem('userType')
      
      // 跳转到学生登录页
      this.$router.push('/student/login')
    },
    // 考试弹窗相关方法
    showExamModal() {
      this.showExamList = true
      this.loadAvailableExams()
    },
    closeExamModal() {
      this.showExamList = false
    },
    async loadAvailableExams() {
      try {
        // 从localStorage获取学生信息
        const studentInfo = JSON.parse(localStorage.getItem('studentInfo') || '{}')
        if (!studentInfo.id) {
          console.error('未找到学生ID')
          this.$message.error('请重新登录')
          return
        }

        // 调用API获取学生可参加的考试列表
         const response = await axios.get('/api/v1/student/exams/available', {
           params: {
             studentId: studentInfo.id
           }
         })
        
        if (response.data && response.data.code === 200) {
          this.availableExams = response.data.data || []
          console.log('成功加载可用考试列表:', this.availableExams)
        } else {
          console.error('API返回错误:', response.data)
          this.$message.error(response.data.message || '获取考试列表失败')
        }
      } catch (error) {
        console.error('加载考试列表失败:', error)
        if (error.response) {
          this.$message.error(error.response.data.message || '获取考试列表失败')
        } else {
          this.$message.error('网络错误，请检查连接')
        }
      }
    },
    formatExamTime(timeString) {
      const date = new Date(timeString)
      return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    getCountdown(timeString) {
      const targetTime = new Date(timeString)
      const now = new Date()
      const diff = targetTime - now
      
      if (diff <= 0) {
        return '已结束'
      }
      
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      
      if (days > 0) {
        return `${days}天${hours}小时`
      } else if (hours > 0) {
        return `${hours}小时${minutes}分钟`
      } else {
        return `${minutes}分钟`
      }
    },
    startExam(exam) {
      console.log('开始考试:', exam)
      // 跳转到考试页面
      this.$router.push(`/student/exam/${exam.id}`)
      this.closeExamModal()
    },
    viewResults() {
      console.log('查看成绩')
      // 跳转到成绩查看页面
      this.$router.push('/student/results')
    },
    viewWrongQuestions() {
      console.log('查看错题本')
      this.$router.push('/student/wrong-questions')
    }
  }
}
</script>

<style scoped>
/* 主容器 */
.student-dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 顶部导航栏 */
.dashboard-header {
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}

.logo {
  font-size: 1.5rem;
  font-weight: 700;
  color: #4facfe;
  margin: 0;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.welcome-text {
  font-weight: 600;
  color: #333;
  font-size: 1rem;
}

.student-number {
  font-size: 0.875rem;
  color: #666;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(238, 90, 82, 0.3);
}

.logout-icon {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

/* 主要内容区域 */
.dashboard-main {
  padding: 30px 20px;
}

.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border-radius: 20px;
  padding: 40px;
  margin-bottom: 30px;
  color: white;
  box-shadow: 0 10px 30px rgba(79, 172, 254, 0.3);
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 10px 0;
}

.welcome-text p {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
}

.welcome-graphic {
  width: 120px;
  height: 120px;
}

.welcome-svg {
  width: 100%;
  height: 100%;
}

/* 功能卡片网格 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 25px;
}

.feature-card {
  background: white;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.card-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-icon svg {
  width: 24px;
  height: 24px;
  fill: white;
}

.personal-info {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.exam-record {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.recent {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.quick-actions {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.card-header h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

/* 个人信息卡片内容 */
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 500;
  color: #666;
}

.info-value {
  font-weight: 600;
  color: #333;
}

/* 统计数据网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 12px;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: 700;
  color: #4facfe;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 0.875rem;
  color: #666;
}

/* 最近考试列表 */
.recent-exams {
  grid-column: span 2;
}

.exam-list {
  max-height: 200px;
  overflow-y: auto;
}

.exam-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.exam-item:last-child {
  border-bottom: none;
}

.exam-info {
  flex: 1;
}

.exam-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.exam-date {
  font-size: 0.875rem;
  color: #666;
}

.exam-score {
  font-weight: 700;
  font-size: 1.1rem;
  padding: 6px 12px;
  border-radius: 20px;
}

.exam-score.excellent {
  background: #d4edda;
  color: #155724;
}

.exam-score.good {
  background: #cce7ff;
  color: #004085;
}

.exam-score.pass {
  background: #fff3cd;
  color: #856404;
}

.exam-score.fail {
  background: #f8d7da;
  color: #721c24;
}

/* 无数据状态 */
.no-data {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.no-data-icon {
  width: 48px;
  height: 48px;
  fill: #ddd;
  margin-bottom: 15px;
}

/* 快速操作按钮 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 20px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  text-align: left;
}

.action-btn svg {
  width: 20px;
  height: 20px;
  fill: currentColor;
}

.start-exam {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  color: white;
}

.view-results {
  background: linear-gradient(135deg, #a8edea, #fed6e3);
  color: #333;
}

.wrong-questions {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: white;
}

.profile-settings {
  background: linear-gradient(135deg, #ffecd2, #fcb69f);
  color: #333;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 15px;
  }
  
  .user-section {
    gap: 10px;
  }
  
  .user-info {
    display: none;
  }
  
  .welcome-content {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .welcome-graphic {
    width: 80px;
    height: 80px;
  }
  
  .feature-grid {
    grid-template-columns: 1fr;
  }
  
  .recent-exams {
    grid-column: span 1;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .dashboard-main {
    padding: 20px 15px;
  }
  
  .welcome-card {
    padding: 25px 20px;
  }
  
  .feature-card {
    padding: 20px 15px;
  }
}

/* 考试弹窗样式 */
.exam-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.exam-modal {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 800px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px 30px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(90deg);
}

.close-btn svg {
  width: 20px;
  height: 20px;
  fill: currentColor;
}

.modal-content {
  padding: 30px;
  max-height: 60vh;
  overflow-y: auto;
}

.exam-section {
  margin-bottom: 30px;
}

.exam-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.section-icon {
  width: 24px;
  height: 24px;
  fill: #4facfe;
}

.exam-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.exam-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-radius: 16px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.exam-item.pending {
  border-color: #ffc107;
  background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
}

.exam-item.active {
  border-color: #28a745;
  background: linear-gradient(135deg, #d4edda 0%, #a8e6cf 100%);
}

.exam-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.exam-info {
  flex: 1;
  margin-right: 20px;
}

.exam-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.exam-details {
  display: flex;
  gap: 20px;
  margin-bottom: 8px;
}

.exam-time,
.exam-duration {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
  color: #666;
}

.detail-icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

.exam-description {
  font-size: 0.9rem;
  color: #888;
  line-height: 1.4;
}

.exam-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.countdown {
  font-size: 0.9rem;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 20px;
  text-align: center;
  min-width: 80px;
}

.countdown:not(.active) {
  background: #fff3cd;
  color: #856404;
}

.countdown.active {
  background: #d4edda;
  color: #155724;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.exam-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 25px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 100px;
}

.exam-btn.waiting {
  background: #6c757d;
  color: white;
  cursor: not-allowed;
  opacity: 0.6;
}

.exam-btn.start {
  background: linear-gradient(135deg, #28a745, #20c997);
  color: white;
}

.exam-btn.start:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
}

.no-exams {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.no-exam-icon {
  width: 64px;
  height: 64px;
  fill: #ddd;
  margin-bottom: 20px;
}

.no-exams h4 {
  font-size: 1.2rem;
  margin-bottom: 10px;
  color: #333;
}

.no-exams p {
  font-size: 1rem;
  margin: 0;
}

/* 响应式设计 - 考试弹窗 */
@media (max-width: 768px) {
  .exam-modal {
    width: 95%;
    max-height: 85vh;
  }
  
  .modal-header {
    padding: 20px;
  }
  
  .modal-content {
    padding: 20px;
  }
  
  .exam-item {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .exam-info {
    margin-right: 0;
  }
  
  .exam-details {
    flex-direction: column;
    gap: 8px;
  }
  
  .exam-actions {
    flex-direction: row;
    justify-content: space-between;
  }
}

@media (max-width: 480px) {
  .exam-modal {
    width: 98%;
    margin: 10px;
  }
  
  .modal-header {
    padding: 15px;
  }
  
  .modal-content {
    padding: 15px;
  }
  
  .exam-item {
    padding: 15px;
  }
}
</style>