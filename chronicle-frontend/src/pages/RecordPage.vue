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

    <!-- 中间：无限滚动记录列表（最新在上方，历史在下方） -->
    <main
      class="page-body record-page__body"
      ref="scrollContainerRef"
    >
      <van-pull-refresh
        v-model="refreshing"
        @refresh="onRefresh"
        class="record-page__pull-refresh"
      >
        <!-- 初始加载中 -->
        <div v-if="loading" class="list-center-tip">
          <van-loading size="1.5rem" color="var(--color-primary)" />
        </div>

        <template v-else>
          <!--
            van-list 默认 direction="down"：
            当滚动容器接近底部时自动触发 load 事件，
            并将 v-model:loading 设为 true；
            scroller 指定真实滚动容器（main.record-page__body）。
          -->
          <van-list
            v-model:loading="loadingMore"
            :finished="noMoreHistory"
            :disabled="loading || refreshing"
            :scroller="scrollContainerRef || undefined"
            :immediate-check="false"
            :offset="80"
            class="record-page__list"
            @load="loadOlderEntries"
          >
            <!-- 自定义加载中提示（底部） -->
            <template #loading>
              <div class="load-more-tip">
                <van-loading size="1rem" color="#94a3b8" />
                <span>正在加载更早记录...</span>
              </div>
            </template>

            <!-- 自定义全部加载完毕提示（底部） -->
            <template #finished>
              <div class="no-more-tip">· 已加载全部历史记录 ·</div>
            </template>

            <!-- 空状态 -->
            <div v-if="entries.length === 0" class="list-center-tip">
              <van-empty description="暂无记录" image-size="5rem" />
            </div>

            <!-- 按日期分组渲染所有记录 -->
            <template v-for="(entry, index) in entries" :key="entry.id">
              <!-- 日期分组标题 -->
              <div v-if="shouldShowDateHeader(index)" class="date-group-header">
                <span class="date-group-header__label">{{ formatEntryDate(entry.createTime) }}</span>
              </div>

              <!-- 记录条目（左滑删除） -->
              <EntryItem
                :entry="entry"
                @click="openEdit(entry)"
                @delete="handleDelete(entry)"
                @toggle-checked="(val: boolean) => handleToggleChecked(entry, val)"
              />
            </template>
          </van-list>
        </template>
      </van-pull-refresh>
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

    <!-- 编辑底部弹层（从 DailyEntries 迁移） -->
    <van-popup
      v-model:show="editVisible"
      position="bottom"
      round
      :style="{ padding: '1.25rem 1rem 2rem' }"
    >
      <div class="edit-popup">
        <div class="edit-popup__title">编辑记录</div>

        <div class="type-pills" style="margin-bottom: 0.875rem">
          <button
            v-for="t in ENTRY_TYPES"
            :key="t.value"
            class="type-pill"
            :class="{ active: editForm.entryType === t.value }"
            :style="{ '--pill-color': t.color }"
            @click="editForm.entryType = t.value"
          >
            {{ t.label }}
          </button>
        </div>

        <van-field
          v-model="editForm.content"
          type="textarea"
          :placeholder="currentEditPlaceholder"
          rows="4"
          :border="false"
          maxlength="200"
          show-word-limit
          class="edit-popup__field"
          autosize
        />

        <div v-if="editForm.entryType === 'Do'" class="edit-popup__completion">
          <div class="edit-popup__completion-header">
            <span class="edit-popup__completion-label">完成进度</span>
            <span class="edit-popup__completion-value" :style="{ color: currentEditColor }">{{ editForm.completionRate }}%</span>
          </div>
          <van-slider
            v-model="editForm.completionRate"
            :min="0"
            :max="100"
            :step="5"
            :bar-height="4"
            :active-color="currentEditColor"
          />
        </div>

        <van-button
          block
          type="primary"
          :loading="saving"
          :color="currentEditColor"
          style="margin-top: 0.875rem; border-radius: var(--radius-lg)"
          @click="submitEdit"
        >
          保存
        </van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useLoginUserStore } from '@/stores/loginUser'
import {
  addEntry,
  deleteEntry,
  queryDailyEntries,
  queryHistoryEntries,
  updateChecked,
} from '@/api/entriesController'
import { ENTRY_TYPES } from '@/constants/entries'
import defaultAvatar from '@/assets/default_avatar.png'
import dayjs from 'dayjs'
import EntryItem from '@/components/EntryItem.vue'
import { useEntryEdit } from '@/composables/useEntryEdit'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const loginUser = computed(() => loginUserStore.loginUser)

// =========== 列表状态 ===========
const PAGE_SIZE = 15
const entries = ref<API.EntriesVo[]>([]) // 按时间倒序（新→旧）排列
const loading = ref(false)
const loadingMore = ref(false)
const noMoreHistory = ref(false)
const refreshing = ref(false)
const scrollContainerRef = ref<HTMLElement | null>(null)

// =========== 工具函数 ===========
function sortDesc(data: API.EntriesVo[]): API.EntriesVo[] {
  return [...data].sort(
    (a, b) => new Date(b.createTime!).getTime() - new Date(a.createTime!).getTime(),
  )
}

