<script setup lang="ts">
    import { ref } from "vue";
    
    import { ScrollAbilityService } from "../services/ScrollAbilityService";
    
    const overlayEl = ref(null);
    
	function toggle() {
        overlayEl.value.classList.remove("blocked");

        overlayEl.value
        .classList.toggle("active");

        ScrollAbilityService.toggleScroll(true);
    }
    
    defineExpose({
        toggle
    });
</script>

<template>
    <div class="overlay blocked" ref="overlayEl">
        <section class="overlay-scroll">
            <div class="overlay-wrapper">
                <a class="overlay-close" @click="toggle()"></a>
                <div>
                    <slot></slot>
                </div>
            </div>
        </section>
    </div>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";
    
    .overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        transition: all 200ms;
        overflow-y: scroll;
        user-select: none;
        pointer-events: none;
        z-index: 1000;

        &::before {
            content: "";
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: var(--color-bg-dark--light);
            transition: inherit;
            z-index: -1;
            filter: opacity(0);
        }

        &.blocked {
            display: none;
        }
        &.active {
            user-select: auto;
            pointer-events: auto;

            &::before {
                filter: opacity(0.5);
            }
            
            .overlay-wrapper {
                transform: translateY(0);
                filter: opacity(1);
            }
        }

        &-scroll {
            width: 100%;
        }
        &-wrapper {
            position: relative;
            margin: var(--space-l) auto;
            padding: calc(1.25 * var(--space-m));
            padding-bottom: calc(1.5 * var(--space-m));
            background-color: var(--color-bg-light);
            width: 100%;
            max-width: calc($wrapperWidth * 0.625);
            border-radius: var(--border-radius-m);
            transform: translateY(10%);
            transition: inherit;
            filter: opacity(0);
            
            @include shadow;

            img {
                width: 100%;
                max-width: 35em;
            }
        }
        &-close {
            $size: 1.35rem;
            
            position: absolute;
            display: block;
            margin: calc(1.25 * var(--space-m));
            top: 0;
            right: 0;
            width: $size;
            height: $size;
            z-index: 1;

            @include icon("cross", $size);

            &:hover {
                filter: brightness(0.8);
            }
        }
    }
</style>