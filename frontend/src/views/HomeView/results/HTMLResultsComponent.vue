<script setup lang="ts">
    /// TEMP
    
	import { Ref, ref } from "vue";

    import { ResultsService } from "../../../services/ResultsService";
    
    const el: Ref<HTMLElement> = ref(null);
    const active = ref(false);

    ResultsService.on("html", (results: string) => {
        active.value = true;
        
        el.value.innerHTML = results;
    });
    ResultsService.on("clear", () => {
        active.value = false;

        el.value.innerHTML = "";
    });
</script>

<template>
    <div class="results-html" v-show="active" ref="el"></div>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";
    
	.results-html {
        margin-top: var(--space-l);
        width: 100%;
    }
</style>