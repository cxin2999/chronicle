import type { Ref } from 'vue'
import { computed, reactive, ref } from 'vue'
import { showToast } from 'vant'
import { updateEntriesContent } from '@/api/entriesController'
import { ENTRY_TYPES, EntryType } from '@/constants/entries'

export function useEntryEdit(entries: Ref<API.EntriesVo[]>) {
  const editVisible = ref(false)
  const saving = ref(false)
  const editForm = reactive({
    id: '',
    content: '',
    entryType: EntryType.Do,
    completionRate: 0,
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
    editForm.entryType = entry.entryType ?? EntryType.Do
    editForm.completionRate = entry.completionRate ?? 0
    editVisible.value = true
  }

  async function submitEdit() {
    if (!editForm.content.trim()) {
      showToast('内容不能为空')
      return
    }
    saving.value = true
    try {
      const isDo = editForm.entryType === EntryType.Do
      const res = await updateEntriesContent({
        id: editForm.id,
        content: editForm.content.trim(),
        entryType: editForm.entryType,
        ...(isDo ? { completionRate: editForm.completionRate } : {}),
      })
      if (res.data.code === 0) {
        const target = entries.value.find((e) => e.id === editForm.id)
        if (target) {
          target.content = editForm.content.trim()
          target.entryType = editForm.entryType
          if (isDo) {
            target.completionRate = editForm.completionRate
            target.checked = editForm.completionRate === 100 ? 1 : 0
          }
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

  return {
    editVisible,
    saving,
    editForm,
    currentEditPlaceholder,
    currentEditColor,
    openEdit,
    submitEdit,
  }
}
