import { computed, ref, type Ref, watch } from 'vue'
import { queryHeatmapData } from '@/api/entriesController'

/** 模块级缓存，跨组件实例持久存在 */
const cache = new Map<string, { data: API.HeatmapDataVo[]; timestamp: number }>()
const STALE_TIME = 5 * 60 * 1000 // 5 分钟

/**
 * 自定义 Hook：封装年历热力图数据获取逻辑
 * - 使用模块级缓存，避免 5 分钟内重复请求同一年份数据
 * @param year 响应式年份字符串，如 "2026"
 */
export function useCalendarData(year: Ref<string>) {
  const loading = ref(false)
  const heatmapData = ref<API.HeatmapDataVo[]>([])

  /** 以日期字符串为键的 Map，方便 O(1) 查找某天是否有记录 */
  const heatmapMap = computed(() => {
    const map = new Map<string, number>()
    for (const item of heatmapData.value) {
      if (item.date) map.set(item.date, item.count ?? 0)
    }
    return map
  })

  async function fetchData() {
    const y = year.value
    const cached = cache.get(y)
    if (cached && Date.now() - cached.timestamp < STALE_TIME) {
      heatmapData.value = cached.data
      return
    }

    loading.value = true
    try {
      const res = await queryHeatmapData({ year: y })
      if (res.data.code === 0 && res.data.data) {
        heatmapData.value = res.data.data
        cache.set(y, { data: res.data.data, timestamp: Date.now() })
      }
    } finally {
      loading.value = false
    }
  }

  /** 年份切换时自动重新获取，首次也立即执行 */
  watch(year, fetchData, { immediate: true })

  return { loading, heatmapData, heatmapMap, fetchData }
}
