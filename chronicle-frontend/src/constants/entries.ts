/**
 * 记录类型枚举常量
 */
export const ENTRY_TYPES = [
  {
    value: 'Do',
    label: 'Do',
    color: '#3B82F6',
    bgColor: 'rgba(59,130,246,0.10)',
    placeholder: '记录一件要做的事...',
  },
  {
    value: 'Idea',
    label: 'Idea',
    color: '#F59E0B',
    bgColor: 'rgba(245,158,11,0.10)',
    placeholder: '写下一个想法...',
  },
  {
    value: 'Think',
    label: 'Think',
    color: '#8B5CF6',
    bgColor: 'rgba(139,92,246,0.10)',
    placeholder: '记录一个思考...',
  },
  {
    value: 'Rule',
    label: 'Rule',
    color: '#22C55E',
    bgColor: 'rgba(34,197,94,0.10)',
    placeholder: '写下一条原则...',
  },
] as const

export type EntryTypeValue = (typeof ENTRY_TYPES)[number]['value']

export function getEntryType(value: string) {
  return ENTRY_TYPES.find((t) => t.value === value) ?? ENTRY_TYPES[0]
}
