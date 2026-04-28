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

    <!-- 中间：无限滚动记录列表（最新在底部，历史在上方） -->
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
            van-list direction="up"：
            当滚动容器 scrollTop 接近 0 (offset 范围内) 时自动触发 load 事件，
            并将 v-model:loading 设为 true；
            scroller 指定真实滚动容器（main.record-page__body）。
          -->
          <van-list
            v-model:loading="loadingMore"
            :finished="noMoreHistory"
            direction="up"
            :scroller="scrollContainerRef || undefined"
            :immediate-check="false"
            :offset="80"
            class="record-page__list"
            @load="loadOlderEntries"
          >
            <!-- 自定义加载中提示（顶部） -->
            <template #loading>
              <div class="load-more-tip">
                <van-loading size="1rem" color="#94a3b8" />
                <span>加载历史中...</span>
              </div>
            </template>

            <!-- 自定义全部加载完毕提示（顶部） -->
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
              <van-swipe-cell class="entry-swipe-cell">
                <div class="entry-item" @click="openEdit(entry)" :style="{ '--entry-color': getEntryType(entry.entryType ?? '').color }">
                  <div class="entry-item__accent" />
                  <div class="entry-item__body">
                    <div class="entry-item__main">
                      <van-checkbox
                        v-if="entry.entryType === 'Do'"
                        :model-value="entry.checked === 1"
                        shape="square"
                        icon-size="1rem"
                        :checked-color="getEntryType('Do').color"
                        @click.stop
                        @update:model-value="(val: boolean) => handleToggleChecked(entry, val)"
                      />
                      <span
                        class="entry-content"
                        :class="{ 'is-checked': entry.entryType === 'Do' && entry.checked === 1 }"
                      >
                        {{ entry.content }}
                      </span>
                    </div>
                    <div class="entry-item__meta">
                      <span class="entry-type-label" :style="{ color: getEntryType(entry.entryType ?? '').color }">{{ entry.entryType }}</span>
                      <span class="entry-time">{{ formatEntryTime(entry.createTime) }}</span>
                    </div>
                  </div>
                </div>
                <template #right>
                  <van-button
                    square
                    type="danger"
                    text="删除"
                    class="entry-delete-btn"
                    @click="handleDelete(entry)"
                  />
                </template>
              </van-swipe-cell>
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
import { computed, nextTick, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useLoginUserStore } from '@/stores/loginUser'
import {
  addEntry,
  deleteEntry,
  queryDailyEntries,
  queryHistoryEntries,
  updateChecked,
  updateContentAndType,
} from '@/api/entriesController'
import { ENTRY_TYPES, getEntryType } from '@/constants/entries'
import defaultAvatar from '@/assets/default_avatar.png'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const loginUser = computed(() => loginUserStore.loginUser)

// =========== 列表状态 ===========
const PAGE_SIZE = 20
const entries = ref<API.EntriesVo[]>([]) // 按时间正序（旧→新）排列
const loading = ref(false)
const loadingMore = ref(false)
const noMoreHistory = ref(false)
const refreshing = ref(false)
const scrollContainerRef = ref<HTMLElement | null>(null)

// =========== 工具函数 ===========
function sortAsc(data: API.EntriesVo[]): API.EntriesVo[] {
  return [...data].sort(
    (a, b) => new Date(a.createTime!).getTime() - new Date(b.createTime!).getTime(),
  )
}

function scrollToBottom() {
  const el = scrollContainerRef.value
  if (el) el.scrollTop = el.scrollHeight
}

