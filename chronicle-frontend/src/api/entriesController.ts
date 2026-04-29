// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 添加记录 POST /entries/add */
export async function addEntry(body: API.EntriesAddReq, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/entries/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 删除记录 POST /entries/delete */
export async function deleteEntry(body: API.EntriesDeleteReq, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/entries/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 查询日记录 POST /entries/query/daily */
export async function queryDailyEntries(
  body: API.EntriesQueryReq,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListEntriesVo>('/entries/query/daily', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 查询热力图数据 POST /entries/query/heatmap */
export async function queryHeatmapData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.queryHeatmapDataParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListHeatmapDataVo>('/entries/query/heatmap', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 游标分页查询历史记录 POST /entries/query/history */
export async function queryHistoryEntries(
  body: API.EntriesHistoryReq,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListEntriesVo>('/entries/query/history', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 更新记录勾选状态 POST /entries/update/checked */
export async function updateChecked(
  body: API.EntriesUpdateCheckedReq,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/entries/update/checked', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 更新记录（内容、类型、完成百分比） POST /entries/update/content */
export async function updateEntriesContent(
  body: API.EntriesContentUpdateReq,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/entries/update/content', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
