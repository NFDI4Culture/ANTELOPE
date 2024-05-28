<script setup lang="ts">
	import { Ref, ref, onMounted, useAttrs } from "vue";

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
        onToggle(callback: TToggleHandler) {
            toggleHandlers.push(callback);
        }
    });
</script>

<template>
    <label class="checkbox">
        <input v-model="checked" type="checkbox" :checked="prechecked" :key="renderKey" @change="toggle()">
        <span class="checkbox-tick"></span>
        <slot></slot>
    </label>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";

    .checkbox {
        cursor: pointer;
        display: flex;
        align-items: center;
        margin-bottom: var(--space-xs);
        width: fit-content;
        user-select: none;

        @include font-s;

        &:hover {
            .checkbox-tick {
                background-color: var(--color-secondary-subtle);
            }
        }

        input {
            display: none;

            &:checked + .checkbox-tick {
                background-color: var(--color-secondary);

                &::before, &::after {
                    filter: opacity(1);
                }
            }
        }

        &-tick {
            $size: 1.075rem;

            position: relative;
            margin-right: var(--space-xs);
            height: $size;
            width: $size;
            background-color: var(--color-fg-contrast);
            border-radius: var(--border-radius-s);
            transition: all 100ms;

            &::before, &::after {
                content: "";
                position: absolute;
                display: block;
                width: calc(0.15 * $size);
                background-color: var(--color-fg-light);
                transition: inherit;
                filter: opacity(0);
            }
            &::before {
                height: calc(0.75 * $size);
                transform: translateX(calc(0.525 * $size)) translateY(calc(0.125 * $size)) rotate(37.5deg);
            }
            &::after {
                height: calc(0.375 * $size);
                transform: translateX(calc(0.25 * $size)) translateY(calc(0.475 * $size)) rotate(-52.5deg);
            }
        }
    }
</style>