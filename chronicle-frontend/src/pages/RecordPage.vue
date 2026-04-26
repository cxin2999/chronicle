<template>
  <div class="record-page mobile-page">
    <!-- 头部：头像 + 标题 + 搜索 -->
    <header class="record-page__header page-header">
      <div class="record-page__avatar" @click="goToProfile">
        <van-image
          round
          fit="cover"
          :src="loginUser.userAvatar || defaultAvatar"
          width="2.25rem"
          height="2.25rem"
          class="avatar-img"
        />
      </div>
      <span class="page-header__title">记录</span>
      <van-button
        icon="search"
        size="small"
        plain
        :border="false"
        class="record-page__search-btn"
        @click="goToHistory"
      />
    </header>

    <!-- 中间：当日记录 -->
    <main class="page-body record-page__body" ref="bodyRef">
      <div class="record-page__date-label">
        <div class="date-label__left">
          <van-icon name="calendar-o" size="0.875rem" />
          <span>{{ dateLabel }}</span>
        </div>
        <div class="date-label__stats">
          <span
            v-for="t in entryCounts"
            :key="t.value"
            class="entry-stat-tag"
            :style="{ background: t.bgColor, color: t.color }"
            >{{ t.label }}&nbsp;{{ t.count }}</span
          >
        </div>
      </div>
      <DailyEntries :date="today" ref="dailyEntriesRef" />
    </main>

    <!-- 底部：输入区域 -->
    <footer class="record-page__input-bar">
      <!-- 类型选择 Segmented Pills -->
      <div class="type-pills" style="margin-bottom: 0.625rem">
        <button
          v-for="t in ENTRY_TYPES"
          :key="t.value"
          class="type-pill"
          :class="{ active: selectedType === t.value }"
          :style="{ '--pill-color': t.color }"
          @click="selectedType = t.value"
        >
          {{ t.label }}
        </button>
      </div>

      <!-- 输入行 -->
      <div class="record-page__input-row">
        <van-field
          v-model="inputContent"
          :placeholder="currentPlaceholder"
          :border="false"
          clearable
          maxlength="200"
          class="record-page__field"
          @keyup.enter="handleAdd"
        />
        <van-button
          icon="plus"
          :color="currentColor"
          size="small"
          round
          :loading="adding"
          class="record-page__add-btn"
          @click="handleAdd"
        />
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useLoginUserStore } from '@/stores/loginUser'
import { createArticle } from '@/api/entriesController'
import DailyEntries from '@/components/DailyEntries.vue'
import { ENTRY_TYPES } from '@/constants/entries'
import defaultAvatar from '@/assets/default_avatar.png'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const loginUser = computed(() => loginUserStore.loginUser)

