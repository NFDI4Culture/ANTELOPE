<script setup lang="ts">
	import { ref } from "vue";
    
    const active = ref(false);
    
    defineExpose({
        start() {
            active.value = true;
        },
        stop() {
            active.value = false;
        }
    });
</script>

<template>
    <div class="loader" :class="active ? 'active' : ''"></div>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";

    .loader {
        $size: 2rem;

        margin: 0;
        width: $size;
        height: 0;
        background: url("@/assets/img/loader.svg") no-repeat;
        background-size: $size;
        background-position: center;
        transition: all 200ms;
        pointer-events: none;
        z-index: 100;
        filter: opacity(0);

        &.active {
            height: $size;
            filter: opacity(1);
            animation-name: rotate;
            animation-duration: 1s;
            animation-iteration-count: infinite;
            animation-timing-function: linear;

            @keyframes rotate {
                0% {
                    transform: rotate(0);
                }
                100% {
                    transform: rotate(360deg);
                }
            }

            &:not(:first-child) {
                margin-top: var(--space-l);
            }
            &:not(:last-child) {
                margin-bottom: var(--space-l);
            }
        }
    }
</style>