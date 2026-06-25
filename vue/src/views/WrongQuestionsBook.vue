<template>
  <div class="wrong-questions-page">
    <!-- 顶部导航栏 -->
    <header class="dashboard-header">
      <div class="header-content">
        <div class="logo-section">
          <button @click="goBack" class="back-btn">
            <svg viewBox="0 0 24 24"><path d="M20,11V13H8L13.5,18.5L12.08,19.92L4.16,12L12.08,4.08L13.5,5.5L8,11H20Z"/></svg>
          </button>
          <h1 class="logo">📝 错题本</h1>
        </div>
        <div class="user-section">
          <span class="welcome-text">{{ studentInfo.name }}</span>
          <button @click="logout" class="logout-btn">退出登录</button>
        </div>
      </div>
    </header>

    <main class="main-content">
      <div class="container">
        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-card total">
            <div class="stat-number">{{ stats.total }}</div>
            <div class="stat-label">全部错题</div>
          </div>
          <div class="stat-card not-mastered">
            <div class="stat-number">{{ stats.notMastered }}</div>
            <div class="stat-label">未掌握</div>
          </div>
          <div class="stat-card mastered">
            <div class="stat-number">{{ stats.mastered }}</div>
            <div class="stat-label">已掌握</div>
          </div>
          <div class="stat-card rate">
            <div class="stat-number">{{ stats.masteryRate }}%</div>
            <div class="stat-label">掌握率</div>
          </div>
        </div>

        <!-- 筛选标签 -->
        <div class="filter-tabs">
          <button
            v-for="tab in tabs" :key="tab.value"
            @click="currentTab = tab.value; loadWrongQuestions()"
            :class="['tab-btn', { active: currentTab === tab.value }]">
            {{ tab.label }}
          </button>
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="loading">加载中...</div>

        <!-- 错题列表 -->
        <div v-else-if="wrongQuestions.length > 0" class="question-list">
          <div
            v-for="wq in wrongQuestions" :key="wq.id"
            :class="['question-card', { mastered: wq.mastered === 1, expanded: expandedId === wq.id }]">
            <!-- 题目摘要 -->
            <div class="question-header" @click="toggleExpand(wq.id)">
              <div class="question-meta">
                <span :class="['type-badge', getTypeClass(wq.questionType)]">
                  {{ getTypeName(wq.questionType) }}
                </span>
                <span :class="['difficulty-badge', getDifficultyClass(wq.questionDifficulty)]">
                  {{ getDifficultyName(wq.questionDifficulty) }}
                </span>
                <span class="wrong-count">错了 {{ wq.wrongCount }} 次</span>
              </div>
              <div class="question-title">{{ wq.questionTitle }}</div>
              <div class="question-actions" @click.stop>
                <button
                  @click="toggleMastered(wq)"
                  :class="['action-icon', { 'is-mastered': wq.mastered === 1 }]"
                  :title="wq.mastered === 1 ? '标记为未掌握' : '标记为已掌握'">
                  {{ wq.mastered === 1 ? '⭐' : '☆' }}
                </button>
                <button @click="deleteWrongQuestion(wq)" class="action-icon delete" title="删除">
                  🗑
                </button>
              </div>
            </div>

            <!-- 展开详情 -->
            <div v-if="expandedId === wq.id" class="question-detail">
              <div class="detail-section">
                <strong>题目内容：</strong>
                <p>{{ wq.questionContent }}</p>
              </div>

              <!-- 选项（选择题/判断题） -->
              <div v-if="wq.questionOptions" class="detail-section">
                <strong>选项：</strong>
                <div class="options-list">
                  <div v-for="(opt, idx) in parseOptions(wq.questionOptions)" :key="idx"
                    :class="['option-item', {
                      'selected-wrong': isOptionSelected(wq, idx),
                      'correct-answer': isCorrectOption(wq, idx)
                    }]">
                    <span class="option-label">{{ getOptionLabel(idx) }}.</span>
                    <span class="option-text">{{ opt }}</span>
                  </div>
                </div>
              </div>

              <div class="answer-compare">
                <div class="your-answer">
                  <strong>你的答案：</strong>
                  <span class="wrong-text">{{ formatAnswer(wq.lastWrongAnswer) }}</span>
                </div>
                <div class="correct-answer-text">
                  <strong>正确答案：</strong>
                  <span class="correct-text">{{ getCorrectAnswerDisplay(wq) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination" v-if="totalPages > 1">
            <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0" class="page-btn">
              上一页
            </button>
            <span class="page-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
            <button @click="changePage(currentPage + 1)" :disabled="currentPage >= totalPages - 1" class="page-btn">
              下一页
            </button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">📚</div>
          <h3>{{ currentTab === null ? '暂无错题记录' : (currentTab === 1 ? '所有错题都已掌握！' : '没有未掌握的错题') }}</h3>
          <p>{{ currentTab === null ? '考试中做错的题目会自动收录到这里' : '继续加油！' }}</p>
        </div>
      </div>
    </main>

    <!-- 确认删除弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click="showDeleteConfirm = false">
      <div class="modal-box" @click.stop>
        <h3>确认删除</h3>
        <p>确定要从错题本中移除这道题吗？</p>
        <div class="modal-actions">
          <button @click="showDeleteConfirm = false" class="btn-cancel">取消</button>
          <button @click="confirmDelete" class="btn-danger">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'WrongQuestionsBook',
  data() {
    return {
      studentInfo: {},
      wrongQuestions: [],
      stats: { total: 0, mastered: 0, notMastered: 0, masteryRate: 0 },
      currentPage: 0,
      totalPages: 0,
      currentTab: null,
      expandedId: null,
      loading: false,
      showDeleteConfirm: false,
      deleteTarget: null,
      tabs: [
        { label: '全部', value: null },
        { label: '未掌握', value: 0 },
        { label: '已掌握', value: 1 }
      ]
    }
  },
  created() {
    this.studentInfo = JSON.parse(localStorage.getItem('studentInfo') || '{}')
    if (!this.studentInfo.id) {
      this.$router.push('/student/login')
      return
    }
    this.loadStats()
    this.loadWrongQuestions()
  },
  methods: {
    goBack() {
      this.$router.push('/student/dashboard')
    },
    logout() {
      localStorage.removeItem('studentToken')
      localStorage.removeItem('studentInfo')
      localStorage.removeItem('userType')
      this.$router.push('/student/login')
    },
    async loadStats() {
      try {
        const res = await axios.get('/api/v1/student/wrong-questions/stats', {
          params: { studentId: this.studentInfo.id }
        })
        if (res.data && res.data.code === 200) {
          this.stats = res.data.data
        }
      } catch (e) {
        console.error('加载统计失败', e)
      }
    },
    async loadWrongQuestions() {
      this.loading = true
      try {
        const params = {
          studentId: this.studentInfo.id,
          page: this.currentPage,
          size: 10
        }
        if (this.currentTab !== null) params.mastered = this.currentTab

        const res = await axios.get('/api/v1/student/wrong-questions', { params })
        if (res.data && res.data.code === 200) {
          const pageInfo = res.data.data
          this.wrongQuestions = pageInfo.list || []
          this.totalPages = pageInfo.pages || 0
          this.currentPage = pageInfo.pageNum - 1
        }
      } catch (e) {
        console.error('加载错题失败', e)
      } finally {
        this.loading = false
      }
    },
    async toggleMastered(wq) {
      try {
        await axios.put(`/api/v1/student/wrong-questions/${wq.id}/master`)
        wq.mastered = wq.mastered === 1 ? 0 : 1
        this.loadStats()
      } catch (e) {
        console.error('操作失败', e)
      }
    },
    deleteWrongQuestion(wq) {
      this.deleteTarget = wq
      this.showDeleteConfirm = true
    },
    async confirmDelete() {
      if (!this.deleteTarget) return
      try {
        await axios.delete(`/api/v1/student/wrong-questions/${this.deleteTarget.id}`)
        this.showDeleteConfirm = false
        this.deleteTarget = null
        this.loadStats()
        this.loadWrongQuestions()
      } catch (e) {
        console.error('删除失败', e)
      }
    },
    toggleExpand(id) {
      this.expandedId = this.expandedId === id ? null : id
    },
    changePage(page) {
      this.currentPage = page
      this.expandedId = null
      this.loadWrongQuestions()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    parseOptions(json) {
      try { return JSON.parse(json) } catch { return [] }
    },
    getOptionLabel(idx) {
      return String.fromCharCode(65 + idx) // A, B, C, D...
    },
    isOptionSelected(wq, idx) {
      const answer = wq.lastWrongAnswer
      if (!answer) return false
      const label = this.getOptionLabel(idx)
      try {
        const parsed = JSON.parse(answer)
        if (Array.isArray(parsed)) return parsed.includes(idx.toString()) || parsed.includes(label)
        return parsed === idx.toString() || parsed === label || parseInt(parsed) === idx
      } catch {
        return answer === idx.toString() || answer === label
      }
    },
    isCorrectOption(wq, idx) {
      const correct = wq.correctAnswers
      if (!correct) return false
      const label = this.getOptionLabel(idx)
      try {
        const parsed = JSON.parse(correct)
        if (Array.isArray(parsed)) return parsed.includes(idx.toString()) || parsed.includes(label)
        return parsed === idx.toString() || parsed === label || parseInt(parsed) === idx
      } catch {
        return correct === idx.toString() || correct === label
      }
    },
    formatAnswer(answer) {
      if (!answer) return '未作答'
      try {
        const parsed = JSON.parse(answer)
        if (Array.isArray(parsed)) {
          return parsed.map(a => {
            const n = parseInt(a)
            return isNaN(n) ? a : String.fromCharCode(65 + n)
          }).join(', ')
        }
        const n = parseInt(parsed)
        return isNaN(n) ? String(parsed) : String.fromCharCode(65 + n)
      } catch {
        return answer
      }
    },
    getCorrectAnswerDisplay(wq) {
      if (wq.questionType === 'single' || wq.questionType === 'multiple') {
        try {
          const parsed = JSON.parse(wq.correctAnswers)
          if (Array.isArray(parsed)) {
            return parsed.map(a => {
              const n = parseInt(a)
              return isNaN(n) ? a : String.fromCharCode(65 + n)
            }).join(', ')
          }
        } catch {}
      }
      return wq.correctAnswer || wq.correctAnswers || ''
    },
    getTypeName(type) {
      const map = { single: '单选题', multiple: '多选题', judge: '判断题', fill: '填空题', essay: '问答题' }
      return map[type] || type
    },
    getTypeClass(type) {
      const map = { single: 'single', multiple: 'multiple', judge: 'judge', fill: 'fill', essay: 'essay' }
      return map[type] || ''
    },
    getDifficultyName(d) {
      const map = { easy: '简单', medium: '中等', hard: '困难' }
      return map[d] || d
    },
    getDifficultyClass(d) {
      const map = { easy: 'easy', medium: 'medium', hard: 'hard' }
      return map[d] || ''
    }
  }
}
</script>

<style scoped>
* { box-sizing: border-box; margin: 0; padding: 0; }

.dashboard-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.header-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.logo-section { display: flex; align-items: center; gap: 12px; }
.back-btn {
  background: rgba(255,255,255,0.2);
  border: none;
  color: white;
  width: 32px; height: 32px;
  border-radius: 8px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.2s;
}
.back-btn svg { width: 20px; height: 20px; fill: white; }
.back-btn:hover { background: rgba(255,255,255,0.35); }
.logo { font-size: 20px; color: white; font-weight: 600; }
.user-section { display: flex; align-items: center; gap: 16px; }
.welcome-text { color: rgba(255,255,255,0.9); font-size: 14px; }
.logout-btn {
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.3);
  color: white;
  padding: 6px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s;
}
.logout-btn:hover { background: rgba(255,255,255,0.3); }

.main-content { background: #f5f7fa; min-height: calc(100vh - 60px); padding: 24px; }
.container { max-width: 900px; margin: 0 auto; }

/* 统计卡片 */
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  border-left: 4px solid #667eea;
}
.stat-card.total { border-left-color: #667eea; }
.stat-card.not-mastered { border-left-color: #e74c3c; }
.stat-card.mastered { border-left-color: #27ae60; }
.stat-card.rate { border-left-color: #f39c12; }
.stat-number { font-size: 28px; font-weight: 700; color: #2c3e50; }
.stat-label { font-size: 13px; color: #888; margin-top: 4px; }

/* 筛选标签 */
.filter-tabs { display: flex; gap: 8px; margin-bottom: 20px; }
.tab-btn {
  padding: 8px 20px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  color: #555;
  transition: all 0.2s;
}
.tab-btn:hover { border-color: #667eea; color: #667eea; }
.tab-btn.active { background: #667eea; color: white; border-color: #667eea; }

.loading { text-align: center; padding: 40px; color: #888; font-size: 15px; }

/* 题目列表 */
.question-list { display: flex; flex-direction: column; gap: 12px; }
.question-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.question-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.question-card.mastered { opacity: 0.75; }
.question-header {
  padding: 16px 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.question-meta { display: flex; gap: 8px; align-items: center; flex-shrink: 0; }
.type-badge {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  color: white;
  font-weight: 500;
}
.type-badge.single { background: #3498db; }
.type-badge.multiple { background: #9b59b6; }
.type-badge.judge { background: #2ecc71; }
.type-badge.fill { background: #f39c12; }
.type-badge.essay { background: #e74c3c; }
.difficulty-badge {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.difficulty-badge.easy { background: #e8f5e9; color: #27ae60; }
.difficulty-badge.medium { background: #fff3e0; color: #f57c00; }
.difficulty-badge.hard { background: #fce4ec; color: #c62828; }
.wrong-count { font-size: 12px; color: #e74c3c; font-weight: 500; }
.question-title { font-size: 15px; color: #2c3e50; flex: 1; min-width: 200px; font-weight: 500; }
.question-actions { display: flex; gap: 4px; margin-left: auto; }
.action-icon {
  background: none; border: none;
  font-size: 20px; cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}
.action-icon:hover { background: #f0f0f0; }
.action-icon.is-mastered { color: #f39c12; }
.action-icon.delete:hover { background: #fce4ec; }

/* 展开详情 */
.question-detail {
  padding: 0 20px 20px;
  border-top: 1px solid #f0f0f0;
  margin-top: 0;
  padding-top: 16px;
}
.detail-section { margin-bottom: 14px; }
.detail-section strong { display: block; font-size: 13px; color: #666; margin-bottom: 6px; }
.detail-section p { font-size: 14px; color: #333; line-height: 1.6; }
.options-list { display: flex; flex-direction: column; gap: 6px; }
.option-item {
  padding: 8px 14px;
  border-radius: 8px;
  background: #f8f9fa;
  border: 1px solid #eee;
  font-size: 14px;
  display: flex; gap: 8px;
}
.option-item.selected-wrong { background: #fce4ec; border-color: #ef5350; }
.option-item.correct-answer { background: #e8f5e9; border-color: #4caf50; }
.option-item.selected-wrong.correct-answer { background: #fff3e0; border-color: #ff9800; }
.option-label { font-weight: 600; color: #555; min-width: 22px; }
.answer-compare { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.your-answer, .correct-answer-text { padding: 12px; border-radius: 8px; font-size: 14px; }
.your-answer { background: #fce4ec; }
.correct-answer-text { background: #e8f5e9; }
.wrong-text { color: #c62828; }
.correct-text { color: #2e7d32; }

/* 分页 */
.pagination {
  display: flex; align-items: center; justify-content: center;
  gap: 16px; margin-top: 24px; padding: 16px;
}
.page-btn {
  padding: 8px 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}
.page-btn:hover:not(:disabled) { border-color: #667eea; color: #667eea; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.page-info { font-size: 14px; color: #666; }

/* 空状态 */
.empty-state { text-align: center; padding: 60px 20px; }
.empty-icon { font-size: 64px; margin-bottom: 16px; }
.empty-state h3 { font-size: 18px; color: #333; margin-bottom: 8px; }
.empty-state p { color: #888; font-size: 14px; }

/* 删除确认弹窗 */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  z-index: 200;
}
.modal-box {
  background: white; border-radius: 16px;
  padding: 32px; width: 400px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
}
.modal-box h3 { font-size: 18px; margin-bottom: 12px; color: #333; }
.modal-box p { font-size: 14px; color: #666; margin-bottom: 24px; }
.modal-actions { display: flex; gap: 12px; justify-content: flex-end; }
.btn-cancel {
  padding: 8px 24px; border: 1px solid #ddd;
  border-radius: 8px; background: white; cursor: pointer; font-size: 14px;
}
.btn-danger {
  padding: 8px 24px; border: none;
  border-radius: 8px; background: #e74c3c; color: white; cursor: pointer; font-size: 14px;
}

@media (max-width: 768px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .answer-compare { grid-template-columns: 1fr; }
}
</style>