// 今天的日期
const today = new Date()
  .toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
  .replace(/\//g, '-')

const dateLabel = new Date().toLocaleDateString('zh-CN', {
  month: 'long',
  day: 'numeric',
  weekday: 'short',
})

// 当前选中类型
const selectedType = ref<string>('Do')
const currentPlaceholder = computed(
  () => ENTRY_TYPES.find((t) => t.value === selectedType.value)?.placeholder ?? '',
)
const currentColor = computed(
  () => ENTRY_TYPES.find((t) => t.value === selectedType.value)?.color ?? '#22C55E',
)

// 输入内容
const inputContent = ref('')
const adding = ref(false)

// DailyEntries 组件实例引用（用于刷新）
const dailyEntriesRef = ref<InstanceType<typeof DailyEntries> | null>(null)
const bodyRef = ref<HTMLElement | null>(null)

// 各类型数量统计（仅显示数量 > 0 的类型）
const entryCounts = computed(() => {
  const list = dailyEntriesRef.value?.entries ?? []
  return ENTRY_TYPES.map((t) => ({
    ...t,
    count: list.filter((e) => e.entryType === t.value).length,
  })).filter((t) => t.count > 0)
})

function scrollToBottom() {
  nextTick(() => {
    if (bodyRef.value) {
      bodyRef.value.scrollTop = bodyRef.value.scrollHeight
    }
  })
}

async function handleAdd() {
  const content = inputContent.value.trim()
  if (!content) {
    showToast('请输入内容')
    return
  }
  if (adding.value) return

  // 乐观更新：立即插入临时条目，清空输入框
  const tempId = `temp_${Date.now()}`
  const tempEntry: API.EntriesVo = {
    id: tempId,
    content,
    entryType: selectedType.value,
    checked: '0',
  }
  dailyEntriesRef.value?.addOptimistic(tempEntry)
  inputContent.value = ''
  scrollToBottom()

  // 异步持久化
  adding.value = true
  try {
    const res = await createArticle({ content, entryType: selectedType.value })
    if (res.data.code !== 0) {
      // 回滚
      dailyEntriesRef.value?.removeEntry(tempId)
      showToast({ type: 'fail', message: '添加失败：' + res.data.message })
    }
  } catch {
    dailyEntriesRef.value?.removeEntry(tempId)
    showToast({ type: 'fail', message: '添加失败，请重试' })
  } finally {
    adding.value = false
  }
}

function goToProfile() {
  router.push('/profile')
}

function goToHistory() {
  router.push('/history')
}
</script>

<style scoped>
/* ======= Header ======= */
.record-page__header {
  background: #fff;
}

/* 头像：圆形 + 细边框 + 立体阴影 */
.record-page__avatar {
  cursor: pointer;
  transition: opacity var(--transition-fast);
  border-radius: 50%;
  overflow: hidden;
  box-shadow:
    0 0 0 1.5px rgba(255, 255, 255, 0.85),
    0 1px 4px rgba(0, 0, 0, 0.12);
}

.record-page__avatar:active {
  opacity: 0.7;
}

.avatar-img {
  display: block;
}

/* 搜索按钮：圆形浅灰背景，轻盈图标 */
.record-page__search-btn {
  width: 2rem !important;
  height: 2rem !important;
  border-radius: 50% !important;
  background: #f1f5f9 !important;
  border: none !important;
  color: #64748b !important;
  padding: 0 !important;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ======= 日期标签 ======= */
.record-page__date-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #f1f5f9;
}

.date-label__left {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.8125rem;
  font-weight: 500;
  color: #94a3b8;
}

.date-label__stats {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.entry-stat-tag {
  font-size: 0.6875rem;
  font-weight: 500;
  border-radius: 9999px;
  padding: 0.125rem 0.45rem;
  line-height: 1.4;
  white-space: nowrap;
}

/* ======= 内容区 ======= */
.record-page__body {
  background: var(--color-background-tertiary);
}

/* ======= 底部输入区 ======= */
.record-page__input-bar {
  flex-shrink: 0;
  padding: 0.75rem 1rem calc(0.875rem + env(safe-area-inset-bottom, 0px));
  background: #fff;
  border-top: 1px solid var(--color-border-light);
  box-shadow: 0 -2px 16px rgba(0, 0, 0, 0.05);
}

/* 输入行：浅灰圆角矩形容器 */
.record-page__input-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #f8fafc;
  border-radius: var(--radius-xl);
  padding: 0 0.375rem 0 0;
}

/* 输入框：透明背景 + 斜体淡色 placeholder */
.record-page__field {
  flex: 1;
  background: transparent !important;
  border-radius: var(--radius-xl);
}

.record-page__field :deep(.van-field__control::placeholder) {
  font-style: italic;
  color: #c0ccda;
}

.record-page__field :deep(.van-field__control) {
  font-size: 0.9375rem;
  color: var(--color-text);
}

/* 添加按钮：圆形 + 动态颜色（由 :color 控制）+ 点击微缩放 */
.record-page__add-btn {
  flex-shrink: 0;
  width: 2.25rem !important;
  height: 2.25rem !important;
  min-width: unset !important;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.18) !important;
  transition:
    transform var(--transition-fast),
    box-shadow var(--transition-fast) !important;
}

.record-page__add-btn:active {
  transform: scale(0.92) !important;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12) !important;
}
</style>
