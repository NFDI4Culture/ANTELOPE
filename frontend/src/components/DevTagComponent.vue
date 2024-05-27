<script setup lang="ts">
    import { ref } from "vue";

    import { EnvService } from "../services/EnvService";

	const isDevMode = EnvService.IS_DEV;
</script>

<template>
	<b v-if="isDevMode">DEV</b>
</template>

<style lang="scss" scoped>
	@import "@/assets/scss/shared";

    b {
        padding: 0.05em 0.2em;
        margin-left: 0.5em;
        background-color: var(--color-error);
        color: var(--color-fg-light--light);
        border-radius: 2px;
        filter: opacity(0.75);

        @include font-s;
    }
</style>