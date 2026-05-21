<template>
  <div class="search-page mobile-page">
    <header class="search-page__header page-header">
      <button class="search-back-btn" @click="goBack">
        <span class="search-back-btn__chevron">‹</span>
        <span class="search-back-btn__label">记录</span>
      </button>
      <span class="page-header__title">搜索</span>
      <div class="search-page__header-placeholder" />
    </header>

    <main class="page-body search-page__body" ref="scrollContainerRef">
      <section class="search-conditions">
        <div class="search-bar">
          <div class="search-bar__type">
            <van-dropdown-menu :overlay="true" :active-color="activeTypeColor">
              <van-dropdown-item
                v-model="selectedType"
                :options="typeOptions"
                teleport="body"
              />
            </van-dropdown-menu>
          </div>
          <div class="search-bar__divider" />
          <van-field
            v-model="keywordInput"
            placeholder="搜索记录…"
            clearable
            :border="false"
            left-icon="search"
            class="search-keyword-field"
          />
        </div>
      </section>

      <div v-if="searching" class="list-center-tip">
        <van-loading size="1.5rem" color="var(--color-primary)" />
      </div>

      <template v-else>
        <van-list
          v-model:loading="loadingMore"
          :finished="finished"
          :disabled="searching"
          :scroller="scrollContainerRef || undefined"
          :immediate-check="false"
          :offset="80"
          class="search-page__list"
          @load="loadMore"
        >
          <template #loading>
            <div class="load-more-tip">
              <van-loading size="0.875rem" color="var(--color-text-muted)" />
              <span>加载更多记录…</span>
            </div>
          </template>

          <template #finished>
            <div class="no-more-tip">
              <van-divider class="no-more-divider">没有更多了</van-divider>
            </div>
          </template>

          <div v-if="entries.length === 0" class="list-center-tip list-empty">
            <van-empty description="没有找到匹配记录" image-size="6rem" />
          </div>

          <div class="list-top-spacer" />

          <template v-for="(entry, index) in entries" :key="entry.id">
            <div v-if="shouldShowDateHeader(index)" class="date-group-header">
              <van-icon name="notes-o" class="date-group-header__icon" />
              <span class="date-group-header__label">{{ formatEntryDate(entry.createTime) }}</span>
            </div>

            <EntryItem
              :entry="entry"
              @click="openEdit(entry)"
              @delete="handleDelete(entry)"
              @toggle-checked="(val: boolean) => handleToggleChecked(entry, val)"
            />
          </template>

          <div class="list-bottom-spacer" />
        </van-list>
      </template>

    </main>

    <!-- 编辑底部弹层 -->
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

        <div v-if="editForm.entryType === EntryType.Do" class="edit-popup__completion">
          <div class="edit-popup__completion-header">
            <span class="edit-popup__completion-label">完成进度</span>
            <span class="edit-popup__completion-value" :style="{ color: currentEditColor }"
              >{{ editForm.completionRate }}%</span
            >
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
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import dayjs from 'dayjs'
import { advancedSearch, deleteEntry, updateChecked } from '@/api/entriesController'
import { ENTRY_TYPES, EntryType } from '@/constants/entries'
import EntryItem from '@/components/EntryItem.vue'
import { useEntryEdit } from '@/composables/useEntryEdit'

const router = useRouter()

const PAGE_SIZE = 15

const ALL_TYPE = 'all'
const selectedType = ref<string>(ALL_TYPE)
const keywordInput = ref('')
const keyword = ref('')

const entries = ref<API.EntriesVo[]>([])
const pageNum = ref(1)
const finished = ref(false)
const searching = ref(false)
const loadingMore = ref(false)
const scrollContainerRef = ref<HTMLElement | null>(null)
const queryToken = ref(0)

const typeOptions = [
  { text: 'All', value: ALL_TYPE },
  ...ENTRY_TYPES.map((t) => ({ text: t.label, value: t.value })),
]
const activeTypeColor = computed(
  () => ENTRY_TYPES.find((t) => t.value === selectedType.value)?.color ?? 'var(--color-primary)',
)
function sortDesc(data: API.EntriesVo[]): API.EntriesVo[] {
  return [...data].sort(
    (a, b) => new Date(b.createTime ?? 0).getTime() - new Date(a.createTime ?? 0).getTime(),
  )
}