function scrollToTop() {
  const el = scrollContainerRef.value
  if (el) el.scrollTop = 0
}

async function loadLatestPage() {
  const res = await queryHistoryEntries({ pageSize: PAGE_SIZE })
  if (res.data.code === 0 && res.data.data) {
    entries.value = sortDesc(res.data.data)
    noMoreHistory.value = res.data.data.length < PAGE_SIZE
  }
}

// =========== 初始化加载 ===========
async function loadInitial() {
  loading.value = true
  noMoreHistory.value = false
  try {
    await loadLatestPage()
  } finally {
    loading.value = false
    await nextTick()
    scrollToTop()
  }
}

// =========== 向下加载历史数据（append） ===========
// 由 van-list 默认 direction="down" 触发，触发时 loadingMore 已被 van-list 设为 true
async function loadOlderEntries() {
  if (noMoreHistory.value || entries.value.length === 0) {
    loadingMore.value = false
    return
  }

  const cursor = entries.value[entries.value.length - 1]?.createTime

  try {
    const res = await queryHistoryEntries({ pageSize: PAGE_SIZE, lastCreateTime: cursor })
    if (res.data.code === 0 && res.data.data) {
      const older = sortDesc(res.data.data)
      if (older.length < PAGE_SIZE) noMoreHistory.value = true
      if (older.length > 0) {
        // 新顺序是新→旧，历史数据追加到列表底部。
        entries.value = [...entries.value, ...older]
        loadingMore.value = false
        return // 正常退出，跳过 finally 中的重复赋值
      }
    }
  } catch {
    // 异常不影响后续 finally 清理
  } finally {
    // 兜底：防止异常或数据为空时 loadingMore 永远为 true
    if (loadingMore.value) loadingMore.value = false
  }
}

// =========== 下拉刷新：重新加载最新数据 ===========
async function onRefresh() {
  try {
    noMoreHistory.value = false
    await loadLatestPage()
  } finally {
    refreshing.value = false
    await nextTick()
    scrollToTop()
  }
}

// =========== 输入 & 添加记录 ===========
const selectedType = ref<string>('Do')
const inputContent = ref('')
const adding = ref(false)

const currentPlaceholder = computed(
  () => ENTRY_TYPES.find((t) => t.value === selectedType.value)?.placeholder ?? '',
)
const currentColor = computed(
  () => ENTRY_TYPES.find((t) => t.value === selectedType.value)?.color ?? '#22C55E',
)

/**
 * 追加最新条目到列表顶部，不替换已加载的历史数据。
 * 通过 ID 去重，只将后端返回中本地不存在的条目 prepend 到开头。
 */
async function appendLatestEntries() {
  const today = dayjs().format('YYYY-MM-DD')
  const res = await queryDailyEntries({ date: today })
  if (res.data.code === 0 && res.data.data) {
    const latest = sortDesc(res.data.data)
    if (entries.value.length === 0) {
      entries.value = latest
    } else {
      const existingIds = new Set(entries.value.map((e) => e.id))
      const newEntries = latest.filter((e) => !existingIds.has(e.id))
      if (newEntries.length > 0) {
        entries.value = [...newEntries, ...entries.value]
      }
    }
    await nextTick()
    scrollToTop()
  }
}

async function handleAdd() {
  const content = inputContent.value.trim()
  if (!content) {
    showToast('请输入内容')
    return
  }
  if (adding.value) return

  adding.value = true
  try {
    const res = await addEntry({ content, entryType: selectedType.value })
    if (res.data.code === 0) {
      inputContent.value = ''
      // 只追加新条目，保留已加载的历史数据，避免清空重载
      await appendLatestEntries()
    } else {
      showToast({ type: 'fail', message: '添加失败：' + res.data.message })
    }
  } catch {
    showToast({ type: 'fail', message: '添加失败，请重试' })
  } finally {
    adding.value = false
  }
}

// =========== 勾选 Do 类型 ===========
async function handleToggleChecked(entry: API.EntriesVo, val: boolean) {
  const checked = val ? 1 : 0
  const prev = entry.checked
  const prevRate = entry.completionRate
  entry.checked = checked
  entry.completionRate = checked === 1 ? 100 : 0
  try {
    const res = await updateChecked({ id: entry.id!, checked })
    if (res.data.code !== 0) {
      entry.checked = prev
      entry.completionRate = prevRate
      showToast({ type: 'fail', message: '更新失败' })
    }
  } catch {
    entry.checked = prev
    entry.completionRate = prevRate
    showToast({ type: 'fail', message: '网络错误' })
  }
}

// =========== 删除记录（乐观删除） ===========
async function handleDelete(entry: API.EntriesVo) {
  const index = entries.value.findIndex((e) => e.id === entry.id)
  entries.value.splice(index, 1)
  try {
    const res = await deleteEntry({ id: entry.id! })
    if (res.data.code !== 0) {
      entries.value.splice(index, 0, entry)
      showToast({ type: 'fail', message: '删除失败' })
    }
  } catch {
    entries.value.splice(index, 0, entry)
    showToast({ type: 'fail', message: '删除失败，请重试' })
  }
}

