<script setup lang="ts">
	import { Ref, ref } from "vue";

    import { ResultsService } from "../../../services/ResultsService";
    import { EntitySelectionService } from "../../../services/EntitySelectionService";
    import TabsComponent from "../../../components/TabsComponent.vue";
    import InfoComponent from "../../../components/InfoComponent.vue";
    import ResultsGraphComponent from "./ResultsGraphComponent.vue";
    import ResultsTableComponent from "./ResultsTableComponent.vue";
    import SelectionComponent from "./ResultsGraphComponent.vue";
    import { IResults } from "../../../interfaces";
    
    const tabsEl: Ref<TabsComponent> = ref(null);
    const error: Ref<string> = ref(null);
    const active = ref(false);
    const resultsCount = ref(0);

    ResultsService.on("define", (results: IResults) => {
        active.value = true;

        resultsCount.value = results.entities.length;
    });
    ResultsService.on("clear", () => {
        active.value = false;

        tabsEl.value.activateTab(0);
    });
    
    defineExpose({
        setError(errorOrMessage: Error|string) {
            error.value = ((errorOrMessage instanceof Error)
            ? errorOrMessage.message
            : errorOrMessage.toString())
            .replace(/^ *\w+Error: */, "");
        },
        clearError() {
            error.value = null;
        }
    });
</script>

<template>
    <div class="results">
        <span v-show="error" class="results-error">{{ error }}</span>
        <span v-show="active" class="results-count">{{ resultsCount }} results</span>
        <TabsComponent ref="tabsEl" v-show="active">
            <div tab-label="Tree Graph">
                <ResultsGraphComponent />
            </div>
            <div tab-label="Table">
                <ResultsTableComponent />
            </div>
        </TabsComponent>
    </div>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";

	.results {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-top: var(--space-l);
        width: 100%;

        &-error {
            color: var(--color-error);
        }
        &-count {
            color: var(--color-fg-gray);
        }
    }
</style>