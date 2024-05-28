<script setup lang="ts">
	import { Ref, ref, onMounted, useAttrs } from "vue";

    const showSmall = ref(false);
    const showContrast = ref(false);
    const buttonEl: Ref<HTMLElement> = ref(null);

    onMounted(() => {
        showSmall.value = useAttrs().small !== undefined;
        showContrast.value = useAttrs().contrast !== undefined;
    });

    function passClick() {
        if(!buttonEl.value) return;
        
        Array.from(buttonEl.value.children)
        .forEach((child: HTMLElement) => child.dispatchEvent(new Event("click")));
    }
</script>

<template>
    <button type="button" :class="[ showSmall ? 'small' : '', showContrast ? 'contrast' : '' ]" @click="passClick()" ref="buttonEl">
        <slot></slot>
    </button>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";

    button {
        position: relative;
        display: inline-flex;
        flex-direction: row;
        align-items: center;
        padding: 0.85em 2em;
        background-color: var(--color-primary);
        font-weight: 600;
        text-transform: uppercase;
        border-radius: var(--border-radius-s);
        user-select: none;

        @include font-s;

        &::after {
            @media (prefers-color-scheme: dark) {
                filter: invert(0) !important;
            }
        }

        &:hover {
            filter: brightness(0.95);
        }
        
        &:not(:last-child) {
            margin-right: var(--space-s);
        }

        &.small {
            padding: 0.55em 1.15em;

            &:not(:last-child) {
                margin-right: var(--space-xs);
            }
        }
        &.contrast {
            background-color: var(--color-bg-contrast);
            color: var(--color-fg-gray);
        }
    }
</style>