// =========== 编辑记录 ===========
const { editVisible, saving, editForm, currentEditPlaceholder, currentEditColor, openEdit, submitEdit } =
  useEntryEdit(entries)

// =========== 日期分组 ===========
function getEntryDay(createTime?: string): string {
  if (!createTime) return ''
  return dayjs(createTime).format('YYYY-MM-DD')
}

function shouldShowDateHeader(index: number): boolean {
  if (index === 0) return true
  return getEntryDay(entries.value[index]?.createTime) !== getEntryDay(entries.value[index - 1]?.createTime)
}

function formatEntryDate(createTime?: string): string {
  if (!createTime) return ''
  const d = dayjs(createTime)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${d.format('YYYY年M月D日')} ${weekdays[d.day()]}`
}

// =========== 路由 ===========
function goToProfile() {
  router.push('/profile')
}

function goToHistory() {
  router.push('/history')
}

// =========== 初始化 ===========
loadInitial()
</script>

<style scoped>
/* ======= Header ======= */
.record-page__header {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.record-page__avatar {
  cursor: pointer;
  transition: opacity var(--transition-fast);
  border-radius: 50%;
  overflow: hidden;
}

.record-page__avatar:active {
  opacity: 0.55;
}

.avatar-img {
  display: block;
}

.record-page__search-btn {
  width: 2rem !important;
  height: 2rem !important;
  border-radius: 50% !important;
  background: rgba(120, 120, 128, 0.1) !important;
  border: none !important;
  color: var(--color-text-secondary) !important;
  padding: 0 !important;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition-fast) !important;
}

.record-page__search-btn:active {
  background: rgba(120, 120, 128, 0.2) !important;
}

/* ======= 内容区 ======= */
.record-page__body {
  background: var(--color-background-tertiary);
  overflow-anchor: none;
}

.record-page__pull-refresh {
  min-height: 100%;
}

/* ======= 日期分组标题（Apple 风格，左对齐节标题） ======= */
.date-group-header {
  padding: 1.125rem 1rem 0.3125rem 1.25rem;
}

.date-group-header__label {
  font-size: 0.6875rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.055em;
  font-family: var(--font-sans);
}

/* ======= 加载状态提示 ======= */
.list-center-tip {
  display: flex;
  justify-content: center;
  padding: 2.5rem 0;
}

.load-more-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.875rem 1rem;
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  font-family: var(--font-sans);
}

.no-more-tip {
  display: flex;
  justify-content: center;
  padding: 0.875rem 1rem;
  font-size: 0.6875rem;
  color: var(--color-text-muted);
  letter-spacing: 0.06em;
  font-family: var(--font-sans);
}

/* ======= 编辑弹层 ======= */
.edit-popup__title {
  font-size: 1.0625rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 1.25rem;
  text-align: center;
  letter-spacing: -0.01em;
  font-family: var(--font-sans);
}

.edit-popup__field {
  background: rgba(120, 120, 128, 0.08);
  border-radius: var(--radius-lg);
}

.edit-popup__field :deep(.van-field__control::placeholder) {
  color: var(--color-text-muted);
  font-style: normal;
}

.edit-popup__field :deep(.van-field__control) {
  font-size: 0.9375rem;
  color: var(--color-text);
  font-family: var(--font-sans);
}

.edit-popup__completion {
  margin-top: 1rem;
  padding: 0.75rem 0.875rem;
  background: var(--color-background-tertiary);
  border-radius: var(--radius-md);
}

.edit-popup__completion-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.625rem;
}

.edit-popup__completion-label {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  font-family: var(--font-sans);
}

.edit-popup__completion-value {
  font-size: 0.9375rem;
  font-weight: 600;
  font-family: var(--font-sans);
  transition: color 0.2s;
}

/* ======= 底部输入区 ======= */
.record-page__input-bar {
  flex-shrink: 0;
  padding: 0.625rem 1rem calc(0.75rem + env(safe-area-inset-bottom, 0px));
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 0.5px solid var(--color-separator);
}

/* 输入行 */
.record-page__input-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(120, 120, 128, 0.08);
  border-radius: var(--radius-xl);
  padding: 0 0.375rem 0 0;
  transition: background var(--transition-fast);
}

.record-page__field {
  flex: 1;
  background: transparent !important;
  border-radius: var(--radius-xl);
}

.record-page__field :deep(.van-field__control::placeholder) {
  color: var(--color-text-muted);
  font-style: normal;
}

.record-page__field :deep(.van-field__control) {
  font-size: 0.9375rem;
  color: var(--color-text);
  font-family: var(--font-sans);
}

/* 添加按钮 */
.record-page__add-btn {
  flex-shrink: 0;
  width: 2.125rem !important;
  height: 2.125rem !important;
  min-width: unset !important;
  transition:
    transform var(--transition-fast),
    opacity var(--transition-fast) !important;
}

.record-page__add-btn:active {
  transform: scale(0.88) !important;
  opacity: 0.85 !important;
}
</style>
