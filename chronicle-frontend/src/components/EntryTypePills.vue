<!--记录类型选择条-->
<template>
  <div class="type-pills">
    <button
      v-for="t in ENTRY_TYPES"
      :key="t.value"
      class="type-pill"
      :class="{ active: modelValue === t.value }"
      :style="{ '--pill-color': t.color }"
      @click="emit('update:modelValue', t.value)"
    >
      {{ t.label }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { ENTRY_TYPES } from '@/constants/entries'
import type { EntryTypeValue } from '@/constants/entries'

defineProps<{
  modelValue: EntryTypeValue
}>()

const emit = defineEmits<{
  'update:modelValue': [value: EntryTypeValue]
}>()
</script>