function buildQuery(nextPageNum: number): API.EntriesSearchReq {
  return {
    pageNum: nextPageNum,
    pageSize: PAGE_SIZE,
    sortField: 'createTime',
    sortOrder: 'descend',
    keyword: keyword.value || undefined,
    entryType: selectedType.value === ALL_TYPE ? undefined : selectedType.value,
  }
}

async function runSearch(reset: boolean) {
  const token = queryToken.value

  if (reset) {
    searching.value = true
    finished.value = false
    pageNum.value = 1
  } else {
    // van-list 触发 @load 时已将 loadingMore 设为 true，不在此重复判断，直接执行
    if (finished.value) {
      loadingMore.value = false
      return
    }
  }

  const nextPageNum = reset ? 1 : pageNum.value + 1

  try {
    const res = await advancedSearch(buildQuery(nextPageNum))
    if (token !== queryToken.value) return

    if (res.data.code !== 0 || !res.data.data) {
      if (reset) entries.value = []
      finished.value = true
      return
    }

    const pageData = res.data.data
    const records = sortDesc(pageData.records ?? [])

    if (reset) {
      entries.value = records
    } else {
      entries.value = [...entries.value, ...records]
    }

    pageNum.value = nextPageNum

    const pages = pageData.pages ?? 0
    finished.value = records.length < PAGE_SIZE || (pages > 0 && nextPageNum >= pages)
  } finally {
    if (token === queryToken.value) {
      searching.value = false
      loadingMore.value = false
    }
  }
}

function triggerResetSearch() {
  queryToken.value += 1
  runSearch(true)
}

let keywordTimer: number | undefined
watch(keywordInput, (val) => {
  if (keywordTimer) window.clearTimeout(keywordTimer)
  keywordTimer = window.setTimeout(() => {
    keyword.value = val.trim()
  }, 250)
})

watch([selectedType, keyword], () => {
  triggerResetSearch()
}, { immediate: true })

function loadMore() {
  runSearch(false)
}

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

