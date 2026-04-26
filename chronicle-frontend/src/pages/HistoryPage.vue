<template>
  <div class="history-page mobile-page">
    <!-- 头部：返回 + 年份导航 + 跳转到记录 -->
    <header class="history-page__header page-header">
      <!-- 返回按钮 -->
      <van-button
        icon="arrow-left"
        size="small"
        plain
        :border="false"
        class="history-page__back-btn"
        @click="goToRecord"
      />

      <!-- 年份切换 -->
      <van-button
        icon="arrow-left"
        size="small"
        plain
        :border="false"
        :disabled="currentYear <= MIN_YEAR"
        @click="prevYear"
      />
      <span class="page-header__title">{{ currentYear }} 年</span>
      <van-button
        icon="arrow"
        size="small"
        plain
        :border="false"
        :disabled="currentYear >= thisYear"
        @click="nextYear"
      />
    </header>

    <!-- 日历内容 -->
    <main class="page-body history-page__body">
      <van-loading v-if="loading" class="history-page__loading" color="var(--color-primary)" />

      <div v-else class="calendar-grid">
        <div v-for="month in 12" :key="month" class="month-block">
          <div class="month-block__title">{{ month }} 月</div>
          <!-- 星期表头 -->
          <div class="month-block__week-row">
            <span v-for="d in WEEK_DAYS" :key="d" class="week-label">{{ d }}</span>
          </div>
          <!-- 日期网格 -->
          <div class="month-block__days">
            <!-- 月份首日偏移空格 -->
            <span
              v-for="i in getFirstDayOffset(currentYear, month)"
              :key="'empty-' + i"
              class="day-cell day-cell--empty"
            />
            <!-- 日期格 -->
            <span
              v-for="day in getDaysInMonth(currentYear, month)"
              :key="day"
              class="day-cell"
              :class="{
                'day-cell--has-record': hasRecord(currentYear, month, day),
                'day-cell--today': isToday(currentYear, month, day),
              }"
              @click="handleDayClick(currentYear, month, day)"
            >
              {{ day }}
            </span>
          </div>
        </div>
      </div>
    </main>

    <!-- 点击某天的记录弹层 -->
    <van-popup v-model:show="dayPopupVisible" position="bottom" round :style="{ height: '65vh' }">
      <div class="day-popup">
        <div class="day-popup__header">
          <span class="day-popup__date">{{ selectedDate }}</span>
          <van-icon name="cross" class="day-popup__close" @click="dayPopupVisible = false" />
        </div>
        <div class="day-popup__body">
          <DailyEntries v-if="selectedDate" :date="selectedDate" :key="selectedDate" />
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCalendarData } from '@/composables/useCalendarData'
import DailyEntries from '@/components/DailyEntries.vue'

const router = useRouter()

const WEEK_DAYS = ['日', '一', '二', '三', '四', '五', '六']
const MIN_YEAR = 2020

const thisYear = new Date().getFullYear()
const currentYear = ref(thisYear)

const yearStr = computed(() => String(currentYear.value))
const { loading, heatmapMap } = useCalendarData(yearStr)

function prevYear() {
  if (currentYear.value > MIN_YEAR) currentYear.value--
}
function nextYear() {
  if (currentYear.value < thisYear) currentYear.value++
}

/** 某月天数 */
function getDaysInMonth(year: number, month: number): number {
  return new Date(year, month, 0).getDate()
}

/** 某月1日是周几（0=周日） */
function getFirstDayOffset(year: number, month: number): number {
  return new Date(year, month - 1, 1).getDay()
}

/** 格式化日期为 yyyy-MM-dd */
function toDateStr(year: number, month: number, day: number): string {
  const mm = String(month).padStart(2, '0')
  const dd = String(day).padStart(2, '0')
  return `${year}-${mm}-${dd}`
}

function hasRecord(year: number, month: number, day: number): boolean {
  return (heatmapMap.value.get(toDateStr(year, month, day)) ?? 0) > 0
}

function isToday(year: number, month: number, day: number): boolean {
  const now = new Date()
  return now.getFullYear() === year && now.getMonth() + 1 === month && now.getDate() === day
}

// 弹层逻辑
const dayPopupVisible = ref(false)
const selectedDate = ref('')

function handleDayClick(year: number, month: number, day: number) {
  selectedDate.value = toDateStr(year, month, day)
  dayPopupVisible.value = true
}

function goToRecord() {
  router.push('/record')
}
</script>

<style scoped>
.history-page__header {
  justify-content: flex-start;
  gap: 0.25rem;
}

.history-page__back-btn {
  flex-shrink: 0;
  color: var(--color-text-secondary) !important;
}

.history-page__loading {
  display: flex;
  justify-content: center;
  padding: 3rem 0;
}

/* 日历网格 */
.calendar-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
  padding: 0.75rem;
}

@media (max-width: 768px) {
  .calendar-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.month-block {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 0.625rem 0.5rem;
  box-shadow: var(--shadow-card);
}

.month-block__title {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-text);
  text-align: center;
  margin-bottom: 0.375rem;
}

.month-block__week-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 0.25rem;
}

.week-label {
  font-size: 0.5625rem;
  color: var(--color-text-muted);
  text-align: center;
}

.month-block__days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
}

.day-cell {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  aspect-ratio: 1;
  font-size: 0.625rem;
  color: var(--color-text-secondary);
  border-radius: 50%;
  cursor: pointer;
  user-select: none;
  transition: background var(--transition-fast);
  line-height: 1;
  gap: 0.1rem;
}

.day-cell:active {
  background: var(--color-background-tertiary);
}

.day-cell--empty {
  cursor: default;
}

.day-cell--today {
  font-weight: 700;
  color: var(--color-primary);
}

.day-cell--today::after {
  content: '';
  display: block;
  width: 0.25rem;
  height: 0.25rem;
  border-radius: 50%;
  background: #ef4444;
  flex-shrink: 0;
}

.day-cell--has-record {
  background: var(--color-primary);
  color: #fff;
  font-weight: 600;
}

/* today + has-record: 实心圆优先，保留红点 */
.day-cell--today.day-cell--has-record {
  background: var(--color-primary);
  color: #fff;
}

/* 日记录弹层 */
.day-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.day-popup__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.875rem 1rem 0.625rem;
  border-bottom: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.day-popup__date {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-text);
}

.day-popup__close {
  font-size: 1.125rem;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0.25rem;
}

.day-popup__body {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}
</style>
