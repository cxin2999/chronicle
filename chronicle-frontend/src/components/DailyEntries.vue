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
      <EntryItem
        v-for="entry in entries"
        :key="entry.id"
        :entry="entry"
        @click="openEdit(entry)"
        @delete="handleDelete(entry)"
        @toggle-checked="(val: boolean) => handleToggleChecked(entry, val)"
      />
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
import { ref } from 'vue'
import { showToast } from 'vant'
import {
  deleteEntry,
  queryDailyEntries,
  updateChecked,
} from '@/api/entriesController'
import { ENTRY_TYPES, EntryType } from '@/constants/entries'
import EntryItem from '@/components/EntryItem.vue'
import { useEntryEdit } from '@/composables/useEntryEdit'

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
  const previousRate = entry.completionRate
  // 乐观更新
  entry.checked = checked
  entry.completionRate = checked === 1 ? 100 : 0
  try {
    const res = await updateChecked({ id: entry.id!, checked })
    if (res.data.code !== 0) {
      // 回滚
      entry.checked = previousChecked
      entry.completionRate = previousRate
      showToast({ type: 'fail', message: '更新失败' })
    }
  } catch {
    entry.checked = previousChecked
    entry.completionRate = previousRate
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
const { editVisible, saving, editForm, currentEditPlaceholder, currentEditColor, openEdit, submitEdit } =
  useEntryEdit(entries)

// 首次加载及日期变化时重新拉取
fetchEntries()

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
</style>
