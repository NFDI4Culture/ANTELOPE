<script setup lang="ts">
import { Ref, ref, onMounted, useAttrs } from 'vue';

type TToggleHandler = (checked: boolean) => void;

const prechecked = ref(false);
const checked = ref(false);
const renderKey = ref(0);

const toggleHandlers: TToggleHandler[] = [];

let initiallyChecked: boolean;

onMounted(() => {
  initiallyChecked = useAttrs().checked !== undefined;
  prechecked.value = initiallyChecked;
  checked.value = initiallyChecked;

  toggle();
});

function toggle() {
  toggleHandlers.forEach((callback: TToggleHandler) => callback(checked.value));
}

defineExpose({
  reset() {
    checked.value = initiallyChecked;

    renderKey.value += 1;

    toggle();
  },
  getChecked() {
    return checked.value;
  },
  setChecked(state = true) {
    checked.value = state;
  },
  onToggle(callback: TToggleHandler) {
    toggleHandlers.push(callback);
  },
});
</script>

<template>
  <button type="button" class="checkbutton">
    <input v-model="checked" type="checkbox" :checked="prechecked" :key="renderKey" @change="toggle()" />
    <span class="checkbutton-caption">
      <slot></slot>
    </span>
  </button>
</template>

<style scoped lang="scss">
@import '@/assets/scss/shared';

.checkbutton {
  position: relative;
  display: inline-block;
  margin-bottom: var(--space-xs);
  margin-right: var(--space-xs);
  padding: 0;
  background-color: transparent;
  width: fit-content;
  height: fit-content;
  line-height: 1em;
  border: none;

  @include font-s;

  input {
    cursor: pointer;
    position: absolute;
    display: block;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    opacity: 0 !important;

    &:checked + .checkbutton-caption {
      color: var(--color-secondary);
      border-color: var(--color-secondary);
    }
  }

  &-caption {
    display: block;
    padding: 0.375em var(--space-xs);
    background-color: var(--color-bg-gray);
    color: var(--color-fg-dark);
    border: 2px solid var(--color-bg-contrast);
    border-radius: var(--border-radius-s);
    user-select: none;
    pointer-events: none;
  }
}
</style>