function goBack() {
  router.back()
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

// =========== 编辑 ===========
const {
  editVisible,
  saving,
  editForm,
  currentEditPlaceholder,
  currentEditColor,
  openEdit,
  submitEdit,
} = useEntryEdit(entries)
</script>

<style scoped>
.search-page__header {
  justify-content: space-between;
}

.search-back-btn {
  display: flex;
  align-items: center;
  gap: 0.0625rem;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem 0.5rem 0.25rem 0.125rem;
  border-radius: var(--radius-full);
  color: var(--color-primary);
  font-family: var(--font-sans);
  -webkit-tap-highlight-color: transparent;
  transition: opacity var(--transition-fast);
}

.search-back-btn:active {
  opacity: 0.45;
}

.search-back-btn__chevron {
  font-size: 1.875rem;
  line-height: 1;
  font-weight: 200;
  margin-top: -0.1rem;
  letter-spacing: -0.05em;
}

.search-back-btn__label {
  font-size: 1.0625rem;
  font-weight: 400;
  letter-spacing: -0.01em;
}

.search-page__header-placeholder {
  width: 4.25rem;
}

.search-page__body {
  background: #f4f4f8;
}

.search-conditions {
  position: sticky;
  top: 0;
  z-index: 2;
  padding: 0.625rem 0.875rem;
  background: rgba(244, 244, 248, 0.97);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 0.5px solid var(--color-separator);
}

.search-bar {
  display: flex;
  align-items: center;
  height: 2.625rem;
  background: #fff;
  border-radius: var(--radius-xl);
  box-shadow:
    0 0 0 1px rgba(60, 60, 67, 0.08),
    0 1px 3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: box-shadow var(--transition-fast);
}

.search-bar:focus-within {
  box-shadow:
    0 0 0 2px rgba(34, 197, 94, 0.2),
    0 1px 4px rgba(0, 0, 0, 0.06);
}

.search-bar__type {
  flex-shrink: 0;
  width: 5.5rem;
}

.search-bar__type :deep(.van-dropdown-menu) {
  background: transparent;
}

.search-bar__type :deep(.van-dropdown-menu__bar) {
  height: 2.625rem;
  background: transparent;
  box-shadow: none;
  border-radius: 0;
}

.search-bar__type :deep(.van-dropdown-menu__title) {
  font-size: 0.8125rem;
  font-weight: 500;
  padding: 0 0.25rem 0 0.5rem;
}

.search-bar__type :deep(.van-dropdown-item) {
  --van-dropdown-item-z-index: 30;
}

.search-bar__divider {
  flex-shrink: 0;
  width: 0.5px;
  height: 1.125rem;
  background: rgba(60, 60, 67, 0.15);
}

.search-keyword-field {
  flex: 1;
  background: transparent !important;
  height: 2.625rem;
  border-radius: var(--radius-lg) !important;
  transition: box-shadow 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.search-keyword-field:focus-within {
  box-shadow: none !important;
}

.search-keyword-field :deep(.van-field__body) {
  height: 100%;
  align-items: center;
  border-radius: var(--radius-lg);
  transition:
    background 0.25s cubic-bezier(0.16, 1, 0.3, 1),
    box-shadow 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.search-keyword-field:focus-within :deep(.van-field__body) {
  background: rgba(120, 120, 128, 0.04);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-primary) 8%, transparent);
}

.search-keyword-field :deep(.van-field__control) {
  font-size: 0.875rem;
}

.search-keyword-field :deep(.van-field__control::placeholder) {
  color: var(--color-text-muted);
}

.search-keyword-field :deep(.van-field__left-icon) {
  color: var(--color-text-muted);
  margin-right: 0.125rem;
  font-size: 1rem;
  transition: color 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.search-keyword-field:focus-within :deep(.van-field__left-icon) {
  color: var(--color-primary);
}

.search-page__list {
  padding-bottom: 0.5rem;
}

.list-top-spacer {
  height: 0.5rem;
}

.list-bottom-spacer {
  height: 0.75rem;
}

.list-center-tip {
  display: flex;
  justify-content: center;
  padding: 3rem 0;
}

.list-empty {
  padding-top: 4rem;
}

.load-more-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4375rem;
  padding: 0.75rem 1rem;
  font-size: 0.75rem;
  color: var(--color-text-muted);
  font-family: var(--font-sans);
}

.no-more-tip {
  padding: 0 1.25rem 0.25rem;
}

.no-more-divider {
  font-size: 0.6875rem !important;
  color: var(--color-text-muted) !important;
  border-color: var(--color-border-light) !important;
}

.date-group-header {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 1rem 1.25rem 0.375rem;
}

.date-group-header__icon {
  font-size: 0.875rem !important;
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.date-group-header__label {
  font-size: 0.6875rem;
  font-weight: 600;
  color: var(--color-text-secondary);
  letter-spacing: 0.03em;
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
  background: rgba(120, 120, 128, 0.07);
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
  background: rgba(120, 120, 128, 0.05);
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

/* 类型选择胶囊 */
.type-pills {
  display: flex;
  gap: 0.375rem;
}

.type-pill {
  height: 1.625rem;
  padding: 0 0.625rem;
  border-radius: var(--radius-full);
  border: 1.5px solid transparent;
  background: rgba(120, 120, 128, 0.08);
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--color-text-secondary);
  font-family: var(--font-sans);
  cursor: pointer;
  transition:
    background var(--transition-fast),
    color var(--transition-fast),
    border-color var(--transition-fast);
  -webkit-tap-highlight-color: transparent;
  letter-spacing: 0.01em;
}

.type-pill.active {
  background: color-mix(in srgb, var(--pill-color) 15%, transparent);
  border-color: var(--pill-color);
  color: var(--pill-color);
  font-weight: 600;
}

.type-pill:active {
  opacity: 0.7;
}
</style>
