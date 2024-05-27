<script setup lang="ts">
import { Ref, ref, onMounted } from 'vue';

const showHover = ref(false);
const infoEl: Ref<HTMLElement> = ref(null);
const hoverEl: Ref<HTMLElement> = ref(null);
const empty = ref(true);

onMounted(() => {
  hoverEl.value.parentNode.removeChild(hoverEl.value);
  document.body.appendChild(hoverEl.value);

  empty.value = !hoverEl.value.textContent.trim().length;
});

function adaptHover(keepALive = false) {
  if (!keepALive) return;

  const position = infoEl.value.getBoundingClientRect();
  const offset = 30;
  hoverEl.value.style.top = `${Math.min(position.y, window.innerHeight - hoverEl.value.offsetHeight - offset)}px`;
  hoverEl.value.style.left = `${Math.max(position.x - hoverEl.value.offsetWidth, offset)}px`;

  window.requestAnimationFrame(() => adaptHover(showHover.value));
}

function toggleHover() {
  adaptHover(!showHover.value);

  showHover.value = !showHover.value;
}
</script>

<template>
  <div v-show="!empty" class="info desktop" @mouseenter="toggleHover()" @mouseout="toggleHover()" ref="infoEl">
    <i class="info-icon"></i>
    <p class="info-hover" :class="showHover ? 'active' : ''" ref="hoverEl">
      <slot></slot>
    </p>
  </div>
</template>

<style scoped lang="scss">
@import '@/assets/scss/shared';

.info {
  overflow-wrap: break-word;

  &-icon {
    position: relative;
    z-index: 1;

    @include icon('info');
  }
  &-hover {
    position: fixed;
    padding: 0.4rem 0.6rem;
    margin-top: calc(0.75rem + var(--space-xs));
    margin-left: calc(-0.75 * var(--space-xs));
    max-width: 300px;
    line-height: 1.25em;
    background-color: var(--color-bg-dark);
    color: var(--color-bg-light);
    font-weight: 400;
    text-align: justify;
    text-transform: none;
    border-radius: var(--border-radius-s);
    transform: translateY(10%);
    transition: filter 200ms, transform 200ms;
    transition-delay: 100ms;
    pointer-events: none;
    user-select: none;
    z-index: 100;
    filter: opacity(0);

    @include font-s;

    &.active {
      transform: translateY(0);
      filter: opacity(0.875);
    }
  }
}
</style>
<style lang="scss">
*:not(:empty) .info {
  margin-left: 0.5em;
}
</style>