// =========== 初始化加载 ===========
async function loadInitial() {
  loading.value = true
  noMoreHistory.value = false
  try {
    const res = await queryHistoryEntries({ pageSize: PAGE_SIZE })
    if (res.data.code === 0 && res.data.data) {
      entries.value = sortAsc(res.data.data)
      if (res.data.data.length < PAGE_SIZE) noMoreHistory.value = true
    }
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

// =========== 向上加载历史数据（prepend） ===========
// 由 van-list direction="up" 触发，触发时 loadingMore 已被 van-list 设为 true
async function loadOlderEntries() {
  if (noMoreHistory.value || entries.value.length === 0) {
    loadingMore.value = false
    return
  }

  const el = scrollContainerRef.value
  // 在任何 await 之前同步捕获高度（loading slot 尚未渲染到 DOM）
  const prevScrollHeight = el?.scrollHeight ?? 0
  const cursor = entries.value[0]?.createTime

  try {
    const res = await queryHistoryEntries({ pageSize: PAGE_SIZE, lastCreateTime: cursor })
    if (res.data.code === 0 && res.data.data) {
      const older = sortAsc(res.data.data)
      if (older.length < PAGE_SIZE) noMoreHistory.value = true
      if (older.length > 0) {
        // 关键：在同一同步块内同时更新数据并关闭 loading，
        // Vue 会把「新增条目」+「移除 loading slot」合并到同一次 nextTick 渲染，
        // 使得 scrollHeight 的增量 = 纯新增条目高度，loading slot 的高度变化相互抵消。
        entries.value = [...older, ...entries.value]
        loadingMore.value = false
        await nextTick()
        if (el) el.scrollTop += el.scrollHeight - prevScrollHeight
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
    const res = await queryHistoryEntries({ pageSize: PAGE_SIZE })
    if (res.data.code === 0 && res.data.data) {
      entries.value = sortAsc(res.data.data)
      noMoreHistory.value = res.data.data.length < PAGE_SIZE
    }
  } finally {
    refreshing.value = false
    await nextTick()
    scrollToBottom()
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
 * 追加最新条目到列表底部，不替换已加载的历史数据。
 * 通过 ID 去重，只将后端返回中本地不存在的条目 append 到末尾。
 */
async function appendLatestEntries() {
  const today = new Date().toISOString().slice(0, 10)
  const res = await queryDailyEntries({ date: today })
  if (res.data.code === 0 && res.data.data) {
    const latest = sortAsc(res.data.data)
    if (entries.value.length === 0) {
      entries.value = latest
    } else {
      const existingIds = new Set(entries.value.map((e) => e.id))
      const newEntries = latest.filter((e) => !existingIds.has(e.id))
      if (newEntries.length > 0) {
        entries.value = [...entries.value, ...newEntries]
      }
    }
    await nextTick()
    scrollToBottom()
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
  entry.checked = checked
  try {
    const res = await updateChecked({ id: entry.id!, checked })
    if (res.data.code !== 0) {
      entry.checked = prev
      showToast({ type: 'fail', message: '更新失败' })
    }
  } catch {
    entry.checked = prev
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
const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({ id: '', content: '', entryType: 'Do' })

const currentEditPlaceholder = computed(
  () => ENTRY_TYPES.find((t) => t.value === editForm.entryType)?.placeholder ?? '',
)
const currentEditColor = computed(
  () => ENTRY_TYPES.find((t) => t.value === editForm.entryType)?.color ?? '#22C55E',
)

function openEdit(entry: API.EntriesVo) {
  editForm.id = entry.id ?? ''
  editForm.content = entry.content ?? ''
  editForm.entryType = entry.entryType ?? 'Do'
  editVisible.value = true
}

async function submitEdit() {
  if (!editForm.content.trim()) {
    showToast('内容不能为空')
    return
  }
  saving.value = true
  try {
    const res = await updateContentAndType({
      id: editForm.id,
      content: editForm.content.trim(),
      entryType: editForm.entryType,
    })
    if (res.data.code === 0) {
      const target = entries.value.find((e) => e.id === editForm.id)
      if (target) {
        target.content = editForm.content.trim()
        target.entryType = editForm.entryType
      }
      editVisible.value = false
      showToast({ type: 'success', message: '已更新' })
    } else {
      showToast({ type: 'fail', message: '更新失败' })
    }
  } finally {
    saving.value = false
  }
}

// =========== 日期分组 ===========
function getEntryDay(createTime?: string): string {
  if (!createTime) return ''
  const d = new Date(createTime)
  return `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`
}

function shouldShowDateHeader(index: number): boolean {
  if (index === 0) return true
  return getEntryDay(entries.value[index]?.createTime) !== getEntryDay(entries.value[index - 1]?.createTime)
}

function formatEntryDate(createTime?: string): string {
  if (!createTime) return ''
  return new Date(createTime).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  })
}

function formatEntryTime(createTime?: string): string {
  if (!createTime) return ''
  const d = new Date(createTime)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
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

/* ======= 记录条目 ======= */
.entry-swipe-cell {
  border-bottom: 0.5px solid var(--color-border-light);
  background: #fff;
}

.entry-swipe-cell:last-child {
  border-bottom: none;
}

.entry-delete-btn {
  height: 100%;
  width: 4.5rem;
  font-size: 0.875rem;
}

/* Checkbox 与文字同行对齐 */
.entry-item :deep(.van-checkbox) {
  flex-shrink: 0;
  align-self: flex-start;
  margin-top: 0.1rem;
}

.entry-item :deep(.van-checkbox__icon) {
  height: 1rem;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
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
