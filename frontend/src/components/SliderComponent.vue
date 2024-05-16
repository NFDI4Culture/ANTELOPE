<script setup lang="ts">
	import { Ref, ref, onMounted, useAttrs } from "vue";
    
    const rangeEl: Ref<HTMLElement> = ref(null);
    const fillEl: Ref<HTMLElement> = ref(null);
    const handleEl: Ref<HTMLElement> = ref(null);
    const active = ref(false);
    const value = ref(0.5);

    let initialValue: number;

    onMounted(() => {
        initialValue = parseFloat(useAttrs().default as string) ?? 0.5;

        setValue(initialValue);

        const mouseUpHandler = () => {
            active.value = false;
            
            document.removeEventListener("mousemove", setPosition);
            document.removeEventListener("mouseup", mouseUpHandler);
        };
        handleEl.value.addEventListener("mousedown", () => {
            active.value = true;
            
            document.addEventListener("mousemove", setPosition);
            document.addEventListener("mouseup", mouseUpHandler);
        });

        rangeEl.value.addEventListener("click", setPosition);
    });

    function setPosition(e) {
        setValue((e.clientX - rangeEl.value.getBoundingClientRect().left) / rangeEl.value.clientWidth);
    }

    function setValue(newValue) {
        value.value = Math.min(1.0, Math.max(0.0, newValue));

        fillEl.value.style.width = `${value.value * 100}%`;
        
        handleEl.value.title = newValue;
    }

    defineExpose({
        reset() {
            setValue(initialValue);
        },
        getValue() {
            return value.value;
        }
    });
</script>

<template>
    <div class="slider" :class="active ? 'active' : ''">
        <label>
            <slot name="label"></slot>
        </label>
        <div class="slider-range" ref="rangeEl">
            <div class="slider-fill" ref="fillEl">
                <div class="slider-fill-handle" ref="handleEl"></div>
            </div>
        </div>
        <div class="slider-labels">
            <label>
                <slot name="label-min"></slot>
            </label>
            <label>
                <slot name="label-max"></slot>
            </label>
        </div>
    </div>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";

    .slider {
        display: block;
        width: 100%;
        max-width: 12.5rem;
        text-align: left;

        @include font-s;

        &:not(:first-child) {
            margin-top: var(--space-m);
        }

        &.active {
            user-select: none;
        }

        & > label {
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: flex-start;
            margin-bottom: var(--space-s);
        }

        &-range {
            cursor: pointer;
            width: 100%;
            height: 0.325rem;
            background-color: var(--color-bg-contrast);
            overflow: visible;
        }
        &-fill {
            position: relative;
            width: 50%;
            height: 100%;
            background-color: var(--color-secondary);

            &-handle {
                $handleWidth: calc(1.25 * var(--font-size-l));

                cursor: grab;
                position: absolute;
                margin-top: calc(-0.5 * $handleWidth);
                margin-right: calc(-0.5 * $handleWidth);
                top: 50%;
                right: 0;
                width: $handleWidth;
                height: $handleWidth;
                background-color: var(--color-bg-light);
                border: 1px solid var(--color-fg-contrast);
                border-radius: 100%;
                z-index: 1;
                
                @include shadow(0.175rem);

                &:active {
                    cursor: grabbing;
                }
            }
        }
        &-labels {
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: space-between;
            margin-top: var(--space-xs);
            color: var(--color-fg-gray);

            label {
                margin: 0;
            }
        }
    }
</style>