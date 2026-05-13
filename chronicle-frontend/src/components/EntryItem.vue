<template>
  <van-swipe-cell class="entry-swipe-cell">
    <div
      class="entry-item"
      @click="$emit('click')"
      :style="{ '--entry-color': getEntryType(entry.entryType ?? '').color }"
    >
      <!-- 左侧色条 -->
      <div class="entry-item__accent" />
      <div class="entry-item__body">
        <!-- 主内容行 -->
        <div class="entry-item__main">
          <van-checkbox
            v-if="entry.entryType === EntryType.Do"
            :model-value="entry.checked === 1"
            shape="square"
            icon-size="1.0625rem"
            :checked-color="getEntryType(EntryType.Do).color"
            @click.stop
            @update:model-value="(val: boolean) => $emit('toggle-checked', val)"
          />
          <span
            class="entry-content"
            :class="{ 'is-checked': entry.entryType === EntryType.Do && entry.checked === 1 }"
          >
            {{ entry.content }}
          </span>
        </div>
        <!-- 底部元信息行 -->
        <div class="entry-item__meta">
          <!-- 类型 tag + 进度环 -->
          <div class="entry-type-group">
            <van-tag
              plain
              :color="getEntryType(entry.entryType ?? '').color"
              class="entry-tag"
            >{{ entry.entryType }}</van-tag>
            <van-circle
              v-if="entry.entryType === EntryType.Do"
              v-model:current-rate="circleRate"
              :rate="entry.completionRate ?? 0"
              :clockwise="false"
              :speed="90"
              :size="14"
              :stroke-width="80"
              color="#22C55E"
              layer-color="rgba(34,197,94,0.15)"
              class="entry-circle"
            />
            <van-tag
              v-if="entry.entryType === EntryType.Do && entry.checked === 1"
              type="success"
              class="entry-done-tag"
            >已完成</van-tag>
          </div>
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
        @click="$emit('delete')"
      />
    </template>
  </van-swipe-cell>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { EntryType, getEntryType } from '@/constants/entries'
import dayjs from 'dayjs'

defineProps<{
  entry: API.EntriesVo
}>()

defineEmits<{
  (e: 'click'): void
  (e: 'delete'): void
  (e: 'toggle-checked', val: boolean): void
}>()

const circleRate = ref(0)

function formatEntryTime(createTime?: string): string {
  if (!createTime) return ''
  return dayjs(createTime).format('HH:mm')
}
</script>

<style scoped>
/* ======= 滑动单元格容器 ======= */
.entry-swipe-cell {
  border-bottom: 0.5px solid var(--color-border-light);
  background: #fff;
}

.entry-swipe-cell:last-child {
  border-bottom: none;
}

/* ======= 删除按钮 ======= */
.entry-delete-btn {
  height: 100%;
  width: 4.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  border-radius: 0;
}

/* ======= 条目主体 ======= */
.entry-item {
  display: flex;
  align-items: stretch;
  background: #fff;
  min-height: 3.5rem;
  cursor: pointer;
  transition: background var(--transition-fast);
  position: relative;
  overflow: hidden;
}

.entry-item::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0);
  transition: background var(--transition-fast);
  pointer-events: none;
}

.entry-item:active::after {
  background: rgba(0, 0, 0, 0.03);
}

/* ======= 左侧色条 ======= */
.entry-item__accent {
  width: 3px;
  background: var(--entry-color, var(--color-primary));
  flex-shrink: 0;
  border-radius: 0;
  opacity: 0.9;
}

/* ======= 右侧内容区 ======= */
.entry-item__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.3125rem;
  padding: 0.625rem 0.875rem 0.5rem 0.75rem;
  min-width: 0;
}

/* ======= 主内容行 ======= */
.entry-item__main {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.entry-item :deep(.van-checkbox) {
  flex-shrink: 0;
  align-self: flex-start;
  margin-top: 0.125rem;
}

.entry-item :deep(.van-checkbox__icon) {
  height: 1.0625rem;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 3px;
}

.entry-content {
  flex: 1;
  font-size: 0.9375rem;
  line-height: 1.5;
  color: var(--color-text);
  font-family: var(--font-sans);
  word-break: break-all;
  white-space: pre-wrap;
  transition: color var(--transition-fast), text-decoration var(--transition-fast);
}

.entry-content.is-checked {
  color: var(--color-text-muted);
  text-decoration: line-through;
  text-decoration-color: var(--color-text-muted);
}

/* ======= 元信息行 ======= */
.entry-item__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.entry-type-group {
  display: flex;
  align-items: center;
  gap: 0.3125rem;
}

/* van-tag 样式微调 */
.entry-tag {
  font-size: 0.6875rem !important;
  padding: 0 0.3125rem !important;
  height: 1.125rem !important;
  line-height: 1.125rem !important;
  border-radius: 3px !important;
  font-weight: 500 !important;
  letter-spacing: 0.01em;
}

.entry-done-tag {
  font-size: 0.625rem !important;
  height: 1.125rem !important;
  line-height: 1.125rem !important;
  padding: 0 0.3125rem !important;
  border-radius: 3px !important;
}

.entry-circle {
  flex-shrink: 0;
}

.entry-circle :deep(text) {
  display: none;
}

.entry-done-tag {
  display: none;
}

/* 时间 */
.entry-time {
  font-size: 0.6875rem;
  color: var(--color-text-muted);
  font-family: var(--font-sans);
  letter-spacing: 0.02em;
  flex-shrink: 0;
}
</style>
