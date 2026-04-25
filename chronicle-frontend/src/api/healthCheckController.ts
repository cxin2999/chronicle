// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 健康检查 GET /health/check */
export async function check(options?: { [key: string]: any }) {
  return request<string>('/health/check', {
    method: 'GET',
    ...(options || {}),
  })
}
