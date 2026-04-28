<template>
  <div class="daily-entries">
    <!-- 加载状态 -->
    <div v-if="loading && entries.length === 0" class="daily-entries__loading">
      <van-loading size="1.5rem" color="var(--color-primary)" />
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading && entries.length === 0" class="daily-entries__empty">
      <van-empty description="暂无记录" image-size="5rem" />
    </div>

    <!-- 记录列表 -->
    <template v-else>
      <van-swipe-cell v-for="entry in entries" :key="entry.id" class="daily-entries__swipe-cell">
        <!-- 记录内容区 -->
        <div class="entry-item" @click="openEdit(entry)" :style="{ '--entry-color': getEntryType(entry.entryType ?? '').color }">
          <div class="entry-item__accent" />
          <div class="entry-item__body">
            <div class="entry-item__main">
              <!-- Do 类型展示 Checkbox -->
              <van-checkbox
                v-if="entry.entryType === 'Do'"
                :model-value="entry.checked === 1"
                shape="square"
                icon-size="1rem"
                :checked-color="getEntryType('Do').color"
                @click.stop
                @update:model-value="(val: boolean) => handleToggleChecked(entry, val)"
              />

              <!-- 记录文本 -->
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

        <!-- 左滑显示删除按钮 -->
        <template #right>
          <van-button
            square
            type="danger"
            text="删除"
            class="daily-entries__delete-btn"
            @click="handleDelete(entry)"
          />
        </template>
      </van-swipe-cell>
    </template>

    <!-- 编辑底部弹层 -->
    <van-popup
      v-model:show="editVisible"
      position="bottom"
      round
      :style="{ padding: '1.25rem 1rem 2rem' }"
    >
      <div class="edit-popup">
        <div class="edit-popup__title">编辑记录</div>

        <!-- 类型选择 -->
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

        <!-- 内容编辑框 -->
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
import { computed, reactive, ref } from 'vue'
import { showToast } from 'vant'
import {
  deleteEntry,
  queryDailyEntries,
  updateChecked,
  updateContentAndType,
} from '@/api/entriesController'
import { ENTRY_TYPES, getEntryType } from '@/constants/entries'

const props = defineProps<{
  date: string
}>()

const loading = ref(false)
const entries = ref<API.EntriesVo[]>([])

async function fetchEntries() {
  if (!props.date) return
  loading.value = true
  try {
    const res = await queryDailyEntries({ date: props.date })
    if (res.data.code === 0 && res.data.data) {
      entries.value = res.data.data
    }
  } finally {
    loading.value = false
  }
}

// 勾选/取消 Do 类型
async function handleToggleChecked(entry: API.EntriesVo, val: boolean) {
  const checked = val ? 1 : 0
  const previousChecked = entry.checked === 1 ? 1 : 0
  // 乐观更新
  entry.checked = checked
  try {
    const res = await updateChecked({ id: entry.id!, checked })
    if (res.data.code !== 0) {
      // 回滚
      entry.checked = previousChecked
      showToast({ type: 'fail', message: '更新失败' })
    }
  } catch {
    entry.checked = previousChecked
    showToast({ type: 'fail', message: '网络错误' })
  }
}

// 删除记录
async function handleDelete(entry: API.EntriesVo) {
  // 乐观移除
  const index = entries.value.findIndex((e) => e.id === entry.id)
  entries.value.splice(index, 1)

  try {
    const res = await deleteEntry({ id: entry.id! })
    if (res.data.code !== 0) {
      // 回滚
      entries.value.splice(index, 0, entry)
      showToast({ type: 'fail', message: '删除失败' })
    }
  } catch {
    entries.value.splice(index, 0, entry)
    showToast({ type: 'fail', message: '删除失败，请重试' })
  }
}

// -------- 编辑 --------
const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({
  id: '',
  content: '',
  entryType: 'Do',
})

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
      // 同步本地数据
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

// 首次加载及日期变化时重新拉取
fetchEntries()

function formatEntryTime(createTime?: string): string {
  if (!createTime) return ''
  const d = new Date(createTime)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 暴露方法供父组件调用
defineExpose({ refresh: fetchEntries, entries })
</script>

<style scoped>
.daily-entries {
  width: 100%;
}

.daily-entries__loading {
  display: flex;
  justify-content: center;
  padding: 2.5rem 0;
}

.daily-entries__swipe-cell {
  border-bottom: 0.5px solid var(--color-border-light);
  background: #fff;
}

.daily-entries__swipe-cell:last-child {
  border-bottom: none;
}

.daily-entries__delete-btn {
  height: 100%;
  width: 4.5rem;
  font-size: 0.875rem;
}

/* Checkbox 对齐 */
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

/* 编辑弹层 */
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
</style>
