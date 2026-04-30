<template>
  <div class="history-page mobile-page">
    <!-- 头部：返回 + 年份导航 -->
    <header class="history-page__header page-header">
      <!-- 返回按钮（iOS 风格） -->
      <button class="history-back-btn" @click="goToRecord">
        <span class="history-back-btn__chevron">‹</span>
        <span class="history-back-btn__label">记录</span>
      </button>

      <!-- 年份导航胶囊 -->
      <div class="year-nav">
        <button class="year-nav__arrow" :disabled="currentYear <= MIN_YEAR" @click="prevYear">
          ‹
        </button>
        <span class="year-nav__label">{{ currentYear }} 年</span>
        <button class="year-nav__arrow" :disabled="currentYear >= thisYear" @click="nextYear">
          ›
        </button>
      </div>
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
                'day-cell--weekend': isWeekend(currentYear, month, day),
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
          <span class="day-popup__date">{{ formatSelectedDate }}</span>
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

const WEEK_DAYS = ['一', '二', '三', '四', '五', '六', '日']
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

/** 某月1日在 Mon-first 网格中的列偏移（Mon=0, ..., Sun=6） */
function getFirstDayOffset(year: number, month: number): number {
  const dow = new Date(year, month - 1, 1).getDay() // 0=Sun
  return dow === 0 ? 6 : dow - 1
}

/** 是否是周末（周六或周日） */
function isWeekend(year: number, month: number, day: number): boolean {
  const dow = new Date(year, month - 1, day).getDay()
  return dow === 0 || dow === 6
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

const formatSelectedDate = computed(() => {
  if (!selectedDate.value) return ''
  return new Date(selectedDate.value).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  })
})
</script>

<style scoped>
/* ======= Header ======= */
.history-page__header {
  justify-content: space-between;
}

/* iOS 风格返回按钮 */
.history-back-btn {
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

.history-back-btn:active {
  opacity: 0.45;
}

.history-back-btn__chevron {
  font-size: 1.875rem;
  line-height: 1;
  font-weight: 200;
  margin-top: -0.1rem;
  letter-spacing: -0.05em;
}

.history-back-btn__label {
  font-size: 1.0625rem;
  font-weight: 400;
  letter-spacing: -0.01em;
}

/* 年份导航胶囊 */
.year-nav {
  display: flex;
  align-items: center;
  background: rgba(120, 120, 128, 0.1);
  border-radius: var(--radius-full);
  overflow: hidden;
  height: 2rem;
}

.year-nav__arrow {
  width: 2rem;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.1875rem;
  line-height: 1;
  font-weight: 300;
  color: var(--color-text-secondary);
  transition:
    background var(--transition-fast),
    color var(--transition-fast);
  -webkit-tap-highlight-color: transparent;
  padding: 0;
}

.year-nav__arrow:active:not(:disabled) {
  background: rgba(120, 120, 128, 0.18);
  color: var(--color-text);
}

.year-nav__arrow:disabled {
  opacity: 0.28;
  cursor: default;
}

.year-nav__label {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-text);
  padding: 0 0.375rem;
  letter-spacing: -0.01em;
  white-space: nowrap;
  font-family: var(--font-sans);
  font-variant-numeric: tabular-nums;
  min-width: 4rem;
  text-align: center;
}

/* ======= 加载 ======= */
.history-page__loading {
  display: flex;
  justify-content: center;
  padding: 3rem 0;
}

/* ======= 日历网格 ======= */
.calendar-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.625rem;
  padding: 0.75rem;
}

.month-block {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 0.625rem 0.5rem 0.5rem;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.month-block__title {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-text);
  text-align: center;
  margin-bottom: 0.3125rem;
  letter-spacing: -0.01em;
  font-family: var(--font-sans);
}

.month-block__week-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 0.1875rem;
}

.week-label {
  font-size: 0.5rem;
  color: var(--color-text-muted);
  text-align: center;
  font-family: var(--font-sans);
  font-weight: 500;
}

/* 周六日的表头用不同颜色 */
.week-label:nth-child(6),
.week-label:nth-child(7) {
  color: rgba(255, 59, 48, 0.55);
}

.month-block__days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
}

/* ======= 日期格 ======= */
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
  font-family: var(--font-sans);
  font-variant-numeric: tabular-nums;
}

.day-cell:active {
  background: var(--color-background-tertiary);
}

.day-cell--empty {
  cursor: default;
}

/* 周末颜色（在 today / has-record 之前声明，优先级更低） */
.day-cell--weekend {
  color: #ff3b30;
}

/* today 覆盖 weekend */
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
  background: #ff3b30;
  flex-shrink: 0;
}

/* has-record 覆盖一切 */
.day-cell--has-record {
  background: var(--color-primary);
  color: #fff;
  font-weight: 600;
}

.day-cell--today.day-cell--has-record {
  background: var(--color-primary);
  color: #fff;
}

/* ======= 弹层 ======= */
.day-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.day-popup__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1rem 0.75rem;
  border-bottom: 0.5px solid var(--color-separator);
  flex-shrink: 0;
}

.day-popup__date {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-text);
  letter-spacing: -0.01em;
  font-family: var(--font-sans);
}

.day-popup__close {
  width: 1.75rem;
  height: 1.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(120, 120, 128, 0.12);
  font-size: 0.875rem;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.day-popup__close:active {
  background: rgba(120, 120, 128, 0.22);
}

.day-popup__body {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}
</style>
